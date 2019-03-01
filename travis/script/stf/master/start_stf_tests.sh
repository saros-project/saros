#!/bin/bash -e
# Has to be execute in a stf_master container

[ ! -z "$DEBUG_BASH" ] && set -x

echo "STARTING REGRESSION: TIMEOUT IS 60 MINUTES"
cd $WORKSPACE

timeout -t 3600 ./gradlew --stacktrace --no-daemon -Dstf.client.configuration.files=/home/ci/saros_src/travis/config/stf_config cleanAll :saros.eclipse:stfTest
