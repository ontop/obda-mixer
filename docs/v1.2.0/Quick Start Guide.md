In this tutorial we will set-up a testing environment based on the [NPD benchmark](https://github.com/ontop/npd-benchmark). We assume 
the steps described in this tutorial to be performed from the `${MIXER_HOME}` directory.

This tutorial requires:

- The [MySQL](https://www.mysql.com) relational engine;
- The [Ontop](https://github.com/ontop/ontop/tree/release/1.18.1) OBDA system, version `1.18.1`.

### Downoload and build `obda-mixer`

We here use the already compiled jar available in the [`mixer/compiled-jar`](https://github.com/ontop/obda-mixer/blob/master/mixer/compiled-jar/mixer-distribution-1.2-jar-with-dependencies.jar) folder. We call this file `obda-mixer.jar`, and copy it in `${MIXER_HOME}`.

### Retrieve the NPD Benchmark

In particular, we will need:

- The [NPD MySQL database](https://github.com/ontop/npd-benchmark/tree/master/data/mysql/original_npd)
- Some [query templates](https://github.com/ontop/npd-benchmark/tree/master/query_templates/Templates)
- The NPD [mappings](https://github.com/ontop/npd-benchmark/blob/master/mappings/mysql/ontop>%3D1.17/npd-v2-ql-mysql-ontop1.17.obda) and [ontology](https://github.com/ontop/npd-benchmark/blob/master/ontology/npd-v2-ql.owl) files. We call such files `npd.obda` and `npd.owl`, respectively.

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
log-path resources
driver-class com.mysql.jdbc.Driver
~~~

Such parameters tells `obda-mixer` how to access the database, and where to find the files necessary to its execution. For detailed information on the configuration file, please refer to the [Setup page](setup).

### Run!!

That's it. Just run through

~~~
java -jar obda-mixer.jar --conf=resources/configuration.conf
~~~

Such command runs the mixer with the parameters passed through the configuration files, as well as the default values for the parameters not explicitly specified in the configuration file. To see what such values are set to, use:

~~~
java -jar obda-mixer.jar --help-verbose
~~~
