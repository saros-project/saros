package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
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
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.buildSessionDoneSequentially(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            bob);
        bob.basic
            .captureScreenshot((bob.basic.getPathToScreenShot() + "/invitee_in_sharedproject.png"));
        alice.basic
            .captureScreenshot((alice.basic.getPathToScreenShot() + "/inviter_in_sharedproject.png"));

        assertTrue(bob.sessionV.isParticipant());
        assertTrue(alice.sessionV.isParticipant());

        assertTrue(bob.sessionV.isObserver());
        assertFalse(alice.sessionV.isObserver());

        assertTrue(alice.sessionV.isDriver());
        assertFalse(bob.sessionV.isDriver());

        alice.leaveSessionPeersFirstDone(bob);

        assertFalse(bob.sessionV.isParticipant());
        assertFalse(alice.sessionV.isParticipant());

        assertFalse(bob.sessionV.isObserver());
        assertFalse(alice.sessionV.isObserver());

        assertFalse(alice.sessionV.isDriver());
        assertFalse(bob.sessionV.isDriver());

    }
}
