#!/bin/sh

#
# Saros Connectivity Diagnosis
# (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2010-2011
# (c) Björn Kahlert - 2010-2011
# (c) Sandor Szuecs - 2010
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 1, or (at your option)
# any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
#
 
PATH="/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/sbin:$PATH"

HOST_CMD=`which host`
NSLOOKUP_CMD=`which nslookup`
DIG_CMD=`which dig`
PING_CMD=`which ping`
TRACEROUTE_CMD=`which traceroute`
CURL_CMD=`which curl`
WGET_CMD=`which wget`
IFCONFIG_CMD=`which ifconfig`
NETCAT_CMD=`which nc`
TELNET_CMD=`which telnet`
TEE_CMD=`which tee`

XMPP_SERVER="saros-con.imp.fu-berlin.de"
WWW_SERVER="saros-project.org"

LOGFILE="$(date +"%Y%m%dT%H%M%S")-saros-connectivity-diagnosis.txt"
MAX_HOPS="32"

VERSION="0.4"

print_n_times() {
  repeated_str="$(printf "%$2s" "")"
  printf "%s\n" "${repeated_str// /$1}"
}

log() {
  if [[ -x $TEE_CMD ]]; then
    echo $1 | $TEE_CMD -a $LOGFILE
  else
    echo $1 >> $LOGFILE
  fi
}

run_cmd() {
  log "$1"
  log $(print_n_times "-" ${#1})
  if [[ -x $TEE_CMD ]]; then
    $1 | $TEE_CMD -a $LOGFILE
  else
    $1 >> $LOGFILE
  fi
  log "\n"
}

print_banner() {
  echo "Running Saros Connectivity Diagnosis..."
  echo "Please be patient. The diagnosis can take up to 10 minutes.\n"
}

delay_start() {
  echo "Diagnosis begins in 5 seconds...\n"
  sleep 5
}

create_logfile() {
  echo "Saros Connectivity Diagnosis" > $LOGFILE
  echo "----------------------------" >> $LOGFILE
}

logfile_banner() {
  log "Version: $VERSION"
  log "Date: $(date +"%Y-%m-%d")"
  log "Time: $(date +"%H:%M:%S")"
  log "\n"
}

log_public_ip() {
  if [[ -x $CURL_CMD ]]; then
    log "Public IP: $($CURL_CMD -s http://whatismyip.org/)"
  elif [[ -x $WGET_CMD ]]; then
    log "Public IP: $($WGET_CMD --quiet -O - http://whatismyip.org/)"
  fi
  log "\n"
}

log_network_setup() {
  run_cmd "$IFCONFIG_CMD"
}

log_dnslookups() {
  if [[ -x $HOST_CMD ]]; then
    run_cmd "$HOST_CMD $WWW_SERVER"
    run_cmd "$HOST_CMD -t SRV _xmpp-server._tcp.$XMPP_SERVER"
    run_cmd "$HOST_CMD $XMPP_SERVER"
  elif [[ -x $DIG_CMD ]]; then
    run_cmd "$DIG_CMD $WWW_SERVER"
    run_cmd "$DIG_CMD -t SRV _xmpp-server._tcp.$XMPP_SERVER"
    run_cmd "$DIG_CMD $XMPP_SERVER"
  elif [[ -x $NSLOOKUP_CMD ]]; then
    run_cmd "$NSLOOKUP_CMD $WWW_SERVER"
    run_cmd "$NSLOOKUP_CMD" <<HERE
set type=SRV
_xmpp-server._tcp.$XMPP_SERVER
HERE

    run_cmd "$NSLOOKUP_CMD $XMPP_SERVER"
  fi
}

log_icmp_tests() {
  run_cmd "$PING_CMD -t 3600 -c 3 $WWW_SERVER"
  run_cmd "$PING_CMD -t 3600 -c 3 $XMPP_SERVER"
  run_cmd "$TRACEROUTE_CMD -m $MAX_HOPS $WWW_SERVER"
  run_cmd "$TRACEROUTE_CMD -I -m $MAX_HOPS $WWW_SERVER"
  run_cmd "$TRACEROUTE_CMD -m $MAX_HOPS $XMPP_SERVER"
  run_cmd "$TRACEROUTE_CMD -I -m $MAX_HOPS $XMPP_SERVER"
}

log_tcp_tests() {
  if [[ -x $NETCAT_CMD ]]; then
    run_cmd "$NETCAT_CMD -vz $XMPP_SERVER 5222"
    run_cmd "$NETCAT_CMD -vz $XMPP_SERVER 5269"
  elif [[ -x $TELNET_CMD ]]; then
    run_cmd "$TELNET_CMD $XMPP_SERVER 5222" <<HERE
close
HERE
    run_cmd "$TELNET_CMD $XMPP_SERVER 5269" <<HERE
close
HERE
  fi
}

main() {
  print_banner
  delay_start
  create_logfile
  logfile_banner
  log_public_ip
  log_network_setup
  log_dnslookups
  log_icmp_tests
  log_tcp_tests
  echo "\nResults saved in: $LOGFILE"
}

clear
main