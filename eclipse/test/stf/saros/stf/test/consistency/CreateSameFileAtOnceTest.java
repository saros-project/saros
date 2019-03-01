package saros.stf.test.consistency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.annotation.TestLink;
import saros.stf.client.StfTestCase;
import saros.stf.client.tester.AbstractTester;
import saros.stf.client.util.Util;
import saros.stf.shared.Constants.TypeOfCreateProject;

@TestLink(id = "Saros-131_create_same_file_at_once")
public class CreateSameFileAtOnceTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @After
  public void restoreNetwork() throws Exception {
    BOB.controlBot().getNetworkManipulator().unblockOutgoingSessionPackets();
    CARL.controlBot().getNetworkManipulator().unblockOutgoingSessionPackets();
  }

  @Test
  public void testCreateSameFileAtOnce() throws Exception {
    ALICE.superBot().internal().createProject("foo");
    ALICE.superBot().internal().createFile("foo", "sync.dummy", "dummy");

    Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/sync.dummy");
    CARL.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/sync.dummy");

    AbstractTester firstTester = BOB;
    AbstractTester secondTester = CARL;

    if ((System.currentTimeMillis() & 1L) == 0L) {
      firstTester = CARL;
      secondTester = BOB;
    }

    BOB.controlBot().getNetworkManipulator().blockOutgoingSessionPackets();
    CARL.controlBot().getNetworkManipulator().blockOutgoingSessionPackets();

    firstTester.superBot().internal().createFile("foo", "readme.txt", firstTester.toString());

    secondTester.superBot().internal().createFile("foo", "readme.txt", secondTester.toString());

    BOB.controlBot().getNetworkManipulator().unblockOutgoingSessionPackets();

    CARL.controlBot().getNetworkManipulator().unblockOutgoingSessionPackets();

    ALICE.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();

    String aliceText = ALICE.remoteBot().editor("readme.txt").getText();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    BOB.remoteBot().editor("readme.txt").waitUntilIsActive();

    String bobText = BOB.remoteBot().editor("readme.txt").getText();

    CARL.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    CARL.remoteBot().editor("readme.txt").waitUntilIsActive();

    String carlText = CARL.remoteBot().editor("readme.txt").getText();

    if (bobText.equals(aliceText) && carlText.equals(aliceText)) return; // already corrected

    AbstractTester tester = null;

    if (aliceText.equals(bobText)) tester = CARL;
    else if (aliceText.equals(carlText)) tester = BOB;
    else {
      fail(
          "the content of Alice editor: '"
              + aliceText
              + "' is not expected, it must be '"
              + BOB.toString()
              + "' or '"
              + CARL.toString()
              + "'");
      return; // just for get rid of the null pointer warning
    }

    tester.superBot().views().sarosView().waitUntilIsInconsistencyDetected();
    tester.superBot().views().sarosView().resolveInconsistency();

    tester.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    tester.remoteBot().editor("readme.txt").waitUntilIsActive();

    String repairedText = tester.remoteBot().editor("readme.txt").getText();
    assertEquals(aliceText, repairedText);
  }
}
