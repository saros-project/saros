package de.fu_berlin.inf.dpp.stf.test.editing;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import org.junit.BeforeClass;
import org.junit.Test;

@TestLink(id = "Saros-108_add_3_new_projects_to_a_existing_session")
public class Editing3ProjectsTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testEditing3Projects() throws Exception {

    ALICE.superBot().internal().createJavaProject("foo");
    ALICE.superBot().internal().createJavaProject("foo1");
    ALICE.superBot().internal().createJavaProject("foo2");

    ALICE.superBot().internal().createFile("foo", "src/bar/HelloAlice.java", "HelloAlice");
    ALICE.superBot().internal().createFile("foo1", "src/bar/HelloBob.java", "HelloBob");
    ALICE.superBot().internal().createFile("foo2", "src/bar/HelloCarl.java", "HelloCarl");

    Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo");

    Util.addProjectToSessionSequentially("foo1", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo1");

    Util.addProjectToSessionSequentially("foo2", TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo2");

    BOB.superBot().views().packageExplorerView().selectClass("foo", "bar", "HelloAlice").open();

    BOB.remoteBot().editor("HelloAlice.java").waitUntilIsActive();

    BOB.superBot().views().packageExplorerView().selectClass("foo1", "bar", "HelloBob").open();

    BOB.remoteBot().editor("HelloBob.java").waitUntilIsActive();

    BOB.superBot().views().packageExplorerView().selectClass("foo2", "bar", "HelloCarl").open();

    BOB.remoteBot().editor("HelloCarl.java").waitUntilIsActive();

    ALICE.superBot().views().packageExplorerView().selectClass("foo", "bar", "HelloAlice").open();

    ALICE.remoteBot().editor("HelloAlice.java").waitUntilIsActive();

    ALICE.remoteBot().editor("HelloAlice.java").typeText("testtext");

    ALICE.superBot().views().packageExplorerView().selectClass("foo1", "bar", "HelloBob").open();

    ALICE.remoteBot().editor("HelloBob.java").waitUntilIsActive();

    ALICE.remoteBot().editor("HelloBob.java").typeText("testtext");

    ALICE.superBot().views().packageExplorerView().selectClass("foo2", "bar", "HelloCarl").open();

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
