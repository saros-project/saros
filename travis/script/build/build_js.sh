#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

#Setup
npm cache verify

echo "Building the HTML GUI"
cd /home/ci/saros_src/ui.frontend/html
npm install
npm run build
npm run test:log


echo "Building the HTML Whiteboard"
cd /home/ci/saros_src/whiteboard/frontend
npm run setup
npm run build
