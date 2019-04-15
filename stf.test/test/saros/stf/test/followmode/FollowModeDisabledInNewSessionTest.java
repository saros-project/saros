package saros.stf.test.followmode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.shared.Constants.TypeOfCreateProject;

public class FollowModeDisabledInNewSessionTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testFollowModeDisabledInNewSession() throws Exception {
    Util.setUpSessionWithProjectAndFile("foo", "readme.txt", "bla bla bla", ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();

    BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).followParticipant();

    BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).waitUntilIsFollowing();

    ALICE.superBot().internal().createFile("foo", "info.txt", "info");

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    leaveSessionPeersFirst(ALICE);

    BOB.remoteBot().closeAllEditors();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();

    Util.buildSessionSequentially("foo", TypeOfCreateProject.EXIST_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    assertFalse(
        "BOB is following ALICE",
        BOB.superBot().views().sarosView().selectUser(ALICE.getJID()).isFollowing());

    assertTrue("editor changed", BOB.remoteBot().editor("readme.txt").isActive());
  }
}
