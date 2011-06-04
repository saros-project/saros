package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.ACCEPT;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SESSION_INVITATION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Constants;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class Share3UsersLeavingSessionTest extends StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB, CARL);
        setUpWorkbench();
        setUpSaros();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice share project with Bob.</li>
     * <li>Alice invites Carl.
     * <li>Alice and Bob leave the session.</li>
     * <li>Carl accepts the session.
     * </ol>
     * 
     * Result: Alice, Bob and Carl are not in a session.
     * 
     */
    @Test
    public void testShare3UsersLeavingSession() throws RemoteException {
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);
        Util.buildSessionSequentially(Constants.PROJECT1,
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB);

        ALICE.superBot().views().packageExplorerView()
            .selectJavaProject("MyProject").copy();

        assertTrue(BOB.superBot().views().sarosView().isInSession());
        assertTrue(ALICE.superBot().views().sarosView().isInSession());

        ALICE.superBot().views().sarosView().selectSession()
            .addBuddies(CARL.getBaseJid());

        ALICE.remoteBot().sleep(1000);

        ALICE.superBot().views().sarosView().selectSession().stopSarosSession();

        ALICE.remoteBot().sleep(1000);
        ALICE.superBot().views().sarosView().disconnect();
        BOB.superBot().views().sarosView().disconnect();

        CARL.remoteBot().shell(SHELL_SESSION_INVITATION).confirm(ACCEPT);

        CARL.remoteBot().sleep(2000);

        assertFalse(CARL + " is in a closed session", CARL.superBot().views()
            .sarosView().isInSession());
        assertFalse(ALICE + " is in a closed session", ALICE.superBot().views()
            .sarosView().isInSession());
        assertFalse(BOB + " is in a closed session", BOB.superBot().views()
            .sarosView().isInSession());

    }
}
