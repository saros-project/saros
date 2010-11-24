package de.fu_berlin.inf.dpp.stf.client.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.inf.dpp.stf.client.test.basicElements.TestBasicSarosElements;
import de.fu_berlin.inf.dpp.stf.client.test.basicElements.TestPackageExplorerViewComponent;
import de.fu_berlin.inf.dpp.stf.client.test.basicElements.TestRosterViewObject;
import de.fu_berlin.inf.dpp.stf.client.test.basicElements.TestSessionViewObject;

/**
 * These are the test cases that test STF itself.
 */
@RunWith(Suite.class)
@SuiteClasses(//
{
// RmiTest.class,
    TestPackageExplorerViewComponent.class,//
    TestBasicSarosElements.class, //
    TestRosterViewObject.class, //
    TestSessionViewObject.class //
})
public class AllSTFTests {
    // empty
}
