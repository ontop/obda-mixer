package it.unibz.inf.mixer.execution.statistics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.hash.Hashing;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Scope for {@code <key, value>} statistics, embracing global, client, mix and query scopes.
 * <p>
 * Statistics scopes can be defined at the following levels:
 * <ul>
 *     <li><b>global scope</b>, via {@link #global()} -
 *     for statistics not related to a specific concurrent client / query mix / query evaluation;</li>
 *     <li><b>client scope</b>, via {@link #forClient} -
 *     for statistics related to a specific concurrent client, but not to a query mix run by that client;</li>
 *     <li><b>mix scope</b>, via {@link #forMix(int, int)} -
 *     for statistics related to a query mix evaluation by a certain concurrent client, but not to a query evaluation
 *     within that mix (e.g., total mix evaluation time);</li>
 *     <li><b>query scope</b>, via {@link #forQuery(int, int, String)} -
 *     for statistics related to a query evaluation in a certain query mix run by a certain concurrent client.</li>
 * </ul>
 * </p>
 * <p>
 * Instances of this class are immutable, thread-safe value objects.
 * </p>
 */
@SuppressWarnings("unused")
public final class StatisticsScope implements Serializable, Comparable<StatisticsScope> {

    private static final String PROCESS_MARKER = randomMarker();

    private static final StatisticsScope GLOBAL = new StatisticsScope(Integer.MIN_VALUE, Integer.MIN_VALUE, null);

    private final int clientId;

    private final int mixId;

    @Nullable
    private final String queryId;

    private StatisticsScope(int clientId, int mixId, @Nullable String queryId) {
        this.clientId = clientId;
        this.mixId = mixId;
        this.queryId = queryId;
    }

    /**
     * Returns the <b>global</b> {@code StatisticsScope}.
     *
     * @return the global scope
     */
    public static StatisticsScope global() {
        return GLOBAL;
    }

    /**
     * Returns a <b>client</b> {@code StatisticsScope} for the concurrent client ID specified.
     *
     * @param clientId the client ID
     * @return the corresponding scope
     */
    public static StatisticsScope forClient(int clientId) {
        Preconditions.checkArgument(clientId != Integer.MIN_VALUE);
        return new StatisticsScope(clientId, Integer.MIN_VALUE, null);
    }

    /**
     * Returns a <b>mix</b> {@code StatisticsScope} for the concurrent client ID, and the mix ID specified.
     *
     * @param clientId the client ID, not negative
     * @param mixId    the mix ID, not negative and unique for that client (e.g., the mix sequence number)
     * @return the corresponding scope
     */
    public static StatisticsScope forMix(int clientId, int mixId) {
        Preconditions.checkArgument(clientId != Integer.MIN_VALUE);
        Preconditions.checkArgument(mixId != Integer.MIN_VALUE);
        return new StatisticsScope(clientId, mixId, null);
    }

    /**
     * Returns a <b>query</b> {@code StatisticsScope} for the concurrent client ID, the mix ID and the query ID specified.
     *
     * @param clientId the client ID, not negative
     * @param mixId    the mix ID, not negative and unique for that client (e.g., the mix sequence number)
     * @param queryId  the query ID, not null (e.g., the name of the query file)
     * @return the corresponding scope
     */
    public static StatisticsScope forQuery(int clientId, int mixId, String queryId) {
        Preconditions.checkArgument(clientId != Integer.MIN_VALUE);
        Preconditions.checkArgument(mixId != Integer.MIN_VALUE);
        Objects.requireNonNull(queryId);
        return new StatisticsScope(clientId, mixId, queryId);
    }

    /**
     * Returns the client ID, if defined for this scope
     *
     * @return the optional client ID
     */
    public OptionalInt getClientId() {
        return clientId == Integer.MIN_VALUE ? OptionalInt.empty() : OptionalInt.of(clientId);
    }

    /**
     * Returns the mix ID, if defined for this scope
     *
     * @return the optional mix ID
     */
    public OptionalInt getMixId() {
        return mixId == Integer.MIN_VALUE ? OptionalInt.empty() : OptionalInt.of(mixId);
    }

    /**
     * Returns the query ID, if defined for this scope
     *
     * @return the optional query ID
     */
    public Optional<String> getQueryId() {
        return Optional.ofNullable(queryId);
    }

    /**
     * {@inheritDoc} Scopes are sorted by ascending client ID, mix ID, query ID, with missing values sorted first.
     */
    @Override
    public int compareTo(StatisticsScope other) {
        int result = Ordering.natural().nullsFirst().compare(this.clientId, other.clientId);
        if (result == 0) {
            result = Ordering.natural().nullsFirst().compare(this.mixId, other.mixId);
            if (result == 0) {
                result = Ordering.natural().nullsFirst().compare(this.queryId, other.queryId);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc} Scopes are equal if they have the same client ID, mix ID and query ID.
     */
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof StatisticsScope)) {
            return false;
        }
        StatisticsScope other = (StatisticsScope) object;
        return clientId == other.clientId && mixId == other.mixId && Objects.equals(queryId, other.queryId);
    }

    /**
     * {@inheritDoc} Scopes are equal if they have the same client ID, mix ID and query ID.
     */
    @Override
    public int hashCode() {
        return Objects.hash(clientId, mixId, queryId);
    }

    /**
     * {@inheritDoc} Delegates to {@link #toString(String)} using {@link #processMarker()} as the marker.
     */
    @Override
    public String toString() {
        return toString(processMarker());
    }

    /**
     * Returns a string representation of the scope prefixed with the specified marker. If unambiguous, the marker can
     * be used to parse back the scope from log files (e.g., from the logged body of an executed query).
     *
     * @param marker the marker to inject in the string representation
     * @return the generated string
     */
    public String toString(String marker) {
        StringBuilder sb = new StringBuilder();
        sb.append('<').append(Objects.requireNonNull(marker));
        if (clientId != Integer.MIN_VALUE) {
            sb.append(':').append(clientId);
            if (mixId != Integer.MIN_VALUE) {
                sb.append(':').append(mixId);
                if (queryId != null) {
                    sb.append(':').append(queryId);
                }
            }
        }
        sb.append('>');
        return sb.toString();
    }

    /**
     * Parses a string obtained from {@link #toString()} or {@link #toString(String)} back to a {@code StatisticsScope}
     * object.
     *
     * @param string the string to parse, not null
     * @return the parsed scope
     */
    public static StatisticsScope fromString(String string) {
        String[] tokens = string.substring(1, string.length() - 1).split(":");
        int clientId = tokens.length < 2 ? Integer.MIN_VALUE : Integer.parseInt(tokens[1]);
        int mixId = tokens.length < 3 ? Integer.MIN_VALUE : Integer.parseInt(tokens[2]);
        String queryId = tokens.length < 4 ? null : tokens[3];
        return new StatisticsScope(clientId, mixId, queryId);
    }

    /**
     * Given the specified markers, returns a parsing function that efficiently extracts all the scopes serialized with
     * those markers in an input string. Scope detection is performed via regex matching, with the regex generated and
     * compiled when calling this method and subsequently reused by the returned function.
     *
     * @param markers the markes to match, or {@link #processMarker()} if empty
     * @return the generated function
     */
    public static Function<String, List<StatisticsScope>> fromStringWithMarkers(String... markers) {
        markers = markers != null && markers.length > 0 ? markers : new String[]{processMarker()};
        @SuppressWarnings("RegExpUnnecessaryNonCapturingGroup")
        Pattern pattern = Pattern.compile("[<](?:"
                + Arrays.stream(markers).map(Pattern::quote).collect(Collectors.joining("|"))
                + ")(?:[:][-]?[0-9]+(?:[:][-]?[0-9]+(?:[:][^>]+)?)?)?[>]");
        return (String string) -> {
            ImmutableList.Builder<StatisticsScope> b = null;
            Matcher m = pattern.matcher(string);
            while (m.find()) {
                b = b != null ? b : ImmutableList.builder();
                b.add(fromString(m.group()));
            }
            return b == null ? ImmutableList.of() : b.build();
        };
    }

    /**
     * Returns a marker identifying this process execution, constant across calls and generated via {@link #randomMarker()}.
     *
     * @return the process marker
     */
    public static String processMarker() {
        return PROCESS_MARKER;
    }

    /**
     * Generates a random, unambiguous marker at each call. The returned marker is an hexadecimal string 12 chars long,
     * prefixed by {@code mixer-} and suitable to be injected into submitted queries as comments.
     *
     * @return the generated marker
     */
    public static String randomMarker() {
        //noinspection UnstableApiUsage
        return "mixer-" + Hashing.murmur3_128()
                .newHasher()
                .putLong(System.currentTimeMillis())
                .putDouble(Math.random())
                .hash()
                .toString()
                .substring(0, 12)
                .toLowerCase();
    }

}
