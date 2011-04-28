package de.fu_berlin.inf.dpp.stf.client.testProject;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.basicWidgets.AllTestsForBasicWidgets;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.contextMenu.AllTestsForContextMenu;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar.AllTestsForMenuBar;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews.AllTestsForSarosViews;

/**
 * These are the test cases that test STF itself.
 */
@RunWith(Suite.class)
@SuiteClasses(//
{ AllTestsForBasicWidgets.class,//
    AllTestsForContextMenu.class,//
    // AllTestsForEditor.class, //
    AllTestsForMenuBar.class, //
    AllTestsForSarosViews.class //
})
public class AllTestsForSTF {
    // empty
}
