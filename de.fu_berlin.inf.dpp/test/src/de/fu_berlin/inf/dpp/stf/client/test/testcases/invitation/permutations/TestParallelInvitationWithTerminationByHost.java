package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.permutations;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestParallelInvitationWithTerminationByHost {

    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String CLS = BotConfiguration.CLASSNAME;

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;
    private static Musician dave;

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
     * <li>Dave (Observer)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void initMusican() throws AccessException, RemoteException,
        InterruptedException {
        /*
         * initialize the musicians simultaneously
         */
        List<Musician> musicians = InitMusician.initMusiciansConcurrently(
            BotConfiguration.PORT_ALICE, BotConfiguration.PORT_BOB,
            BotConfiguration.PORT_CARL, BotConfiguration.PORT_DAVE);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        dave = musicians.get(3);

    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        carl.workbench.resetSaros();
        dave.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * make sure,all opened popup windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        dave.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice invites everyone else simultaneously.</li>
     * <li>Alice opens the Progress View and cancels Bob's invitation before Bob
     * accepts.</li>
     * <li>Carl accepts the invitation but does not choose a target project.</li>
     * <li>Alice opens the Progress View and cancels Carl's invitation before
     * Carl accepts</li>
     * <li>Dave accepts the invitation and chooses a target project.</li>
     * <li>Alice opens the Progress View and cancels Dave 's invitation during
     * the synchronisation.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li>Bob is notified of Alice's canceling the invitation.</li>
     * <li></li>
     * <li>Carl is notified of Alice's canceling the invitation.</li>
     * <li></li>
     * <li>Dave is notified of Alice's canceling the invitation.</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */

    @Test
    public void testExistDirtyFlagByDaveAndEdnaDuringAlicMakeChange()
        throws IOException, CoreException, InterruptedException {
        alice.mainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);

        /*
         * build session with bob, carl and dave simultaneously
         */
        List<String> peersName = new LinkedList<String>();
        peersName.add(bob.getBaseJid());
        peersName.add(dave.getBaseJid());
        peersName.add(carl.getBaseJid());

        alice.packageExplorerV.shareProject(PROJECT, peersName);

        bob.popupWindow.waitUntilShellActive("Session Invitation");
        alice.progressV.cancelInvitation(0);
        bob.popupWindow.waitUntilShellActive("Invitation Cancelled");
        assertTrue(bob.popupWindow.isShellActive("Invitation Cancelled"));
        bob.popupWindow.closeShell("Invitation Cancelled");

        carl.popupWindow.waitUntilShellActive("Session Invitation");
        carl.popupWindow.confirmSessionInvitationWindowStep1();
        alice.progressV.cancelInvitation(0);
        carl.popupWindow.waitUntilShellActive("Invitation Cancelled");
        assertTrue(carl.popupWindow.isShellActive("Invitation Cancelled"));
        carl.popupWindow.closeShell("Invitation Cancelled");

        dave.popupWindow.waitUntilShellActive("Session Invitation");
        dave.popupWindow.confirmSessionInvitationWindowStep1();
        dave.basic.clickButton(SarosConstant.BUTTON_FINISH);
        alice.progressV.cancelInvitation(0);
        dave.popupWindow.waitUntilShellActive("Invitation Cancelled");
        assertTrue(dave.popupWindow.isShellActive("Invitation Cancelled"));
        dave.popupWindow.closeShell("Invitation Cancelled");

    }
}
