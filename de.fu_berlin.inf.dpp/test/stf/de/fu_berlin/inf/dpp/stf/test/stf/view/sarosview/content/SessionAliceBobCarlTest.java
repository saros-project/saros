package de.fu_berlin.inf.dpp.stf.test.stf.view.sarosview.content;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class SessionAliceBobCarlTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB, CARL);
        setUpWorkbench();
        setUpSaros();
        Util.setUpSessionWithAJavaProjectAndAClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        Util.reBuildSession(Constants.PROJECT1, ALICE, BOB);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        Util.resetWriteAccess(ALICE, BOB);
        Util.resetFollowModeSequentially(ALICE, BOB);
    }

    @Test
    public void inviteUsersInSession() throws RemoteException {
        BOB.superBot().views().sarosView().leaveSession();

        BOB.remoteBot().sleep(10000);
        try {
            BOB.superBot().views().packageExplorerView()
                .selectJavaProject(Constants.PROJECT1).delete();
        } catch (Exception e) {
            BOB.remoteBot().sleep(60000);
            fail();

        }
        assertFalse(BOB.superBot().views().sarosView().isInSession());
        ALICE.superBot().views().sarosView().selectSession()
            .addBuddies(BOB.getBaseJid(), CARL.getBaseJid());
        BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);
        CARL.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);
        assertTrue(CARL.superBot().views().sarosView().isInSession());
        assertTrue(BOB.superBot().views().sarosView().isInSession());
    }

}
