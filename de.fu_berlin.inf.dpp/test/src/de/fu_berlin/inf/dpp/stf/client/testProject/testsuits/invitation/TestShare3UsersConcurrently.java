package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestShare3UsersConcurrently extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
        alice.sarosBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        buildSessionConcurrently(PROJECT1, CM_SHARE_PROJECT,
            TypeOfCreateProject.NEW_PROJECT, alice, bob, carl);
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice share project with bob and carl concurrently.</li>
     * <li>Alice and bob leave the session.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Alice, Bob and Carl are participants and have
     * {@link User.Permission#WRITE_ACCESS}.</li>
     * <li>Alice, Bob and Carl have no {@link User.Permission}s after leaving
     * the session.</li>
     * </ol>
     * 
     * @throws InterruptedException
     */
    @Test
    public void testShareProjectConcurrently() throws RemoteException,
        InterruptedException {

        assertTrue(carl.sarosBot().views().sessionView().isParticipantNoGUI());
        assertFalse(carl.sarosBot().views().sessionView()
            .hasReadOnlyAccessNoGUI());
        assertTrue(carl.sarosBot().views().sessionView().hasWriteAccessNoGUI());

        assertTrue(bob.sarosBot().views().sessionView().isParticipantNoGUI());
        assertFalse(bob.sarosBot().views().sessionView()
            .hasReadOnlyAccessNoGUI());
        assertTrue(bob.sarosBot().views().sessionView().hasWriteAccessNoGUI());

        assertTrue(alice.sarosBot().views().sessionView().isParticipantNoGUI());
        assertFalse(alice.sarosBot().views().sessionView()
            .hasReadOnlyAccessNoGUI());
        assertTrue(alice.sarosBot().views().sessionView().hasWriteAccessNoGUI());

        leaveSessionPeersFirst();

        assertFalse(carl.sarosBot().views().sessionView().isParticipantNoGUI());
        assertFalse(carl.sarosBot().views().sessionView()
            .hasReadOnlyAccessNoGUI());
        assertFalse(carl.sarosBot().views().sessionView().hasWriteAccessNoGUI());

        assertFalse(bob.sarosBot().views().sessionView().isParticipantNoGUI());
        assertFalse(bob.sarosBot().views().sessionView()
            .hasReadOnlyAccessNoGUI());
        assertFalse(bob.sarosBot().views().sessionView().hasWriteAccessNoGUI());

        assertFalse(alice.sarosBot().views().sessionView().isParticipantNoGUI());
        assertFalse(alice.sarosBot().views().sessionView()
            .hasReadOnlyAccessNoGUI());
        assertFalse(alice.sarosBot().views().sessionView()
            .hasWriteAccessNoGUI());
    }
}
