# Quick Start Guide

In this tutorial we will set-up a testing environment based on the [NPD benchmark](https://github.com/ontop/npd-benchmark). We assume the steps described in this tutorial to be performed from the `${MIXER_HOME}` directory.

This tutorial requires:

- The [MySQL](https://www.mysql.com) relational engine;
- The [Ontop](https://github.com/ontop/ontop/tree/release/1.18.1) OBDA system, version `1.18.1` (already included in `obda-mixer` as Maven dependency).

### Downoload and build `obda-mixer`

We here use the already compiled jar available in the [`mixer/compiled-jar`](https://github.com/ontop/obda-mixer/blob/master/mixer/compiled-jar/mixer-distribution-1.2-jar-with-dependencies.jar) folder. We call this file `obda-mixer.jar`, and copy it in `${MIXER_HOME}`.

### Retrieve the NPD Benchmark

In particular, we will need:

- The [NPD MySQL database](https://github.com/ontop/npd-benchmark/tree/develop/data/mysql/original_npd/npd.mysql)
- A [query template](https://github.com/ontop/npd-benchmark/tree/develop/query_templates/Templates/01.rq)
- The NPD [mappings](https://github.com/ontop/npd-benchmark/blob/develop/mappings/mysql/ontopv1/ontop>%3D1.17/npd-v2-ql-mysql-ontop1.17.obda) and [ontology](https://github.com/ontop/npd-benchmark/blob/develop/ontology/npd-v2-ql.owl) files. We call such files `npd.obda` and `npd.owl`, respectively.

### Create the Database Instance

Use the MySQL database dump to create the database instance. We assume the following:

- Database address: localhost/npd
- Database user: test
- Database password: test

### Set up the Resources

Create a folder `resources` in `${MIXER_HOME}`. In such folder, copy both the mappings and ontology files. Observe that the mappings file `npd.obda` also contains the connection parameters needed by Ontop to access the database. We set these parameters as follows:

~~~
[SourceDeclaration]
sourceUri	http://sws.ifi.uio.no/vocab/npd-v2
connectionUrl	jdbc:mysql://localhost/npd
username	test
password	test
driverClass	com.mysql.jdbc.Driver
~~~

Create a folder `resources/Templates`, and copy the query templates in such folder.

### Set up the Configuration File

Create a file `src/main/resources/configuration.conf`, with the following content:

~~~
db-url localhost/npd
db-username test
db-pwd test
mappings-file resources/npd.obda
owl-file resources/npd.owl
queries-dir resources/Templates
log-file resources/statsMixer.csv
resources
driver-class com.mysql.jdbc.Driver
~~~

Such parameters tells `obda-mixer` how to access the database, where to find the files necessary to its execution, and to log the results in the file `statsMixer.csv`. For detailed information on the configuration file, please refer to the [Setup page](setup).

### Run

Run `obda-mixer` through:

~~~
java -jar obda-mixer.jar --conf=resources/configuration.conf
~~~

Such command runs the mixer with the parameters passed through the configuration files, as well as the default values for the parameters not explicitly specified in the configuration file. To see what such values are set to, use:

~~~
java -jar obda-mixer.jar --help-verbose
~~~

The results of the test will be saved in the file `resources/statsMixer.csv`. If you open that file, you will observe something as:

~~~
[GLOBAL]
[main] [load-time] = 63104
[thread#0]
[run#0] [num_results#01.q] = 39108
[run#0] [rewriting_time#01.q] = 0
[run#0] [resultset_traversal_time#01.q] = 941
[run#0] [execution_time#01.q] = 1315
[run#0] [unfolding_time#01.q] = 82
[run#0] [mix_time#0] = 2258 
~~~

Such information says that the query was unfolded in 82 ms, executed in 1315 ms, and the results set (containing 39108 answers) was traversed in 941 ms. The time to execute all the queries in the queries mix, which in our case was made only of one query, amounted to roughly 2 seconds.
