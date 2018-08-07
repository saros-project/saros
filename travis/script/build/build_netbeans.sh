#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

cd $WORKSPACE
. $SCRIPT_DIR/build/build_utils.sh

set_jdk "1.7"

netbeans_build_file="de.fu_berlin.inf.dpp.netbeans/build.xml"
if [ -f "$netbeans_build_file" ]; then
  core_jdk7_props="$pr_default $pr_ivy_workspace"
  call_ant de.fu_berlin.inf.dpp.core/build.xml "$core_jdk7_props" "old" "clean build test ivy-publish-jar"

  netbeans_props="$pr_netbeans $pr_netbeans_harness $pr_ivy_workspace"
  call_ant de.fu_berlin.inf.dpp.netbeans/build.xml "$netbeans_props" "old" "netbeans test"
fi
