#!/bin/bash -e

stf_coordinator_image="openjdk:8-jdk-alpine3.8"
stf_worker_image="saros/stf_test_slave:0.6"
stf_xmpp_image="saros/stf_xmpp_server:0.6"

stf_coordinator_name="stf_coordinator"
stf_network_name="stf_test_network"
stf_xmpp_server_name="stf_prosody_test.org"

STF_HOST_WS="$ws_dir/stf_ws/"
STF_CONTAINER_WS="/stf_ws"
SCRIPT_DIR_HOST="$ws_dir/travis/script"
CONFIG_DIR_HOST="$ws_dir/travis/config"
CONTAINER_SRC="/home/ci/saros_src"
SCRIPT_DIR_CONTAINER="$CONTAINER_SRC/travis/script"
CONFIG_DIR_CONTAINER="$CONTAINER_SRC/travis/config"
