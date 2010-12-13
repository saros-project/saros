package de.fu_berlin.inf.dpp.stf.client.testProject;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements.TestBasicSarosElements;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements.TestPackageExplorerViewComponent;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements.TestRosterViewComponent;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements.TestSessionViewComponent;

/**
 * These are the test cases that test STF itself.
 */
@RunWith(Suite.class)
@SuiteClasses(//
{
// RmiTest.class,
    TestPackageExplorerViewComponent.class,//
    TestBasicSarosElements.class, //
    TestRosterViewComponent.class, //
    TestSessionViewComponent.class //
})
public class AllSTFTests_BasicElements {
    // empty
}
