---
title: Continuous Integration Process
---

## Push-Triggered Jobs

Whenever commits are pushed to a branch of the main repository, GitHub Actions triggers a new job that builds and tests the state of the updated branch.
During the test phase, only the JUnit tests (excluding STF tests) are executed.

Furthermore, the static code analysis tool Codacy is run to scan the code and binaries.
This scan only looks at the changes made by the pushed commits, meaning it will not analyze existing/unchanged code.
The results of the analysis are available through the GitHub interface or can be viewed using the [Codacy web interface](https://app.codacy.com/project/Saros/saros/dashboard).

### To branches starting with 'pr/stf/'

As an interim solution only branches with the prefix `pr/stf/'
trigger a GitHub Action that executes the stf tests.

## Pull-Request-Triggered Jobs

GitHub Actions also runs when a new pull request is created or an existing pull request is updated.
See the [GitHub Actions](https://help.github.com/en/actions) for more information.
These jobs are identical to the jobs triggered by a push. The only difference is that issues of the static code analysis are also reported in the pull request.

## Daily Jobs

GitHub Actions executes a daily cron job that triggers the following builds:
* Builds and tests the current master branch
  * If this job fails the failure is reported in GitHub