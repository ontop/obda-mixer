#
# Dockerfile for obda-mixer
# -------------------------
#
# Generate the Docker image without embedding JDBC drives or Ontop (minimum size):
#
#   docker build -t obda-mixer:1.2 .
#
# Generate the Docker image with embedded JDBC drives and Ontop:
#
#   docker build --build-arg MAVEN_ARGS='-Pontop -Pdrivers' -t obda-mixer:1.2 .
#
# Run via docker, displaying the help:
#
#   docker run --network=host -it --rm obda-mixer:latest --help
#
# Run via docker, executing queries as defined in a configuration file (example):
#
#   docker run --network=host -it --rm \
#          -v <PATH_TO_FOLDER_WITH_JDBC_FILES>:/opt/obda-mixer/jdbc \
#          -v <PATH_TO_FOLDER_WITH_CONFIGURATION_AND QUERIES>:/opt/obda-mixer/run \
#          -u $(id -u ${USER}):$(id -g ${USER})
#          obda-mixer:latest \
#          --conf=<NAME_OF_THE_CONFIGURATION_FILE>
#
# Notes:
#   - we use here --network=host in case the endpoint/dbms to connect to are on the Docker host machine
#   - we may mount a directory with JDBC drivers (JAR files) under /opt/obda-mixer/jdbc (vs. passing --jar=...)
#   - we may mount a directory with the configuration and queries under /opt/obda-mixer/run (the empty working dir)
#     the generated mixerStats.txt file will be written here
#   - we pass -u to run the container as the current user, so to allow writing mixerStats.txt in the mounted dir
#

FROM --platform=${BUILDPLATFORM} maven:3-eclipse-temurin-11 AS build
WORKDIR /build
COPY . ./
ARG MAVEN_ARGS=
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -Pfatjar ${MAVEN_ARGS}

FROM eclipse-temurin:11-jre-jammy
WORKDIR /opt/obda-mixer/run
RUN groupadd -r ontop && \
    useradd -r -g ontop -d /opt/ontop -s /sbin/nologin -c "Ontop unprivileged user" ontop
LABEL \
    org.opencontainers.image.url="https://github.com/ontop/obda-mixer/tree/master/" \
    org.opencontainers.image.documentation="https://ontop-vkg.org/obda-mixer/v1.2.0/local_index.html" \
    org.opencontainers.image.source="https://github.com/ontop/obda-mixer/" \
    org.opencontainers.image.title="obda-mixer" \
    org.opencontainers.image.description="obda-mixer is a testing platform for OBDA system" \
    org.opencontainers.image.vendor="Ontop developers: https://github.com/ontop" \
    org.opencontainers.image.authors="Email: https://groups.google.com/g/ontop4obda, Issues: https://github.com/ontop/obda-mixer/issues" \
    org.opencontainers.image.licenses="Apache-2.0"
USER ontop
VOLUME /tmp
COPY --chmod=755 --from=build /build/mixer-main/target/obda-mixer.jar /opt/obda-mixer/obda-mixer.jar
ENTRYPOINT [ "/opt/obda-mixer/obda-mixer.jar", "--jars=/opt/obda-mixer/jdbc/*" ]
