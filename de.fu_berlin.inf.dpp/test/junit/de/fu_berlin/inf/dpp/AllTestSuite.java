package de.fu_berlin.inf.dpp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({

de.fu_berlin.inf.dpp.accountManagement.TestSuite.class,

de.fu_berlin.inf.dpp.activities.business.TestSuite.class,

de.fu_berlin.inf.dpp.concurrent.TestSuite.class,

de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles.TestSuite.class,

de.fu_berlin.inf.dpp.concurrent.undo.TestSuite.class,

de.fu_berlin.inf.dpp.editor.colorstorage.TestSuite.class,

de.fu_berlin.inf.dpp.editor.internal.TestSuite.class,

de.fu_berlin.inf.dpp.feedback.TestSuite.class,

de.fu_berlin.inf.dpp.invitation.TestSuite.class,

de.fu_berlin.inf.dpp.net.TestSuite.class,

de.fu_berlin.inf.dpp.net.internal.TestSuite.class,

de.fu_berlin.inf.dpp.net.internal.extensions.TestSuite.class,

de.fu_berlin.inf.dpp.project.TestSuite.class,

de.fu_berlin.inf.dpp.project.internal.TestSuite.class,

de.fu_berlin.inf.dpp.synchronize.TestSuite.class,

de.fu_berlin.inf.dpp.ui.model.roster.TestSuite.class,

de.fu_berlin.inf.dpp.util.TestSuite.class

})
public class AllTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
