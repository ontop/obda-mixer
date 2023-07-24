package it.unibz.inf.mixer.execution;

import com.google.common.base.Charsets;
import it.unibz.inf.mixer.core.Query;
import it.unibz.inf.mixer.core.QueryLanguage;
import it.unibz.inf.mixer.execution.utils.QualifiedName;
import it.unibz.inf.mixer.execution.utils.Template;
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
import java.util.*;
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

    private final Map<QualifiedName, Integer> resultSetPointer = new HashMap<>(); // Pointers in the database

    private int nExecutedTemplateQuery; // State

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
            this.nExecutedTemplateQuery = 0;
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
            fillPlaceholders(sparqlQueryTemplate);
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

    private void fillPlaceholders(Template sparqlQueryTemplate) {

        LOGGER.debug("[mixer-debug] Call fillPlaceholders");

        Map<Template.PlaceholderInfo, String> mapTIToValue = new HashMap<>();

        this.resultSetPointer.clear();
        ++this.nExecutedTemplateQuery;

        if (sparqlQueryTemplate.getNumPlaceholders() == 0) return;

        for (int i = 1; i <= sparqlQueryTemplate.getNumPlaceholders(); ++i) {
            Template.PlaceholderInfo info = sparqlQueryTemplate.getNthPlaceholderInfo(i);
            String value = mapTIToValue.computeIfAbsent(info, in -> findValueToInsert(in.getQN()));
            String valueQuoted = info.applyQuoting(value);
            sparqlQueryTemplate.setNthPlaceholder(i, valueQuoted);
        }
    }

    /**
     * Tries to look in the same row, as long as possible
     */
    private String findValueToInsert(QualifiedName qN) {

        // Compute position (pointer) in value list obtained from table
        int pointer = 0;
        if (resultSetPointer.containsKey(qN)) {
            pointer = resultSetPointer.get(qN);
            resultSetPointer.put(qN, pointer + 1);
        } else {
            resultSetPointer.put(qN, 1);
        }
        pointer += this.nExecutedTemplateQuery;

        // Try getting value at position, or at 0 in case there is no such value
        while (true) {
            // Build the SQL query to fetch possible placeholder values, depending on the DBMS type
            // NOTE: in principle it would be enough to SELECT the 'second' field instead of '*', but in that case
            // the DBMS may fetch it from some index instead of iterating over table rows, with implications on selected
            // values and whether selected placeholders fillers will actually jointly match a row in the referenced table
            String query;
            if (connectionType.equals("postgresql")) {
                query = ""
                        + "SELECT *\n"
                        + "FROM \"" + qN.getFirst() + "\"\n"
                        + "WHERE \"" + qN.getSecond() + "\" IS NOT NULL\n"
                        + "ORDER BY 1 "
                        + "LIMIT 1 OFFSET " + pointer;
            } else {
                query = ""
                        + "SELECT *\n"
                        + "FROM " + qN.getFirst() + "\n"
                        + "WHERE " + qN.getSecond() + " IS NOT NULL\n"
                        + "ORDER BY 1\n"
                        + "LIMIT " + pointer + ", 1";
            }

            // Fetch the value, handling the case it's not available
            try {
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String value = rs.getString(qN.getSecond());
                            LOGGER.trace("Placeholder for {} = {} via query:\n{}", qN, value, query);
                            return value;
                        } else if (pointer > 0) {
                            pointer = 0; // try seeking back to begin of value list
                            resultSetPointer.put(qN, 1);
                        } else {
                            throw new RuntimeException("Unexpected Problem: No result to fill placeholder. "
                                    + "Is the referenced table non-empty?");
                        }
                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException("Could not fetch placeholder values from DB: " + ex.getMessage(), ex);
            }
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
