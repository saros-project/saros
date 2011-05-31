package de.fu_berlin.inf.dpp.stf.test.stf.view.sarosview.content;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Constants;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class BuddiesByAliceBobCarlTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB, CARL);
        setUpWorkbench();
        setUpSaros();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetBuddies();
        resetBuddiesName();
    }

    @Test
    public void testPreCondition() {
        //
    }

    /**
     * Steps:
     * 
     * 1. Alice share session with Bob.
     * 
     * 2. Alice invite Carl.
     * 
     * Result:
     * <ol>
     * <li>Carl is in the session</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Test
    public void inviteBuddy() throws RemoteException, InterruptedException {
        Util.setUpSessionWithAJavaProjectAndAClass(ALICE, BOB);
        assertFalse(CARL.superBot().views().sarosView().isInSession());
        ALICE.superBot().views().sarosView().selectBuddy(CARL.getJID())
            .addToSarosSession();
        CARL.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);
        CARL.superBot().views().sarosView().waitUntilIsInSession();
        assertTrue(CARL.superBot().views().sarosView().isInSession());

    }
}
