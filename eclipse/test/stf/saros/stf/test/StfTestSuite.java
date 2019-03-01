package de.fu_berlin.inf.dpp.stf.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  de.fu_berlin.inf.dpp.stf.test.account.TestSuite.class,
  de.fu_berlin.inf.dpp.stf.test.chatview.TestSuite.class,
  de.fu_berlin.inf.dpp.stf.test.consistency.TestSuite.class,
  de.fu_berlin.inf.dpp.stf.test.editing.TestSuite.class,
  de.fu_berlin.inf.dpp.stf.test.filefolderoperations.TestSuite.class,
  de.fu_berlin.inf.dpp.stf.test.followmode.TestSuite.class,
  de.fu_berlin.inf.dpp.stf.test.html.TestSuite.class,
  de.fu_berlin.inf.dpp.stf.test.invitation.TestSuite.class,
  de.fu_berlin.inf.dpp.stf.test.partialsharing.TestSuite.class,
  de.fu_berlin.inf.dpp.stf.test.permissions.TestSuite.class,
  de.fu_berlin.inf.dpp.stf.test.roster.TestSuite.class,
  de.fu_berlin.inf.dpp.stf.test.session.TestSuite.class
})
public class StfTestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations

}
