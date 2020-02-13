# Saros Test Frameworks (STF) scripts
The stf test process is much more complex than the build process and also requires a more complex infrastructure.

## Directory structure
* `stf/build`
  * Contains only scripts that are build related and are called within the build and test container of saros (`saros/saros_test`).
* `stf/master`
  * Contains only scripts which are called within the master docker container of the stf infrastructure (`saros/stf_master`).
  * The purpose of the scripts is to prepare the mounted workspace `/stf_ws` for testing and execute the tests
* `stf/slave`
  * Contains only scripts which are called within the slave docker container of the stf infrastructure (`saros/stf_slave`).
  * The purpose of the scripts is to start eclipse
* `stf`
  * Contains scripts which are directly executed from GitHub Actions.
  * The purpose of the scripts is to parse the stf configuration file and derive the required infrastructure from the configuration.
    Then the required docker containers are created.

## Testing process
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
