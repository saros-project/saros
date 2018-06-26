#!/bin/bash -e

github_token=$1
repo=$2
branch=$3
description="Daily $branch build"
tag=$branch-daily
prerelease=true

. $SCRIPT_DIR/release.sh "$github_token" "$repo" "$branch" "$tag"

get_release_id
if [ "$retval" = "null" ]; then
  echo "Release $tag does not exist in GitHub"
else
  echo "Release $tag already exists in GitHub"
  delete_release
fi

create_release "$prerelease" "$description"
upload_asset "de.fu_berlin.inf.dpp.intellij/bin/saros-i/artifacts/de.fu_berlin.inf.dpp.intellij.zip"
