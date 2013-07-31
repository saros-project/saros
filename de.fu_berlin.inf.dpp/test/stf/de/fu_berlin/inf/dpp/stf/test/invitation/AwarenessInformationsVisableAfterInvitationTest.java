package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class AwarenessInformationsVisableAfterInvitationTest extends
    StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB, CARL);
    }

    /**
     * Tests that an invited client can see the awareness informations after the
     * projectNegotiation finished
     */
    @Test
    public void AwarenessInformationsVisablTest() throws Exception {
        ALICE.superBot().internal().createJavaProject(Constants.PROJECT1);
        ALICE.superBot().internal()
            .createFile(Constants.PROJECT1, Constants.FILE1, "");

        ALICE.superBot().views().packageExplorerView()
            .selectFile(Constants.PROJECT1, Constants.FILE1).open();
        Util.buildSessionConcurrently(Constants.PROJECT1,
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);
        BOB.superBot()
            .views()
            .packageExplorerView()
            .waitUntilResourceIsShared(
                Constants.PROJECT1 + "/" + Constants.FILE1);

        // We have to wait a short time for the remotes to send their
        // awareness informations
        Thread.sleep(5000);

        BOB.remoteBot().view("Saros").setFocus();

        String expectedOpenProject = Constants.PROJECT1 + ": "
            + Constants.FILE1;

        String projectOpenAtAlice = BOB.remoteBot().view("Saros").bot().tree()
            .selectTreeItem("Session").getNode("    Host " + ALICE.getName())
            .getNode(0).getText();

        assertEquals(
            "Bob sees the wrong file as open by alice or none at all.",
            expectedOpenProject, projectOpenAtAlice);
    }
}
