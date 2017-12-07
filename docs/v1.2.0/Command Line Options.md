# Command Line Options

For a complete list of parameters, with explanation, launch mixer with the parameter:

~~~~~~~~
--help-verbose
~~~~~~~~

You should observe a description similar to the following one:

~~~~~~~~~~~~
USAGE: java -jar mixer.jar [OPTIONS]

CONFIGURATION OPTIONS

--res               <string>                                               (default: src/main/resources)Location of the resources directory

Mixer OPTIONS

--runs              <int>               [1 -- 2147483647]                  (default: 50)       Number of query mix runs.

--warm-ups          <int>               [1 -- 2147483647]                  (default: 10)       Number of warm up runs.

--timeout           <int>               [0 -- 2147483647]                  (default: 0)        Maximum execution time allowed to a query, in seconds. A value of zero means no timeout.

--clients           <int>               [1 -- 64]                          (default: 1)        Number of clients querying the system in parallel. Rewriting and unfolding times are unavailable in multi-client mode

--obda              <string>            [ontop]                            (default: ontop)    The OBDA system under test
~~~~~~~~~~~~
