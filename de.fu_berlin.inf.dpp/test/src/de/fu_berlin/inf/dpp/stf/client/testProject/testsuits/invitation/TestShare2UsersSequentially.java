package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestShare2UsersSequentially extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestShare2UsersSequentially.class);

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
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
     * <li>Alice and Bob are participants and have both
     * {@link User.Permission#WRITE_ACCESS}.</li>
     * <li>Alice and bob have no {@link User.Permission}s after leaving the
     * session.</li>
     * </ol>
     * 
     * @throws InterruptedException
     */
    @Test
    public void aliceShareProjectWithBobSequentially() throws RemoteException,
        InterruptedException {
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.buildSessionDoneSequentially(VIEW_PACKAGE_EXPLORER, PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            bob);
        bob.workbench
            .captureScreenshot((bob.workbench.getPathToScreenShot() + "/invitee_in_sharedproject.png"));
        alice.workbench.captureScreenshot((alice.workbench
            .getPathToScreenShot() + "/inviter_in_sharedproject.png"));

        assertTrue(bob.sarosSessionV.isParticipantNoGUI());
        assertTrue(alice.sarosSessionV.isParticipantNoGUI());

        assertFalse(bob.sarosSessionV.hasReadOnlyAccessNoGUI());
        assertFalse(alice.sarosSessionV.hasReadOnlyAccessNoGUI());

        assertTrue(alice.sarosSessionV.hasWriteAccessNoGUI());
        assertTrue(bob.sarosSessionV.hasWriteAccessNoGUI());

        alice.leaveSessionPeersFirstDone(bob);

        assertFalse(bob.sarosSessionV.isParticipantNoGUI());
        assertFalse(alice.sarosSessionV.isParticipantNoGUI());

        assertFalse(bob.sarosSessionV.hasReadOnlyAccessNoGUI());
        assertFalse(alice.sarosSessionV.hasReadOnlyAccessNoGUI());

        assertFalse(alice.sarosSessionV.hasWriteAccessNoGUI());
        assertFalse(bob.sarosSessionV.hasWriteAccessNoGUI());

    }
}
