#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

. $SCRIPT_DIR/shared_vars.sh

function start_container_master()
{
  echo "Starting stf master container: $stf_master_name"
  docker run -dt --name $stf_master_name \
    -v $STF_HOST_WS:$STF_CONTAINER_WS \
    -v $CONFIG_DIR_HOST:$CONFIG_DIR_CONTAINER \
    -v $SCRIPT_DIR_HOST/master:$SCRIPT_DIR_CONTAINER \
    --net=$stf_network_name \
    --net-alias=$stf_master_name \
    $stf_master_image /bin/bash
}

function setup_container_master()
{
  echo "Executing setup_stf_ws.sh on $stf_master_name"
  docker exec -t "$stf_master_name" "$SCRIPT_DIR_CONTAINER/setup_stf_ws.sh"
}
