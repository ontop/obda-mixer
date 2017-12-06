# Setup

The configuration file (_configuration.conf_) contains various information like the location of the mappings file, or the desired path for logging. Parameters
are specified in the CSV format:

~~~~~~~
db-url 10.7.20.39:3306/npd
db-username user
db-pwd pwd
mappings-file src/main/resources/npd-v2-ql_a.obda
owl-file src/main/resources/npd-v2-ql_a.owl
queries-dir src/main/resources
log-path src/main/resources
driver-class com.mysql.jdbc.Driver
~~~~~~~

**Legenda:**

* **db-url**: URL of the database that the mixer should use for extracting values in order to fill the [SPARQL Templates](SPARQL Templates Syntax (v 1.1.0))
* **db-username**: Username for accessing the database
* **db-pwd**: Password for accessing the database
* **mappings-file**: Path to the mapping file
* **owl-file**: Path to the file containing the ontology
* **queries-dir**: Location of the directory containing the SPARQL templates (_Templates_ folder). For more information see [SPARQL Templates](SPARQL Templates Syntax (v 1.1.0)).
* **log-path**: Path where the mixer will write the results of the tests (_MixerStats.txt_)
* **driver-class**: Database driver class
