package it.unibz.inf.mixer.execution.statistics;

import com.fasterxml.jackson.databind.node.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BinaryOperator;

/**
 * Collector for {@code <attribute, value>} statistics in a certain scope (global, client, mix, query).
 * <p>
 * Each scope (e.g., the global one, a query mix run, a query execution) is associated to statistics in the form of
 * {@code <attribute, value>} pairs where:
 * <ul>
 * <li>the attribute is an arbitrary string, with the exception of reserved attribute names {@code client}, {@code mix}
 * and {@code query} (to avoid clashes in serialized statistics);</li>
 * <li>the value is a non-null instance of type {@code String}, {@code Boolean}, {@code Long}, {@code Integer},
 * {@code Double}, {@code Float}, or a {@code List} or {@code Map} of the former scalar types (for maps, the key has to
 * be a string); a {@link com.fasterxml.jackson.databind.JsonNode} convertible to one of the these types is also accepted
 * when setting avalue.</li>
 * </ul>
 * </p>
 * <p>
 * Instances of this class are thread-safe. Note that {@code StatisticsCollector} only defines the general contract and
 * supplies a stateless skeleton where all operations are mapped to atomic invocation of abstract methods
 * {@link #doGet(String)} and {@link #doSet(String, Object, BinaryOperator)} that have to be implemented by overriding
 * classes.
 * </p>
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class StatisticsCollector {

    static final Set<String> RESERVED_ATTRIBUTES = ImmutableSet.of("client", "mix", "query");

    abstract Object doGet(String attribute);

    abstract void doSet(String attribute, @Nullable Object value, @Nullable BinaryOperator<Object> merger);

    public final boolean has(String attribute) {
        checkAttribute(attribute);
        return doGet(attribute) != null;
    }

    public final <T> T get(String attribute) {
        checkAttribute(attribute);
        //noinspection unchecked
        return (T) doGet(attribute);
    }

    public final <T> T get(String attribute, Class<T> type) {
        checkAttribute(attribute);
        return type.cast(doGet(attribute));
    }

    public final StatisticsCollector set(String attribute, @Nullable Object value) {
        checkAttribute(attribute);
        doSet(attribute, checkAndNormalizeValue(value), null);
        return this;
    }

    public final <T> StatisticsCollector set(String attribute, T value, BinaryOperator<T> merger) {
        checkAttribute(attribute);
        Objects.requireNonNull(value);
        Objects.requireNonNull(merger);
        doSet(attribute, checkAndNormalizeValue(value), (oldValue, newValue) -> {
            if (oldValue.getClass() == newValue.getClass()
                    || oldValue instanceof List<?> && newValue instanceof List<?>
                    || oldValue instanceof Map<?, ?> && newValue instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked") T mergedValue = merger.apply((T) oldValue, (T) newValue);
                return checkAndNormalizeValue(mergedValue);
            } else {
                throw new IllegalArgumentException("Unmergeable incompatible values " + oldValue + ", " + newValue);
            }
        });
        return this;
    }

    public final StatisticsCollector add(String attribute, Object increment) {
        return set(attribute, increment, (v, i) -> {
            if (v instanceof String) {
                return ((String) v) + i;
            } else if (v instanceof Long) {
                return ((Long) v) + ((Long) i);
            } else if (v instanceof Integer) {
                return ((Integer) v) + ((Integer) i);
            } else if (v instanceof Float) {
                return ((Float) v) + ((Float) i);
            } else if (v instanceof Double) {
                return ((Double) v) + ((Double) i);
            } else if (v instanceof List<?>) {
                ImmutableList.Builder<Object> builder = ImmutableList.builder();
                builder.addAll((List<?>) v);
                builder.addAll((List<?>) i);
                return builder.build();
            } else if (v instanceof Map<?, ?>) {
                ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
                builder.putAll((Map<?, ?>) v);
                builder.putAll((Map<?, ?>) i);
                return builder.build();
            } else {
                throw new IllegalArgumentException("Cannot add " + v + ", " + i);
            }
        });
    }

    private void checkAttribute(String attribute) {
        Objects.requireNonNull(attribute);
        Preconditions.checkArgument(!RESERVED_ATTRIBUTES.contains(attribute),
                "Illegal attribute name: %s", attribute);
    }

    @Nullable
    private Object checkAndNormalizeValue(@Nullable Object value) {

        // Accept nulls and immutable scalar values
        if (isNormalizedScalarValue(value)) {
            return value;
        }

        // Accept scalar JSON ValueNode, converting their value to Java native types
        if (value instanceof ValueNode) {
            if (value instanceof BooleanNode) {
                return ((BooleanNode) value).booleanValue();
            } else if (value instanceof LongNode) {
                return ((LongNode) value).longValue();
            } else if (value instanceof IntNode) {
                return ((IntNode) value).intValue();
            } else if (value instanceof FloatNode) {
                return ((FloatNode) value).floatValue();
            } else if (value instanceof DoubleNode) {
                return ((DoubleNode) value).doubleValue();
            }
            return ((ValueNode) value).textValue();
        }

        // Accept List<?> and JSON ArrayNode, normalizing their values and making the resulting list non-modifiable
        if (value instanceof List<?> || value instanceof ArrayNode) {
            if (value instanceof ImmutableList<?> && ((List<?>) value).stream().allMatch(this::isNormalizedScalarValue)) {
                return value;
            }
            boolean containsNull = false;
            int size = value instanceof ArrayNode ? ((ArrayNode) value).size() : ((List<?>) value).size();
            List<Object> newList = Lists.newArrayListWithCapacity(size);
            for (Object v : (Iterable<?>) value) {
                Object v2 = checkAndNormalizeValue(v);
                containsNull |= (v2 == null);
                newList.add(v2);
            }
            return containsNull ? Collections.unmodifiableList(newList) : ImmutableList.copyOf(newList);
        }

        // Accept Map<String, ?>, checking and normalizing values and making the resulting map non-modifiable
        if (value instanceof Map<?, ?> || value instanceof ObjectNode) {
            if (value instanceof ImmutableMap<?, ?> && ((Map<?, ?>) value).entrySet().stream()
                    .allMatch(e -> e.getKey() instanceof String && isNormalizedScalarValue(e.getValue()))) {
                return value;
            }
            boolean containsNull = false;
            Map<String, Object> newMap = Maps.newLinkedHashMap();
            Iterator<?> i = (value instanceof ObjectNode)
                    ? ((ObjectNode) value).fields()
                    : ((Map<?, ?>) value).entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) i.next();
                Preconditions.checkArgument(e.getKey() instanceof String, "Only String keys accepted in maps");
                Object v2 = checkAndNormalizeValue(e.getValue());
                containsNull |= (v2 == null);
                newMap.put((String) e.getKey(), v2);
            }
            return containsNull ? Collections.unmodifiableMap(newMap) : ImmutableMap.copyOf(newMap);
        }

        // Fail reporting offending value
        throw new IllegalArgumentException("Unsupported statistics value: " + value);
    }

    private boolean isNormalizedScalarValue(Object v) {
        return v == null
                || v instanceof String
                || v instanceof Boolean
                || v instanceof Long
                || v instanceof Integer
                || v instanceof Float
                || v instanceof Double;
    }

}
