package saros.stf.test.consistency;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.annotation.TestLink;
import saros.stf.client.StfTestCase;
import saros.test.util.EclipseTestThread;

@TestLink(id = "Saros-7_consistency_watchdog_and_stop_manager")
public class RecoveryWhileTypingTest extends StfTestCase {

  private EclipseTestThread aliceEditTaskThread;

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

    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).grantWriteAccess();
    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).waitUntilHasWriteAccess();
    if (checkIfTestRunInTestSuite()) {
      ALICE.superBot().internal().deleteFolder("Foo1_Saros", "src");
      tearDownSaros();
    } else {
      tearDownSarosLast();
    }
  }

  @Test
  public void testRecoveryWhileTyping() throws Exception {

    ALICE
        .superBot()
        .internal()
        .createFile("Foo1_Saros", "src/readme.txt", "Harry Potter und der geheime Pornokeller"); //

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared("Foo1_Saros/src/readme.txt");

    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).restrictToReadOnlyAccess();

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFile("Foo1_Saros", "src", "readme.txt")
        .open();

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

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectFile("Foo1_Saros", "src", "readme.txt")
        .open();
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
