package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * These are the test cases that test STF itself.
 */
@RunWith(Suite.class)
@SuiteClasses(//
{ TestMenuEdit.class,//
    TestMenuFile.class, //
    TestMenuRefactor.class, //
    TestMenuSarosByAliceBob.class,//
    TestMenuSarosByAliceBobCarl.class,//
    TestSarosPreferences.class //
})
public class AllTestsForMenuBar {
    // empty
}
