#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

#Setup
npm cache verify

echo "Building the HTML GUI"
cd /home/ci/saros_src/de.fu_berlin.inf.dpp.ui.frontend/html
npm install
npm run lint
npm run build
npm run test:log


echo "Building the HTML Whiteboard"
cd /home/ci/saros_src/de.fu_berlin.inf.dpp.whiteboard/frontend
npm install
npm run build

