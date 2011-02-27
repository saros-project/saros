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
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
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
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        buildSessionConcurrently(PROJECT1, CM_SHARE_PROJECT,
            TypeOfCreateProject.NEW_PROJECT, alice, bob, carl);
        assertTrue(carl.sarosBot().sessionView().isParticipantNoGUI());
        assertFalse(carl.sarosBot().sessionView().hasReadOnlyAccessNoGUI());
        assertTrue(carl.sarosBot().sessionView().hasWriteAccessNoGUI());

        assertTrue(bob.sarosBot().sessionView().isParticipantNoGUI());
        assertFalse(bob.sarosBot().sessionView().hasReadOnlyAccessNoGUI());
        assertTrue(bob.sarosBot().sessionView().hasWriteAccessNoGUI());

        assertTrue(alice.sarosBot().sessionView().isParticipantNoGUI());
        assertFalse(alice.sarosBot().sessionView().hasReadOnlyAccessNoGUI());
        assertTrue(alice.sarosBot().sessionView().hasWriteAccessNoGUI());

        leaveSessionPeersFirst();

        assertFalse(carl.sarosBot().sessionView().isParticipantNoGUI());
        assertFalse(carl.sarosBot().sessionView().hasReadOnlyAccessNoGUI());
        assertFalse(carl.sarosBot().sessionView().hasWriteAccessNoGUI());

        assertFalse(bob.sarosBot().sessionView().isParticipantNoGUI());
        assertFalse(bob.sarosBot().sessionView().hasReadOnlyAccessNoGUI());
        assertFalse(bob.sarosBot().sessionView().hasWriteAccessNoGUI());

        assertFalse(alice.sarosBot().sessionView().isParticipantNoGUI());
        assertFalse(alice.sarosBot().sessionView().hasReadOnlyAccessNoGUI());
        assertFalse(alice.sarosBot().sessionView().hasWriteAccessNoGUI());
    }
}
