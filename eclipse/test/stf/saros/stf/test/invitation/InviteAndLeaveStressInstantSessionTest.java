package saros.stf.test.invitation;

import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class InviteAndLeaveStressInstantSessionTest extends InviteAndLeaveStressTest {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);

    ALICE.superBot().menuBar().saros().preferences().preferInstantSessionStart();
    BOB.superBot().menuBar().saros().preferences().preferInstantSessionStart();
    CARL.superBot().menuBar().saros().preferences().preferInstantSessionStart();
  }

  @AfterClass
  public static void reset() throws Exception {
    ALICE.superBot().menuBar().saros().preferences().preferArchiveSessionStart();
    BOB.superBot().menuBar().saros().preferences().preferArchiveSessionStart();
    CARL.superBot().menuBar().saros().preferences().preferArchiveSessionStart();
  }
}
