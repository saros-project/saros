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
import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class InviteBuddiesByAliceBobCarlTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB, CARL);
        setUpWorkbench();
        setUpSaros();
    }

    @Override
    @After
    public void after() throws RemoteException {
        announceTestCaseEnd();
        leaveSessionHostFirst(ALICE);
        // TODO remove this code, because it is a workaround
        // against a bug in Saros Session Management

        for (AbstractTester tester : getCurrentTesters()) {
            tester.remoteBot().sleep(1000);
            if (tester.remoteBot().isShellOpen("Synchronizing")) {
                IRemoteBotShell sync = tester.remoteBot()
                    .shell("Synchronizing");

                sync.confirm("Cancel");
                tester.remoteBot().sleep(1000);
                if (tester.remoteBot().isShellOpen("Problem Occurred")) {
                    IRemoteBotShell problem = tester.remoteBot().shell(
                        "Problem Occurred");
                    problem.confirm("OK");
                }
            }

        }
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
        Util.setUpSessionWithAJavaProjectAndAClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB);
        assertFalse(CARL.superBot().views().sarosView().isInSession());
        ALICE.superBot().views().sarosView().selectBuddy(CARL.getJID())
            .addToSarosSession();
        CARL.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);
        CARL.superBot().views().sarosView().waitUntilIsInSession();
        assertTrue(ALICE.superBot().views().sarosView().isInSession());
        assertTrue(BOB.superBot().views().sarosView().isInSession());
        assertTrue(CARL.superBot().views().sarosView().isInSession());

    }
}
