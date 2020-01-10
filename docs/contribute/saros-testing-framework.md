---
title: Saros Testing Framework (STF)
---

For further information about STF take a look at the [STF User Manual](https://saros-build.imp.fu-berlin.de/stf/STF_Manual.pdf).
This document is not up-to-date, but still contains useful information about the STF.

## Eclipse

### Prerequisites

You need a [development environment](development-environment.md) with **Eclipse 4.8** which is currently the only supported eclipse version.

### Configuration

* Before you can run the STF tests you need four XMPP accounts. If you want to use our XMPP server you can create accounts as described in the [user documentation](../documentation/getting-started.html?tab=eclipse).
* Then you have to create the file `configuration.properties` in directory `stf/src/saros/stf/client`.
* Add the following lines to the config and **replace the placeholders with your credentials** (make sure **every tester has an unique JID**)

```properties
ALICE_JID = <insert user here>@<insert domain here>/Saros
ALICE_PASSWORD = <insert password here>
ALICE_HOST = localhost
ALICE_PORT = 12345

BOB_JID = <insert user here>@<insert domain here>/Saros
BOB_PASSWORD = <insert password here>
BOB_HOST = localhost
BOB_PORT = 12346

CARL_JID = <insert user here>@<insert domain here>/Saros
CARL_PASSWORD = <insert password here>
CARL_HOST = localhost
CARL_PORT = 12347

DAVE_JID = <insert user here>@<insert domain here>/Saros
DAVE_PASSWORD = <insert password here>
DAVE_HOST = localhost
DAVE_PORT = 12348
```

### Prepare Dependencies

Eclipse uses the `META-INF/MANIFEST.MF` in order to resolve dependencies inside the launch config.
Therefore the current workaround is to execute the Gradle task `generateLibAll` in order to generate a directory `lib` that contains the required dependencies.

### Run Tests

1.  Start the **requiered launch configurations** which are located
    in the directory `stf/launch` by **right clicking the
    launch file** and chose **Run As > Saros\_STF\_\<name\>**. This
    will start a new Eclipse instance with the selected launch
    configuration.
    You only need to run the appropriate launch file(s). E.g. the
    test `AccountPreferenceTest.java` in the package
    `saros.stf.test.account` only needs the tester
    *ALICE*. In this case you only need to start *Saros\_STF\_Alice*.
2.  **Right click** on the test you want to run and select **Run As > JUnit test**

## IntelliJ

We are currently working on an version of STF that work with the HTML-GUI and therefore with both IDEs, but for now it is not possible to run the Eclipse STF tests from within IntelliJ or execute STF tests for IntelliJ.
However IntelliJ/Gradle supports to run multiple IntelliJ instances from within IntelliJ in order to test Saros by hand.

### Run Multiple IntelliJ Instances for Testing

* Open the `Gradle` tool window
* Open the task directory `saros.intellij > Tasks > intellij`
* Execute task `runIde` for each IntelliJ IDEA instance you need.

Each simultaneously execution of `runIde` creates a sandbox directory in the `build` directory of the IntelliJ project.
If you want to configure the parent directory of the sandbox directories you have to set the environment variable `SAROS_INTELLIJ_SANDBOX`
to the corresponding directory.

## Without an IDE

### Test IntelliJ by Hand

If you want to test IntelliJ by hand simply call the Gradle task `./gradlew runIde` as within IntelliJ. If you close the IntelliJ instance
by killing the Gradle process per terminal the IntelliJ workspaces cannot be reused. Therefore you should close the instances by closing the IDE window.

### Start Eclipse STF Tests

If you want to execute the Eclipse STF tests without an IDE you can use the docker setup which is used in the CI server.
In order to use the corresponding script you need a current **docker installation**.

* Open a bash terminal
* Change the current working directory to the Saros repository root directory
* Create the directory `stf_ws` which is used by the containers to store test artifacts
* Execute `export CONFIG_DIR=travis/config SCRIPT_DIR=travis/script/stf`
* Create the required docker contains by executing  
  `./travis/script/stf/setup_stf_container.sh $PWD`
* Start the STF tests by executing
  `docker exec -t stf_master /home/ci/saros_src/travis/script/stf/master/start_stf_tests.sh`

### Clean Test Environment

In order to clean the environment you have to stop and remove all created containers and
remove the network.

* Stop and remove containers `docker rm -f stf_slave_1 stf_prosody_test.org stf_master`
* Remove network `docker network rm stf_test_network`
* Remove the `stf_ws` directory

### View Eclipse Test Instances

If you want to see how the tests are performed you have install a vncviewer.
Start the vncviewer and connect to the vncservers running on the test container. You have to connect to host `localhost` and the ports `5901`, `5902`, `5903` and `5904`.
