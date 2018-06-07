#!/bin/bash -e
# To be called within a saros_test container
[ ! -z "$DEBUG_BASH" ] && set -x
$SCRIPT_DIR/build/build_eclipse.sh

cd $WORKSPACE
cp **/build/plugins/*.jar $STF_WS/
