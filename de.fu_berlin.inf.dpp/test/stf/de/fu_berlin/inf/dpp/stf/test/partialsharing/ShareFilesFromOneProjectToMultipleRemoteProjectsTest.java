package de.fu_berlin.inf.dpp.stf.test.partialsharing;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.FINISH;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.RADIO_USING_EXISTING_PROJECT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_ADD_PROJECTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShareFilesFromOneProjectToMultipleRemoteProjectsTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB);
  }

  @Test
  public void testShareFilesFromOneProjectToMultipleRemoteProjects() throws Exception {

    ALICE.superBot().internal().createProject("A");
    ALICE.superBot().internal().createFile("A", "a/a.txt", "");
    ALICE.superBot().internal().createFile("A", "b/b.txt", "");

    Util.buildFileSessionConcurrently(
        "A", new String[] {"a/a.txt"}, TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("A/a/a.txt");

    ALICE.superBot().menuBar().saros().addProject("A", new String[] {"b/b.txt"});

    IRemoteBotShell projectDialog = BOB.remoteBot().shell(SHELL_ADD_PROJECTS);

    assertTrue(
        "partial shared project is not preselected",
        projectDialog.bot().radio(RADIO_USING_EXISTING_PROJECT).isSelected());
    assertFalse(
        "option to choose project location must be disabled",
        projectDialog.bot().radio(RADIO_USING_EXISTING_PROJECT).isEnabled());

    projectDialog.bot().button(FINISH).click();
    projectDialog.waitShortUntilIsClosed();

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("A/b/b.txt");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectFile("A", new String[] {"b", "b.txt"})
        .open();

    BOB.remoteBot().editor("b.txt").waitUntilIsActive();
    BOB.remoteBot().editor("b.txt").typeText("Triple BBB");

    BOB.superBot()
        .views()
        .packageExplorerView()
        .selectFile("A", new String[] {"a", "a.txt"})
        .open();

    BOB.remoteBot().editor("a.txt").waitUntilIsActive();
    BOB.remoteBot().editor("a.txt").typeText("Triple AAA");

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 60 * 1000);

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFile("A", new String[] {"b", "b.txt"})
        .open();
    ALICE.remoteBot().editor("b.txt").waitUntilIsActive();

    ALICE
        .superBot()
        .views()
        .packageExplorerView()
        .selectFile("A", new String[] {"a", "a.txt"})
        .open();
    ALICE.remoteBot().editor("a.txt").waitUntilIsActive();

    assertEquals(
        BOB.remoteBot().editor("a.txt").getText(), ALICE.remoteBot().editor("a.txt").getText());

    assertEquals(
        BOB.remoteBot().editor("b.txt").getText(), ALICE.remoteBot().editor("b.txt").getText());
  }
}
