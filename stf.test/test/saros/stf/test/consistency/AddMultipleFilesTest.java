package saros.stf.test.consistency;

import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.test.util.EclipseTestThread;

public class AddMultipleFilesTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
        restoreSessionIfNecessary("Foo1_Saros", ALICE, BOB);
    }

    @Before
    public void setUp() throws Exception {

        closeAllShells();
        closeAllEditors();

    }

    @After
    public void cleanUpSaros() throws Exception {
        if (checkIfTestRunInTestSuite()) {
            ALICE.superBot().internal().deleteFolder("Foo1_Saros", "src");
            tearDownSaros();
        } else {
            tearDownSarosLast();
        }

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

        ALICE.superBot().internal().createJavaClass("Foo1_Saros", "my.pkg",
            "MyClass");
        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("Foo1_Saros/src/my/pkg/MyClass.java");

        EclipseTestThread.Runnable aliceFileTask = new EclipseTestThread.Runnable() {
            @Override
            public void run() throws Exception {
                for (int i = 0; i < 10; i++) {

                    if (Thread.currentThread().isInterrupted())
                        break;

                    ALICE.superBot().internal().createFile("Foo1_Saros",
                        "src/my/pkg/bigfile" + i, 10 * 1024 * 1024, true);
                }
            }
        };

        EclipseTestThread.Runnable bobFileTask = new EclipseTestThread.Runnable() {
            @Override
            public void run() throws Exception {
                for (int i = 0; i < 1000; i++) {
                    if (Thread.currentThread().isInterrupted())
                        break;

                    BOB.superBot().internal().createFile("Foo1_Saros",
                        "src/my/pkg/smallfile" + i, 100 * 1024, true);
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

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 2 * 60 * 1000);

        ALICE.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(BOB.getJID(), 2 * 60 * 1000);

        for (int i = 0; i < 1000; i++) {
            assertTrue(
                "file " + "Foo1_Saros/src/my/pkg/smallfile" + i
                    + " does not exist on ALICEs side",
                ALICE.superBot().internal()
                    .existsResource("Foo1_Saros/src/my/pkg/smallfile" + i));

            assertTrue(
                "file " + "Foo1_Saros/src/my/pkg/smallfile" + i
                    + " does not exist on BOBs side",
                BOB.superBot().internal()
                    .existsResource("Foo1_Saros/src/my/pkg/smallfile" + i));
        }

    }
}
