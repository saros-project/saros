#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

$SCRIPT_DIR/build/build_java.sh
$SCRIPT_DIR/build/build_js.sh
