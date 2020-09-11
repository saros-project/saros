#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x


. $SCRIPT_DIR/shared_vars.sh
. $SCRIPT_DIR/config_utils.sh

function start_container_xmpp()
{
  echo "Starting xmpp server container"
  docker run -td --name $stf_xmpp_server_name \
    --net=$stf_network_name \
    --net-alias=$stf_xmpp_server_name \
    -p 80:80 \
    -p 5222:5222 \
    -p 5269:5269 \
    -p 5347:5347 \
    -p 5280:5280 \
    -p 5281:5281 \
    $stf_xmpp_image /bin/bash
}

function setup_container_xmpp()
{
  echo "Starting XMPP Server"
  docker exec $stf_xmpp_server_name bash -c "prosodyctl start"

  echo "Creating xmpp test users"

  get_jid_pwd_tuple_list
  IFS=' ' read -r -a list <<< "$retval"
  echo "${list[@]}"

  for tuple in ${list[@]}; do
    local jid=${tuple%,*}
    local passwd=${tuple#*,}

    echo "Creating user $jid"
    docker exec $stf_xmpp_server_name bash -c "printf \"$passwd\n$passwd\n\" |  prosodyctl adduser $jid"
  done
}
