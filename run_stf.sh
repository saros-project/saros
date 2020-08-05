#!/bin/bash

export CONFIG_DIR=travis/config \
       SCRIPT_DIR=travis/script/stf \
       ws_dir=$PWD

user="$(id -u):$(id -g)"

source "$SCRIPT_DIR/shared_vars.sh"
source "$SCRIPT_DIR/config_utils.sh"

# Default arguments
aggregate_results=false
ci_mode=false
run_self_tests=false

function print_help {
  echo "run_stf.sh [OPTION]..."
  echo "  -h|--help          print this message"
  echo "  -s|--self          execute self tests"
  echo "  -c|--ci            skip clean-up for ci"
  echo "  -a|--aggregate     aggregate test results"
}

function parse_arguments {
  while [[ $# -gt 0 ]]
  do
    key="$1"
    case $key in
        -h|--help)
        print_help
        exit 1
        ;;
        -s|--self)
        run_self_tests=true
        shift
        ;;
        -c|--ci)
        ci_mode=true
        shift
        ;;
        -a|--aggregate)
        aggregate_results=true
        shift
        ;;
    esac
  done
}

function restore_workspaces_permissions {
    local user="$1"

    # variables provided by shared_vars.sh
    docker exec -t "$stf_coordinator_name" bash -c "chown -R $user $CONTAINER_SRC $STF_CONTAINER_WS"
}

function stop_and_remove_container {
    local container="$1"

    echo "::Stop and remove container: $container"
    container_exists=$(docker ps -aq -f name="$container")
    [ "$container_exists" != "" ] && docker rm -f "$container"
}

function stop_and_remove_worker_containers {
  # Method get_distinct_hosts provided by config_utils.sh
  get_distinct_hosts
  stf_worker_names="$retval"
  for worker_name in $stf_worker_names; do
    stop_and_remove_container "$worker_name"
  done
}

function remove_network {
    network_name="$stf_network_name"
    network_exists=$(docker network ls -q -f name="$network_name")
    [ "$network_exists" != "" ] && docker network rm "$network_name"
}

function teardown_containers {
    # variables provided by shared_vars.sh
    stop_and_remove_container "$stf_coordinator_name"
    stop_and_remove_worker_containers
    stop_and_remove_container "$stf_xmpp_server_name"
    remove_network
}

function aggregate_test_results {
    echo "::Aggregate test results"
    result_dir="test_results"
    [ -d "$result_dir" ] && rm -r "$result_dir"
    mkdir "$result_dir"
    ws_dir="$STF_HOST_WS/ws"

    echo "::Copy test report"
    [ "$run_self_tests" == "true" ] && project="stf.test" || project="stf"
    cp -r "$project/build/reports/tests" "$result_dir/reports"
    cp "$ws_dir"/*.log "$result_dir/"

    for ws in $(ls -1 "$ws_dir" | grep "workspace_" ) ; do
      user_name=$(echo $ws | awk -F_ '{print tolower($2)}') # workspace_ALICE -> alice
      echo "::Copy test results of $user_name"
      user_result_dir="$result_dir/$user_name"

      mkdir "$user_result_dir"
      
      meta_dir="$ws_dir/$ws/.metadata"

      screenshots_dir="$meta_dir/saros_screenshots"
      [ -d "$screenshots_dir" ] && cp -r "$screenshots_dir" "$user_result_dir/screenshots"

      log_file="$meta_dir/.log"
      [ -f "$log_file" ] && cp "$log_file" "$user_result_dir/log"

      logs_dir="$meta_dir/.plugins/saros.eclipse/log"
      [ -d "$logs_dir" ] && cp -r "$logs_dir" "$user_result_dir/logs"
    done
}

function finalize {
    local user="$1"; shift
    local mode="$1"; shift

    if [ "$aggregate_results" == "true" ]; then
      aggregate_test_results
    fi

    if [ "$ci_mode" != "true" ]; then 
      restore_workspaces_permissions "$user"
      teardown_containers
    fi
}

# variables stf_*_image provided by shared_vars.sh
function pull_images {
    docker pull "$stf_coordinator_image"
    docker pull "$stf_worker_image"
    docker pull "$stf_xmpp_image"
}


parse_arguments "$@"

echo "::Pull images"
pull_images

echo "::Start and configure containers"
# variable SCRIPT_DIR_HOST provided by shared_vars.sh
$SCRIPT_DIR_HOST/stf/setup_stf_container.sh $PWD
if [ "$?" != "0" ]; then
    echo "::Failed to setup the containers"
    finalize "$user" "$mode"
    exit 1
fi

echo "::Start stf tests"
# variables stf_coordinator_name, SCRIPT_DIR_CONTAINER provided by shared_vars.sh

[ "$run_self_tests" == "true" ] && test_script=start_stf_self_tests.sh || test_script=start_stf_tests.sh

docker exec -t "$stf_coordinator_name" "$SCRIPT_DIR_CONTAINER/stf/coordinator/$test_script"

rc="$?"
if [ "$rc" != "0" ]; then
    echo "::Failed during the stf test execution"
fi
finalize "$user" "$mode"
exit "$rc"
