package it.unibz.inf.mixer_main.execution;

import it.unibz.inf.utils_options.core.BooleanOption;
import it.unibz.inf.utils_options.core.IntOption;
import it.unibz.inf.utils_options.core.StringOption;
import it.unibz.inf.utils_options.core.StringOptionWithRange;
import it.unibz.inf.utils_options.ranges.IntRange;
import it.unibz.inf.utils_options.ranges.StringRange;

abstract class MixerOptionsInterface {
    // Command-line options
    static final IntOption optNumRuns = new IntOption("--runs", "Number of query mix runs.", "Mixer", 1, new IntRange(1, Integer.MAX_VALUE, true, true));
    static final IntOption optNumWarmUps = new IntOption("--warm-ups", "Number of warm up runs.", "Mixer", 1, new IntRange(0, Integer.MAX_VALUE, true, true));
    static final IntOption optTimeout = new IntOption("--timeout", "Maximum execution time allowed to a query, in seconds. A value of zero means no timeout. This parameter works only in `owl-api` mode, and only with jdbc drivers supporting the timeout feature.", "Mixer", 0, new IntRange(0, Integer.MAX_VALUE, true, true));
    static final IntOption optNumClients = new IntOption("--clients", "Number of clients querying the system in parallel. Rewriting and unfolding times are unavailable in multi-client mode", "Mixer", 1, new IntRange(1, 64, true, true));
    static final BooleanOption optRewriting = new BooleanOption("--rewriting", "If query rewriting is enabled. Either true or false.", "Mixer", false);

	static final StringOptionWithRange optLang = new StringOptionWithRange("--mode", "The query language, "
			+ "one of: sql, or sparql", "Mixer", "sparql", new StringRange("[sql,sparql]"));

    // Command-line option deciding which Mixer implementation should be used
    static final StringOptionWithRange optMode = new StringOptionWithRange("--mode", "The operating mode, "
	    + "one of: java api mode (java-api), sparql endpoint mode (web), or shell script mode (shell), ", "Mixer", "java-api", new StringRange("[java-api,web,shell]"));
    static final StringOption optServiceUrl = new StringOption("--url", "URL for the SPARQL Endpoint (To be used with --obda=web)", "Mixer", "");

    static final StringOption optConfFile = new StringOption("--conf", "Location of the configuration file", "CONFIGURATION", "resources/configuration.conf");

    static final StringOption optDbUrl = 
	    new StringOption("--db-url", 
		    "URL of the database that the obda-mixer should use for extracting "
			    + "values in order to instantiate the query templates", 
			    "CONFIGURATION", "");
    static final StringOption optDbUsername = 
	    new StringOption("--db-user", 
		    "Username for accessing the database", 
		    "CONFIGURATION", "");
    static final StringOption optDbPassword = 
	    new StringOption("--db-pwd", 
		    "Password for accessing the database", 
		    "CONFIGURATION", "");
    static final StringOption optDbDriverClass = 
	    new StringOption("--db-driverclass", 
		    "Database driver class", 
		    "CONFIGURATION", "");
    static final StringOption optOwlFile = 
	    new StringOption("--ontology", 
		    "Database driver class", 
		    "CONFIGURATION", "");
    static final StringOption optMappingsFile = 
	    new StringOption("--mappings-file", 
		    "Path to the mapping file", 
		    "CONFIGURATION", "");
	static final StringOption optPropertiesFile =
	    new StringOption("--properties-file",
		    "Path to the properties file",
		    "CONFIGURATION", "");
    static final StringOption optQueriesDir = 
	    new StringOption("--queries-dir", 
		    "Path to the queries directory", 
		    "CONFIGURATION", "resources/Templates");
    static final StringOption optLogFile = 
	    new StringOption("--log-file", 
		    "Path where obda-mixer will write the results of the tests", 
		    "Mixer", "resources/MixerStats.txt");
	static final StringOption optLogImport =
			new StringOption("--log-import",
					"Path of files from which to import further statistics at the end of the tests",
					"Mixer", "");
	static final StringOption optLogImportFilter =
			new StringOption("--log-import-filter",
					"Regex (Java syntax) matching the 'field1.field2...' paths to import from --log-import file",
					"Mixer", "");
	static final StringOption optLogImportPrefix =
			new StringOption("--log-import-prefix",
					"Prefix to prepend to 'field1.field2...' paths imported from --log-import file",
					"Mixer", "");
	static final StringOption optJavaApiClass =
	    new StringOption("--api-class", 
		    "Class for the Mixer implementation. This parameter should be used "
			    + "in combination with the `--mode=java-api` option.", 
			    "CONFIGURATION", "it.unibz.inf.mixer_ontop.core.MixerOntop");
    static final StringOption optShellCmd = 
	    new StringOption("--shell-cmd", 
		    "Command-line string for shell execution. This parameter should be used"
			    + "in combination with the `--mode=shell` option.", 
			    "Mixer", "");
    static final BooleanOption optShellOut = 
	    new BooleanOption("--shell-out", "Should the output of the shell command be logged? This "
		    + "parameter should be used in combination "
		    + "with the `--mode=shell` option.", "Mixer", false);
    static final StringOption optForceTimeouts =
	    new StringOption("--force-timeouts", "It forces the specified space-separated queries "
		    + "to timeout. The timeout value is specified through the option"
		    + " --timeout-value", "Mixer", "");
    static final IntOption optForcedTimeoutsValue = 
	    new IntOption("--timeout-value", "Number of clients querying the system in parallel. Rewriting "
		    + "and unfolding times are unavailable in multi-client mode", 
		    "Mixer", 1200, new IntRange(1, Integer.MAX_VALUE, true, true));

    // JDBC-mode
	static final StringOption optJDBCModeDbUrl =
			new StringOption("--db-url",
					"In JDBC-mode, URL of the database to be queried",
					"CONFIGURATION", "");
	static final StringOption optJDBCModeDbUsername =
			new StringOption("--db-user",
					"In JDBC-mode, Username for accessing the database to be queried",
					"CONFIGURATION", "");
	static final StringOption optJDBCModeDbPassword =
			new StringOption("--db-pwd",
					"In JDBC-mode, Password for accessing the database to be queried",
					"CONFIGURATION", "");
	static final StringOption optJDBCModeDbDriverClass =
			new StringOption("--db-driverclass",
					"In JDBC-mode, driver class of the database to be queried",
					"CONFIGURATION", "");
};