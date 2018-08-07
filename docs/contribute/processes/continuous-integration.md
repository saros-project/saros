---
title: Continuous Integration Process
---

# {{ page.title }}
{:.no_toc}

{% include toc.html %}

## Travis CI Triggers
### Push triggered jobs
If a new commit arrives in branch, Travis CI triggers a new job which builds and tests the current state. During the
test phase only the JUnit tests (not STF tests) are executed. If the build and test was successful a the static
code analysis tool Sonar starts to scan the code and binaries. The results of the process are posted to [SonarCloud](https://sonarcloud.io/).

### Pull request triggered jobs
If a pull request is created two Travis CI jobs are executed. See the [travis documentation](https://docs.travis-ci.com/user/pull-requests/#%E2%80%98Double-builds%E2%80%99-on-pull-requests) for more information.
These jobs are the same as the jobs triggered by a push. The only difference is that issues of Sonar are reported as comments in the pull request.

### Daily jobs
Travis CI executes a daily cron job that triggers the following builds:
* Builds and tests the current master branch
  * If this job fails the failure is reported in GitHub
* Builds Saros/E and executes the STF tests
  * If this job fails **the failure is not reported to GitHub**
* Builds Saros/E and executes the STF self tests
  * If this job fails **the failure is not reported to GitHub**

## STF Test Execution
The stf test process is much more complex than the build process and also requires a more complex infrastructure.

### Directory structure
The follwing paths are relative to the directory `travis/script`:
* `stf/build`
  * Contains only scripts that are build related and are called within the build and test container of saros ([`saros/build_test`](https://hub.docker.com/r/saros/build_test/)).
* `stf/master`
  * Contains only scripts which are called within the master docker container of the stf infrastructure ([`saros/stf_test_master`](https://hub.docker.com/r/saros/stf_test_master/)).
  * The purpose of the scripts is to prepare the mounted workspace `/stf_ws` for testing and execute the tests
* `stf/slave`
  * Contains only scripts which are called within the slave docker container of the stf infrastructure ([`saros/stf_test_slave`](https://hub.docker.com/r/saros/stf_test_slave/)).
  * The purpose of the scripts is to start eclipse
* `stf`
  * Contains scripts which are directly executed from travis.
  * The purpose of the scripts is to parse the stf configuration file and derive the required infrastructure from the configuration.
    Then the required docker containers are created.

### Testing process
Setting:
* Each container mounts a central stf workspace dir `stf_ws`
  which is created in the project root of the Travis VM
* See repository saros/dockerfiles for more information

Process
1. Step: Build testees (all jars which have to be tested)
  * Script triggered by: `master_setup_utils.sh`
  * Executed in: container of `saros/build_test`
  * Implemented in: `build/provide_testee.sh`
  * Tasks:
    * Build as in the standard build process
      * After build the required jars are copied to the stf workspace
2. Step: Determine required containers
  * Script triggered by: `setup_stf_container.sh`
  * Executed in: Travis VM
  * Implemented in: `config_utils.sh`
  * Tasks:
    * Analyze stf configuration
      * Determine distinct set of specified hosts
      * Determine host related port specifications
3. Step: Start required containers (processed in `setup_stf_container.sh`)
  * Script triggered by: Travis
  * Executed in: Travis VM
  * Implemented in: `setup_stf_container.sh`
  * Tasks:
    * Start master container
    * Start xmpp server container
    * Start determined set of containers
4. Step: Prepare stf workspace
  * Script triggered by: `setup_stf_container.sh`
  * Executed in: container of `saros/stf_test_master`
  * Implemented in: `master/setup_stf_ws.sh`
  * Tasks:
    * extract sources from jars in stf workspace which are created by the build step
    * instrument jars
    * provide instrumented jars in plugin directory for provisioning into eclipse
5. Step: Setup XMPP Server
  * Script triggered by: `setup_stf_container.sh`
  * Executed in: container of `saros/stf_test_master`
  * Implemented in: `xmpp_setup_utils.sh`
  * Tasks:
    * Start prosody xmpp server in container
    * Create test user in database
6. Step: Start services in all slaves
  * Script triggered by: Travis
  * Executed in: Travis VM
  * Implemented in: `setup_stf_container.sh`
  * Tasks executed for each slave:
    * start vncserver in container
    * wait until xfwm (window manager) is available
    * start eclipse in container (via script `slave/start_eclipse.sh`)
    * wait until rmi server binded corresponding port
6. Step: Execute stf tests
  * Script triggered by: Travis
  * Executed in: container of `saros/stf_master`
  * Implemented in: `master/start_stf_tests.sh`
  * Tasks:
    * Execute tests via ant
