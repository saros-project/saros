package de.fu_berlin.inf.dpp.stf.test.consistency;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.test.util.EclipseTestThread;
import org.junit.BeforeClass;
import org.junit.Test;

@TestLink(id = "Saros-7_consistency_watchdog_and_stop_manager")
public class RecoveryWhileTypingTest extends StfTestCase {

  private EclipseTestThread aliceEditTaskThread;

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testRecoveryWhileTyping() throws Exception {
    Util.setUpSessionWithProjectAndFile(
        "foo", "readme.txt", "Harry Potter und der geheime Pornokeller", ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");

    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).restrictToReadOnlyAccess();

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();

    EclipseTestThread.Runnable aliceEditTask =
        new EclipseTestThread.Runnable() {
          @Override
          public void run() throws Exception {

            ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();
            while (!Thread.currentThread().isInterrupted()) {
              ALICE.remoteBot().editor("readme.txt").typeText("abcdefghijklmnopqrstuvwxyz\n");
            }
          }
        };

    aliceEditTaskThread = createTestThread(aliceEditTask);
    aliceEditTaskThread.start();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    BOB.remoteBot().editor("readme.txt").waitUntilIsActive();
    BOB.remoteBot().editor("readme.txt").setText("Veni vidi vici");

    BOB.superBot().views().sarosView().waitUntilIsInconsistencyDetected();
    BOB.superBot().views().sarosView().resolveInconsistency();

    aliceEditTaskThread.interrupt();
    aliceEditTaskThread.join(10000);
    aliceEditTaskThread.verify();

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 10000);

    String aliceText = ALICE.remoteBot().editor("readme.txt").getText();
    String bobText = BOB.remoteBot().editor("readme.txt").getText();
    assertEquals(aliceText, bobText);
  }
}
