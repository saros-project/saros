#!/bin/bash -ex
# Has to be execute in a stf_master container

[ ! -z "$DEBUG_BASH" ] && set -x

echo "STARTING REGRESSION: TIMEOUT IS 60 MINUTES"

$GRADLE_HOME/bin/gradle --stacktrace -Dstf.client.configuration.files=$CONFIG_DIR/stf_config :de.fu_berlin.inf.dpp:stfTest

#return_code=$?

#[ $return_code = 124 ] && echo "TIMEOUT EXCEEDED!"
#exit "$return_code"
