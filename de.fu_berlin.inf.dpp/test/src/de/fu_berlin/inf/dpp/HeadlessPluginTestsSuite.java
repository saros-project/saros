package de.fu_berlin.inf.dpp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.fu_berlin.inf.dpp.project.TestResourceActivityFilter;
import de.fu_berlin.inf.dpp.project.TestSharedProject;
import de.fu_berlin.inf.dpp.project.TestSharedProject_UpdatableValue;
import de.fu_berlin.inf.dpp.vcs.TestVCSActivity;

/**
 * This test suite is supposed to be run as a headless JUnit Plugin test. See
 * HeadlessPluginTestsSuite.launch in /test/launch/.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ TestVCSActivity.class,
    TestSharedProject_UpdatableValue.class, TestSharedProject.class,
    TestResourceActivityFilter.class })
public class HeadlessPluginTestsSuite {
    // empty class
}
