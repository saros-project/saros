---
title: Continuous Integration Process
---

## Push-Triggered Jobs

Whenever commits are pushed to a branch of the main repository, Travis CI triggers a new job that builds and tests the state of the updated branch.
During the test phase, only the JUnit tests (excluding STF tests) are executed.

Furthermore, the static code analysis tool Codacy is run to scan the code and binaries.
This scan only looks at the changes made by the pushed commits, meaning it will not analyze existing/unchanged code.
The results of the analysis are available through the GitHub interface or can be viewed using the [Codacy web interface](https://app.codacy.com/project/Saros/saros/dashboard).

## Pull-Request-Triggered Jobs

Travis CI also runs when a new pull request is created or an existing pull request is updated.
See the [Travis documentation](https://docs.travis-ci.com/user/pull-requests/#%E2%80%98Double-builds%E2%80%99-on-pull-requests) for more information.
These jobs are identical to the jobs triggered by a push. The only difference is that issues of the static code analysis are also reported in the pull request.

## Daily Jobs

Travis CI executes a daily cron job that triggers the following builds:
* Builds and tests the current master branch
  * If this job fails the failure is reported in GitHub
* Builds Saros/E and executes the STF tests
  * If this job fails **the failure is not reported to GitHub**
* Builds Saros/E and executes the STF self tests
  * If this job fails **the failure is not reported to GitHub**
