package it.unibz.inf.mixer.execution;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import it.unibz.inf.mixer.core.Query;
import it.unibz.inf.mixer.core.QueryLanguage;
import it.unibz.inf.mixer.execution.utils.Template;
import it.unibz.inf.mixer.execution.utils.Template.PlaceholderInfo;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TemplateQuerySelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateQuerySelector.class);

    private static final int MAX_FILL_PLACEHOLDERS_ATTEMPTS = 200;

    private final String[] queryFilenames;

    private final String[] queryTemplates;

    private final Connection connection;

    private final String connectionType;

    private final QueryLanguage language;

    private final Set<String> generatedQueries = new HashSet<>();

    private final Table<String, String, Integer> queryPointers = HashBasedTable.create();

    private int index; // Fields to use this class as an iterator

    public TemplateQuerySelector(String templatesDir, Connection connection, QueryLanguage language) {

        // Check arguments
        Objects.requireNonNull(templatesDir);
        Objects.requireNonNull(connection);
        Objects.requireNonNull(language);

        try {
            // List and sort query files under specified templates directory
            String[] filenames = Files.list(Paths.get(templatesDir))
                    .filter(f -> !Files.isDirectory(f))
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .toArray(String[]::new);

            // Read the content of query files as string templates
            String[] templates = new String[filenames.length];
            for (int i = 0; i < templates.length; ++i) {
                templates[i] = Files.readString(Paths.get(templatesDir + "/" + filenames[i]), Charsets.UTF_8);
            }

            // Log loading results
            LOGGER.info("Loaded {} {} queries", filenames.length, language);

            // Initialize state
            this.connection = connection;
            this.connectionType = getConnectionType(connection);
            this.language = language;
            this.index = 0;
            this.queryFilenames = filenames;
            this.queryTemplates = templates;

        } catch (IOException ex) {
            // Wrap and propagate
            throw new UncheckedIOException(ex);
        }
    }

    public @Nullable Query nextQuery() {

        // Return null if a query mix has been completed, resetting index so to start a new mix
        if (index >= queryFilenames.length) {
            index = 0;
            return null;
        }

        // Retrieve current query ID and template, incrementing the index
        String queryId = queryFilenames[index];
        String queryTemplate = queryTemplates[index];
        ++index;

        // Fill placeholders (if any) until an unseen query is generated, or the max number of attempts is reached
        String queryString = null;
        List<String> queryPlaceholders = null;
        Template sparqlQueryTemplate = new Template(queryTemplate);
        for (int i = 0; i < MAX_FILL_PLACEHOLDERS_ATTEMPTS && (queryString == null || !generatedQueries.add(queryString)); ++i) {
            fillPlaceholders(queryId, sparqlQueryTemplate);
            queryString = sparqlQueryTemplate.getFilled();
            queryPlaceholders = IntStream.range(1, sparqlQueryTemplate.getNumPlaceholders() + 1)
                    .mapToObj(sparqlQueryTemplate::getNthPlaceholder)
                    .collect(Collectors.toList());
        }

        // Return a Query object for the selected query ID and filled template
        return Query.builder(queryString)
                .withId(queryId)
                .withPlaceholders(queryPlaceholders)
                .withLanguage(language)
                .build();
    }

    private void fillPlaceholders(String queryId, Template sparqlQueryTemplate) {
        try {
            // List the placeholders in the query
            LOGGER.debug("[mixer-debug] Call fillPlaceholders");
            List<PlaceholderInfo> placeholders = IntStream.range(1, sparqlQueryTemplate.getNumPlaceholders() + 1)
                    .mapToObj(sparqlQueryTemplate::getNthPlaceholderInfo)
                    .collect(Collectors.toList());

            // Iterate over the tables these placeholders refer to
            for (String table : placeholders.stream().map(p -> p.getQN().getFirst()).collect(Collectors.toSet())) {

                // Extract the distinct IDs (e.g., 1, 2, ...) and columns referred by placeholders for current table
                Set<Integer> ids = Sets.newHashSet();
                Set<String> cols = Sets.newHashSet();
                placeholders.stream().filter(p -> p.getQN().getFirst().equals(table)).forEach(p -> {
                    ids.add(p.getId());
                    cols.add(p.getQN().getSecond());
                });

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
                                    for (int i = 0; i < placeholders.size(); ++i) {
                                        PlaceholderInfo p = placeholders.get(i);
                                        if (p.getQN().getFirst().equals(table) && p.getId() == id) {
                                            String value = rs.getString(p.getQN().getSecond());
                                            String valueQuoted = p.applyQuoting(value);
                                            sparqlQueryTemplate.setNthPlaceholder(i + 1, valueQuoted);
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

    private static String getConnectionType(Connection connection) {
        try {
            // Retrieve the connection type as X from JDBC URL 'jdbc:X:...'
            String url = connection.getMetaData().getURL();
            int start = "jdbc:".length();
            int end = url.indexOf(':', start);
            return url.substring(start, end).toLowerCase();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Could not obtain the connection URL", ex);
        }
    }

}
