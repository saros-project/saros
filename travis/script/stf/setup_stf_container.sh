#!/bin/bash -e
# Has to be called by Travis outside of the containers

[ ! -z "$DEBUG_BASH" ] && set -x

ws_dir=$1
if [[ -z "$ws_dir" || ! -e "$ws_dir" ]]; then
  echo "Usage: setup_stf_container.sh <workspace>"
  echo " workspace - base directory which contains the sources as well as"
  echo "             the stf workspace"
  echo " workspace '$ws_dir' is not a valid directory"
  exit 1
fi

[ -z "$SCRIPT_DIR" ] && echo "ERROR: Environment variable SCRIPT_DIR is not set" && exit 1
[ -z "$CONFIG_DIR" ] && echo "ERROR: Environment variable CONFIG_DIR is not set" && exit 1

. $SCRIPT_DIR/shared_vars.sh
. $SCRIPT_DIR/coordinator_setup_utils.sh
. $SCRIPT_DIR/worker_setup_utils.sh
. $SCRIPT_DIR/xmpp_setup_utils.sh


[ -d "$STF_HOST_WS" ] && rm -r $STF_HOST_WS
mkdir -p $STF_HOST_WS

####### Start required containers

# Create a network which is used by all containers in order to
# communicate with each other
echo "Creating docker network: $stf_network_name"
docker network create "$stf_network_name"

start_container_coordinator
start_container_xmpp
start_container_worker

####### Start required services and setup dir structure

setup_container_coordinator
setup_container_xmpp
setup_container_worker

