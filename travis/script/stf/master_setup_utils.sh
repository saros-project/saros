#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

. $SCRIPT_DIR/shared_vars.sh

function start_container_master()
{
  echo "Starting stf master container: $stf_master_name"
  docker run -dt --name $stf_master_name \
    -v $ws_dir:$CONTAINER_SRC \
    -v $STF_HOST_WS:$STF_CONTAINER_WS \
    --net=$stf_network_name \
    --net-alias=$stf_master_name \
    $stf_master_image /bin/sh
}

function setup_container_master()
{
  echo "Build testees"
  docker exec -t "$stf_master_name" "$SCRIPT_DIR_CONTAINER/stf/master/build_testee.sh"
  echo "Executing setup_stf_ws.sh on $stf_master_name"
  docker exec -t "$stf_master_name" "$SCRIPT_DIR_CONTAINER/stf/master/setup_stf_ws.sh"
}
