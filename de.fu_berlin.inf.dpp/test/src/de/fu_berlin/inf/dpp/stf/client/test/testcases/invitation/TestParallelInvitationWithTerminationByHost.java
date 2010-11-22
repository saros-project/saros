package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestParallelInvitationWithTerminationByHost extends STFTest {

    @BeforeClass
    public static void initMusicians() throws AccessException, RemoteException,
        InterruptedException {
        // initialize the musicians simultaneously
        List<Musician> musicians = InitMusician.initAliceBobCarlConcurrently();
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
    }

    /**
     * makes sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted. if you need
     * some more after class condition for your tests, please add it.
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
     * make sure,all opened popup windows and editor should be closed. if you
     * need some more after condition for your tests, please add it.
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
     * <li>Alice invites Bob and Carl simultaneously.</li>
     * <li>Carl accepts the invitation but does not choose a target project.</li>
     * <li>Alice opens the Progress View and cancels Bob's invitation before Bob
     * accepts.</li>
     * <li>Alice opens the Progress View and cancels Carl's invitation before
     * Carl accepts.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob is notified of Alice's canceling the invitation.</li>
     * <li>Carl is notified of Alice's canceling the invitation.</li>
     * <li>Carl and Bob are not in session</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testInvitationWithTerminationByHost() throws RemoteException {
        alice.pEV.shareProject(PROJECT1, bob.getBaseJid(), carl.getBaseJid());
        carl.pEV.confirmFirstPageOfWizardSessionInvitation();

        alice.progressV.cancelInvitation();
        bob.pEV.waitUntilIsWindowInvitationCnacelledActive();
        assertTrue(bob.pEV.isWindowInvitationCancelledActive());
        bob.pEV.confirmWindowInvitationCancelled();
        alice.progressV.removeProgress();

        alice.progressV.cancelInvitation();
        carl.pEV.waitUntilIsWindowInvitationCnacelledActive();
        assertTrue(carl.pEV.isWindowInvitationCancelledActive());
        carl.pEV.confirmWindowInvitationCancelled();
        alice.progressV.removeProgress();

        assertFalse(bob.sessionV.isInSession());
        assertFalse(carl.sessionV.isInSession());

    }
}
