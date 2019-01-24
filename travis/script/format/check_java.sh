#!/bin/bash

shopt -s globstar

project_root_dir=$1
formatter_jar_name=formatter.jar

function download_formatter() {
  local jar_name=$1
  local url="https://repo.maven.apache.org/maven2/com/google/googlejavaformat/google-java-format/1.7/google-java-format-1.7-all-deps.jar"
  echo "Downloading formatter from $url"
  curl -Lo "$jar_name" "$url"
}

function check_formatting() {
  local jar_name=$1

  echo 'Checking format'
  java -jar "$jar_name" --dry-run --set-exit-if-changed **/*.java

  local rc=$?
  [ "$rc" == "0" ] &&
    echo 'All files are well formatted' ||
    printf "\nERROR: Some files are not well formatted. The file list above shows all files containing a wrong formatting.\n"

  return "$rc"
}

cd $project_root_dir

download_formatter $formatter_jar_name
check_formatting $formatter_jar_name

exit $?
