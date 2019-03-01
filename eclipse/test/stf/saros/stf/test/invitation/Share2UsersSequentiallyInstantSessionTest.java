package saros.stf.test.invitation;

import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class Share2UsersSequentiallyInstantSessionTest extends Share2UsersSequentiallyTest {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);

    ALICE.superBot().menuBar().saros().preferences().preferInstantSessionStart();
    BOB.superBot().menuBar().saros().preferences().preferInstantSessionStart();
  }

  @AfterClass
  public static void reset() throws Exception {
    ALICE.superBot().menuBar().saros().preferences().preferArchiveSessionStart();
    BOB.superBot().menuBar().saros().preferences().preferArchiveSessionStart();
  }
}
