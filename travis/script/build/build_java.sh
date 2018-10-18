#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

# TODO: workaround for intermedia gradle solution
cd /home/ci/saros_src/
update-java-alternatives -s /usr/lib/jvm/java-1.8.0-openjdk-amd64

./gradlew -PskipTestSuites=true -PeclipsePluginDir=$ECLIPSE_HOME/plugins -PintellijLibDir=$INTELLIJ_HOME/lib --parallel \
  clean cleanAll \
  sarosEclipse \
  sarosServer \
  sarosIntellij
