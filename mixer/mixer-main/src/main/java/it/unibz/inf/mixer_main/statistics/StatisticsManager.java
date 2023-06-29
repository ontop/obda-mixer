package it.unibz.inf.mixer_main.statistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Manager class for the collection and saving of evaluation statistics.
 * <p>
 * An instance of this class can be created via {@link #StatisticsManager()}, after which statistics for a given
 * {@link StatisticsScope} can be submitted by obtaining a {@link StatisticsCollector} for that scope via
 * {@link #getCollector(StatisticsScope)}.
 * </p>
 * <p>
 * Instances of this class and of all the employed {@code StatisticsCollector} are thread-safe.
 * </p>
 */
@SuppressWarnings("unused")
public final class StatisticsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsManager.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Map<StatisticsScope, Collector> collectors;

    /**
     * Creates a new {@code StatisticsManager} instance, starting with empty statistics.
     */
    public StatisticsManager() {
        this.collectors = Maps.newHashMap();
    }

    /**
     * Returns the {@code Collector} for the {@code StatisticsScope} specified.
     *
     * @param scope the statistics scope (e.g., for a specific query execution)
     * @return the collector for the specified scope, not null, possibly reused across calls with the same scope
     */
    public StatisticsCollector getCollector(StatisticsScope scope) {
        Objects.requireNonNull(scope);
        synchronized (collectors) {
            return collectors.computeIfAbsent(scope, Collector::new);
        }
    }

    public void write(Path file) throws IOException {
        try (Writer out = Files.newBufferedWriter(file)) {
            if (file.getFileName().toString().endsWith(".json")) {
                writeJson(out);
            } else {
                writeText(out);
            }
        }
    }

    /**
     * Writes collected statistics to the supplied {@code Writer} using the original text format of the OBDA Mixer tool.
     *
     * @param out the sink to write to, which will not be closed by this method
     * @throws IOException in case of errors while writing to the sink
     */
    public void writeText(Writer out) throws IOException {

        // Check arguments (if null, method may otherwise succeed if statistics are empty)
        Objects.requireNonNull(out);

        // Take a snapshot of current Collector objects, synchronizing access to the collectors map
        List<Collector> collectors;
        synchronized (this.collectors) {
            collectors = Lists.newArrayList(this.collectors.values());
        }

        // Sort collectors by scope, i.e., global -> thread -> runs for the thread -> queries for the run
        collectors.sort(Comparator.comparing(c -> c.scope));

        // Keep track of the concurrent client for the last emitted scope
        int lastClientId = -1;

        // Iterate over scope statistics in order (starting from global ones)
        for (Collector c : collectors) {

            // Extract scope 'coordinates'
            StatisticsScope s = c.scope;
            OptionalInt clientId = s.getClientId();
            OptionalInt mixId = s.getMixId();
            Optional<String> queryId = s.getQueryId();

            // Emit single line [header], if needed
            if (clientId.isEmpty()) {
                out.append("[GLOBAL]\n");
            } else if (clientId.getAsInt() != lastClientId) {
                out.append("[thread#").append(Integer.toString(clientId.getAsInt())).append("]\n");
                lastClientId = clientId.getAsInt();
            }

            // Emit scope attribute = value pairs, sorted by attribute name and locking underlying map
            synchronized (c.attributes) {
                for (Entry<String, Object> e : c.attributes.entrySet().stream()
                        .sorted(Entry.comparingByKey())
                        .collect(Collectors.toList())) {
                    out.append("[");
                    if (mixId.isEmpty()) {
                        out.append("main");
                    } else {
                        out.append("run#").append(Integer.toString(mixId.getAsInt()));
                    }
                    out.append("] [").append(e.getKey());
                    if (queryId.isPresent()) {
                        out.append("#").append(queryId.get());
                    }
                    out.append("] = ").append(Objects.toString(e.getValue())).append("\n");
                }
            }
        }
    }

    /**
     * Writes collected statistics to the supplied {@code Writer} using the JSON format.
     *
     * @param out the sink to write to, which will not be closed by this method
     * @throws IOException in case of errors while writing to the sink
     */
    public void writeJson(Writer out) throws IOException {

        // Check arguments (if null, method may otherwise succeed if statistics are empty)
        Objects.requireNonNull(out);

        // Take a snapshot of current Collector objects, synchronizing access to the collectors map
        List<Collector> collectors;
        synchronized (this.collectors) {
            collectors = Lists.newArrayList(this.collectors.values());
        }

        // Sort collectors by scope, i.e., global -> thread -> runs for the thread -> queries for the run
        collectors.sort(Comparator.comparing(c -> c.scope));

        // Allocate JSON structures for the different scopes
        JsonNodeFactory nf = MAPPER.getNodeFactory();
        ArrayNode clients = nf.arrayNode();
        ArrayNode mixes = nf.arrayNode();
        ArrayNode queries = nf.arrayNode();
        ObjectNode global = null;

        // Iterate over statistics scopes, mapping them to JSON
        for (Collector c : collectors) {

            // Extract scope 'coordinates'
            StatisticsScope s = c.scope;
            OptionalInt clientId = s.getClientId();
            OptionalInt mixId = s.getMixId();
            Optional<String> queryId = s.getQueryId();

            // Create the JSON object containing the <key, value> pairs for the current scope
            ObjectNode stats = nf.objectNode();

            // Fill the 'client', 'mix', and 'query' attributes, tracking at which level we stopped
            ArrayNode a = null;
            if (clientId.isPresent()) {
                stats.put("client", clientId.getAsInt());
                a = clients;
            }
            if (mixId.isPresent()) {
                stats.put("mix", mixId.getAsInt());
                a = mixes;
            }
            if (queryId.isPresent()) {
                stats.put("query", queryId.get());
                a = queries;
            }

            // Assign the JSON object to the corresponding global / client / mix / query level
            if (a != null) {
                a.add(stats);
            } else {
                global = stats;
            }

            // Map <key, value> statistics attributes to JSON object fields
            synchronized (c.attributes) {
                for (Entry<String, Object> e : c.attributes.entrySet().stream()
                        .sorted(Entry.comparingByKey())
                        .collect(Collectors.toList())) {
                    stats.set(e.getKey(), MAPPER.valueToTree(e.getValue()));
                }
            }
        }

        // Assemble the root JSON object collecting scope statistics at different levels
        ObjectNode root = nf.objectNode();
        root.set("global", global);
        root.set("clients", clients);
        root.set("mixes", mixes);
        root.set("queries", queries);

        // Serialize
        MAPPER.writeValue(out, root);
    }

    public void importJson(Reader in, @Nullable Pattern filter, @Nullable String prefix, String... markers) throws IOException {

        // Ensure to operate on a buffered reader, for line-based reading
        BufferedReader bufIn = in instanceof BufferedReader ? (BufferedReader) in : new BufferedReader(in);

        // Setup the regex-based function to detect scopes in input lines matching the supplied markers
        Function<String, List<StatisticsScope>> extractor = StatisticsScope.fromStringWithMarkers(markers);

        // Track statistics about processed lines / attributes
        var numLine = new AtomicInteger();
        var numLinesImported = new AtomicInteger();
        var numLinesDiscarded = new AtomicInteger();
        var numAttrsImported = new AtomicInteger();
        var numAttrsDiscarded = new AtomicInteger();

        // Process the input one line at a time until stream completion
        while (true) {

            // Read the next line
            String line = bufIn.readLine();
            if (line == null) {
                break;
            }
            numLine.incrementAndGet();

            // Extract scopes from current line, skipping it if no scope is found or (emitting warning) if more are found
            Set<StatisticsScope> scopes = ImmutableSet.copyOf(extractor.apply(line));
            if (scopes.size() != 1) {
                if (scopes.size() > 1) {
                    LOGGER.warn("Multiple scopes in line {} when importing statistics: {}", line, scopes);
                    numLinesDiscarded.incrementAndGet();
                }
                continue;
            }

            // Retrieve unique scope and corresponding statistics collector
            var scope = scopes.iterator().next();
            var collector = getCollector(scope);

            // Define a recursive procedure to import nested JSON records into that collector, using filter & prefix
            class RecordImporter {

                public void importRecord(@Nullable String parentPath, ObjectNode record) {
                    for (Iterator<Entry<String, JsonNode>> i = record.fields(); i.hasNext(); ) {

                        // Obtain current <name: value> field in the JSON record
                        Entry<String, JsonNode> e = i.next();

                        // Compute the path leading to this field value, i.e., parent + field name
                        String path = parentPath == null ? e.getKey() : parentPath + "." + e.getKey();

                        // Either recurse if value is an object, or import the value if it maches the filter
                        if (e.getValue() instanceof ObjectNode) {
                            importRecord(path, (ObjectNode) e.getValue());
                        } else if (filter == null || filter.matcher(path).matches()) {
                            try {
                                String attribute = prefix == null ? path : prefix + path;
                                collector.set(attribute, e.getValue());
                                numAttrsImported.incrementAndGet();
                            } catch (Throwable ex) {
                                LOGGER.error("Could not import attribute " + path + " at line " + numLine + ": " + e.getValue(), ex);
                                numAttrsDiscarded.incrementAndGet();
                            }
                        }
                    }
                }

            }

            // Attempt parsing the line into a JSON object, and then try importing (nested) fields matching the filter
            try {
                var record = MAPPER.readValue(line, ObjectNode.class);
                new RecordImporter().importRecord(null, record);
                numLinesImported.incrementAndGet();

            } catch (Throwable ex) {
                LOGGER.error("Could not import line content into statistics: " + line, ex);
                numLinesDiscarded.incrementAndGet();
            }
        }

        // Log completion and relevant statistics
        LOGGER.info("Imported statistics: {}/{}/{} lines read/imported/discarded, {}/{} attributes imported/discarded",
                numLine, numLinesImported, numLinesDiscarded, numAttrsImported, numAttrsDiscarded);
    }

    private static final class Collector extends StatisticsCollector {

        final StatisticsScope scope;

        final Map<String, Object> attributes;

        Collector(StatisticsScope scope) {
            this.scope = scope;
            this.attributes = Maps.newHashMap();
        }

        @Override
        Object doGet(String attribute) {
            synchronized (attributes) {
                return attributes.get(attribute);
            }
        }

        @Override
        void doSet(String attribute, @Nullable Object value, @Nullable BinaryOperator<Object> merger) {
            synchronized (attributes) {
                if (value == null) {
                    assert merger == null;
                    attributes.remove(attribute);
                } else if (merger == null) {
                    attributes.put(attribute, value);
                } else {
                    attributes.merge(attribute, value, merger);
                }
            }
        }

    }

}
