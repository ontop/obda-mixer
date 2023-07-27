package it.unibz.inf.mixer.execution;

import com.google.common.collect.Range;
import it.unibz.inf.mixer.core.Mixers;
import it.unibz.inf.mixer.execution.utils.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MixerOptions {

    private static final Logger LOGGER = LoggerFactory.getLogger(MixerOptions.class);

    // Query templates

    public static final Option<String> optQueriesDir = Option.builder("--queries-dir", String.class)
            .withDescription("Path to the queries directory")
            .withCategory("QUERY TEMPLATES")
            .build();

    public static final Option<String> optLang = Option.builder("--lang", String.class)
            .withDescription("The query language, one of: sql, or sparql")
            .withCategory("QUERY TEMPLATES")
            .withDefaultValue("sparql")
            .withAllowedValues("sql", "sparql")
            .build();

    // Database access

    public static final Option<String> optDbUrl = Option.builder("--db-url", String.class)
            .withDescription("URL of the database that the obda-mixer should use for extracting values in order to "
                    + "instantiate the query templates")
            .withCategory("DATABASE")
            .build();

    public static final Option<String> optDbUsername = Option.builder("--db-user", String.class)
            .withConfigKey("db-username")
            .withDescription("Username for accessing the database")
            .withCategory("DATABASE")
            .build();

    public static final Option<String> optDbPassword = Option.builder("--db-pwd", String.class)
            .withDescription("Password for accessing the database")
            .withCategory("DATABASE")
            .build();

    public static final Option<String> optDbDriverClass = Option.builder("--db-driverclass", String.class)
            .withConfigKey("driver-class")
            .withDescription("Database driver class")
            .withCategory("DATABASE")
            .build();

    // Execution settings

    public static final Option<String> optMode = Option.builder("--mode", String.class)
            .withDescription("The operating mode, one of: java api mode (java-api), sparql endpoint mode (web), "
                    + "or shell script mode (shell), ")
            .withCategory("EXECUTION")
            .build();

    public static final Option<Integer> optNumClients = Option.builder("--clients", Integer.class)
            .withConfigKey("num-clients")
            .withDescription("Number of clients querying the system in parallel. Rewriting and unfolding times are "
                    + "unavailable in multi-client mode")
            .withCategory("EXECUTION")
            .withDefaultValue(1)
            .withAllowedValues(Range.closed(1, 64))
            .build();

    public static final Option<Integer> optNumWarmUps = Option.builder("--warm-ups", Integer.class)
            .withConfigKey("num-warmups")
            .withDescription("Number of warm up runs.")
            .withCategory("EXECUTION")
            .withDefaultValue(1)
            .withAllowedValues(Range.atLeast(0))
            .build();

    public static final Option<Integer> optNumRuns = Option.builder("--runs", Integer.class)
            .withConfigKey("num-runs")
            .withDescription("Number of query mix runs.")
            .withCategory("EXECUTION")
            .withDefaultValue(1)
            .withAllowedValues(Range.atLeast(1))
            .build();

    public static final Option<Integer> optTimeout = Option.builder("--timeout", Integer.class)
            .withDescription("Maximum execution time allowed to a query, in seconds. A value of zero means no timeout.")
            .withCategory("EXECUTION")
            .withDefaultValue(0)
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

    static {
        // Iterate over all mixer types registered in the classpath (via "META-INF/mixer.properties" files)
        for (String mixerType : Mixers.list()) {

            // Obtain the mixer type metadata from "META-INF/mixer.properties"
            Map<String, String> mixerMetadata = Mixers.describe(mixerType);

            // Identify all option names, matching metadata keys 'conf.NAME.xyz'
            String prefix = "conf.";
            List<String> names = mixerMetadata.keySet().stream()
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
                    Option.builder("--" + mixerType + "-" + name,
                                    (Class) Class.forName(mixerMetadata.get(prefix + name + ".type")))
                            .withDescription(mixerMetadata.get(prefix + name + ".desc"))
                            .withDefaultValue(mixerMetadata.get(prefix + name + ".default"))
                            .withCategory("MIXER '" + mixerType + "'")
                            .build();
                } catch (Throwable ex) {
                    LOGGER.warn("Invalid definition of option '{}' of '{}': {}", name, mixerType, ex.getMessage());
                }
            }
        }
    }

    public static void parse(String... args) {
        Option.parse("obda-mixer", args);
    }

}