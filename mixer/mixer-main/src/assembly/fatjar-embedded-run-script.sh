#!/bin/bash

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=java
fi

declare -a opts
opts=()
for o in "$@"; do
    if [[ "$o" =~ ^"--jars=".*$ ]]; then
        jars="${jars+$jars:}${o#*=}"
    else
        opts+=("$o")
    fi
done

# shellcheck disable=SC2086
exec $JAVA $JAVA_OPTS \
    -cp "$0${jars+:$jars}${JAVA_CLASSPATH+:$JAVA_CLASSPATH}" \
    it.unibz.inf.mixer.execution.MixerMain \
    "${opts[@]}"
