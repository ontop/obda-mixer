package it.unibz.inf.mixer_main.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Manager class for the collection and saving of evaluation statistics.
 * <p>
 * An instance of this class can be created via {@link #StatisticsManager()}, after which statistics can be submitted
 * through {@link StatisticsCollector} at different scopes:
 * <ul>
 *     <li><b>global scope</b>, via {@link #getGlobalCollector()} -
 *     for statistics not related to a specific concurrent client / query mix / query evaluation;</li>
 *     <li><b>client scope</b>, via {@link #getGlobalCollector()} -
 *     for statistics related to a specific concurrent client, but not to a query mix run by that client;</li>
 *     <li><b>mix scope</b>, via {@link #getMixCollector(int, int)} -
 *     for statistics related to a query mix evaluation by a certain concurrent client, but not to a query evaluation
 *     within that mix (e.g., total mix evaluation time);</li>
 *     <li><b>query scope</b>, via {@link #getQueryCollector(int, int, String)} -
 *     for statistics related to a query evaluation in a certain query mix run by a certain concurrent client.</li>
 * </ul>
 * </p>
 * <p>
 * Instances of this class and of all the employed {@code StatisticsCollector} are thread-safe.
 * </p>
 */
@SuppressWarnings("unused")
public final class StatisticsManager {

    private final Map<Scope, Collector> collectors;

    /**
     * Creates a new {@code StatisticsManager} instance, starting with empty statistics.
     */
    public StatisticsManager() {
        this.collectors = Maps.newHashMap();
    }

    /**
     * Returns the {@code Collector} for statistics in the <b>global</b> scope, i.e., not associated to any concurrent
     * client / query mix / query evaluation.
     *
     * @return the collector for global-level statistics, not null, possibly reused across calls
     */
    public StatisticsCollector getGlobalCollector() {
        return doGetCollector(-1, -1, null);
    }

    /**
     * Returns the {@code Collector} for statistics in a <b>client</b> scope, i.e., associated to a concurrent client
     * (identified by non-zero client ID) sending query mixes to the tested system, but not to a specific query mix run.
     *
     * @param client the client ID, not negative
     * @return the collector for client-level statistics, not null, possibly reused across calls
     */
    public StatisticsCollector getClientCollector(int client) {
        Preconditions.checkArgument(client >= 0);
        return doGetCollector(client, -1, null);
    }

    /**
     * Returns the {@code Collector} for statistics in a <b>mix</b> scope, i.e., associated to a specific query mix
     * evaluation submitted by certain concurrent client (both identified by non-zero numeric ID), but not to a specific
     * query.
     *
     * @param client the client ID, not negative
     * @param mix    the mix ID, not negative and unique for that client (e.g., the mix sequence number)
     * @return the collector for mix-level statistics, not null, possibly reused across calls
     */
    public StatisticsCollector getMixCollector(int client, int mix) {
        Preconditions.checkArgument(client >= 0);
        Preconditions.checkArgument(mix >= 0);
        return doGetCollector(client, mix, null);
    }

    /**
     * Returns the {@code Collector} for statistics in a <b>query</b> scope, i.e., associated to a specific query
     * execution within a specific query mix run by a specific concurrent client against the tested system.
     *
     * @param client the client ID, not negative
     * @param mix    the mix ID, not negative and unique for that client (e.g., the mix sequence number)
     * @param query  the query ID, not null (e.g., the name of the query file)
     * @return the collector for query-level statistics, not null, possibly reused across calls
     */
    public StatisticsCollector getQueryCollector(int client, int mix, String query) {
        Preconditions.checkArgument(client >= 0);
        Preconditions.checkArgument(mix >= 0);
        Objects.requireNonNull(query);
        return doGetCollector(client, mix, query);
    }

    private StatisticsCollector doGetCollector(int client, int mix, @Nullable String query) {
        Scope scope = new Scope(client, mix, query);
        synchronized (collectors) {
            return collectors.computeIfAbsent(scope, Collector::new);
        }
    }

    public void mergeJson(Reader in, String idAttribute) {
        // TODO
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
     * @param out the sink to write to, open and not closed by this method
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
        int lastClient = -1;

        // Iterate over scope statistics in order (starting from global ones)
        for (Collector c : collectors) {

            // Emit single line [header], if needed
            Scope s = c.scope;
            if (s.client < 0) {
                out.append("[GLOBAL]\n");
            } else if (s.client != lastClient) {
                out.append("[thread#").append(Integer.toString(s.client)).append("]\n");
                lastClient = s.client;
            }

            // Emit scope attribute = value pairs, sorted by attribute name and locking underlying map
            synchronized (c.attributes) {
                for (Entry<String, Object> e : c.attributes.entrySet().stream()
                        .sorted(Entry.comparingByKey())
                        .collect(Collectors.toList())) {
                    out.append("[");
                    if (s.mix < 0) {
                        out.append("main");
                    } else {
                        out.append("run#").append(Integer.toString(s.mix));
                    }
                    out.append("] [").append(e.getKey());
                    if (s.query != null) {
                        out.append("#").append(s.query);
                    }
                    out.append("] = ").append(Objects.toString(e.getValue())).append("\n");
                }
            }
        }
    }

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

        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules()
                .enable(SerializationFeature.INDENT_OUTPUT);
        JsonNodeFactory nf = mapper.getNodeFactory();

        ObjectNode global = null;
        ArrayNode clients = nf.arrayNode();
        ArrayNode mixes = nf.arrayNode();
        ArrayNode queries = nf.arrayNode();

        for (Collector c : collectors) {

            ObjectNode stats = nf.objectNode();

            Scope s = c.scope;
            ArrayNode a = null;
            if (s.client >= 0) {
                stats.put("client", s.client);
                a = clients;
            }
            if (s.mix >= 0) {
                stats.put("mix", s.mix);
                a = mixes;
            }
            if (s.query != null) {
                stats.put("query", s.query);
                a = queries;
            }

            if (a != null) {
                a.add(stats);
            } else {
                global = stats;
            }

            synchronized (c.attributes) {
                for (Entry<String, Object> e : c.attributes.entrySet().stream()
                        .sorted(Entry.comparingByKey())
                        .collect(Collectors.toList())) {
                    stats.set(e.getKey(), mapper.valueToTree(e.getValue()));
                }
            }
        }

        ObjectNode root = nf.objectNode();
        root.set("global", global);
        root.set("clients", clients);
        root.set("mixes", mixes);
        root.set("queries", queries);

        mapper.writeValue(out, root);
    }

    private static final class Scope implements Comparable<Scope> {

        final int client;

        final int mix;

        final String query;

        Scope(int client, int mix, String query) {
            this.client = client;
            this.mix = mix;
            this.query = query;
        }

        @Override
        public int compareTo(Scope other) {
            int result = Ordering.natural().nullsFirst().compare(this.client, other.client);
            if (result == 0) {
                result = Ordering.natural().nullsFirst().compare(this.mix, other.mix);
                if (result == 0) {
                    result = Ordering.natural().nullsFirst().compare(this.query, other.query);
                }
            }
            return result;
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof Scope)) {
                return false;
            }
            Scope other = (Scope) object;
            return client == other.client && mix == other.mix && Objects.equals(query, other.query);
        }

        @Override
        public int hashCode() {
            return Objects.hash(client, mix, query);
        }

    }

    private static final class Collector extends StatisticsCollector {

        final Scope scope;

        final Map<String, Object> attributes;

        Collector(Scope scope) {
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
