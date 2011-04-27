package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * These are the test cases that test STF itself.
 */
@RunWith(Suite.class)
@SuiteClasses(//
{ TestBuddiesByAlice.class, //
    TestBuddiesByAliceBobCarl.class,//
    TestGroupSession.class, //
    TestSarosView.class //
})
public class AllTestsForSarosViews {
    // empty
}
