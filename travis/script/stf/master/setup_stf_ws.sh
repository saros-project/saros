#!/bin/bash -e
# Has to be called within a stf_master container

[ ! -z "$DEBUG_BASH" ] && set -x

cd $STF_WS

mkdir src lib instr ws

cd src

jar xf ../de.fu_berlin.inf.dpp.core.source*.jar
jar xf ../de.fu_berlin.inf.dpp.ui.source*.jar
jar xf ../de.fu_berlin.inf.dpp.source*.jar

cd ..

jar xf de.fu_berlin.inf.dpp.eclipse_*.jar lib
jar xf de.fu_berlin.inf.dpp.core_*.jar lib
jar xf de.fu_berlin.inf.dpp.ui_*.jar lib

# Instrument

cp de.fu_berlin.inf* instr/

$COBERTURA_HOME/cobertura-instrument.sh --basedir "$PWD/instr" \
  --includeClasses 'de\.fu_berlin\.inf\.dpp\.stf\.server\..*' de.fu_berlin.inf*

# Copy files to be deployed to eclipse
echo "Copy file to be tested:"
mkdir plugins
cp -v instr/de.fu_berlin.inf* plugins/

