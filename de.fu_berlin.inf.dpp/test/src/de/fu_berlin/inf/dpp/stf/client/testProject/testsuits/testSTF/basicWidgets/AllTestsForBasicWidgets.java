package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.basicWidgets;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * These are the test cases that test STF itself.
 */
@RunWith(Suite.class)
@SuiteClasses(//
{ TestBasicWidgetsByAlice.class,//
    TestBasicWidgetsByAliceAndBob.class, //

})
public class AllTestsForBasicWidgets {
    // empty
}
