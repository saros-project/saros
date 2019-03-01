package saros.stf.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  saros.stf.test.account.TestSuite.class,
  saros.stf.test.chatview.TestSuite.class,
  saros.stf.test.consistency.TestSuite.class,
  saros.stf.test.editing.TestSuite.class,
  saros.stf.test.filefolderoperations.TestSuite.class,
  saros.stf.test.followmode.TestSuite.class,
  saros.stf.test.html.TestSuite.class,
  saros.stf.test.invitation.TestSuite.class,
  saros.stf.test.partialsharing.TestSuite.class,
  saros.stf.test.permissions.TestSuite.class,
  saros.stf.test.roster.TestSuite.class,
  saros.stf.test.session.TestSuite.class
})
public class StfTestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations

}
