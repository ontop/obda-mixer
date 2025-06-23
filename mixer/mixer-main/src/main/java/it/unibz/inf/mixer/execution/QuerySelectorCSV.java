package it.unibz.inf.mixer.execution;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unibz.inf.mixer.core.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@SuppressWarnings("unused")
public class QuerySelectorCSV extends AbstractPlugin implements QuerySelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuerySelectorCSV.class);

    private QueryLanguage language;

    private String[] queryFilenames;

    private Template[] queryTemplates;

    private CSVUniqueRecordSupplier fillersSupplier;

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

        // Load the query templates and collect all placeholder names
        Set<String> placeholderNames = Sets.newHashSet();
        this.queryTemplates = new Template[queryFilenames.length];
        for (int i = 0; i < this.queryTemplates.length; ++i) {
            Path path = Paths.get(queryDirectory + "/" + queryFilenames[i]);
            this.queryTemplates[i] = new Template(Files.readString(path, Charsets.UTF_8));
            this.queryTemplates[i].getPlaceholders().stream()
                    .map(Template.Placeholder::getName)
                    .forEach(placeholderNames::add);
        }
        LOGGER.info("Loaded {} {} queries", queryFilenames.length, language);

        // Create a CSVUniqueRecordSupplier to read CSV records from the fillers file (possibly in multiple passes)
        Path fillersFile = Paths.get(conf.get("queries-fillers-file"));
        this.fillersSupplier = new CSVUniqueRecordSupplier(fillersFile, placeholderNames);
    }

    @Override
    public synchronized List<Query> nextQueryMix() {
        try {
            // Get the next fillers CSV record
            CSVRecord fillers = fillersSupplier.next();

            // Allocate a list for the queries of the mix to generate
            List<Query> queryMix = Lists.newArrayListWithCapacity(queryFilenames.length);

            // Iterate over all query filenames / templates
            for (int index = 0; index < queryFilenames.length; ++index) {

                // Retrieve current query ID and template, incrementing the index
                String queryId = queryFilenames[index];
                Template queryTemplate = queryTemplates[index];
                List<String> queryFillers = queryTemplate.getPlaceholders().stream()
                        .map(p -> fillers.get(p.getName()))
                        .collect(Collectors.toList());

                // Create a Query object for the selected query ID, template and placeholder fillers, and add it to the mix
                queryMix.add(Query.builder(queryTemplate)
                        .withPlaceholderFillers(queryFillers)
                        .withId(queryId)
                        .withLanguage(language)
                        .build());
            }

            // Return the generated query mix
            return queryMix;

        } catch (IOException ex) {
            // Wrap and propagate
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void close() throws Exception {
        try {
            // Close the CSV file
            if (fillersSupplier != null) {
                this.fillersSupplier.close();
            }

        } finally {
            // Reset state to release resources and delegate to super-class
            this.language = null;
            this.queryFilenames = null;
            this.queryTemplates = null;
            this.fillersSupplier = null;
            super.close();
        }
    }

    /**
     * Helper class providing an infinite stream of CSVRecord objects from a (gzipped) CSV file.
     */
    private static class CSVUniqueRecordSupplier implements AutoCloseable {

        private final Path file;

        private CSVParser parser;

        private Iterator<CSVRecord> iterator;

        private long numRecordsReturned;

        private long numRecordsFile;

        public CSVUniqueRecordSupplier(Path csvFile, Iterable<String> requiredFields) throws IOException {

            // Create a CSVParser reading from the file (also handling '.gz' decompression)
            CSVParser parser = parse(csvFile);

            try {
                // Initialize the object starting from the CSVParser
                this.file = csvFile;
                this.parser = parser;
                this.iterator = parser.iterator();
                this.numRecordsReturned = 0;
                this.numRecordsFile = 0;

                // Check that the field headers got by the CSVParser cover the required fields specified
                Set<String> missingFields = Sets.newHashSet(requiredFields);
                missingFields.removeAll(parser.getHeaderMap().keySet());
                if (!missingFields.isEmpty()) {
                    throw new IllegalArgumentException("File " + csvFile + " does not provide placeholder(s) "
                            + Joiner.on(", ").join(missingFields));
                }

                // Check that there is at least a CSV record to return
                if (!iterator.hasNext()) {
                    throw new IllegalArgumentException("File " + csvFile + " does not contain any CSV record");
                }

            } catch (Throwable ex) {
                // On failure, make sure to close the parser and propagate
                parser.close();
                Throwables.propagateIfPossible(ex, IOException.class);
                throw new RuntimeException(ex);
            }
        }

        public CSVRecord next() throws IOException {

            // Check this object was not closed or previously encountered an issue in reopening the CSV file
            Preconditions.checkState(parser != null);

            // If there are no further CSV records to return, start a new pass on the CSV file
            if (iterator == null || !iterator.hasNext()) {
                iterator = null;
                parser.close();
                parser = null;
                parser = parse(file);
                iterator = parser.iterator();
                numRecordsFile = numRecordsFile != 0 ? numRecordsFile : numRecordsReturned;
            }

            // Pick the next CSV record and increase the number of records
            ++numRecordsReturned;
            return iterator.next();
        }

        public void close() throws IOException {

            // Skip if already closed
            if (parser == null) {
                return;
            }

            try {
                // Otherwise, close the CSV parser and the underlying file stream
                parser.close();
            } finally {
                // Mark as closed and report some stats do help checking whether enough unique records were available
                parser = null;
                LOGGER.info("{} records returned from file {}{}", numRecordsReturned, file, numRecordsFile == 0 ? ""
                        : " (file only has " + numRecordsFile + " records, multiple passes were needed)");
            }
        }

        private static CSVParser parse(Path file) throws IOException {

            // Track the file stream, so to close it in case something goes wrong
            InputStream stream = null;

            try {
                // Open the file as a UTF-8 char stream, adding GZIP decoding if its name ends with .gz
                stream = Files.newInputStream(file);
                stream = file.toString().endsWith(".gz") ? new GZIPInputStream(stream) : stream;
                Reader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));

                // Return a CSVParser over the file content
                return CSVFormat.DEFAULT.builder()
                        .setCommentMarker('#')
                        .setHeader() // parse automatically from file
                        .setSkipHeaderRecord(true)
                        .build()
                        .parse(reader);

            } catch (Throwable ex) {
                // Make sure the file stream is closed, then propagate
                if (stream != null) {
                    stream.close();
                }
                throw ex;
            }
        }

    }

}
