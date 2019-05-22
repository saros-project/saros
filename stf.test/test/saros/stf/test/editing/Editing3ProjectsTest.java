package saros.stf.test.editing;

import static org.junit.Assert.assertEquals;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.annotation.TestLink;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.shared.Constants.TypeOfCreateProject;

@TestLink(id = "Saros-108_add_3_new_projects_to_a_existing_session")
public class Editing3ProjectsTest extends StfTestCase {

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
      ALICE.superBot().internal().deleteProject("Foo2_Saros");
      ALICE.superBot().internal().deleteProject("Foo3_Saros");
      BOB.superBot().internal().deleteProject("Foo2_Saros");
      BOB.superBot().internal().deleteProject("Foo3_Saros");
      tearDownSaros();
    } else {
      tearDownSarosLast();
    }
  }

  @Test
  public void testEditing3Projects() throws Exception {
    ALICE.superBot().internal().createFile("Foo1_Saros", "src/bar/HelloAlice.java", "HelloAlice");
    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared("Foo1_Saros/src/bar/HelloAlice.java");
    ALICE.superBot().internal().createJavaProject("Foo2_Saros");
    ALICE.superBot().internal().createFile("Foo2_Saros", "src/bar/HelloBob.java", "HelloBob");
    Thread.sleep(1000);
    Util.addProjectToSessionSequentially("Foo2_Saros", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot()
        .views()
        .packageExplorerView()
        .waitUntilResourceIsShared("Foo2_Saros/src/bar/HelloBob.java");

    ALICE.superBot().internal().createJavaProject("Foo3_Saros");

    ALICE.superBot().internal().createFile("Foo3_Saros", "src/bar/HelloCarl.java", "HelloCarl");

    Util.addProjectToSessionSequentially("Foo3_Saros", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("Foo3_Saros");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass("Foo1_Saros", "bar", "HelloAlice")
        .open();

    BOB.remoteBot().editor("HelloAlice.java").waitUntilIsActive();

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass("Foo2_Saros", "bar", "HelloBob")
        .open();

    BOB.remoteBot().editor("HelloBob.java").waitUntilIsActive();

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectClass("Foo3_Saros", "bar", "HelloCarl")
        .open();

    BOB.remoteBot().editor("HelloCarl.java").waitUntilIsActive();

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass("Foo1_Saros", "bar", "HelloAlice")
        .open();

    ALICE.remoteBot().editor("HelloAlice.java").waitUntilIsActive();

    ALICE.remoteBot().editor("HelloAlice.java").typeText("testtext");

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass("Foo2_Saros", "bar", "HelloBob")
        .open();

    ALICE.remoteBot().editor("HelloBob.java").waitUntilIsActive();

    ALICE.remoteBot().editor("HelloBob.java").typeText("testtext");

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectClass("Foo3_Saros", "bar", "HelloCarl")
        .open();

    ALICE.remoteBot().editor("HelloCarl.java").waitUntilIsActive();

    ALICE.remoteBot().editor("HelloCarl.java").typeText("testtext");

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    assertEquals(
        ALICE.remoteBot().editor("HelloAlice.java").getText(),
        BOB.remoteBot().editor("HelloAlice.java").getText());
    assertEquals(
        ALICE.remoteBot().editor("HelloBob.java").getText(),
        BOB.remoteBot().editor("HelloBob.java").getText());
    assertEquals(
        ALICE.remoteBot().editor("HelloCarl.java").getText(),
        BOB.remoteBot().editor("HelloCarl.java").getText());
  }
}
