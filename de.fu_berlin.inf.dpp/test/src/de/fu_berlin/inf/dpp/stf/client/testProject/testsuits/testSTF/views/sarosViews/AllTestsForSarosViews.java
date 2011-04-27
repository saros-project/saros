package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews.content.TestBuddiesByAlice;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews.content.TestBuddiesByAliceBob;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews.content.TestBuddiesByAliceBobCarl;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews.content.TestSessionAliceBob;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews.content.TestSessionAliceBobCarl;

/**
 * These are the test cases that test STF itself.
 */
@RunWith(Suite.class)
@SuiteClasses(//
{ TestBuddiesByAlice.class, //
    TestBuddiesByAliceBob.class,//
    TestBuddiesByAliceBobCarl.class,//
    TestSessionAliceBob.class, //
    TestSessionAliceBobCarl.class, //
    TestSarosView.class //
})
public class AllTestsForSarosViews {
    // empty
}
