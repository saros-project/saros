#!/bin/bash -e
# Has to be execute in a stf_master container

[ ! -z "$DEBUG_BASH" ] && set -x

echo "STARTING REGRESSION: TIMEOUT IS 60 MINUTES"
cd /home/ci/saros_src

timeout 60m ./gradlew --stacktrace -Dstf.client.configuration.files=/home/ci/saros_src/travis/config/stf_config :de.fu_berlin.inf.dpp:stfTest
