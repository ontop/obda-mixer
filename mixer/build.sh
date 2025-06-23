#!/bin/bash

# Halt at first error
set -e

# Move to root folder of obda-mixer source tree
cd "$( dirname "${BASH_SOURCE[0]}" )"

# Compile using Maven
mvn clean package -DskipTests -Pfatjar "$@"

# Display some instructions
echo
echo "Built $( realpath ./mixer-main/target/obda-mixer.jar ) ($( stat -c %s ./mixer-main/target/obda-mixer.jar ) bytes)"
echo
echo "Notes"
echo "- pass '-Pdrivers' to 'build.sh' to embed JDBC drives for main DBMS in the executable (or call it adding --jars=<path-to-driver-jar>)"
echo "- pass '-Pontop' to 'build.sh' to include embedded Ontop OBDA engine in the executable"
echo "- remember to set up the configuration files!!"
echo "- for the first run, run with the '--help' or '--help-verbose' option"
echo
