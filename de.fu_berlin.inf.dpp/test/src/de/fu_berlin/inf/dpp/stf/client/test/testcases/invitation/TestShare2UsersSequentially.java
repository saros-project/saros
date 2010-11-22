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

    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    @Test
    public void testShareProject2UsersSequentially() throws RemoteException,
        InterruptedException {
        log.trace("testShareProject enter");

        alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, bob);
        bob.basic
            .captureScreenshot((bob.state.getPathToScreenShot() + "/invitee_in_sharedproject.png"));
        alice.basic
            .captureScreenshot((alice.state.getPathToScreenShot() + "/inviter_in_sharedproject.png"));
        log.trace("inviter.setTextInClass");
        alice.editor.setTextInJavaEditorWithSave(CP1, PROJECT1, PKG1, CLS1);

        log.trace("invitee.openFile");
        bob.pEV.openFile(getClassNodes(PROJECT1, PKG1, CLS1));

        // invitee.sleep(2000);
        assertTrue(bob.state.isParticipant(bob.jid));
        assertTrue(bob.state.isObserver(bob.jid));
        assertTrue(bob.state.isParticipant(alice.jid));
        assertTrue(bob.state.isDriver(alice.jid));

        assertTrue(alice.state.isParticipant(alice.jid));
        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(alice.state.isParticipant(bob.jid));
        assertTrue(alice.state.isObserver(bob.jid));

        alice.leaveSessionFirstByPeers(bob);
        assertFalse(bob.state.isParticipant(bob.jid));
        assertFalse(alice.state.isParticipant(alice.jid));
    }
}
