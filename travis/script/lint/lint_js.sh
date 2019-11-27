#!/bin/bash -e

project_root_dir=$1
linting_script=$project_root_dir/travis/script/lint/lint.js

echo "Linting the HTML Whiteboard"
node $linting_script $project_root_dir/whiteboard/frontend
