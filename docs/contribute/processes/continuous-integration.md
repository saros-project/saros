---
title: Continuous Integration Process
---

We are using GitHub Actions as CI server solution (see [here](https://docs.github.com/en/actions/configuring-and-managing-workflows) for more information).

## Build and Tests

Whenever commits are pushed to a branch of the main repository or Pull Requests are created or changed, GitHub Actions triggers a new job that builds and tests the state of the updated branch.
During the test phase, only the JUnit tests (excluding STF tests) are executed.

See configuration [`build.yml`](https://github.com/saros-project/saros/blob/master/.github/workflows/build.yml).

## Static Code Analysis

### Codacy

The static code analysis is executed for each commit and Pull Request to scan the code and binaries.
This scan only looks at the changes made by the pushed commits, meaning it will not analyze existing/unchanged code.
The results of the analysis are available through the GitHub interface or can be viewed using the [Codacy web interface](https://app.codacy.com/project/Saros/saros/dashboard).

Configured in the admin interface of our [codacy project](https://app.codacy.com/manual/Saros/saros/dashboard).

### PMD

We use the checker tool PMD for analyzing our Java code. PMD is executed by Codacy, but the [configuration](https://github.com/saros-project/saros/blob/master/ruleset.xml)
is located in our repository and Codacy only uses the version on the master branch. Therefore, we execute PMD in a workflow if a commit is pushed or a Pull Request is
opened (or updated) that changes the configurations.

See configuration [`build_pmd.yml`](https://github.com/saros-project/saros/blob/master/.github/workflows/build_pmd.yml).

## STF

The STF test execution is triggered for each push to the master branch and each branch with the prefix `pr/stf/`.
Furthermore, it is possible to [manually trigger](https://github.blog/changelog/2020-07-06-github-actions-manual-triggers-with-workflow_dispatch/) the STF
workflow with:
- Open the project's [`Actions` tab](https://github.com/saros-project/saros/actions?query=workflow%3A%22STF+Tests%22) (with the selected workflow `STF Tests`).
- Click the drop-down button `Run workflow`.
- Select a branch.
- If needed, add arguments that are handed to [`run_stf.sh`](https://github.com/saros-project/saros/blob/master/run_stf.sh).
  If you want to run the **STF self tests** (located in `stf/test`) that test the framework (and are not intended to test Saros) add the argument `--self`.
- Click the button `Run workflow`

See configuration [`stf.yml`](https://github.com/saros-project/saros/blob/master/.github/workflows/stf.yml).


## Documentation

All documentation changes (via pushing a commit or opening a Pull Request) trigger a build of the Jekyll documentation.
If these changes are pushed to the master branch, the result is published as new website.

See configuration [`build_doc.yml`](https://github.com/saros-project/saros/blob/master/.github/workflows/build_doc.yml)
