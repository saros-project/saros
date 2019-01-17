#!/bin/bash -e

stf_master_image="saros/ci_build:0.3"
stf_slave_image="saros/stf_test_slave:0.3"
stf_xmpp_image="saros/stf_xmpp_server:0.3"

stf_master_name="stf_master"
stf_network_name="stf_test_network"
stf_xmpp_server_name="stf_prosody_test.org"

STF_HOST_WS="$ws_dir/stf_ws/"
STF_CONTAINER_WS="/stf_ws"
SCRIPT_DIR_HOST="$ws_dir/travis/script"
CONFIG_DIR_HOST="$ws_dir/travis/config"
SCRIPT_DIR_CONTAINER="/home/ci/saros_src/travis/script"
CONFIG_DIR_CONTAINER="/home/ci/saros_src/travis/config"
