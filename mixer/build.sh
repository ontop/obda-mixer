#!/bin/bash

# clean
mvn clean

# Build and skip the unit tests
mvn -Dmaven.test.skip=true install

cd mixer-distribution/

mvn assembly:single

echo
echo
echo "Installation completed. Now remember to set up the configuration files!!"
echo "For the first run, run with the --help or --help-verbose option."
