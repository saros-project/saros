#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

/home/ci/saros_src/travis/script/build/build_java.sh
/home/ci/saros_src/travis/script/build/build_js.sh
