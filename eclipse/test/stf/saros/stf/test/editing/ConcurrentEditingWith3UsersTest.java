package saros.stf.test.editing;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.test.util.EclipseTestThread;

/**
 * This class tests the concurrent editing of three users during a session.
 *
 * @author nwarnatsch
 */
public class ConcurrentEditingWith3UsersTest extends StfTestCase {
  private EclipseTestThread aliceEditTaskThread;
  private EclipseTestThread bobEditTaskThread;
  private EclipseTestThread carlEditTaskThread;

  /*
   * The interval is the period of time in which the users edit the file. The
   * interval is set in minutes
   */
  private static final int INTERVAL = 1;

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Before
  public void runBeforeEveryTest() throws Exception {
    clearWorkspaces();
  }

  @After
  public void runAfterEveryTest() throws Exception {
    terminateTestThreads(10000);
    leaveSessionHostFirst(ALICE);
  }

  /**
   * The User ALICE and BOB insert characters in the file, CARL deletes a range of characters
   *
   * @throws Exception
   */
  @Test
  public void testTwoInsertOneDelete() throws Exception {
    Util.setUpSessionWithProjectAndFile(
        "foo",
        "readme.txt",
        "\nVerbesserung des algorithmischen Kerns, Gleichzeitiges Editieren\n",
        ALICE,
        CARL,
        BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");
    CARL.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    BOB.remoteBot().editor("readme.txt").waitUntilIsActive();

    CARL.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    CARL.remoteBot().editor("readme.txt").waitUntilIsActive();

    /*
     * The Alice-Thread insert the lower case characters
     */
    EclipseTestThread.Runnable aliceEditTask =
        new EclipseTestThread.Runnable() {
          @Override
          public void run() throws Exception {

            int i = 97;
            while (!Thread.currentThread().isInterrupted()) {
              if (i >= 123) { //
                ALICE.remoteBot().editor("readme.txt").typeText("\n");
                i = 96;
              } else {
                ALICE.remoteBot().editor("readme.txt").typeText("" + ((char) i) + "");
                ALICE.remoteBot().editor("readme.txt").navigateTo(1, 1);
              }
              i++;
            }
          }
        };

    /*
     * The Carl-Thread delete a range of characters
     */
    EclipseTestThread.Runnable carlEditTask =
        new EclipseTestThread.Runnable() {
          @Override
          public void run() throws Exception {
            int i = 48;
            while (!Thread.currentThread().isInterrupted()) {
              if (i >= 58) {
                CARL.remoteBot().editor("readme.txt").selectRange(1, 1, 5);
                CARL.remoteBot().editor("readme.txt").pressShortCutDelete();
                CARL.remoteBot().editor("readme.txt").selectRange(1, 1, 6);
                CARL.remoteBot().editor("readme.txt").typeText("#");
                i = 47;
              } else {
                CARL.remoteBot().editor("readme.txt").navigateTo(1, 1);
              }
              i++;
            }
          }
        };

    /*
     * The Bob-Thread insert the upper case characters
     */
    EclipseTestThread.Runnable bobEditTask =
        new EclipseTestThread.Runnable() {
          @Override
          public void run() throws Exception {
            int i = 65;
            while (!Thread.currentThread().isInterrupted()) {
              if (i >= 91) {
                BOB.remoteBot().editor("readme.txt").typeText("\n");
                i = 64;
              } else {
                BOB.remoteBot().editor("readme.txt").typeText("" + ((char) i) + "");
                BOB.remoteBot().editor("readme.txt").navigateTo(1, 1);
              }
              i++;
            }
          }
        };

    aliceEditTaskThread = createTestThread(aliceEditTask);
    aliceEditTaskThread.start();

    bobEditTaskThread = createTestThread(bobEditTask);
    bobEditTaskThread.start();

    carlEditTaskThread = createTestThread(carlEditTask);
    carlEditTaskThread.start();

    Thread.sleep(INTERVAL * 60 * 1000);

    aliceEditTaskThread.interrupt();
    bobEditTaskThread.interrupt();
    carlEditTaskThread.interrupt();

    aliceEditTaskThread.join(10000);
    bobEditTaskThread.join(10000);
    carlEditTaskThread.join(10000);

    aliceEditTaskThread.verify();
    bobEditTaskThread.verify();
    carlEditTaskThread.verify();

    // ensure that all queues on the client sides are flushed
    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 60 * 1000);

    CARL.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 60 * 1000);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(CARL.getJID(), 60 * 1000);

    String aliceText = ALICE.remoteBot().editor("readme.txt").getText();
    String bobText = BOB.remoteBot().editor("readme.txt").getText();
    String carlText = CARL.remoteBot().editor("readme.txt").getText();

    ALICE.remoteBot().editor("readme.txt").closeWithoutSave();
    ALICE.remoteBot().waitUntilEditorClosed("readme.txt");

    BOB.remoteBot().editor("readme.txt").closeWithoutSave();
    BOB.remoteBot().waitUntilEditorClosed("readme.txt");

    CARL.remoteBot().editor("readme.txt").closeWithoutSave();
    CARL.remoteBot().waitUntilEditorClosed("readme.txt");

    assertEquals(bobText, carlText);
    assertEquals(aliceText, bobText);
  }

  /**
   * Three users (everyone in one thread) insert text in the same file on the same line (Line 1).
   *
   * @throws Exception
   */
  @Test
  public void testThreeInsert() throws Exception {
    Util.setUpSessionWithProjectAndFile(
        "foo",
        "readme.txt",
        "\nVerbesserung des algorithmischen Kerns, Gleichzeitiges Editieren\n",
        ALICE,
        CARL,
        BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");
    CARL.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    BOB.remoteBot().editor("readme.txt").waitUntilIsActive();

    CARL.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    CARL.remoteBot().editor("readme.txt").waitUntilIsActive();

    /*
     * The Alice-Thread insert the lower case characters
     */
    EclipseTestThread.Runnable aliceEditTask =
        new EclipseTestThread.Runnable() {
          @Override
          public void run() throws Exception {
            int i = 97;
            while (!Thread.currentThread().isInterrupted()) {
              if (i >= 123) {
                ALICE.remoteBot().editor("readme.txt").typeText("\n");
                i = 96;
              } else {
                ALICE.remoteBot().editor("readme.txt").typeText("" + ((char) i) + "");
                ALICE.remoteBot().editor("readme.txt").navigateTo(1, 1);
              }
              i++;
            }
          }
        };

    /*
     * The Carl-Thread insert numbers
     */
    EclipseTestThread.Runnable carlEditTask =
        new EclipseTestThread.Runnable() {
          @Override
          public void run() throws Exception {
            int i = 48;
            while (!Thread.currentThread().isInterrupted()) {
              if (i >= 58) {
                CARL.remoteBot().editor("readme.txt").typeText("\n");
                i = 47;
              } else {
                CARL.remoteBot().editor("readme.txt").typeText("" + ((char) i) + "");
                CARL.remoteBot().editor("readme.txt").navigateTo(1, 1);
              }
              i++;
            }
          }
        };

    /*
     * The Bob-Thread insert the upper case characters
     */
    EclipseTestThread.Runnable bobEditTask =
        new EclipseTestThread.Runnable() {
          @Override
          public void run() throws Exception {
            int i = 65;
            while (!Thread.currentThread().isInterrupted()) {
              if (i >= 91) {
                BOB.remoteBot().editor("readme.txt").typeText("\n");
                i = 64;
              } else {
                BOB.remoteBot().editor("readme.txt").typeText("" + ((char) i) + "");
                BOB.remoteBot().editor("readme.txt").navigateTo(1, 1);
              }
              i++;
            }
          }
        };

    aliceEditTaskThread = createTestThread(aliceEditTask);
    aliceEditTaskThread.start();

    bobEditTaskThread = createTestThread(bobEditTask);
    bobEditTaskThread.start();

    carlEditTaskThread = createTestThread(carlEditTask);
    carlEditTaskThread.start();

    Thread.sleep(INTERVAL * 60 * 1000);

    aliceEditTaskThread.interrupt();
    bobEditTaskThread.interrupt();
    carlEditTaskThread.interrupt();
    aliceEditTaskThread.join(10000);
    bobEditTaskThread.join(10000);
    carlEditTaskThread.join(10000);

    aliceEditTaskThread.verify();
    bobEditTaskThread.verify();
    carlEditTaskThread.verify();

    // ensure that all queues on the client sides are flushed
    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 60 * 1000);

    CARL.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 60 * 1000);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(CARL.getJID(), 60 * 1000);

    String aliceText = ALICE.remoteBot().editor("readme.txt").getText();
    String bobText = BOB.remoteBot().editor("readme.txt").getText();
    String carlText = CARL.remoteBot().editor("readme.txt").getText();

    ALICE.remoteBot().editor("readme.txt").closeWithoutSave();
    ALICE.remoteBot().waitUntilEditorClosed("readme.txt");

    BOB.remoteBot().editor("readme.txt").closeWithoutSave();
    BOB.remoteBot().waitUntilEditorClosed("readme.txt");

    CARL.remoteBot().editor("readme.txt").closeWithoutSave();
    CARL.remoteBot().waitUntilEditorClosed("readme.txt");

    assertEquals(bobText, carlText);
    assertEquals(aliceText, bobText);
  }
}
