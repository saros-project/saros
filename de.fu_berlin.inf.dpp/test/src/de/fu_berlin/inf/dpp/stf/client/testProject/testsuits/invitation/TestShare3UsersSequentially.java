package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestShare3UsersSequentially extends STFTest {
    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
        alice.sarosBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        buildSessionSequentially(PROJECT1, CM_SHARE_PROJECT,
            TypeOfCreateProject.NEW_PROJECT, alice, carl, bob);
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice share project with bob and carl sequentially.</li>
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
    public void testShareProject3UsersSequentially() throws RemoteException,
        InterruptedException {

        assertTrue(carl.sarosBot().views().sessionView().isParticipant());
        assertFalse(carl.sarosBot().views().sessionView().hasReadOnlyAccess());
        assertTrue(carl.sarosBot().views().sessionView().hasWriteAccess());

        assertTrue(bob.sarosBot().views().sessionView().isParticipant());
        assertFalse(bob.sarosBot().views().sessionView().hasReadOnlyAccess());
        assertTrue(bob.sarosBot().views().sessionView().hasWriteAccess());

        assertTrue(alice.sarosBot().views().sessionView().isParticipant());
        assertFalse(alice.sarosBot().views().sessionView().hasReadOnlyAccess());
        assertTrue(alice.sarosBot().views().sessionView().hasWriteAccess());

        leaveSessionPeersFirst();

        assertFalse(carl.sarosBot().views().sessionView().isInSession());
        assertFalse(bob.sarosBot().views().sessionView().isInSession());
        assertFalse(alice.sarosBot().views().sessionView().isInSession());

    }
}
