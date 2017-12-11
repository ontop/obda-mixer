# Command Line Options

Command-line options specified by the user override the settings provided by the configuration file. For a complete list of options, with explanation, launch mixer with the parameter:

~~~~~~~~
--help-verbose
~~~~~~~~

You should observe a description similar to the following one:

~~~~~~~~~~~~
USAGE: java -jar vig.jar [OPTIONS]

CONFIGURATION OPTIONS

--conf              <string>                                               (default: resources/configuration.conf)Location of the configuration file

--db-url            <string>                                               (default: )         URL of the database that the obda-mixer should use for extracting values in order to instantiate the query templates

--db-user           <string>                                               (default: )         Username for accessing the database

--db-pwd            <string>                                               (default: )         Password for accessing the database

--db-driverclass    <string>                                               (default: )         Database driver class

--ontology          <string>                                               (default: )         Database driver class

--mappings-file     <string>                                               (default: )         Path to the mapping file

--queries-dir       <string>                                               (default: resources/Templates)Path to the queries directory

--api-class         <string>                                               (default: it.unibz.inf.mixer_ontop.core.MixerOntop)Class for the Mixer implementation. This parameter should be used in combination with the `--mode=java-api` option.

Mixer OPTIONS

--rewriting         <bool>              [true -- false]                    (default: false)    If query rewriting is enabled. Either true or false.

--shell-out         <bool>              [true -- false]                    (default: false)    Should the output of the shell command be logged? This parameter should be used in combination with the `--mode=shell` option.

--runs              <int>               [1 -- 2147483647]                  (default: 1)        Number of query mix runs.

--warm-ups          <int>               [0 -- 2147483647]                  (default: 1)        Number of warm up runs.

--timeout           <int>               [0 -- 2147483647]                  (default: 0)        Maximum execution time allowed to a query, in seconds. A value of zero means no timeout. This parameter works only in `owl-api` mode, and only with jdbc drivers supporting the timeout feature.

--clients           <int>               [1 -- 64]                          (default: 1)        Number of clients querying the system in parallel. Rewriting and unfolding times are unavailable in multi-client mode

--timeout-value     <int>               [1 -- 2147483647]                  (default: 1200)     Number of clients querying the system in parallel. Rewriting and unfolding times are unavailable in multi-client mode

--mode              <string>            [java-api,web,shell]               (default: java-api) The operating mode, one of: java api mode (java-api), sparql endpoint mode (web), or shell script mode (shell), 

--url               <string>                                               (default: )         URL for the SPARQL Endpoint (To be used with --obda=web)

--log-file          <string>                                               (default: resources/MixerStats.txt)Path where obda-mixer will write the results of the tests

--shell-cmd         <string>                                               (default: )         Command-line string for shell execution. This parameter should be usedin combination with the `--mode=shell` option.

--force-timeouts    <string>                                               (default: )         It forces the specified space-separated queries to timeout. The timeout value is specified through the option --timeout-value
~~~~~~~~~~~~
