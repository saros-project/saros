#!/bin/bash -e

mode=$1
[ ! -z "$DEBUG_BASH" ] && set -x

cd $WORKSPACE

tasks='clean cleanAll'
[ "$mode" == 'stf' ] && tasks+=' sarosStf' || tasks+=' sarosEclipse'
tasks+=' sarosServer sarosIntellij'

./gradlew -PskipTestSuites=true -DmaxParallelForks=4 --no-daemon --parallel $tasks
