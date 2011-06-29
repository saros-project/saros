package de.fu_berlin.inf.dpp.stf.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ de.fu_berlin.inf.dpp.stf.test.account.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.test.chatview.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.test.editing.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.test.filefolderoperations.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.test.initialising.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.test.invitation.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.test.permissionsandfollowmode.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.test.reproducebugs.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.test.rosterviewbehaviour.TestSuite.class,
    de.fu_berlin.inf.dpp.stf.test.saving.TestSuite.class })
public class StfTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations

}
