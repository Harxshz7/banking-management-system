#!/bin/sh
# Maven Wrapper startup script for UN*X systems
# Downloads Maven if not present and runs it.

set -e

MAVEN_HOME="${HOME}/.m2/wrapper/dists/apache-maven-3.9.6"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"
WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"

if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven 3.9.6..."
    mkdir -p "$MAVEN_HOME"
    TMPZIP=$(mktemp)
    curl -sL -o "$TMPZIP" "$MAVEN_URL"
    unzip -qo "$TMPZIP" -d "$MAVEN_HOME"
    mv "$MAVEN_HOME"/apache-maven-3.9.6/* "$MAVEN_HOME/"
    rmdir "$MAVEN_HOME/apache-maven-3.9.6" 2>/dev/null || true
    rm -f "$TMPZIP"
    echo "Maven installed to $MAVEN_HOME"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
