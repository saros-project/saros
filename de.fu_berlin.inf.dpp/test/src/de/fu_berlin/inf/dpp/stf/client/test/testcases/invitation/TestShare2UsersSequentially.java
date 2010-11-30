package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestShare2UsersSequentially extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestShare2UsersSequentially.class);

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
    }

    /**
     * Closes all opened xmppConnects, popup windows and editor.<br/>
     * Delete all existed projects.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * Closes all opened popup windows and editor.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice share project with bob.</li>
     * <li>Alice and bob leave the session.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Alice has the Role as participant and driver, bob has the role as
     * participant and observer</li>
     * <li>Alice and bob have no Role after leaving the session.</li>
     * </ol>
     * 
     * @throws InterruptedException
     */
    @Test
    public void aliceShareProjectWithBobSequentially() throws RemoteException,
        InterruptedException {
        log.trace("testShareProject enter");

        alice.buildSessionSequentially(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, bob);
        bob.basic
            .captureScreenshot((bob.state.getPathToScreenShot() + "/invitee_in_sharedproject.png"));
        alice.basic
            .captureScreenshot((alice.state.getPathToScreenShot() + "/inviter_in_sharedproject.png"));
        log.trace("inviter.setTextInClass");
        alice.editor.setTextInJavaEditorWithSave(CP1, PROJECT1, PKG1, CLS1);

        log.trace("invitee.openFile");
        bob.pEV.openClass(PROJECT1, PKG1, CLS1);

        assertTrue(bob.state.isParticipant());
        assertTrue(alice.state.isParticipant());

        assertTrue(bob.state.isObserver());
        assertFalse(alice.state.isObserver());

        assertTrue(alice.state.isDriver());
        assertFalse(bob.state.isDriver());

        alice.leaveSessionFirstByPeers(bob);

        assertFalse(bob.state.isParticipant());
        assertFalse(alice.state.isParticipant());

        assertFalse(bob.state.isObserver());
        assertFalse(alice.state.isObserver());

        assertFalse(alice.state.isDriver());
        assertFalse(bob.state.isDriver());

    }
}
