package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.editor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * These are the test cases that test STF itself.
 */
@RunWith(Suite.class)
@SuiteClasses(//
{ TestEditor2.class,//
    TestEditorByAlice.class, //
    TestEditorByAliceBob.class, //

})
public class AllTestsForEditor {
    // empty
}
