#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

cd $WORKSPACE
. $SCRIPT_DIR/build/build_utils.sh

set_jdk "1.7"

server_build_file="de.fu_berlin.inf.dpp.server/build.xml"
if [ -f "$server_build_file" ]; then
  # Workaround: Executing build on a fresh VM fails because ivy retrieve is called twice
  # which leads to the exception: "java.lang.ClassCastException: org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor"
  ant -buildfile $server_build_file init-ivy
  call_ant $server_build_file
fi
