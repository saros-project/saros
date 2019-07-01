---
title: Open Topics
---

Here you can find:
* information about topics which are started, but not completed
* ideas and implementations which are never integrated into the main branch, but might be interesting.

Therefore this page can be used for finding open and interesting topics which can be continued.
Please, let us know (e.g. via Gitter) before you continue a topic so we can share our thoughts and experiences.

## GIT support
Corresponding Pull Requests:
* [Add JGit facade](https://github.com/saros-project/saros/pull/428)
* [Add Activties to Share Commit](https://github.com/saros-project/saros/pull/444)

A former contributor started in implementation of a basic git support.
The main idea was to send differences between the git history of two developers as [git bundle](https://git-scm.com/docs/git-bundle). The determination of the differences was 
implemented via Saros activities. See page 18 of the [the corresponding thesis](https://www.inf.fu-berlin.de/inst/ag-se/theses/Jeschke2019-saros-git-support.pdf) for a corresponding interaction diagram.

## Improving the runtime of the STF Tests

A former contributor already thought about ways to reduce the runtime of the STF tests (which is is very high).
She proposed the following approaches (see [the corresponding thesis for more information](https://www.inf.fu-berlin.de/inst/ag-se/theses/Puscasu18-saros-improving-quality-STF-tests.pdf)):

### Conditional Testing
See here for the corresponding [Pull Request](https://github.com/saros-project/saros/pull/527)<br/>
The main idea is to skip further tests if a basic test already failed.

We did not integrate the pull request, because of:
* The approach requires to model a dependency relation between tests.
    * The dependency relation model was not compile-save.
    * The dependency relation model was redundant, because it was implemented via a direct relation and the test order within the test suite.
* We don't know how much time is saved by this approach in practice.
* We want to avoid additional complexity which hampers the induction of new contributors.


### Minimize the Setup/Teardown effort of similar tests
See here for the corresponding [Pull Request](https://github.com/saros-project/saros/pull/528)<br/>
The main idea is to identify tests with similar setups. Instead of recreating the setup for each test, the old setup is just cleaned up.

We did not integrate the pull request, because of:
* The approach introduces a lot of complexity and adding a new test becomes very complex.
* The implementation introduces different tear-down methods which has to be changed manually in order to execute tests separately (not in the context of the test suite).

## Enhance the STF for the HTML GUI
See here for the corresponding [Pull Request](https://github.com/saros-project/saros/pull/358)<br/>
As already implemented for SWT components a new view is introduced which allows to trigger GUI events in the RMI server.

The pull request contains a basic implementation with mostly stubbed methods.