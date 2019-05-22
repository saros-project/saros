package saros.stf.test.editing;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.test.util.EclipseTestThread;

public class EditDifferentFilesTest extends StfTestCase {

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

  // alice starts editing HelloWorld class
  // in the meantime bob adds a new class file HelloGermany and start editing
  // it

  @Test
  public void testEditingOnDifferentFiles() throws Exception {

    ALICE.superBot().internal().createJavaClass("Foo1_Saros", "bar", "HelloWorld");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared("Foo1_Saros/src/bar/HelloWorld.java");
    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass("Foo1_Saros", "bar", "HelloWorld")
        .open();
    EclipseTestThread.Runnable aliceEditTask =
        new EclipseTestThread.Runnable() {
          @Override
          public void run() throws Exception {
            String textToType =
                "This is a long, long, long and\n long working test that bla bla bla";

            for (char c : textToType.toCharArray()) {

              ALICE.remoteBot().editor("HelloWorld.java").waitUntilIsActive();
              ALICE.remoteBot().editor("HelloWorld.java").typeText(String.valueOf(c));
            }
          }
        };

    EclipseTestThread.Runnable bobEditTask =
        new EclipseTestThread.Runnable() {
          @Override
          public void run() throws Exception {
            String textToType = "Dieses ist ein sehr, sehr, sehr,\n langer bla bla bla";
            BOB.superBot().internal().createJavaClass("Foo1_Saros", "bar", "HelloGermany");
            BOB.superBot()
                .views()
                .packageExplorerView()
                .selectClass("Foo1_Saros", "bar", "HelloGermany")
                .open();
            for (char c : textToType.toCharArray()) {
              BOB.remoteBot().editor("HelloGermany.java").waitUntilIsActive();
              BOB.remoteBot().editor("HelloGermany.java").typeText(String.valueOf(c));
            }
          }
        };

    EclipseTestThread alice = createTestThread(aliceEditTask);
    EclipseTestThread bob = createTestThread(bobEditTask);

    alice.start();
    bob.start();
    alice.join();
    bob.join();
    alice.verify();
    bob.verify();

    ALICE.remoteBot().saveAllEditors();
    BOB.remoteBot().saveAllEditors();

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 60 * 1000);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    String contentAliceHelloWorld =
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent("Foo1_Saros/src/bar/HelloWorld.java");

    String contentBobHelloWorld =
        BOB.superBot()
            .views()
            .packageExplorerView()
            .getFileContent("Foo1_Saros/src/bar/HelloWorld.java");

    String contentAliceHelloGermany =
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent("Foo1_Saros/src/bar/HelloGermany.java");

    String contentBobHelloWorldGermany =
        BOB.superBot()
            .views()
            .packageExplorerView()
            .getFileContent("Foo1_Saros/src/bar/HelloGermany.java");

    assertEquals(contentAliceHelloWorld, contentBobHelloWorld);
    assertEquals(contentAliceHelloGermany, contentBobHelloWorldGermany);
  }
}
