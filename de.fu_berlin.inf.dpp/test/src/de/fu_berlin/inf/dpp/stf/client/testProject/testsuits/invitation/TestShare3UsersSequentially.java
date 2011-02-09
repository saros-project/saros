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
        setUpWorkbenchs();
        setUpSaros();
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
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        buildSessionSequentially(VIEW_PACKAGE_EXPLORER, PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            alice, carl, bob);

        assertTrue(carl.sarosSessionV.isParticipantNoGUI());
        assertFalse(carl.sarosSessionV.hasReadOnlyAccessNoGUI());
        assertTrue(carl.sarosSessionV.hasWriteAccessNoGUI());

        assertTrue(bob.sarosSessionV.isParticipantNoGUI());
        assertFalse(bob.sarosSessionV.hasReadOnlyAccessNoGUI());
        assertTrue(bob.sarosSessionV.hasWriteAccessNoGUI());

        assertTrue(alice.sarosSessionV.isParticipantNoGUI());
        assertFalse(alice.sarosSessionV.hasReadOnlyAccessNoGUI());
        assertTrue(alice.sarosSessionV.hasWriteAccessNoGUI());

        leaveSessionPeersFirst();

        assertFalse(carl.sarosSessionV.isParticipantNoGUI());
        assertFalse(carl.sarosSessionV.hasReadOnlyAccessNoGUI());
        assertFalse(carl.sarosSessionV.hasWriteAccessNoGUI());

        assertFalse(bob.sarosSessionV.isParticipantNoGUI());
        assertFalse(bob.sarosSessionV.hasReadOnlyAccessNoGUI());
        assertFalse(bob.sarosSessionV.hasWriteAccessNoGUI());

        assertFalse(alice.sarosSessionV.isParticipantNoGUI());
        assertFalse(alice.sarosSessionV.hasReadOnlyAccessNoGUI());
        assertFalse(alice.sarosSessionV.hasWriteAccessNoGUI());

    }
}
