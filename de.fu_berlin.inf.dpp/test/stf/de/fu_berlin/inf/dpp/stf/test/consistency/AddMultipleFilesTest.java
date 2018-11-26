package de.fu_berlin.inf.dpp.stf.test.consistency;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.test.util.EclipseTestThread;
import org.junit.BeforeClass;
import org.junit.Test;

public class AddMultipleFilesTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  private EclipseTestThread alice;
  private EclipseTestThread bob;

  /*
   * So what is going wrong here ? Alice sends a big file, that is now
   * processed in the SharedResourceManager which ACCIDENTLY locks out all
   * threads. While we are writing the big file in the SWT GUI thread, we
   * adding more files from the RMI thread. The SharedResourceManager is
   * locked and so refused to process the newly generated files. Found this
   * bug while playing around with Saros Light. This test simulates are real
   * scenario: multiple users add files per drag and drop at the same time.
   */

  @Test
  public void testAddMultipleFilesSimultaneouslyTest() throws Exception {
    Util.setUpSessionWithProjectAndFile("foo", "main", "main", ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/main");

    EclipseTestThread.Runnable aliceFileTask =
        new EclipseTestThread.Runnable() {
          @Override
          public void run() throws Exception {
            for (int i = 0; i < 10; i++) {

              if (Thread.currentThread().isInterrupted()) break;

              ALICE.superBot().internal().createFile("foo", "bigfile" + i, 10 * 1024 * 1024, true);
            }
          }
        };

    EclipseTestThread.Runnable bobFileTask =
        new EclipseTestThread.Runnable() {
          @Override
          public void run() throws Exception {
            for (int i = 0; i < 1000; i++) {
              if (Thread.currentThread().isInterrupted()) break;

              BOB.superBot().internal().createFile("foo", "smallfile" + i, 100 * 1024, true);
            }
          }
        };

    alice = createTestThread(aliceFileTask);
    bob = createTestThread(bobFileTask);

    bob.start();
    alice.start();

    Util.joinAll(2 * 60 * 1000, alice, bob);

    alice.verify();
    bob.verify();

    BOB.controlBot()
        .getNetworkManipulator()
        .synchronizeOnActivityQueue(ALICE.getJID(), 2 * 60 * 1000);

    ALICE
        .controlBot()
        .getNetworkManipulator()
        .synchronizeOnActivityQueue(BOB.getJID(), 2 * 60 * 1000);

    for (int i = 0; i < 1000; i++) {
      assertTrue(
          "file " + "foo/smallfile" + i + " does not exist on ALICEs side",
          ALICE.superBot().internal().existsResource("foo/smallfile" + i));

      assertTrue(
          "file " + "foo/smallfile" + i + " does not exist on BOBs side",
          BOB.superBot().internal().existsResource("foo/smallfile" + i));
    }
  }
}
