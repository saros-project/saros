package de.fu_berlin.inf.dpp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.fu_berlin.inf.dpp.project.ResourceActivityFilterPluginTest;
import de.fu_berlin.inf.dpp.project.SharedProjectPluginTest;
import de.fu_berlin.inf.dpp.project.SharedProjectUpdatableValuePluginTest;
import de.fu_berlin.inf.dpp.vcs.VCSActivityPluginTest;

/**
 * This test suite is supposed to be run as a headless JUnit Plugin test. See
 * HeadlessPluginTestsSuite.launch in /test/launch/.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ VCSActivityPluginTest.class,
    SharedProjectUpdatableValuePluginTest.class, SharedProjectPluginTest.class,
    ResourceActivityFilterPluginTest.class })
public class HeadlessPluginTestSuite {
    // empty class
}
