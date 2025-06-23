package it.unibz.inf.mixer.jdbc;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.collect.*;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import it.unibz.inf.mixer.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class QuerySelectorJDBC extends AbstractPlugin implements QuerySelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuerySelectorJDBC.class);

    private static final int MAX_FILL_PLACEHOLDERS_ATTEMPTS = 200;

    private final Set<HashCode> generatedQueries = new HashSet<>();

    private final Table<String, String, Integer> queryPointers = HashBasedTable.create();

    private QueryLanguage language;

    private String[] queryFilenames;

    private Template[] queryTemplates;

    private Connection connection;

    private String connectionType;

    public void init(Map<String, String> conf) throws Exception {

        // Initialize superclass
        super.init(conf);

        // Set the query language for generated queries (required to inject syntactically valid comments)
        this.language = QueryLanguage.valueOf(conf.getOrDefault("lang", "sparql").trim().toUpperCase());

        // Set the query templates directory and identify the query files contained within
        String queryDirectory = Objects.requireNonNull(conf.get("queries-dir"));
        this.queryFilenames = Files.list(Paths.get(queryDirectory))
                .filter(f -> !Files.isDirectory(f))
                .map(p -> p.getFileName().toString())
                .sorted()
                .toArray(String[]::new);

        // Load the query templates
        this.queryTemplates = new Template[queryFilenames.length];
        for (int i = 0; i < this.queryTemplates.length; ++i) {
            Path path = Paths.get(queryDirectory + "/" + queryFilenames[i]);
            this.queryTemplates[i] = new Template(Files.readString(path, Charsets.UTF_8));
        }
        LOGGER.info("Loaded {} {} queries", queryFilenames.length, language);

        // Read JDBC parameters for connecting to the database
        String url = Objects.requireNonNull(conf.get("db-url"));
        String user = conf.get("db-user");
        String pwd = conf.get("db-pwd");
        String driver = conf.get("db-driverclass");

        // Extract the connection type as X from JDBC URL 'jdbc:X:...'
        int start = "jdbc:".length();
        int end = url.indexOf(':', start);
        this.connectionType = url.substring(start, end).toLowerCase();

        try {
            // Connect to the JDBC data sources
            if (driver != null) {
                Class.forName(driver); // Load driver (might be needed for old drivers)
            }
            this.connection = DriverManager.getConnection(url, user, pwd);
            LOGGER.info("Connected to {} for filling query templates", url);
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to setup DB connection to " + url + " for filling query templates", ex);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public synchronized List<Query> nextQueryMix() {

        // Allocate a list for the queries of the mix to generate
        List<Query> queryMix = Lists.newArrayListWithCapacity(queryFilenames.length);

        // Iterate over all query filenames / templates, choosing an unseen combination of fillers for each
        for (int index = 0; index < queryFilenames.length; ++index) {

            // Retrieve current query ID and template, incrementing the index
            String queryId = queryFilenames[index];
            Template queryTemplate = queryTemplates[index];

            // Allocate a list where to be (repeatedly) populated with computed placeholder fillers, until success
            List<String> queryFillers = new ArrayList<>(Collections.nCopies(queryTemplate.getPlaceholders().size(), null));

            // Fill placeholders (if any) until an unseen query is generated, or the max number of attempts is reached
            HashCode hash = null;
            for (int i = 0; i < MAX_FILL_PLACEHOLDERS_ATTEMPTS && (hash == null || !generatedQueries.add(hash)); ++i) {
                fillPlaceholders(queryId, queryTemplate, queryFillers);
                Hasher hasher = Hashing.farmHashFingerprint64().newHasher().putUnencodedChars(queryId);
                queryFillers.forEach(f -> hasher.putChar((char) 0).putUnencodedChars(f));
                hash = hasher.hash();
            }

            // Create a Query object for the selected query ID, template and placeholder fillers, and add it to the mix
            queryMix.add(Query.builder(queryTemplate)
                    .withPlaceholderFillers(queryFillers)
                    .withId(queryId)
                    .withLanguage(language)
                    .build());
        }

        // Return the generated query mix
        return queryMix;
    }

    private void fillPlaceholders(String queryId, Template queryTemplate, List<String> queryFillers) {

        // Wipe out the content of the supplied fillers list
        Collections.fill(queryFillers, null);

        // Define a helper record class storing the ${id:table.column} fields forming the placeholder name
        class ParsedPlaceholder {

            final int id;
            final String table;
            final String column;
            final int index;

            ParsedPlaceholder(Template.Placeholder p) {
                String[] tokens = p.getName().split("[:.]");
                this.id = Integer.parseInt(tokens[0]);
                this.table = tokens[1];
                this.column = tokens[2];
                this.index = p.getIndex();
            }
        }

        try {
            // Extract id and table.column of each placeholder in the query
            LOGGER.trace("[mixer-debug] Call fillPlaceholders");
            Multimap<String, ParsedPlaceholder> placeholders = Multimaps.index(
                    queryTemplate.getPlaceholders().stream().map(ParsedPlaceholder::new).iterator(), p -> p.table);

            // Iterate over the tables these placeholders refer to
            for (Map.Entry<String, Collection<ParsedPlaceholder>> e : placeholders.asMap().entrySet()) {

                // Extract the distinct IDs (e.g., 1, 2, ...) and columns referred by placeholders for current table
                String table = e.getKey();
                Collection<ParsedPlaceholder> tablePlaceholders = e.getValue();
                Set<Integer> ids = tablePlaceholders.stream().map(p -> p.id).collect(Collectors.toSet());
                Set<String> cols = tablePlaceholders.stream().map(p -> p.column).collect(Collectors.toSet());

                // Define the parameterized query over the table to fetch placeholder values, enforcing these are not
                // null and extracting as many rows as there are distinct placeholder IDs
                String query;
                if (connectionType.equals("postgresql")) {
                    query = ""
                            + "SELECT *\n"
                            + "FROM \"" + table + "\"\n"
                            + "WHERE " + cols.stream().map(c -> "\"" + c + "\" IS NOT NULL").collect(Collectors.joining(" AND ")) + "\n"
                            + "ORDER BY 1 "
                            + "LIMIT " + ids.size() + " OFFSET ?";
                } else {
                    query = ""
                            + "SELECT *\n"
                            + "FROM " + table + "\n"
                            + "WHERE " + cols.stream().map(c -> c + " IS NOT NULL").collect(Collectors.joining(" AND ")) + "\n"
                            + "ORDER BY 1\n"
                            + "LIMIT ?, " + ids.size();
                }

                // Resume iterating over query results from where we left before (if first time, start at offset 0)
                int pointer = MoreObjects.firstNonNull(queryPointers.get(queryId, table), 0);
                queryPointers.put(queryId, table, pointer + ids.size());

                // Fetch the values, iterating until placeholders for all IDs for the table have been filled
                while (!ids.isEmpty()) {
                    try (PreparedStatement stmt = connection.prepareStatement(query)) {
                        stmt.setInt(1, pointer);
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (!ids.isEmpty()) {

                                // Get the current ID to be served by this row of results
                                int id = ids.iterator().next();

                                // Handle three cases based on whether there is a row of results or not
                                if (rs.next()) {
                                    // (1) data present, fill placeholders for this <table, id> combination and move on
                                    for (ParsedPlaceholder p : e.getValue()) {
                                        if (p.id == id) {
                                            String value = rs.getString(p.column);
                                            queryFillers.set(p.index, value);
                                        }
                                    }
                                    ids.remove(id);

                                } else if (pointer > 0) {
                                    // (2) at end of possible non-empty query results: rewind to beginning & repeat
                                    pointer = 0;
                                    queryPointers.put(queryId, table, ids.size());
                                    break;

                                } else {
                                    // (3) there are no possible query results: fail reporting the issue
                                    throw new RuntimeException("Unexpected problem: no data to fill placeholders of "
                                            + "query " + queryId + " referring to table " + table);
                                }
                            }
                        }
                    }
                }
            }

        } catch (SQLException ex) {
            // Cast any exception to a runtime one, adding a context message
            throw new RuntimeException("Could not fetch placeholder values from DB: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void close() throws Exception {
        try {
            // Close DB connection if open
            if (connection != null) {
                connection.close();
            }
        } finally {
            // Reset state to release resources and delegate to super-class
            this.queryPointers.clear();
            this.generatedQueries.clear();
            this.connection = null;
            this.language = null;
            this.queryFilenames = null;
            this.queryTemplates = null;
            this.connectionType = null;
            super.close();
        }
    }

}
