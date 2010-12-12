package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestShare3UsersSequentially extends STFTest {
    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
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
     * <li>Alice has the Role as participant and driver, bob has the role as
     * participant and observer</li>
     * <li>Alice and bob have no Role after leaving the session.</li>
     * </ol>
     * 
     * @throws InterruptedException
     */
    @Test
    public void testShareProject3UsersSequentially() throws RemoteException,
        InterruptedException {
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.buildSessionDoneSequentially(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            carl, bob);

        assertTrue(carl.sessionV.isParticipant());
        assertTrue(carl.sessionV.isObserver());
        assertFalse(carl.sessionV.isDriver());

        assertTrue(bob.sessionV.isParticipant());
        assertTrue(bob.sessionV.isObserver());
        assertFalse(bob.sessionV.isDriver());

        assertTrue(alice.sessionV.isParticipant());
        assertFalse(alice.sessionV.isObserver());
        assertTrue(alice.sessionV.isDriver());

        alice.leaveSessionPeersFirstDone(carl, bob);

        assertFalse(carl.sessionV.isParticipant());
        assertFalse(carl.sessionV.isObserver());
        assertFalse(carl.sessionV.isDriver());

        assertFalse(bob.sessionV.isParticipant());
        assertFalse(bob.sessionV.isObserver());
        assertFalse(bob.sessionV.isDriver());

        assertFalse(alice.sessionV.isParticipant());
        assertFalse(alice.sessionV.isObserver());
        assertFalse(alice.sessionV.isDriver());

    }
}
