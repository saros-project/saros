package de.fu_berlin.inf.dpp.stf.test.consistency;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  CreateSameFileAtOnceTest.class,
  ModifyFileWithoutEditorTest.class,
  RecoveryWhileTypingTest.class,
  EditDuringInvitationTest.class,
  EditDuringInvitationStressTest.class,
  EditDuringNonHostInvitationTest.class
})
public class TestSuite {
  // the class remains completely empty,
  // being used only as a holder for the above annotations
}
