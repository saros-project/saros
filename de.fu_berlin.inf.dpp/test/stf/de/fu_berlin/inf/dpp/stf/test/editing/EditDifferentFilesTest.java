package de.fu_berlin.inf.dpp.stf.test.editing;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.test.util.EclipseTestThread;
import org.junit.BeforeClass;
import org.junit.Test;

public class EditDifferentFilesTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  // alice starts editing HelloWorld class
  // in the meantime bob adds a new class file HelloGermany and start editing
  // it

  @Test
  public void testEditingOnDifferentFiles() throws Exception {

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .tree()
        .newC()
        .javaProjectWithClasses("foo", "bar", "HelloWorld");

    Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared("foo/src/bar/HelloWorld.java");

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

            BOB.superBot()
                .views()
                .packageExplorerView()
                .selectProject("foo")
                .newC()
                .cls("HelloGermany");

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
            .getFileContent("foo/src/bar/HelloWorld.java");

    String contentBobHelloWorld =
        BOB.superBot().views().packageExplorerView().getFileContent("foo/src/bar/HelloWorld.java");

    String contentAliceHelloGermany =
        ALICE.superBot().views().packageExplorerView().getFileContent("foo/src/HelloGermany.java");

    String contentBobHelloWorldGermany =
        BOB.superBot().views().packageExplorerView().getFileContent("foo/src/HelloGermany.java");

    assertEquals(contentAliceHelloWorld, contentBobHelloWorld);
    assertEquals(contentAliceHelloGermany, contentBobHelloWorldGermany);
  }
}
