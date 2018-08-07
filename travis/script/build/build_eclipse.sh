#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

cd $WORKSPACE
. $SCRIPT_DIR/build/build_utils.sh

set_jdk "1.6"

core_props="$pr_default $pr_junit -Dplugin.name=de.fu_berlin.inf.dpp.core"
call_ant de.fu_berlin.inf.dpp.core/build.xml "$core_props"

ui_props="$pr_default $pr_junit -Dplugin.name=de.fu_berlin.inf.dpp.ui"
call_ant de.fu_berlin.inf.dpp.ui/build.xml "$ui_props"

eclipse_props="$pr_default $pr_junit -Dplugin.name=Saros"
call_ant de.fu_berlin.inf.dpp.eclipse/build.xml "$eclipse_props"

whiteboard_props="$pr_default -Dplugin.name=de.fu_berlin.inf.dpp.whiteboard"
call_ant de.fu_berlin.inf.dpp.whiteboard/build.xml "$whiteboard_props"
