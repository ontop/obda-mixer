package it.unibz.inf.mixer_main.utils;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class Option<T extends Comparable<?>> {

    private static final List<Option<?>> OPTIONS = new ArrayList<>(); // All options

    public static final Option<Boolean> HELP = Option.builder("--help", Boolean.class)
            .withDescription("displays help information")
            .build();

    public static final Option<String> CONF = Option.builder("--conf", String.class)
            .withDescription("specifies the location of the configuration file")
            .build();

    private final String name;
    private final String configKey;
    private final @Nullable String category;
    private final @Nullable String description;
    private final Class<T> type;
    private final @Nullable RangeSet<T> allowedValues;
    private final @Nullable T defaultValue;

    private @Nullable T value;
    private boolean parsed;

    private Option(String name, String configKey, @Nullable String category, @Nullable String description,
                   Class<T> type, @Nullable RangeSet<T> allowedValues, @Nullable T defaultValue) {
        this.name = name;
        this.configKey = configKey;
        this.category = category;
        this.description = description;
        this.defaultValue = defaultValue;
        this.type = type;
        this.allowedValues = allowedValues;
        this.value = defaultValue;
        this.parsed = false;
        OPTIONS.add(this);
    }

    public String getName() {
        return name;
    }

    public String getConfigKey() {
        return configKey;
    }

    public @Nullable String getCategory() {
        return category;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public Class<T> getType() {
        return type;
    }

    public @Nullable RangeSet<T> getAllowedValues() {
        return allowedValues;
    }

    public @Nullable T getDefaultValue() {
        return defaultValue;
    }

    public @Nullable T getValue() {
        return value;
    }

    public boolean isParsed() {
        return parsed;
    }

    @Override
    public String toString() {
        return name;
    }

    public static <T extends Comparable<?>> Builder<T> builder(String name, Class<T> type) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        typeName(type); // check type is supported
        return new Builder<>(name, type);
    }

    public static List<Option<?>> list() {
        return ImmutableList.copyOf(OPTIONS);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String help(String applicationName) {

        // Emit USAGE line
        StringBuilder sb = new StringBuilder();
        sb.append("USAGE: java -jar ").append(applicationName).append(".jar")
                .append(OPTIONS.isEmpty() ? "" : " [OPTIONS]").append('\n');

        // Sort options
        List<Option<?>> sortedOptions = ImmutableList.sortedCopyOf(
                Comparator.<Option<?>, String>comparing(o -> o.category, Ordering.natural().nullsLast())
                        .thenComparing(o -> typeName(o.type))
                        .thenComparing(o -> o.name),
                OPTIONS);

        // Compute (name, type+allowed values, description+default) rows, one for each option
        List<String[]> rows = Lists.newArrayList();
        for (@SuppressWarnings("rawtypes") Option opt : sortedOptions) {
            String typeStr = typeName(opt.type)
                    + (opt.allowedValues == null ? "" : " " + typeFormatValues(opt.type, opt.allowedValues));
            String descStr = MoreObjects.firstNonNull(opt.description, "")
                    + (opt.defaultValue == null ? "" : " (default: " + typeFormatValue(opt.type, opt.defaultValue) + ")");
            rows.add(new String[]{opt.name, typeStr, descStr});
        }

        // Compute maximum column lengths of option rows, and from them the format string to be used to emit options
        int[] lens = IntStream.range(0, 2)
                .map(i -> rows.stream().map(row -> row[i]).mapToInt(String::length).max().orElse(0))
                .toArray();
        String format = "  %-" + lens[0] + "s  %-" + lens[1] + "s  %s";

        // Emit options sorted by category, type, name
        String lastCategory = null;
        boolean first = true;
        for (int i = 0; i < rows.size(); ++i) {
            Option<?> o = sortedOptions.get(i);
            if (first || !Objects.equals(lastCategory, o.category)) {
                if (o.category != null) {
                    sb.append("\n").append(o.category.toUpperCase()).append(" OPTIONS:\n");
                } else if (lastCategory != null) {
                    sb.append("\nOTHER OPTIONS:\n");
                } else {
                    sb.append("\nOPTIONS:\n"); // in case there are no categories (always null
                }
            }
            String[] row = rows.get(i);
            sb.append(String.format(format, (Object[]) row)).append("\n");
            first = false;
            lastCategory = o.category;
        }
        return sb.toString();
    }

    public static void parse(String applicationName, String... args) {

        // Try matching every command line arguments to a corresponding option, loading the option value
        for (String arg : args) {
            boolean parsed = false;
            for (Option<?> opt : OPTIONS) {
                if (opt.parse(arg)) {
                    parsed = true;
                    break;
                }
            }
            if (!parsed) {
                System.err.println("Unknown option \"" + arg + "\". Type --help for a list of options");
                System.exit(1);
            }
        }

        // Handle builtin --help option
        if (HELP.isParsed() && HELP.getValue()) {
            System.out.println(help(applicationName));
            System.exit(0);
        }

        // Handle builtin --conf option
        if (CONF.parsed) {
            try {
                Map<String, String> config = loadConfig(Paths.get(CONF.getValue()));
                //noinspection rawtypes
                for (Option o : OPTIONS) {
                    if (!o.parsed && config.containsKey(o.configKey)) {
                        //noinspection unchecked
                        o.value = typeParseValue(o.type, config.get(o.configKey));
                    }
                }
            } catch (Throwable ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean parse(String string) {
        try {
            // Check if arg matches the option name and extract the value parsed into the expected type (may fail)
            T value;
            if (string.startsWith(name + "=")) {
                value = typeParseValue(type, string.substring(name.length() + 1));
            } else if (string.equals(name) && type.equals(Boolean.class)) {
                value = (T) Boolean.TRUE;
            } else {
                return false;
            }

            // If there is restriction in terms of allowed values, validate it
            if (allowedValues != null && !allowedValues.contains(value)) {
                throw new IllegalArgumentException("non-allowed value " + typeFormatValue(type, value)
                        + ", expected " + typeFormatValues(type, allowedValues));
            }

            // On success, store value, mark as parsed and report the match
            this.value = value;
            this.parsed = true;
            return true;

        } catch (Throwable ex) {
            // On failure, exit the application
            System.err.println("ERROR! Value out of range for option " + this.name + ": " + ex.getMessage());
            System.exit(1);
            return false; // useless
        }
    }

    private static Map<String, String> loadConfig(Path path) {

        try {
            if (path.endsWith(".properties")) {
                // Try reading the file as a java.util.Properties file
                try (Reader in = Files.newBufferedReader(path)) {
                    Properties properties = new Properties();
                    properties.load(in);
                    //noinspection rawtypes,unchecked
                    return (Map) properties;
                }

            } else {
                // Try reading the file using our "property value # comment" configuration format
                Map<String, String> config = Maps.newHashMap();
                for (String line : Files.readAllLines(path)) {
                    List<String> tokens = Arrays.asList(line.split("\\s+"));
                    if (tokens.size() < 2) {
                        continue;
                    }
                    String key = tokens.get(0);
                    StringBuilder valueBuilder = new StringBuilder();
                    for (int i = 1; i < tokens.size() && !tokens.get(i).startsWith("#"); ++i) {
                        valueBuilder.append(i > 1 ? " " : "").append(tokens.get(i));
                    }
                    String value = valueBuilder.toString();
                    config.put(key, value);
                }
                return config;
            }

        } catch (Throwable ex) {
            // Wrap and propagate
            throw new IllegalArgumentException("Could not read configuration file at " + path + ": " + ex.getMessage(), ex);
        }
    }


    // TYPE-SPECIFIC METHODS

    private static <T extends Comparable<?>> String typeName(Class<T> type) {
        if (type.equals(Boolean.class)) {
            return "<bool>";
        } else if (type.equals(String.class)) {
            return "<string>";
        } else if (type.equals(Double.class)) {
            return "<double>";
        } else if (type.equals(Integer.class)) {
            return "<int>";
        } else {
            throw new IllegalArgumentException("Unsupported option type " + type.getName());
        }
    }

    private static <T extends Comparable<?>> T typeParseValue(Class<T> type, String value) {
        Object result;
        if (type.equals(Boolean.class)) {
            if (value.equalsIgnoreCase("true")) {
                result = Boolean.TRUE;
            } else if (value.equalsIgnoreCase("false")) {
                result = Boolean.FALSE;
            } else {
                throw new IllegalArgumentException("Invalid boolean value");
            }
        } else if (type.equals(String.class)) {
            result = value;
        } else if (type.equals(Double.class)) {
            result = Double.valueOf(value);
        } else if (type.equals(Integer.class)) {
            result = Integer.valueOf(value);
        } else {
            throw new IllegalArgumentException("Unsupported option type " + type.getName());
        }
        return type.cast(result);
    }

    private static <T extends Comparable<?>> String typeFormatValue(Class<T> type, T value) {
        return value.toString();
    }

    private static <T extends Comparable<?>> String typeFormatValues(Class<T> type, RangeSet<T> range) {
        StringBuilder sb = new StringBuilder();
        Set<Range<T>> ranges = range.asRanges();
        sb.append(ranges.size() == 1 ? "" : "[");
        for (Range<T> r : ranges) {
            sb.append(sb.length() > 1 ? ", " : "");
            if (r.hasLowerBound() && r.hasUpperBound() && r.lowerBoundType() == BoundType.CLOSED
                    && r.upperBoundType() == BoundType.CLOSED && r.lowerEndpoint().equals(r.upperEndpoint())) {
                sb.append(typeFormatValue(type, r.lowerEndpoint()));
            } else {
                sb.append(r.hasLowerBound() && r.lowerBoundType() == BoundType.CLOSED ? "[" : "(");
                sb.append(r.hasLowerBound() ? typeFormatValue(type, r.lowerEndpoint()) : "");
                sb.append(", ");
                sb.append(r.hasUpperBound() ? typeFormatValue(type, r.upperEndpoint()) : "");
                sb.append(r.hasUpperBound() && r.upperBoundType() == BoundType.CLOSED ? "]" : ")");
            }
        }
        sb.append(ranges.size() == 1 ? "" : "]");
        return sb.toString();
    }

    public static final class Builder<T extends Comparable<?>> {

        private final String name;
        private @Nullable String configKey;
        private @Nullable String category;
        private @Nullable String description;
        private final Class<T> type;
        private @Nullable RangeSet<T> allowedValues;
        private @Nullable T defaultValue;

        private Builder(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }

        public Builder<T> withCategory(@Nullable String category) {
            this.category = category;
            return this;
        }

        public Builder<T> withDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        @SafeVarargs
        public final Builder<T> withAllowedValues(T... allowedValues) {
            return withAllowedValues(ImmutableRangeSet.copyOf(Arrays.stream(allowedValues)
                    .map(Range::singleton)
                    .collect(Collectors.toList())));
        }

        @SafeVarargs
        public final Builder<T> withAllowedValues(@Nullable Range<T>... allowedValues) {
            return withAllowedValues(allowedValues == null ? null : ImmutableRangeSet.unionOf(Arrays.asList(allowedValues)));
        }

        public Builder<T> withAllowedValues(@Nullable RangeSet<T> allowedValues) {
            this.allowedValues = allowedValues;
            return this;
        }

        public Builder<T> withDefaultValue(@Nullable Object defaultValue) {
            this.defaultValue = defaultValue == null || type.isInstance(defaultValue)
                    ? type.cast(defaultValue)
                    : typeParseValue(type, defaultValue.toString());
            return this;
        }

        public Builder<T> withConfigKey(@Nullable String configKey) {
            this.configKey = configKey;
            return this;
        }

        public Option<T> build() {
            // Derive configuration key from option name, if missing
            String configKey = this.configKey != null ? this.configKey : name.replaceAll("^-+", "");

            // Normalize category and description strings, setting them to null if empty
            String category = Strings.emptyToNull(this.category == null ? null : this.category.trim());
            String description = Strings.emptyToNull(this.description == null ? null : this.description.trim());

            // Build and register the Option
            return new Option<>(name, configKey, category, description, type, allowedValues, defaultValue);
        }

    }

}
