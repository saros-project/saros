#!/bin/bash -e

stf_coordinator_image="saros/stf_test_coordinator:0.8"
stf_worker_image="saros/stf_test_worker:0.8"
stf_xmpp_image="saros/stf_xmpp_server:0.8"

stf_coordinator_name="stf_coordinator"
stf_network_name="stf_test_network"
stf_xmpp_server_name="stf_prosody_test.org"

STF_HOST_WS="$ws_dir/stf_ws/"
STF_CONTAINER_WS="/stf_ws"
SCRIPT_DIR_HOST="$ws_dir/ci_scripts/script"
CONFIG_DIR_HOST="$ws_dir/ci_scripts/config"
CONTAINER_SRC="/home/ci/saros_src"
SCRIPT_DIR_CONTAINER="$CONTAINER_SRC/ci_scripts/script"
CONFIG_DIR_CONTAINER="$CONTAINER_SRC/ci_scripts/config"
