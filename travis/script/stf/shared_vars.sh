#!/bin/bash -e

# tag 0.9
stf_coordinator_image="saros/stf_test_coordinator@sha256:9558dc015ae0e0905255d93bdb5170fa9596c81dbe5c1092afc39dbe01747d94"
stf_worker_image="saros/stf_test_worker@sha256:a3cb6b46827b00b72f7f47a44785dd86121a36b4699fc17735313476acdb2dfd"
stf_xmpp_image="saros/stf_xmpp_server@sha256:df33af58a040d9e6173ed3a3047bc767f6b54e9104b7ecc91296c778c4d50a91"

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
