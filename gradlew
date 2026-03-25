#!/bin/sh
# Gradle wrapper script - Fixed for CI/CD
set -e

APP_NAME="Gradle"
APP_BASE_NAME="${0##*/}"

DEFAULT_JVM_OPTS=-Xmx64m -Xms64m

# Find java
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
        echo "Please set the JAVA_HOME variable in your environment to match the location of your Java installation." >&2
        exit 1
    fi
else
    JAVACMD=java
    command -v java >/dev/null 2>&1 || { 
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
        echo "Please set the JAVA_HOME variable in your environment to match the location of your Java installation." >&2
        exit 1
    }
fi

# Get the directory of this script
APP_HOME="$(cd "$(dirname "$0")" && pwd)"

# Use the wrapper jar from gradle/wrapper folder
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Wrapper jar not found at $WRAPPER_JAR" >&2
    echo "The gradle-wrapper.jar file must be committed to the repository." >&2
    exit 1
fi

# Collect all arguments
if [ -n "$JAVA_OPTS" ]; then
  JAVA_OPTS="$DEFAULT_JVM_OPTS $JAVA_OPTS"
else
  JAVA_OPTS="$DEFAULT_JVM_OPTS"
fi

# Execute Gradle
exec "$JAVACMD" \
  $JAVA_OPTS \
  "-classpath" "$WRAPPER_JAR" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
