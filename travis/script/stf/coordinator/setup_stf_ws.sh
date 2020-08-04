#!/bin/sh -e
# Has to be called within a stf_coordinator container

[ ! -z "$DEBUG_BASH" ] && set -x

cd /stf_ws
mkdir plugins ws
cp -v /home/ci/saros_src/build/distribution/eclipse/*.jar plugins/
