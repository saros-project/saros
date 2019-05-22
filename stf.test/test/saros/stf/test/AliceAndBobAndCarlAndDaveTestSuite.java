package saros.stf.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import saros.stf.test.invitation.ParallelInvitationWithTerminationByHostTest;
import saros.stf.test.permissions.AllParticipantsFollowUserWithWriteAccessTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  ParallelInvitationWithTerminationByHostTest.class,
  AllParticipantsFollowUserWithWriteAccessTest.class
})
public class AliceAndBobAndCarlAndDaveTestSuite {
  //
}
