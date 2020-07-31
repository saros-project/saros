#!/bin/sh -e
# Has to be execute in a stf_coordinator container

# TODO: Script is currently unused and would fail in execution,
# because of deprecated environment vars.
[ ! -z "$DEBUG_BASH" ] && set -x

echo "STARTING REGRESSION: TIMEOUT IS 30 MINUTES"
stf_ws=/home/ci/stf_ws

timeout 30m ant -Dsrc.dir=$stf_ws/src \
    -Dlib.dir=$stf_ws/lib \
    -Declipse.plugin.dir=$stf_ws/eclipse_plugins \
    -Djunit.dir=$stf_ws/junit \
    -Dsaros.plugin.dir=$stf_ws/plugins \
    -Dstf.client.config.files=$CONFIG_DIR/stf_config \
    -lib $JUNIT_HOME -lib $COBERTURA_HOME -f $CONFIG_DIR/stf_self_test.xml

return_code=$?

[ $return_code = 124 ] && echo "TIMEOUT EXCEEDED!"
exit "$return_code"
