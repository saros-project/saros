package de.fu_berlin.inf.dpp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.fu_berlin.inf.dpp.project.SharedProjectUpdatableValueTest;

/**
 * This test suite is supposed to be run as a headless JUnit Plugin test. See
 * HeadlessPluginTestsSuite.launch in /test/launch/.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ SharedProjectUpdatableValueTest.class })
public class HeadlessPluginTestSuite {
    // empty class
}
