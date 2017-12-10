# Setup

The configuration file (`configuration.conf` in this guide) contains various information like the location of the mappings file, or the desired path for logging. Parameters are specified in the CSV format:

~~~~~~~
num-runs 1
num-warmups 1
timeout 1200
num-clients 1
rewriting off
mode java-api
service-url 

db-url localhost/npd_clean_no_spatial
db-username user
db-pwd pwd
driver-class com.mysql.jdbc.Driver

owl-file src/main/resources/npd-v2-ql.owl
mappings-file src/main/resources/npd-v2-ql-mysql-ontop1.17.obda

queries-dir src/main/resources/Templates
log-path src/main/resources

java-api-class it.unibz.inf.mixer_ontop.core.MixerOntop
shell-cmd null
shell-out null
forced-timeouts null
forced-timeouts-timeout-value 1200
~~~~~~~

**Legenda:**

* **num-runs**: Number of test runs
* **num-warmups**: Number of warm-up runs (i.e., runs whose execution times will not be recorded)
* **timeout**: Maximum execution time allowed to a query, in seconds. A value of zero means no timeout. This parameter works only in `owl-api` mode, and only with jdbc drivers supporting the timeout feature.
* **num-clients**: querying the system in parallel. Rewriting and unfolding times are unavailable in multi-client mode.
* **rewriting**: If query rewriting is enabled. True or False.
* **mode**: Execution mode. One of: java api mode (`java-api`), sparql endpoint mode (`web`), or shell script mode (`shell`)
* **service-url**: URL for the SPARQL Endpoint (To be used with --obda=web)
* **db-url**: URL of the database that the obda-mixer should use for extracting values in order to instantiate the [query templates](Query Templates Syntax)
* **db-username**: Username for accessing the database
* **db-pwd**: Password for accessing the database
* **mappings-file**: Path to the mapping file
* **owl-file**: Path to the file containing the ontology
* **queries-dir**: Location of the directory containing the [query templates](Query Templates Syntax) (_Templates_ folder). 
* **log-path**: Path where obda-mixer will write the results of the tests (_MixerStats.txt_)
* **driver-class**: Database driver class. Database values are used to instantiate the [query templates](Query Templates Syntax).
* **java-api-class**: Class for the Mixer implementation. This parameter should be used in combination with the `--mode=java-api` option.
* **shell-cmd**: Command-line string for shell execution. This parameter should be used in combination with the `--mode=shell` option.
* **shell-out**: Should the output of the shell command be logged? This parameter should used in combination with the `--mode=shell` option.
* **forced-timeouts**: It forces the specified space-separated queries to timeout. The timeout value is specified through `--timeout-value` the option.
* **forced-timeouts-timeout-value**: Timeout value, in seconds, assigned to forcefully (through the `--forced-timeouts` option) timeouted queries. Default is 1200.
