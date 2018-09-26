#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

cd $WORKSPACE

$GRADLE_HOME/bin/gradle -PeclipsePluginDir=$ECLIPSE_HOME/plugins -PintellijLibDir=$INTELLIJ_HOME/lib \
  clean cleanAll \
  sarosEclipse \
  sarosIntellij
