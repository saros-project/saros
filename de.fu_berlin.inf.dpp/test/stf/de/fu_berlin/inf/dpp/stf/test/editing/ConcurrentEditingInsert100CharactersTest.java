package de.fu_berlin.inf.dpp.stf.test.editing;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.test.util.EclipseTestThread;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConcurrentEditingInsert100CharactersTest extends StfTestCase {

  private static final long TIMEOUT = 5 * 60 * 1000;

  private static final int KEYSTROKE_LIMIT = 100;

  private EclipseTestThread aliceEditTaskThread;

  private EclipseTestThread bobEditTaskThread;

  private EclipseTestThread carlEditTaskThread;

  private static class TypeTask implements EclipseTestThread.Runnable {

    private AbstractTester tester;
    private String editorName;
    private char from;
    private char to;

    public TypeTask(AbstractTester tester, String editorName, char from, char to) {
      this.tester = tester;
      this.editorName = editorName;
      this.from = from;
      this.to = to;
    }

    @Override
    public void run() throws Exception {
      char character = from;
      int typed = 0;

      while ((!Thread.currentThread().isInterrupted()) && (typed++ < KEYSTROKE_LIMIT)) {

        tester.remoteBot().editor(editorName).navigateTo(0, 0);

        if (character <= to) {
          tester.remoteBot().editor(editorName).typeText(String.valueOf(character));

          character++;
        } else {
          tester.remoteBot().editor(editorName).typeText("\n");
          character = from;
        }
      }
    }
  }

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  public void testInsertCharactersOnTheSameLine() throws Exception {

    ALICE.superBot().internal().createProject("foo");
    ALICE
        .superBot()
        .internal()
        .createFile(
            "foo",
            "readme.txt",
            "package test;\n\npublic class paulaBean {\n\n\tprivate String paula = \"Brillant\";\n\n\tpublic String getPaula() {\n\t\treturn paula;\n\t}\n}");

    Util.buildSessionConcurrently("foo", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");

    CARL.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/readme.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();

    BOB.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    BOB.remoteBot().editor("readme.txt").waitUntilIsActive();

    CARL.superBot().views().packageExplorerView().selectFile("foo", "readme.txt").open();
    CARL.remoteBot().editor("readme.txt").waitUntilIsActive();

    aliceEditTaskThread = createTestThread(new TypeTask(ALICE, "readme.txt", 'A', 'Z'));

    aliceEditTaskThread.start();

    bobEditTaskThread = createTestThread(new TypeTask(BOB, "readme.txt", 'a', 'z'));

    bobEditTaskThread.start();

    carlEditTaskThread = createTestThread(new TypeTask(CARL, "readme.txt", '0', '9'));

    carlEditTaskThread.start();

    /*
     * Wait up to 5 minutes, if the test threads are still running, the test
     * will fail
     */

    Util.joinAll(TIMEOUT, aliceEditTaskThread, bobEditTaskThread, carlEditTaskThread);

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
