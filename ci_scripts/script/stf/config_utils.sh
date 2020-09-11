#!/bin/bash -e
# Has to be sourced by other scripts (that is called in a container) in order to read the config file
[ ! -z "$DEBUG_BASH" ] && set -x

stf_config_file=$CONFIG_DIR/stf_config

if [ ! -f "$stf_config_file" ];then
  echo "Stf configuration file $stf_config_file does not exist"
  exit 1
fi

declare -A stf_config

function add_property()
{
  local name=$1
  local prop_name=$2
  local prop_value=$3

  stf_config["$name","$prop_name"]="$prop_value"
}

# Read stf configuration
# Save user name, ports and hosts in separate arrays
while read -r l; do
  if [[ $l =~ ^[[:space:]]*([A-Za-z]*)_JID[[:space:]]*=[[:space:]]*([^[:space:]]+)[[:space:]]*$ ]]; then
    add_property "${BASH_REMATCH[1]}" "JID" "${BASH_REMATCH[2]}"
  fi

  if [[ $l =~ ^[[:space:]]*([A-Za-z]*)_PASSWORD[[:space:]]*=[[:space:]]*([^[:space:]]+)[[:space:]]*$ ]]; then
    add_property "${BASH_REMATCH[1]}" "PASSWORD" "${BASH_REMATCH[2]}"
  fi

  if [[ $l =~ ^[[:space:]]*([A-Za-z]*)_HOST[[:space:]]*=[[:space:]]*([^[:space:]]+)[[:space:]]*$ ]]; then
    add_property "${BASH_REMATCH[1]}" "HOST" "${BASH_REMATCH[2]}"
  fi

  if [[ $l =~ ^[[:space:]]*([A-Za-z]*)_PORT[[:space:]]*=[[:space:]]*([1-9][0-9]*)[[:space:]]*$ ]]; then
    add_property "${BASH_REMATCH[1]}" "PORT" "${BASH_REMATCH[2]}"
  fi

done < "$stf_config_file"

function get_distinct_hosts()
{
  local hosts=""
  for key_tuple in "${!stf_config[@]}"; do
    local prop_name=${key_tuple#*,}

    [ "$prop_name" = 'HOST' ] && hosts+="${stf_config[$key_tuple]}\n"
  done
  hosts=$(printf $hosts | sort | uniq | tr "\n" " ")
  hosts=${hosts% } # remove trailing whitespace
  retval=$hosts
}

function get_host_ports()
{
  local host=$1
  local ports=""

  for key_tuple in "${!stf_config[@]}"; do
    local key_name=${key_tuple%,*}
    local prop_name=${key_tuple#*,}

    if [[ "$prop_name" = 'HOST'  &&  "${stf_config[$key_tuple]}" = "$host" ]]; then
      ports+=" ${stf_config["$key_name",'PORT']}"
    fi
  done

  retval=$ports
}

function get_jid_pwd_tuple_list()
{
  local list=""
  for key_tuple in "${!stf_config[@]}"; do
    local key_name=${key_tuple%,*}
    local prop_name=${key_tuple#*,}

    if [ "$prop_name" = 'JID' ]; then
      local jid="${stf_config[$key_tuple]}"
      jid=${jid%/*}
      list+=" $jid,${stf_config["$key_name",'PASSWORD']}"
    fi
  done
  list=${list# } # remove leading whitespace
  retval="$list"
}

function get_host_users()
{
  local host=$1
  local users=""
  for key_tuple in "${!stf_config[@]}"; do
    local key_name=${key_tuple%,*}
    local prop_name=${key_tuple#*,}

    if [[ "$prop_name" = "HOST"  &&  "${stf_config[$key_tuple]}" = "$host" ]]; then
      users+=" ${key_name}"
    fi
  done
  retval=$users
}
