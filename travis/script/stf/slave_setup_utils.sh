#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

. $SCRIPT_DIR/shared_vars.sh
. $SCRIPT_DIR/config_utils.sh

last_vnc_host_port=5901
vnc_base_port=5900

# Function that receives a whitespace separated list of ports which have to
# be exposed by a docker container. Based on this list a port mapping argument string
# is created which can be used by the docker run command.
# Instead of directly mapping the vnc port of the container to the same port of the host
# machine, this function has a global counter 'last_vnc_host_port' which makes sure
# that docker does not try to bind the same port on the host machine twice. This is very likely
# because the vncserver is executed with multiple displays on multple containers and uses the
# following schema to determine the port: '5900 + #display'
function create_vnc_port_mapping()
{
  local ports=$1
  local max_display=$(echo $ports | sed "s/^ //" | tr " " "\n" | wc -l)
  local mapping=""

  # maps the container port to an arbitrary free port


  for i in $(seq 1 $max_display); do
    mapping+=" -p $last_vnc_host_port:$(( vnc_base_port+i ))"
    ((last_vnc_host_port++))
  done
  retval=$mapping
}

# Function that either waits until the vncserver on slave host 'slave'
# binded the corresponding port or the timeout of poll*delay seconds is exceeded.
function wait_for_xfwm()
{
  local poll=$1
  local delay=$2
  local display=$3
  local slave=$4

  while [ $poll -gt 0 ]; do
    sleep $delay
    ((poll--))
    local res=$(docker exec -t $slave bash -c "ps -eF | grep \"[x]fwm\" | wc -l | tr -d '\n'")
    if [[ "$display" -eq "$res" ]]; then
      echo "Vnc and Xfwm of display $display on $slave alive"
      return 0
    fi
  done
  echo "Timout waiting for vnc and Xfwm"
  exit 1
}

# Function that either waits until the rmi server on slave host 'slave'
# binded the corresponding port or the timeout of poll*delay seconds is exceeded.
# In fact the function is used to wait for a stable state of the started eclipse
function wait_for_rmi_server()
{
  local poll=$1
  local delay=$2
  local port=$3
  local slave=$4

  while [ $poll -gt 0 ]; do
    sleep "$delay"
    ((poll--))
    local res=$(docker exec -t "$slave" bash -c "netstat -tulpn | grep java | grep $port")
    if [ ! -z "$res" ]; then
      echo "Rmi server of port $port on $slave alive"
      return 0
    fi
    echo "Still waiting for rmi server of port $port on $slave"
  done
  echo "Timout waiting for rmi server"
  exit 1
}

# The function creates a direct docker port mapping argument string
function create_rmi_port_mapping()
{
  local mapping=$(echo "$ports" | sed "s/ \([[:digit:]]*\)/-p \1:\1 /g")
  retval="$mapping"
}

# The function creates one big port mapping string based on the results of
# create_rmi_port_mapping and create_vnc_port_mapping
function create_port_mapping()
{
  local slave_name=$1
  get_host_ports "$slave_name"
  local ports="$retval"

  # the number of required ports is equal to the number required displays
  create_rmi_port_mapping "$ports"
  local mapping="$retval"
  create_vnc_port_mapping "$ports"
  mapping+=" $retval"
  retval=$mapping
}

function start_container_slave()
{
  get_distinct_hosts
  stf_slave_names="$retval"
  for slave_name in $stf_slave_names; do

    create_port_mapping "$slave_name"
    local port_mapping_args="$retval"

    echo "Starting stf slave container: $slave_name"
    docker run -dt --name $slave_name \
      -v $STF_HOST_WS:$STF_CONTAINER_WS \
      -v $CONFIG_DIR_HOST:$CONFIG_DIR_CONTAINER \
      -v $SCRIPT_DIR_HOST:$SCRIPT_DIR_CONTAINER \
      $port_mapping_args \
      --net=$stf_network_name \
      --net-alias=$slave_name \
      $stf_slave_image /bin/bash
  done
}

function setup_container_slave()
{
  get_distinct_hosts
  local stf_slave_names="$retval"
  for slave_name in $stf_slave_names; do
    # Cast whitespace separed string to array
    get_host_users $slave_name
    IFS=' ' read -r -a users <<< "$retval"

    get_host_ports $slave_name
    IFS=' ' read -r -a ports <<< "$retval"

    display=1

    for i in "${!users[@]}"; do
      echo "Starting vnc server on $slave_name with display $display"
      docker exec -d "$slave_name" vncserver -geometry 1280x960
      wait_for_xfwm 100 10 $display $slave_name

      echo "Starting eclipse on $slave_name with display $display, user ${users[$i]} and port ${ports[$i]}"
      docker exec -dt "$slave_name" bash -c "$SCRIPT_DIR_CONTAINER/stf/slave/start_eclipse.sh $display ${users[$i]} ${ports[$i]} > $STF_CONTAINER_WS/ws/${users[$i]}.log 2>&1"
      wait_for_rmi_server 100 10 "${ports[$i]}" "$slave_name"
      (( display++ ))
    done
  done
}
