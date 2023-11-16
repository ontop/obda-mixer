package it.unibz.inf.mixer.execution;

import com.google.common.collect.Range;
import it.unibz.inf.mixer.core.Mixer;
import it.unibz.inf.mixer.core.Plugins;
import it.unibz.inf.mixer.core.QuerySelector;
import it.unibz.inf.mixer.execution.utils.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MixerOptions {

    private static final Logger LOGGER = LoggerFactory.getLogger(MixerOptions.class);

    // Execution settings

    public static final Option<String> optSelector = Option.builder("--selector", String.class)
            .withDescription("The query selector to use for generating queries, one of: jdbcsel, csvsel")
            .withCategory("EXECUTION")
            .withDefaultValue("jdbcsel")
            .build();

    public static final Option<String> optMixer = Option.builder("--mixer", String.class)
            .withDescription("The mixer to use for running queries, one of: web (SPARQL endpoint), jdbc, shell, ontop")
            .withCategory("EXECUTION")
            .withDefaultValue("web")
            .build();

    public static final Option<Integer> optNumClients = Option.builder("--clients", Integer.class)
            .withConfigKey("num-clients")
            .withDescription("Number of clients querying the system in parallel. Rewriting and unfolding times are "
                    + "unavailable in multi-client mode")
            .withCategory("EXECUTION")
            .withDefaultValue(1)
            .withAllowedValues(Range.closed(1, 64))
            .build();

    public static final Option<Integer> optNumRuns = Option.builder("--runs", Integer.class)
            .withConfigKey("num-runs")
            .withDescription("Number of query mix query mixes.")
            .withCategory("EXECUTION")
            .withDefaultValue(1)
            .withAllowedValues(Range.atLeast(1))
            .build();

    public static final Option<Integer> optNumWarmUps = Option.builder("--warm-ups", Integer.class)
            .withConfigKey("num-warmups")
            .withDescription("Number of warm up query mixes (min).")
            .withCategory("EXECUTION")
            .withDefaultValue(1)
            .withAllowedValues(Range.atLeast(0))
            .build();

    public static final Option<Integer> optTimeWarmUps = Option.builder("--warm-ups-time", Integer.class)
            .withConfigKey("time-warmups")
            .withDescription("Time spent for warm up query mixes, in seconds (min).")
            .withCategory("EXECUTION")
            .withDefaultValue(0)
            .withAllowedValues(Range.atLeast(0))
            .build();

    public static final Option<Integer> optTimeout = Option.builder("--timeout", Integer.class)
            .withDescription("Maximum execution time allowed to a query, in seconds. A value of zero means no timeout.")
            .withCategory("EXECUTION")
            .withDefaultValue(0)
            .withAllowedValues(Range.atLeast(0))
            .build();

    public static final Option<Integer> optTimeoutWarmUps = Option.builder("--timeout-warm-ups", Integer.class)
            .withConfigKey("timeout-warmups")
            .withDescription("Maximum execution time allowed to a warm up query, in seconds. "
                    + "Defaults to the value of --timeout. A value of zero means no timeout.")
            .withCategory("EXECUTION")
            .withDefaultValue(null)
            .withAllowedValues(Range.atLeast(0))
            .build();

    public static final Option<String> optForceTimeouts = Option.builder("--force-timeouts", String.class)
            .withConfigKey("forced-timeouts")
            .withDescription("It forces the specified space-separated queries to timeout. "
                    + "The timeout value is specified through the option --timeout-value")
            .withCategory("EXECUTION")
            .build();

    public static final Option<Integer> optRetryAttempts = Option.builder("--retry-attempts", Integer.class)
            .withConfigKey("retry-attempts")
            .withDescription("number of query retry attempts (when error matches the retry condition)")
            .withCategory("EXECUTION")
            .withDefaultValue(0)
            .withAllowedValues(Range.atLeast(0))
            .build();

    public static final Option<Integer> optRetryWaitTime = Option.builder("--retry-wait-time", Integer.class)
            .withConfigKey("retry-wait-time")
            .withDescription("time to wait for before retrying a failed query, in seconds")
            .withCategory("EXECUTION")
            .withDefaultValue(0)
            .withAllowedValues(Range.atLeast(0))
            .build();

    public static final Option<String> optRetryCondition = Option.builder("--retry-condition", String.class)
            .withConfigKey("retry-condition")
            .withDescription("regex (Java syntax) matching query error messages for which to retry execution")
            .withCategory("EXECUTION")
            .withDefaultValue(".*")
            .build();

    // Log file handling

    public static final Option<String> optLogFile = Option.builder("--log-file", String.class)
            .withDescription("Path where obda-mixer will write the results of the tests")
            .withCategory("LOGGING")
            .withDefaultValue("mixer.json")
            .build();

    public static final Option<String> optLogImport = Option.builder("--log-import", String.class)
            .withDescription("Path of files from which to import further statistics at the end of the tests")
            .withCategory("LOGGING")
            .build();

    public static final Option<String> optLogImportFilter = Option.builder("--log-import-filter", String.class)
            .withDescription("Regex (Java syntax) matching 'field1.field2...' paths to import from --log-import file")
            .withCategory("LOGGING")
            .build();

    public static final Option<String> optLogImportPrefix = Option.builder("--log-import-prefix", String.class)
            .withDescription("Prefix to prepend to 'field1.field2...' paths imported from --log-import file")
            .withCategory("LOGGING")
            .build();

    @SuppressWarnings("unused") // emitted under 'configuration' when iterating over properties
    public static final Option<String> optLogComment = Option.builder("--log-comment", String.class)
            .withDescription("Optional comment to include in generated log (e.g., to document experimental setting)")
            .withCategory("LOGGING")
            .build();

    static {
        // Iterate over all plugin types registered in the classpath (via "META-INF/mixer.properties" files)
        for (String pluginName : Plugins.list()) {

            // Obtain the plugin metadata from "META-INF/mixer.properties"
            Map<String, String> pluginMetadata = Plugins.describe(pluginName);

            // Identify the category of the plugin (mixer or query selector)
            String category;
            try {
                Class<?> javaClass = Class.forName(pluginMetadata.get("type"));
                if (Mixer.class.isAssignableFrom(javaClass)) {
                    category = "MIXER";
                } else if (QuerySelector.class.isAssignableFrom(javaClass)) {
                    category = "QUERY SELECTOR";
                } else {
                    continue; // ignore the plugin as we are not going to use it
                }
            } catch (Throwable ex) {
                throw new Error(ex); // unexpected
            }

            // Identify all option names, matching metadata keys 'conf.NAME.xyz'
            String prefix = "conf.";
            List<String> names = pluginMetadata.keySet().stream()
                    .filter(k -> k.startsWith(prefix)).map(k -> {
                        int idxStart = prefix.length();
                        int idxEnd = k.indexOf('.', idxStart);
                        return k.substring(idxStart, idxEnd >= idxStart ? idxEnd : k.length());
                    })
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // Iterate over option names, registering corresponding Option objects
            for (String name : names) {
                try {
                    // Extract the properties defining the option
                    //noinspection unchecked,rawtypes
                    Option.builder("--" + pluginName + "-" + name,
                                    (Class) Class.forName(pluginMetadata.get(prefix + name + ".type")))
                            .withDescription(pluginMetadata.get(prefix + name + ".desc"))
                            .withDefaultValue(pluginMetadata.get(prefix + name + ".default"))
                            .withCategory(category + " '" + pluginName + "'")
                            .build();
                } catch (Throwable ex) {
                    LOGGER.warn("Invalid definition of option '{}' of '{}': {}", name, pluginName, ex.getMessage());
                }
            }
        }
    }

    public static void parse(String... args) {
        Option.parse("obda-mixer", args);
    }

}