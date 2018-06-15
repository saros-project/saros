#!/bin/bash -e
# To be called within a saros_test container
[ ! -z "$DEBUG_BASH" ] && set -x
$SCRIPT_DIR/build/build_eclipse.sh

# Provide build artifacts
cd $WORKSPACE
cp **/build/plugins/*.jar $STF_WS/

# Provide plugins of eclipse (required for stf self test)
mkdir -p $STF_WS/eclipse_plugins
cd $ECLIPSE_HOME
cp plugins/*.jar $STF_WS/eclipse_plugins/
