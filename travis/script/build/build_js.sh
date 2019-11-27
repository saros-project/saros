#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

#Setup
npm cache verify

echo "Building the HTML Whiteboard"
cd /home/ci/saros_src/whiteboard/frontend
npm run setup
npm run build
