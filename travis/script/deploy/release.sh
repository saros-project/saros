#!/bin/bash -x
# Requires the package jq

github_token=$1
repo=$2
branch=$3
tag=$4

api_url=https://api.github.com
upload_url=https://uploads.github.com

repo_url=$api_url/repos/$repo
release_url=$repo_url/releases
asset_upload_url=$upload_url/repos/$repo/releases

access_token_postfix=?access_token=$github_token

function create_release()
{
  local prerelease=$1
  local description=$2

  get_release_id
  if [ "$retval" != "null" ]; then
    echo "Unable to create release. Release with tag $tag already exists!"
    exit 1
  fi

  local api_json='
{
  "tag_name": "%s",
  "target_commitish": "%s",
  "name": "%s",
  "body": "Release: %s, %s",
  "draft": false,
  "prerelease": %s
}'

  echo "Creating release $tag with prerelease:$prerelease"
  api_json=$(printf "$api_json" "$tag" "$branch" "$tag" "$tag" "$description" "$prerelease")
  curl --data "$api_json" $release_url$access_token_postfix
}

function get_release_id()
{
  retval=$(curl -X "GET" $release_url/tags/$tag$access_token_postfix | jq '.id')
}

function upload_asset()
{
  local file=$1
  local content_type=$(file -b --mime-type $file)

  get_release_id
  local id=$retval

  if [ "$id" = "null" ]; then
    echo "Unable to upload asset $file. Release with tag $tag does not exist!"
    exit 1
  fi

  curl -X "POST" -H "Content-Type: $content_type" --data-binary @"$file" "$asset_upload_url/$id/assets$access_token_postfix&name=$(basename $file)"
}

function delete_release()
{
  get_release_id
  local id=$retval

  if [ "$id" = "null" ]; then
    echo "Unable to delete release. Release with tag $tag does not exist!"
    exit 1
  fi

  echo "Deleting release $tag"
  curl -X "DELETE" $release_url/$id$access_token_postfix
  echo "Deleting git tag $tag"
  curl -X "DELETE" $repo_url/git/refs/tags/$tag$access_token_postfix
}
