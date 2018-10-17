#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

cd $WORKSPACE
. $SCRIPT_DIR/build/build_utils.sh

ui_package_json="de.fu_berlin.inf.dpp.ui.frontend/html/package.json"
if [ -f "$ui_package_json" ]; then
  cd de.fu_berlin.inf.dpp.ui.frontend/html
  npm cache verify
  npm install
  npm run lint
  npm run build
  npm run test:log
fi
