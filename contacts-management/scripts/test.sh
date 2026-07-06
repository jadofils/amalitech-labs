#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

JUNIT_VERSION="1.10.2"
JUNIT_JAR="lib/junit-platform-console-standalone-${JUNIT_VERSION}.jar"
JUNIT_URL="https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/junit-platform-console-standalone-${JUNIT_VERSION}.jar"

mkdir -p lib out

if [ ! -f "$JUNIT_JAR" ]; then
  echo "Downloading JUnit Platform Console Standalone ${JUNIT_VERSION}..."
  curl -sSL -o "$JUNIT_JAR" "$JUNIT_URL"
fi

echo "Compiling..."
javac -cp "$JUNIT_JAR" -d out $(find src test -name "*.java")

echo "Running tests..."
java -jar "$JUNIT_JAR" execute --classpath out --scan-classpath --details=tree --fail-if-no-tests
