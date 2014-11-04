package de.fu_berlin.inf.dpp.stf.test.activitylog;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACTIVITYLOG_TAB_LABEL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_CLASS;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_FILE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_NEW;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.MENU_OTHER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.tester.SarosTester;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class ActivityLogTest extends StfTestCase {

    // must be static, see http://martinfowler.com/bliki/JunitNewInstance.html
    private static boolean firstRun = true;

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Before
    public void setUp() throws Exception {
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

    private boolean isInActivityLog(SarosTester tester, String text)
        throws RemoteException {
        String[] lines = tester.superBot().views().sarosView()
            .selectActivityLog().getLines();
        boolean matched = false;
        for (String line : lines) {
            if (line.matches(text)) {
                matched = true;
                break;
            }
        }
        return matched;
    }

    /*
     * Tests if the activity log is opened.
     * 
     * 1. ALICE selects the activity log tab
     * 
     * 2. It is tested, if it was activated correctly
     * 
     * 3. BOB selects the activity log tab
     * 
     * 4. Tests, if it was activated correctly
     */
    @Test
    public void testActivityLog() throws Exception {
        String actualTitle = ALICE.superBot().views().sarosView()
            .selectActivityLog().getTitle();
        assertEquals(ACTIVITYLOG_TAB_LABEL, actualTitle);

        actualTitle = BOB.superBot().views().sarosView().selectActivityLog()
            .getTitle();
        assertEquals(ACTIVITYLOG_TAB_LABEL, actualTitle);
    }

    /*
     * Tests if BOB can see the activated view of ALICE
     * 
     * 1. ALICE activates the Saros view
     * 
     * 2. Tests, if BOB can see ALICE' activated Saros view
     */
    @Test
    public void testActivityLogShowsActiveView() throws Exception {
        ALICE.superBot().views().sarosView();
        ALICE.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(BOB.getJID(), 10000);
        // NOTE: two empty spaces because the first one is for the icon
        String expectedActiveView = "\\s*" + Pattern.quote("In view") + "\\s"
            + ALICE.remoteBot().activeView().getTitle();

        boolean isContained = isInActivityLog(BOB, expectedActiveView);
        assertTrue("line ‚" + expectedActiveView
            + " was not in the activity log", isContained);
    }

    /*
     * Tests if ALICE can see BOB's activated 'New' dialog
     * 
     * 1. BOB opens the 'New' dialog
     * 
     * 2. Tests, if ALICE can see BOB's open 'New' dialog
     */
    @Test
    public void testActivityLogShowsOpenDialog() throws Exception {
        BOB.remoteBot().activateWorkbench();
        BOB.remoteBot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_CLASS).click();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        String expectedOpenDialog = "\\s*" + Pattern.quote("Active dialog")
            + "\\s" + BOB.remoteBot().activeShell().getText();

        boolean isContained = isInActivityLog(ALICE, expectedOpenDialog);
        assertTrue("line ‚" + expectedOpenDialog
            + " was not in the activity log", isContained);
    }

    /*
     * Tests if ALICE can see BOB's activae view, in which he landed after
     * closing a dialog.
     * 
     * 1. BOB activates the 'Saros' view
     * 
     * 2. Tests, if ALICE can see BOB's active 'Saros' view
     * 
     * 3. BOB opens the 'New Java Class' dialog
     * 
     * 4. Tests, if ALICE can see BOB's open 'New Java Class' dialog
     * 
     * 5. BOB closes the 'New Java Class' dialog and therefore lands in the
     * 'Saros' view
     * 
     * 6. Tests, if ALICE can see BOB's active view in which he landed after
     * closing the 'New Java Class' dialog
     */
    @Test
    public void testActivityLogShowsLandingViewAfterDialogClose()
        throws Exception {
        BOB.superBot().views().sarosView();
        String expectedActiveView = "\\s*" + Pattern.quote("In view") + "\\s"
            + BOB.remoteBot().activeView().getTitle();

        boolean isContained = isInActivityLog(ALICE, expectedActiveView);
        // Alice sees that Bob is in the 'Saros' view
        assertTrue("line ‚" + expectedActiveView
            + " was not in the activity log", isContained);

        BOB.remoteBot().activateWorkbench();
        BOB.remoteBot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_CLASS).click();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        String expectedOpenDialog = "\\s*" + Pattern.quote("Active dialog")
            + "\\s" + BOB.remoteBot().activeShell().getText();

        isContained = isInActivityLog(ALICE, expectedOpenDialog);
        // Alice sees that Bob opened the 'New Java Class' dialog
        assertTrue("line ‚" + expectedOpenDialog
            + " was not in the activity log", isContained);

        BOB.remoteBot().activeShell().close();
        expectedActiveView = "\\s*" + Pattern.quote("In view") + "\\s"
            + BOB.remoteBot().activeView().getTitle();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        isContained = isInActivityLog(ALICE, expectedActiveView);
        // Alice sees that Bob closed the 'New Java Class' dialog and landed on
        // his last view, the 'Saros' view
        assertTrue("line ‚" + expectedActiveView
            + " was not in the activity log", isContained);
    }

    /*
     * Tests if ALICE can see BOB's changed wizard dialog title, which occurs
     * when a page in a wizard dialog was changed.
     * 
     * 1. BOB opens the 'New' dialog
     * 
     * 2. Tests, if ALICE can see BOB's open 'New' dialog
     * 
     * 3. BOB clicks on 'Java', which redirects him to the 'New Java Class'
     * dialog
     * 
     * 4. Tests, if ALICE can see that the title of the open dialog has changed,
     * thus if it is 'New Java Class'
     */
    @Test
    public void testActivityLogShowsChangedWizardDialogTitle() throws Exception {
        BOB.remoteBot().activateWorkbench();
        BOB.remoteBot().menu(MENU_FILE).menu(MENU_NEW).menu(MENU_OTHER).click();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        String expectedOpenDialog = "\\s*" + Pattern.quote("Active dialog")
            + "\\s" + BOB.remoteBot().activeShell().getText();

        boolean isContained = isInActivityLog(ALICE, expectedOpenDialog);
        assertTrue("line ‚" + expectedOpenDialog
            + " was not in the activity log", isContained);

        BOB.remoteBot().activeShell().bot().tree().selectTreeItem("Java")
            .getNode(MENU_CLASS).click();

        BOB.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(ALICE.getJID(), 10000);

        expectedOpenDialog = "\\s*" + Pattern.quote("Active dialog") + "\\s"
            + BOB.remoteBot().activeShell().getText();

        isContained = isInActivityLog(ALICE, expectedOpenDialog);
        assertTrue("line ‚" + expectedOpenDialog
            + " was not in the activity log", isContained);
    }

    /*
     * Tests if BOB can see that ALICE has created a new file.
     * 
     * 1. ALICE creates a new Java Class.
     * 
     * 2. Tests, if BOB can see that ALICE created a new Java Class
     */
    @Test
    public void testActivityLogShowsCreatedFile() throws Exception {
        ALICE.remoteBot().activateWorkbench();
        ALICE
            .superBot()
            .internal()
            .createJavaClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);

        ALICE.controlBot().getNetworkManipulator()
            .synchronizeOnActivityQueue(BOB.getJID(), 10000);

        String excpetedMessage = "\\s*" + Pattern.quote("Created file") + "\\s"
            + Constants.CLS2 + ".java";

        boolean isContained = isInActivityLog(BOB, excpetedMessage);
        assertTrue("line ‚" + excpetedMessage + " was not in the activity log",
            isContained);
    }
}