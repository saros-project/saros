#!/bin/bash -e
# Has to be called within a stf_master container

[ ! -z "$DEBUG_BASH" ] && set -x

cd /stf_ws
mkdir plugins ws
cp -v $WORKSPACE/build/distribution/eclipse/*.jar plugins/
