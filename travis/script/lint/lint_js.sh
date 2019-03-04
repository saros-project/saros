#!/bin/bash

project_root_dir=$1

echo "Linting the HTML GUI"
node ./lint.js project_root_dir/ui.frontend/html

echo "Linting the HTML Whiteboard"
node ./lint.js project_root_dir/whiteboard/frontend

