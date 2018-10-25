#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

login=$1
pull_request=$2

cd $WORKSPACE

if [ -z "$login" ]; then
  echo "Skipping sonar scan, because the login token is not available"
  exit 0
fi

properties=""
if [[ "$pull_request" =~ ^[0-9][0-9]*$ ]]; then
  properties="-Dsonar.github.pullRequest=$pull_request "
fi
properties+="-Dsonar.login=$login "
properties+="-Dsonar.verbose=true "
properties+="-PskipTestSuites=true "

./gradlew \
 $properties \
 sonarqube
