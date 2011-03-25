package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.contextMenu;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * These are the test cases that test STF itself.
 */
@RunWith(Suite.class)
@SuiteClasses(//
{ TestContextMenuOpen.class,//
    TestContextMenuDelete.class,//
    TestContextMenuShareWith.class //
})
public class AllTestsForContextMenu {
    // empty
}
