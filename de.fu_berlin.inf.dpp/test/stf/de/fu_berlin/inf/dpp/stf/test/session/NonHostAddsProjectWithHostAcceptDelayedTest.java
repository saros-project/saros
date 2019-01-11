package de.fu_berlin.inf.dpp.stf.test.session;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class NonHostAddsProjectWithHostAcceptDelayedTest extends StfTestCase {

  @BeforeClass
  public static void selectTesters() throws Exception {
    select(ALICE, BOB, CARL);
  }

  @Test
  @Ignore("Non-Host adding projects is currently deactivated")
  public void testDelayedHostAccept() throws Exception {

    Util.setUpSessionWithProjectAndFile("foo", "text.txt", "Hello World", ALICE, BOB, CARL);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/text.txt");

    CARL.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/text.txt");

    BOB.superBot().internal().createProject("bar");
    BOB.superBot().internal().createFile("bar", "text.txt", "Hello World");

    BOB.superBot().menuBar().saros().addProjects("bar");

    CARL.superBot().confirmShellAddProjectUsingWhichProject("bar", TypeOfCreateProject.NEW_PROJECT);

    CARL.superBot().views().packageExplorerView().waitUntilResourceIsShared("bar/text.txt");

    BOB.superBot().views().packageExplorerView().selectFile("bar", "text.txt").open();

    CARL.superBot().views().packageExplorerView().selectFile("bar", "text.txt").open();

    BOB.remoteBot().editor("text.txt").waitUntilIsActive();
    BOB.remoteBot().editor("text.txt").typeText("Ich bin der " + BOB);

    CARL.remoteBot().editor("text.txt").waitUntilIsActive();
    CARL.remoteBot().editor("text.txt").typeText("Ich bin der " + CARL);

    ALICE
        .superBot()
        .confirmShellAddProjectUsingWhichProject("bar", TypeOfCreateProject.NEW_PROJECT);

    ALICE.superBot().views().packageExplorerView().waitUntilResourceIsShared("bar/text.txt");

    ALICE.superBot().views().packageExplorerView().selectFile("bar", "text.txt").open();

    ALICE.remoteBot().editor("text.txt").waitUntilIsActive();
    ALICE.remoteBot().editor("text.txt").typeText("Ich bin die " + ALICE);

    BOB.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 60 * 1000);

    CARL.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(ALICE.getJID(), 60 * 1000);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(BOB.getJID(), 60 * 1000);

    ALICE.controlBot().getNetworkManipulator().synchronizeOnActivityQueue(CARL.getJID(), 60 * 1000);

    String alicesText = ALICE.remoteBot().editor("text.txt").getText();
    String bobsText = BOB.remoteBot().editor("text.txt").getText();
    String carlsText = CARL.remoteBot().editor("text.txt").getText();

    assertEquals(alicesText, bobsText);
    assertEquals(alicesText, carlsText);
  }
}
