#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

cd $WORKSPACE
. $SCRIPT_DIR/build/build_utils.sh

set_jdk "1.8"

ui_jdk8_props="$pr_eclipse $pr_eclipse_plugin $pr_ant4eclipse_new $pr_add_plugin $pr_cobertura"
ui_jdk8_props="$ui_jdk8_props $pr_junit -Dplugin.name=de.fu_berlin.inf.dpp.ui"
call_ant de.fu_berlin.inf.dpp.ui/build.xml "$ui_jdk8_props" new

jdk_home=$(get_jdk_home 1.8)
intellij_props="$pr_eclipse $pr_intellij $pr_cobertura_new -Djdk.home=$jdk_home"
call_ant de.fu_berlin.inf.dpp.intellij/build.xml "$intellij_props" new
