#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

login=$1
pull_request=$2

cd $WORKSPACE

properties=""
if [[ "$pull_request" =~ ^[0-9][0-9]*$ ]]; then
  properties="-Dsonar.github.pullRequest=$pull_request "
fi
properties+="-Dsonar.login=$login "
properties+="-Dsonar.verbose=true "
properties+="-PeclipsePluginDir=$ECLIPSE_HOME/plugins "
properties+="-PintellijLibDir=$INTELLIJ_HOME/lib "

$GRADLE_HOME/bin/gradle \
 $properties \
 sonarqube
