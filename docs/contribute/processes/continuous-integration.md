---
title: Continuous Integration Process
---

# {{ page.title }}

## Push triggered jobs
If a new commit arrives in branch, Travis CI triggers a new job which builds and tests the current state. During the
test phase only the JUnit tests (not STF tests) are executed. If the build and test was successful a the static
code analysis tool Sonar starts to scan the code and binaries. The results of the process are posted to [SonarCloud](https://sonarcloud.io/).

## Pull request triggered jobs
If a pull request is created two Travis CI jobs are executed. See the [travis documentation](https://docs.travis-ci.com/user/pull-requests/#%E2%80%98Double-builds%E2%80%99-on-pull-requests) for more information.
These jobs are the same as the jobs triggered by a push. The only difference is that issues of Sonar are reported as comments in the pull request.

## Daily jobs
Travis CI executes a daily cron job that triggers the following builds:
* Builds and tests the current master branch
  * If this job fails the failure is reported in GitHub
* Builds Saros/E and executes the STF tests
  * If this job fails **the failure is not reported to GitHub**
* Builds Saros/E and executes the STF self tests
  * If this job fails **the failure is not reported to GitHub**
