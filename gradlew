#!/bin/sh
exec "${JAVA_HOME:-$(dirname "$(readlink -f "$0")")/jre}"/bin/java \
  -classpath "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
