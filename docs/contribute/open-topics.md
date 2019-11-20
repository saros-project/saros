---
title: Open Topics
---

Here you can find:
* information about topics which were started, but not completed
* ideas and implementations which were never integrated into the main branch, but might be interesting.

Therefore this page can be used for finding open and interesting topics which can be continued.
Please, let us know (e.g. via our [Gitter](https://gitter.im/saros-project/saros) or [mailing list](https://groups.google.com/forum/#!forum/saros-devel)) before you continue a topic so we can share our thoughts and experiences.

## HTML-GUI
You can find all information about the idea of the HTML-GUI [here](html-gui.md).

## GIT Support
Corresponding Pull Requests:
* [Add JGit facade](https://github.com/saros-project/saros/pull/428)
* [Add Activties to Share Commit](https://github.com/saros-project/saros/pull/444)

A former contributor started in implementation of a basic git support.
The main idea was to send differences between the git history of two developers as [git bundle](https://git-scm.com/docs/git-bundle). The determination of the differences was 
implemented via Saros activities. See page 18 of the [the corresponding thesis](https://www.inf.fu-berlin.de/inst/ag-se/theses/Jeschke2019-saros-git-support.pdf) for a corresponding interaction diagram.

## Improving the Runtime of the STF Tests

A former contributor already thought about ways to reduce the runtime of the STF tests (which is is very high).
She proposed the following approaches (see [the corresponding thesis for more information](https://www.inf.fu-berlin.de/inst/ag-se/theses/Puscasu18-saros-improving-quality-STF-tests.pdf)):

### Conditional Testing
See here for the corresponding [Pull Request](https://github.com/saros-project/saros/pull/527)<br/>
The main idea is to skip further tests if a basic test already failed.

We did not integrate the pull request, because:
* The approach requires to model a dependency relation between tests.
    * The dependency relation model was not compile-save.
    * The dependency relation model was redundant, because it was implemented via a direct relation and the test order within the test suite.
* We don't know how much time is saved by this approach in practice.
* We want to avoid additional complexity which hampers the induction of new contributors.


### Minimize the Setup/Teardown Effort of Similar Tests
See here for the corresponding [Pull Request](https://github.com/saros-project/saros/pull/528)<br/>
The main idea is to identify tests with similar setups. Instead of recreating the setup for each test, the old setup is just cleaned up.

We did not integrate the pull request, because of:
* The approach introduces a lot of complexity and adding a new test becomes very complex.
* The implementation introduces different tear-down methods which has to be changed manually in order to execute tests separately (not in the context of the test suite).

## Enhance the STF for the HTML GUI
See here for the corresponding [Pull Request](https://github.com/saros-project/saros/pull/358)<br/>
As already implemented for SWT components a new view is introduced which allows to trigger GUI events in the RMI server.

The pull request contains a basic implementation with mostly stubbed methods.

## Java-JavaScript Bridge
Corresponding Pull Requests:
Reimplementation of the `ui` classes in Kotlin:
* [Part 1](https://github.com/saros-project/saros/pull/437)
* [Part 2](https://github.com/saros-project/saros/pull/438)
* [Part 3](https://github.com/saros-project/saros/pull/436)
* [Part 4](https://github.com/saros-project/saros/pull/435)
* [Part 5](https://github.com/saros-project/saros/pull/434)
* [Part 6](https://github.com/saros-project/saros/pull/433)
* [Part 7](https://github.com/saros-project/saros/pull/432)
* [Part 8](https://github.com/saros-project/saros/pull/431)
* [Part 9](https://github.com/saros-project/saros/pull/430)
* [Part 10](https://github.com/saros-project/saros/pull/429)

The main idea is to reduce redundancy and complexity during the interaction of Java and JavaScript. Therefore this work compared different approaches (Scala with Scala.js, Kotlin and Fantom) which allow to generate the JavaScript code based on a JVM language. See this [thesis](https://www.inf.fu-berlin.de/inst/ag-se/theses/Paul-Gattringer2018-saros-UI-bridge.pdf) (German only) for more information.

We did not integrate the pull requests, because:
* The approach introduces a new language and therefore increases the complexity of our project and build process.
* The savings of redundancy are smaller than expected. Only the redundancy of the model classes can the avoided.
* The most interesting part (The injection of java functions into JavaScript and vice versa) is not simplified by the approach.

## Instant Session Start

Goal of this topic is to speed up the initial file sharing process at session starts.
Instead of waiting for full synchronization, this feature prioritizes files and allows direct access after receiving.
This main goal has already been achieved, but users are still bound to the read-only mode during project sharing and optimizations are open / on hold for merging.
More Infos can be found in this [thesis](https://www.inf.fu-berlin.de/inst/ag-se/theses/Moll18-saros-session-start.pdf) (German only).

Currently the author ([@stefaus](https://github.com/stefaus)) of this thesis wants to finish the rest of this work and some patches are already available.
Nevertheless the topic is on hold mainly because it is mandatory to change the way resources are shared first (from project based to per resource tracking).

An Overview of the current work state is documented here: [Project Board: Instant Session Start Feature](https://github.com/saros-project/saros/projects/15) and a broader view at [Project Board: Session Start Topics](https://github.com/saros-project/saros/projects/18).
