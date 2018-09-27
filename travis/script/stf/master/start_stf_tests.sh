#!/bin/bash -e
# Has to be execute in a stf_master container

[ ! -z "$DEBUG_BASH" ] && set -x

echo "STARTING REGRESSION: TIMEOUT IS 60 MINUTES"

timeout 60m ant -Dsrc.dir=$STF_WS/src \
    -Dlib.dir=$STF_WS/lib \
    -Declipse.dir=$STF_WS \
    -Djunit.dir=$STF_WS/junit \
    -Dsaros.plugin.dir=$STF_WS/plugins \
    -Dstf.client.config.files=$CONFIG_DIR/stf_config \
    -lib $JUNIT_HOME -lib $COBERTURA_HOME -f $CONFIG_DIR/saros_stf_test.xml

return_code=$?

[ $return_code = 124 ] && echo "TIMEOUT EXCEEDED!"
exit "$return_code"
