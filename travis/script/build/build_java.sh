#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

cd $WORKSPACE

./gradlew -PskipTestSuites=true -DmaxParallelForks=4 --no-daemon --parallel \
  clean cleanAll \
  sarosEclipse \
  sarosServer \
  sarosIntellij
