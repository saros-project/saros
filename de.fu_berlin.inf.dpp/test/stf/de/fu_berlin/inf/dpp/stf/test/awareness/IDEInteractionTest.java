package de.fu_berlin.inf.dpp.stf.test.awareness;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_CLASS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_FILE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_NEW;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_OTHER;
import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class IDEInteractionTest extends StfTestCase {

    // must be static, see http://martinfowler.com/bliki/JunitNewInstance.html
    private static boolean firstRun = true;

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Before
    public void runsBeforeEveryTest() throws Exception {
        closeAllShells();

        if (firstRun) {
            firstRun = false;
            Util.setUpSessionWithJavaProjectAndClass(Constants.PROJECT1,
                Constants.PKG1, Constants.CLS1, ALICE, BOB);

            BOB.superBot()
                .views()
                .packageExplorerView()
                .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS1);
        } else {
            Util.reBuildSession(Constants.PROJECT1, ALICE, BOB);
        }
    }

    /**
     * Tests that the host of a session can see the action awareness information
     * about the activated view of the invited participant in her session
     * overview.
     * 
     * Steps:
     * 
     * 1. Alice creates local project
     * 
     * 2. Alice invites Bob
     * 
     * 3. Bob activates 'Saros' view
     * 
     * 4. Alice can see that Bob has activated the 'Saros' view
     * 
     * */
    @Test
    public void testAliceSeesBobsActiveView() throws Exception {

        BOB.superBot().views().sarosView();
        String expectedActiveView = "Active view: "
            + BOB.remoteBot().activeView().getTitle();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        String activeViewAtAlice = ALICE.remoteBot().view("Saros").bot().tree()
            .selectTreeItem("Session")
            .getNodeWithRegex(".*" + Pattern.quote(BOB.getName()) + ".*")
            .getNodeWithRegex("Active view:" + ".*").getText();

        assertEquals(activeViewAtAlice, expectedActiveView);
    }

    /**
     * Tests that the host of a session can see the view on which the invited
     * person is landing after he closed a dialog.
     * 
     * Steps:
     * 
     * 1. Alice creates local project
     * 
     * 2. Alice invites Bob
     * 
     * 3. Bob activates the 'Saros' view
     * 
     * 4. Alice can see that Bob has activated the 'Saros' view
     * 
     * 5. Bob opens the 'New Java Class' dialog
     * 
     * 6. Alice can see that Bob has opened the 'New Java Class' dialog
     * 
     * 7. Bob closes the 'New Java Class' dialog and lands on his last view,
     * which is the 'Saros' view
     * 
     * 8. Alice can see that Bob has activated the 'Saros' view
     * 
     * */
    @Test
    public void testAliceSeesBobsLandingViewAfterDialogClose() throws Exception {

        BOB.superBot().views().sarosView();
        String expectedActiveView = "Active view: "
            + BOB.remoteBot().activeView().getTitle();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        String activeViewAtAlice = ALICE.remoteBot().view("Saros").bot().tree()
            .selectTreeItem("Session")
            .getNodeWithRegex(".*" + Pattern.quote(BOB.getName()) + ".*")
            .getNodeWithRegex("Active view:" + ".*").getText();

        // Alice sees that Bob is in the 'Saros' view
        assertEquals(activeViewAtAlice, expectedActiveView);

        BOB.remoteBot().activateWorkbench();
        BOB.remoteBot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_CLASS).click();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        String expectedOpenDialog = "Open dialog: "
            + BOB.remoteBot().activeShell().getText();

        String activeOpenDialog = ALICE.remoteBot().view("Saros").bot().tree()
            .selectTreeItem("Session")
            .getNodeWithRegex(".*" + Pattern.quote(BOB.getName()) + ".*")
            .getNodeWithRegex("Open dialog:" + ".*").getText();

        // Alice sees that Bob opened the 'New Java Class' dialog
        assertEquals(activeOpenDialog, expectedOpenDialog);

        BOB.remoteBot().activeShell().close();
        expectedActiveView = "Active view: "
            + BOB.remoteBot().activeView().getTitle();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        activeViewAtAlice = ALICE.remoteBot().view("Saros").bot().tree()
            .selectTreeItem("Session")
            .getNodeWithRegex(".*" + Pattern.quote(BOB.getName()) + ".*")
            .getNodeWithRegex("Active view:" + ".*").getText();

        // Alice sees that Bob closed the 'New Java Class' dialog and landed on
        // his last view, the 'Saros' view
        assertEquals(activeViewAtAlice, expectedActiveView);
    }

    /**
     * Tests that the host of a session can see the action awareness information
     * about the opened dialog of the invited participant in her session
     * overview.
     * 
     * Steps:
     * 
     * 1. Alice creates local project
     * 
     * 2. Alice invites Bob
     * 
     * 3. Bob activates 'New Java Class' dialog
     * 
     * 4. Alice can see that Bob has opened the 'New Java Class' dialog
     * 
     * */
    @Test
    public void testAliceSeesBobsOpenDialog() throws Exception {

        BOB.remoteBot().activateWorkbench();
        BOB.remoteBot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_CLASS).click();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        String expectedOpenDialog = "Open dialog: "
            + BOB.remoteBot().activeShell().getText();

        String activeOpenDialog = ALICE.remoteBot().view("Saros").bot().tree()
            .selectTreeItem("Session")
            .getNodeWithRegex(".*" + Pattern.quote(BOB.getName()) + ".*")
            .getNodeWithRegex("Open dialog:" + ".*").getText();

        assertEquals(activeOpenDialog, expectedOpenDialog);
    }

    /**
     * Tests that the host of a session can see the action awareness information
     * about the changed title of the opened dialog of the invited participant
     * in her session overview.
     * 
     * Steps:
     * 
     * 1. Alice creates local project
     * 
     * 2. Alice invites Bob
     * 
     * 3. Bob opens the 'New' dialog
     * 
     * 4. Alice can see that Bob has opened the 'New' dialog
     * 
     * 5. Bob selects the 'Java' in the 'New' dialog
     * 
     * 6. Bon selects the 'Class' item in the 'Java' node and the title of the
     * dialog is changed to 'New Java Class'
     * 
     * 7. Alice can see that Bob has opened the 'New Java Class' dialog
     * 
     * */
    @Test
    public void testAliceSeesBobsChangedWizardDialogTitle() throws Exception {

        BOB.remoteBot().activateWorkbench();
        BOB.remoteBot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_OTHER).click();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        String expectedOpenDialog = "Open dialog: "
            + BOB.remoteBot().activeShell().getText();

        String activeOpenDialog = ALICE.remoteBot().view("Saros").bot().tree()
            .selectTreeItem("Session")
            .getNodeWithRegex(".*" + Pattern.quote(BOB.getName()) + ".*")
            .getNodeWithRegex("Open dialog:" + ".*").getText();

        assertEquals(activeOpenDialog, expectedOpenDialog);

        BOB.remoteBot().activeShell().bot().tree().selectTreeItem("Java")
            .getNode(MENU_CLASS).click();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        expectedOpenDialog = "Open dialog: "
            + BOB.remoteBot().activeShell().getText();

        activeOpenDialog = ALICE.remoteBot().view("Saros").bot().tree()
            .selectTreeItem("Session")
            .getNodeWithRegex(".*" + Pattern.quote(BOB.getName()) + ".*")
            .getNodeWithRegex("Open dialog:" + ".*").getText();

        assertEquals(activeOpenDialog, expectedOpenDialog);
    }
}
