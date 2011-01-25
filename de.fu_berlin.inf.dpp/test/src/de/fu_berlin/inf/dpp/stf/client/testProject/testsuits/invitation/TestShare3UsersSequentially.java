package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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

    @AfterClass
    public static void runAfterClass() {
        //
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() {
        //
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
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.buildSessionDoneSequentially(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            carl, bob);

        assertTrue(carl.sessionV.isParticipant());
        assertFalse(carl.sessionV.hasReadOnlyAccess());
        assertTrue(carl.sessionV.hasWriteAccess());

        assertTrue(bob.sessionV.isParticipant());
        assertFalse(bob.sessionV.hasReadOnlyAccess());
        assertTrue(bob.sessionV.hasWriteAccess());

        assertTrue(alice.sessionV.isParticipant());
        assertFalse(alice.sessionV.hasReadOnlyAccess());
        assertTrue(alice.sessionV.hasWriteAccess());

        alice.leaveSessionPeersFirstDone(carl, bob);

        assertFalse(carl.sessionV.isParticipant());
        assertFalse(carl.sessionV.hasReadOnlyAccess());
        assertFalse(carl.sessionV.hasWriteAccess());

        assertFalse(bob.sessionV.isParticipant());
        assertFalse(bob.sessionV.hasReadOnlyAccess());
        assertFalse(bob.sessionV.hasWriteAccess());

        assertFalse(alice.sessionV.isParticipant());
        assertFalse(alice.sessionV.hasReadOnlyAccess());
        assertFalse(alice.sessionV.hasWriteAccess());

    }
}
