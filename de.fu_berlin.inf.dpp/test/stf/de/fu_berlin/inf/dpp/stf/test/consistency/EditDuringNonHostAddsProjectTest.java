package de.fu_berlin.inf.dpp.stf.test.consistency;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.test.util.EclipseTestThread;
import org.junit.BeforeClass;
import org.junit.Test;

public class EditDuringNonHostAddsProjectTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  public void testNonHostAddsProject() throws Exception {

    // Initial session setup
    Util.setUpSessionWithProjectAndFile("foo", "text.txt", "Hello World", ALICE, BOB, CARL);
    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/text.txt");
    CARL.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/text.txt");

    // Bob creates a new project
    BOB.superBot().internal().createProject("bar");
    BOB.superBot().internal().createFile("bar", "text.txt", "Hello World");
    BOB.superBot().views().packageExplorerView().selectFile("bar", "text.txt").open();
    BOB.remoteBot().editor("text.txt").waitUntilIsActive();

    EclipseTestThread bobIsWriting =
        createTestThread(
            new EclipseTestThread.Runnable() {

              @Override
              public void run() throws Exception {
                while (!Thread.currentThread().isInterrupted()) {
                  BOB.remoteBot().editor("text.txt").typeText("Bob");
                }
              }
            });

    // Bob adds the project and starts typing
    BOB.superBot().menuBar().saros().addProjects("bar");
    bobIsWriting.start();

    EclipseTestThread aliceIsWriting =
        createTestThread(
            new EclipseTestThread.Runnable() {

              @Override
              public void run() throws Exception {
                while (!Thread.currentThread().isInterrupted()) {
                  ALICE.remoteBot().editor("text.txt").typeText("Alice");
                }
              }
            });

    // Alice receives the project and also starts typing
    ALICE.superBot().confirmShellAddProjectWithNewProject("bar");
    ALICE.superBot().views().packageExplorerView().waitUntilResourceIsShared("bar/text.txt");
    ALICE.superBot().views().packageExplorerView().selectFile("bar", "text.txt").open();
    ALICE.remoteBot().editor("text.txt").waitUntilIsActive();
    aliceIsWriting.start();

    // Carl receives the project and also adds some text
    CARL.superBot().confirmShellAddProjectWithNewProject("bar");
    CARL.superBot().views().packageExplorerView().waitUntilResourceIsShared("bar/text.txt");
    CARL.superBot().views().packageExplorerView().selectFile("bar", "text.txt").open();
    CARL.remoteBot().editor("text.txt").waitUntilIsActive();
    CARL.remoteBot().editor("text.txt").typeText("Ich bin der " + CARL);

    // Everybody stops typing
    aliceIsWriting.interrupt();
    aliceIsWriting.join(10000);
    aliceIsWriting.verify();

    bobIsWriting.interrupt();
    bobIsWriting.join(10000);
    bobIsWriting.verify();

    // Sync & Check for any inconsistencies
    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(CARL.getJID(), 60 * 1000);
    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(CARL.getJID(), 60 * 1000);
    CARL.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);
    CARL.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 60 * 1000);

    String alicesText = ALICE.remoteBot().editor("text.txt").getText();
    String bobsText = BOB.remoteBot().editor("text.txt").getText();
    String carlsText = CARL.remoteBot().editor("text.txt").getText();

    assertEquals(alicesText, bobsText);
    assertEquals(alicesText, carlsText);
  }
}
