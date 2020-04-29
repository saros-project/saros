#!/bin/sh -e

[ ! -z "$DEBUG_BASH" ] && set -x

cd /home/ci/saros_src

./gradlew -PskipTestSuites=true -PuseBuildScan=true -DmaxParallelForks=4 --no-daemon --parallel sarosStf
