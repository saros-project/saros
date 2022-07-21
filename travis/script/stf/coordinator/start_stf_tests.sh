#!/bin/sh -e
# Has to be execute in a stf_coordinator container

[ ! -z "$DEBUG_BASH" ] && set -x

echo "STARTING REGRESSION: TIMEOUT IS 60 MINUTES"
cd /home/ci/saros_src

timeout 3600 ./gradlew \
  --stacktrace \
  --no-daemon \
  -Dstf.client.configuration.files=/home/ci/saros_src/travis/config/stf_config \
  -Dsaros.debug=true \
  -PskipSTFTests=false \
  -PuseBuildScan=true \
  cleanAll :saros.stf.test:stfTest :saros.stf.test:stfFlakyTest
