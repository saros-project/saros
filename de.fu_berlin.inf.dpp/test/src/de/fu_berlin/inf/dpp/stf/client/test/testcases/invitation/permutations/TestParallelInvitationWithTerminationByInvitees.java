package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.permutations;

import static org.junit.Assert.assertFalse;
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

public class TestParallelInvitationWithTerminationByInvitees {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String CLS = BotConfiguration.CLASSNAME;

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;
    private static Musician dave;
    private static Musician edna;

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
     * <li>Dave (Observer)</li>
     * <li>Edna (Observer)</li>
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
            BotConfiguration.PORT_CARL, BotConfiguration.PORT_DAVE,
            BotConfiguration.PORT_EDNA);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        dave = musicians.get(3);
        edna = musicians.get(4);

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
        edna.workbench.resetSaros();
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
        edna.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice invites everyone else simultaneously.</li>
     * <li>Bob cancels the invitation at the beginning.</li>
     * <li>Carl cancels the invitation after receiving the file list, but before
     * the synchronisation starts.</li>
     * <li>Dave cancels the invitation during the synchronisation.</li>
     * <li>Edna accepts the invitation (using a new project).</li>
     * <li>Edna leaves the session.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Alice is notified of the peer canceling the invitation.</li>
     * <li>Alice is notified of Edna joining the session.</li>
     * <li></li>
     * <li>Alice is notified of Edna leaving the session.</li> *
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
        peersName.add(edna.getBaseJid());

        alice.packageExplorerV.shareProject(PROJECT, peersName);

        bob.eclipseWindow.waitUntilShellActive("Session Invitation");
        bob.basic.clickButton(SarosConstant.BUTTON_CANCEL);
        alice.eclipseWindow.waitUntilShellActive("Problem Occurred");
        assertTrue(alice.eclipseWindow.getSecondLabelOfProblemOccurredWindow()
            .matches(bob.getName() + ".*"));
        alice.basic.clickButton(SarosConstant.BUTTON_OK);

        carl.eclipseWindow.waitUntilShellActive("Session Invitation");
        carl.popupWindow.confirmSessionInvitationWindowStep1();
        carl.basic.clickButton(SarosConstant.BUTTON_CANCEL);
        alice.eclipseWindow.waitUntilShellActive("Problem Occurred");
        assertTrue(alice.eclipseWindow.getSecondLabelOfProblemOccurredWindow()
            .matches(carl.getName() + ".*"));
        alice.basic.clickButton(SarosConstant.BUTTON_OK);

        dave.eclipseWindow.waitUntilShellActive("Session Invitation");
        dave.popupWindow.confirmSessionInvitationWindowStep1();
        // dave.bot.clickButton(SarosConstant.BUTTON_FINISH);
        dave.basic.clickButton(SarosConstant.BUTTON_CANCEL);
        alice.eclipseWindow.waitUntilShellActive("Problem Occurred");
        assertTrue(alice.eclipseWindow.getSecondLabelOfProblemOccurredWindow()
            .matches(dave.getName() + ".*"));
        alice.basic.clickButton(SarosConstant.BUTTON_OK);

        edna.eclipseWindow.waitUntilShellActive("Session Invitation");
        edna.popupWindow.confirmSessionInvitationWindowStep1();
        edna.popupWindow
            .confirmSessionInvitationWindowStep2UsingNewproject(PROJECT);
        edna.sessionV.leaveSessionByPeer();
        assertFalse(alice.state.isDriver(edna.jid));
    }
}
