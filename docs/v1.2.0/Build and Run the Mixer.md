# Build and Run the Mixer

## Build the JAR

From the mixer directory, call

~~~~~~~~~~
./build.sh
~~~~~~~~~~

If you do not have a UNIX shell, then manually follow what prescribed in build.sh, that is

~~~~~~~~
mvn -Dmaven.test.skip=true install
cd mixer-distribution/
mvn assembly:single
~~~~~~~~

The compiled jars can be found in 

~~~~~
/mixer/mixer/mixer-distribution/target
~~~~~

## Configuration
In order to run the Mixer, a number of parameters need to be passed through a [configuration file](Setup).

Additionally to the configuration file, Mixer accepts a number of [command line parameters](Command Line Options). 

## First Run
In order to get a view of the available command-line parameters, run it with the help option:

~~~~~
--help-verbose
~~~~~
