package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestShare3UsersConcurrently extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestShare3UsersConcurrently.class);

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
    public static void initMusicians() throws RemoteException,
        InterruptedException {
        List<Musician> musicians = InitMusician.initAliceBobCarlConcurrently();
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
    }

    /**
     * Closes all opened xmppConnects, popup windows and editor.<br/>
     * Delete all existed projects.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        carl.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * Closes all opened popup windows and editor.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
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
     * <li>Alice has the Role as participant and driver, bob has the role as
     * participant and observer</li>
     * <li>Alice and bob have no Role after leaving the session.</li>
     * </ol>
     * 
     * @throws InterruptedException
     */
    @Test
    public void testShareProjectConcurrently() throws RemoteException,
        InterruptedException {
        alice.buildSessionConcurrently(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
            bob, carl);
        assertTrue(carl.sessionV.isParticipant());
        assertTrue(carl.sessionV.isObserver());
        assertFalse(carl.sessionV.isDriver());

        assertTrue(bob.sessionV.isParticipant());
        assertTrue(bob.sessionV.isObserver());
        assertFalse(bob.sessionV.isDriver());

        assertTrue(alice.sessionV.isParticipant());
        assertFalse(alice.sessionV.isObserver());
        assertTrue(alice.sessionV.isDriver());

        alice.leaveSessionFirstByPeers(bob, carl);

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
