package de.fu_berlin.inf.dpp.stf.test.editing;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.test.util.TestThread;

/**
 * This class tests the concurrent editing of three buddies during a session.
 * 
 * @author nwarnatsch
 */

public class ConcurrentEditingWith3BuddiesTest extends StfTestCase {
    private TestThread aliceEditTaskThread;
    private TestThread bobEditTaskThread;
    private TestThread carlEditTaskThread;

    /*
     * The interval is the period of time in which the users edit the file. The
     * interval is set in minutes
     */
    private final static int INTERVAL = 1;

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
        if (aliceEditTaskThread != null && aliceEditTaskThread.isAlive()) {
            aliceEditTaskThread.interrupt();
            aliceEditTaskThread.join(10000);
        }
        if (bobEditTaskThread != null && bobEditTaskThread.isAlive()) {
            bobEditTaskThread.interrupt();
            bobEditTaskThread.join(10000);
        }
        if (carlEditTaskThread != null && carlEditTaskThread.isAlive()) {
            carlEditTaskThread.interrupt();
            carlEditTaskThread.join(10000);
        }

        leaveSessionHostFirst(ALICE);
    }

    /**
     * The User ALICE and BOB insert characters in the file, CARL deletes a
     * range of characters
     * 
     * @throws Exception
     */
    @Test
    public void testTwoInsertOneDelete() throws Exception {
        Util.setUpSessionWithProjectAndFile(
            "foo",
            "readme.txt",
            "\nVerbesserung des algorithmischen Kerns, Gleichzeitiges Editieren\n",
            ALICE, CARL, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/readme.txt");
        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/readme.txt");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();

        BOB.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        BOB.remoteBot().editor("readme.txt").waitUntilIsActive();

        CARL.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        CARL.remoteBot().editor("readme.txt").waitUntilIsActive();

        /*
         * The Alice-Thread insert the lower case characters
         */
        TestThread.Runnable aliceEditTask = new TestThread.Runnable() {
            @Override
            public void run() throws Exception {

                int i = 97;
                while (!Thread.currentThread().isInterrupted()) {
                    if (i >= 123) { //
                        ALICE.remoteBot().editor("readme.txt").typeText("\n");
                        i = 96;
                    } else {
                        ALICE.remoteBot().editor("readme.txt")
                            .typeText("" + ((char) i) + "");
                        ALICE.remoteBot().editor("readme.txt").navigateTo(1, 1);

                    }
                    i++;
                }

            }
        };

        /*
         * The Carl-Thread delete a range of characters
         */
        TestThread.Runnable carlEditTask = new TestThread.Runnable() {
            @Override
            public void run() throws Exception {
                int i = 48;
                while (!Thread.currentThread().isInterrupted()) {
                    if (i >= 58) {
                        CARL.remoteBot().editor("readme.txt")
                            .selectRange(1, 1, 5);
                        CARL.remoteBot().editor("readme.txt")
                            .pressShortCutDelete();
                        CARL.remoteBot().editor("readme.txt")
                            .selectRange(1, 1, 6);
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
        TestThread.Runnable bobEditTask = new TestThread.Runnable() {
            @Override
            public void run() throws Exception {
                int i = 65;
                while (!Thread.currentThread().isInterrupted()) {
                    if (i >= 91) {
                        BOB.remoteBot().editor("readme.txt").typeText("\n");
                        i = 64;
                    } else {
                        BOB.remoteBot().editor("readme.txt")
                            .typeText("" + ((char) i) + "");
                        BOB.remoteBot().editor("readme.txt").navigateTo(1, 1);
                    }
                    i++;
                }
            }
        };

        aliceEditTaskThread = new TestThread(aliceEditTask);
        aliceEditTaskThread.start();

        bobEditTaskThread = new TestThread(bobEditTask);
        bobEditTaskThread.start();

        carlEditTaskThread = new TestThread(carlEditTask);
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
        Thread.sleep(10 * 1000);

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
     * Three Buddies (everyone in one thread) insert text in the same file on
     * the same line (Line 1).
     * 
     * @throws Exception
     */
    @Test
    public void testThreeInsert() throws Exception {
        Util.setUpSessionWithProjectAndFile(
            "foo",
            "readme.txt",
            "\nVerbesserung des algorithmischen Kerns, Gleichzeitiges Editieren\n",
            ALICE, CARL, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/readme.txt");
        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/readme.txt");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();

        BOB.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        BOB.remoteBot().editor("readme.txt").waitUntilIsActive();

        CARL.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        CARL.remoteBot().editor("readme.txt").waitUntilIsActive();

        /*
         * The Alice-Thread insert the lower case characters
         */
        TestThread.Runnable aliceEditTask = new TestThread.Runnable() {
            @Override
            public void run() throws Exception {
                int i = 97;
                while (!Thread.currentThread().isInterrupted()) {
                    if (i >= 123) {
                        ALICE.remoteBot().editor("readme.txt").typeText("\n");
                        i = 96;
                    } else {
                        ALICE.remoteBot().editor("readme.txt")
                            .typeText("" + ((char) i) + "");
                        ALICE.remoteBot().editor("readme.txt").navigateTo(1, 1);

                    }
                    i++;
                }
            }
        };

        /*
         * The Carl-Thread insert numbers
         */
        TestThread.Runnable carlEditTask = new TestThread.Runnable() {
            @Override
            public void run() throws Exception {
                int i = 48;
                while (!Thread.currentThread().isInterrupted()) {
                    if (i >= 58) {
                        CARL.remoteBot().editor("readme.txt").typeText("\n");
                        i = 47;
                    } else {
                        CARL.remoteBot().editor("readme.txt")
                            .typeText("" + ((char) i) + "");
                        CARL.remoteBot().editor("readme.txt").navigateTo(1, 1);

                    }
                    i++;
                }
            }
        };

        /*
         * The Bob-Thread insert the upper case characters
         */
        TestThread.Runnable bobEditTask = new TestThread.Runnable() {
            @Override
            public void run() throws Exception {
                int i = 65;
                while (!Thread.currentThread().isInterrupted()) {
                    if (i >= 91) {
                        BOB.remoteBot().editor("readme.txt").typeText("\n");
                        i = 64;
                    } else {
                        BOB.remoteBot().editor("readme.txt")
                            .typeText("" + ((char) i) + "");
                        BOB.remoteBot().editor("readme.txt").navigateTo(1, 1);
                    }
                    i++;
                }
            }
        };

        aliceEditTaskThread = new TestThread(aliceEditTask);
        aliceEditTaskThread.start();

        bobEditTaskThread = new TestThread(bobEditTask);
        bobEditTaskThread.start();

        carlEditTaskThread = new TestThread(carlEditTask);
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
        Thread.sleep(10 * 1000);

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
