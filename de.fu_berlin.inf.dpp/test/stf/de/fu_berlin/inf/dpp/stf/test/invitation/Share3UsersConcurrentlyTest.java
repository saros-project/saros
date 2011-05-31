package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Constants;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class Share3UsersConcurrentlyTest extends StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(ALICE, BOB, CARL);
        setUpWorkbench();
        setUpSaros();
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);
        Util.buildSessionConcurrently(Constants.PROJECT1,
            TypeOfCreateProject.NEW_PROJECT, ALICE, BOB, CARL);
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice share project with BOB and CARL concurrently.</li>
     * <li>Alice and BOB leave the session.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Alice, Bob and Carl are participants and have
     * {@link User.Permission#WRITE_ACCESS}.</li>
     * <li>Alice, Bob and Carl have no {@link User.Permission}s after leaving
     * the session.</li>
     * </ol>
     * 
     * @throws InterruptedException
     */
    @Test
    public void testShareProjectConcurrently() throws RemoteException,
        InterruptedException {

        assertTrue(CARL.superBot().views().sarosView().isInSession());
        assertFalse(ALICE.superBot().views().sarosView()
            .selectParticipant(CARL.getJID()).hasReadOnlyAccess());
        assertTrue(ALICE.superBot().views().sarosView()
            .selectParticipant(CARL.getJID()).hasWriteAccess());

        assertTrue(BOB.superBot().views().sarosView().isInSession());
        assertFalse(ALICE.superBot().views().sarosView()
            .selectParticipant(BOB.getJID()).hasReadOnlyAccess());
        assertTrue(ALICE.superBot().views().sarosView()
            .selectParticipant(BOB.getJID()).hasWriteAccess());

        assertTrue(ALICE.superBot().views().sarosView().isInSession());

        leaveSessionPeersFirst();

        assertFalse(CARL.superBot().views().sarosView().isInSession());

        assertFalse(BOB.superBot().views().sarosView().isInSession());

        assertFalse(ALICE.superBot().views().sarosView().isInSession());

    }
}
