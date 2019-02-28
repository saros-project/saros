#!/bin/bash

project_root_dir=$1

echo "Linting the HTML GUI"
node ./lint.js project_root_dir/de.fu_berlin.inf.dpp.ui.frontend/html

echo "Linting the HTML Whiteboard"
node ./lint.js project_root_dir/de.fu_berlin.inf.dpp.whiteboard/frontend

