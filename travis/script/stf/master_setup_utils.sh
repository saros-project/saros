#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

. $SCRIPT_DIR/shared_vars.sh

function start_container_master()
{
  echo "Starting stf master container: $stf_master_name"
  docker run -dt --name $stf_master_name \
    -v $ws_dir:/home/ci/saros_src \
    -v $STF_HOST_WS:$STF_CONTAINER_WS \
    --net=$stf_network_name \
    --net-alias=$stf_master_name \
    $stf_master_image /bin/bash
}

function setup_container_master()
{
  echo "Build testees"
  docker exec -t "$stf_master_name" sh -c "$SCRIPT_DIR_CONTAINER/build/build_java.sh stf"
  echo "Executing setup_stf_ws.sh on $stf_master_name"
  docker exec -t "$stf_master_name" "$SCRIPT_DIR_CONTAINER/stf/master/setup_stf_ws.sh"
}
