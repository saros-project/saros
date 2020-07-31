#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

. $SCRIPT_DIR/shared_vars.sh

function start_container_coordinator()
{
  echo "Starting stf coordinator container: $stf_coordinator_name"
  docker run -dt --name $stf_coordinator_name \
    -v $ws_dir:$CONTAINER_SRC \
    -v $STF_HOST_WS:$STF_CONTAINER_WS \
    --net=$stf_network_name \
    --net-alias=$stf_coordinator_name \
    $stf_coordinator_image /bin/sh
}

function setup_container_coordinator()
{
  # Workaround: Package installation has to be moved into a Dockerfile
  docker exec -t "$stf_coordinator_name" /bin/sh -c "apk --update add --no-cache gtk+3.0"
  echo "Build testees"
  docker exec -t "$stf_coordinator_name" "$SCRIPT_DIR_CONTAINER/stf/coordinator/build_testee.sh"
  echo "Executing setup_stf_ws.sh on $stf_coordinator_name"
  docker exec -t "$stf_coordinator_name" "$SCRIPT_DIR_CONTAINER/stf/coordinator/setup_stf_ws.sh"
}
