#!/bin/bash -e
# Has to be execute in a stf_master container

[ ! -z "$DEBUG_BASH" ] && set -x

echo "STARTING REGRESSION: TIMEOUT IS 30 MINUTES"

timeout 30m ant -Dsrc.dir=$STF_WS/src \
    -Dlib.dir=$STF_WS/lib \
    -Declipse.plugin.dir=$STF_WS/eclipse_plugins \
    -Djunit.dir=$STF_WS/junit \
    -Dsaros.plugin.dir=$STF_WS/plugins \
    -Dstf.client.config.files=$CONFIG_DIR/stf_config \
    -lib $JUNIT_HOME -lib $COBERTURA_HOME -f $CONFIG_DIR/stf_self_test.xml

return_code=$?

[ $return_code = 124 ] && echo "TIMEOUT EXCEEDED!"
exit "$return_code"
