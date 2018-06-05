#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

function get_jdk_home()
{
  local jdk=$1
  update-java-alternatives --list | grep $jdk | cut -d' ' -f3
}

function set_jdk()
{
  local jdk=$1
  local jdk_path=$(get_jdk_home $jdk)
  update-java-alternatives --set "$jdk_path"
}

function call_ant()
{
  local build_file=$1
  local prop_args=$2
  local ant4eclipse_mode=$3
  local targets_arg=$4
  local targets=${targets_arg:-clean build test}

  local ant4eclipse_new_lib="-lib $ANT4ECLIPSE_NEW_HOME/org.ant4eclipse_20160920_0849_8.jar"
  local ant4eclipse_new_lib="$ant4eclipse_new_lib -lib $ANT4ECLIPSE_NEW_HOME/libs"

  local ant4eclipse_old_lib="-lib $ANT4ECLIPSE_HOME/org.ant4eclipse_1.0.0.M4.jar"
  local ant4eclipse_old_lib="$ant4eclipse_old_lib -lib $ANT4ECLIPSE_HOME/libs"

  [ "$ant4eclipse_mode" == "new" ] && ant4eclipse_lib=$ant4eclipse_new_lib || ant4eclipse_lib=$ant4eclipse_old_lib

  ant -buildfile "$build_file" -Dtest.haltonfailure=on $prop_args \
    -lib "$JUNIT_HOME/junit-4.11.jar" \
    -lib "$JUNIT_HOME/hamcrest-core-1.3.jar" \
    -lib "$FINDBUGS_JAR" \
    -lib "$PMD_HOME/lib" \
    $ant4eclipse_lib \
    $targets
}

pr_eclipse="-Declipse.dir=${ECLIPSE_HOME}"
pr_eclipse_plugin="-Declipse.plugin.dir=${ECLIPSE_HOME}"
pr_ant4eclipse="-Dant4eclipse.dir=${ANT4ECLIPSE_HOME}"
pr_ant4eclipse_new="-Dant4eclipse.dir=${ANT4ECLIPSE_NEW_HOME}"
pr_add_plugin="-Dadditional.plugin.dir=${WORKSPACE}"
pr_cobertura="-Dcobertura.dir=${COBERTURA_HOME}"
pr_cobertura_new="-Dcobertura.dir=${COBERTURA_NEW_HOME}"
pr_junit="-Dsrc.test.dir=test/junit"
pr_intellij="-Didea.home=$INTELLIJ_HOME"
pr_netbeans="-Dnbplatform.default.netbeans.dest.dir=$NETBEANS_HOME"
pr_netbeans_harness="-Dnbplatform.default.harness.dir=$NETBEANS_HOME/harness"
pr_ivy_workspace="-Divy.workspace=${WORKSPACE}/ivy_workspace"
pr_default="$pr_eclipse $pr_eclipse_plugin $pr_ant4eclipse $pr_add_plugin $pr_cobertura"
