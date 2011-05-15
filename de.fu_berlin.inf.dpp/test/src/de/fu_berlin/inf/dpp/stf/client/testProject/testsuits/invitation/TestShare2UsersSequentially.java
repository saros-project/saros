package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestShare2UsersSequentially extends STFTest {

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
        setUpWorkbench();
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
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        buildSessionSequentially(PROJECT1, TypeOfCreateProject.NEW_PROJECT,
            alice, bob);
        bob.remoteBot()
            .captureScreenshot(
                (bob.remoteBot().getPathToScreenShot() + "/invitee_in_sharedproject.png"));
        alice
            .remoteBot()
            .captureScreenshot(
                (alice.remoteBot().getPathToScreenShot() + "/inviter_in_sharedproject.png"));

        assertTrue(bob.superBot().views().sarosView().isInSession());
        assertTrue(alice.superBot().views().sarosView().isInSession());

        assertFalse(alice.superBot().views().sarosView()
            .selectParticipant(bob.getJID()).hasReadOnlyAccess());

        assertTrue(alice.superBot().views().sarosView()
            .selectParticipant(bob.getJID()).hasWriteAccess());

        leaveSessionPeersFirst();

        assertFalse(bob.superBot().views().sarosView().isInSession());
        assertFalse(alice.superBot().views().sarosView().isInSession());

    }
}
