package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.permissionsAndFollowmode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestWriteAccessChangeAndImmediateWrite extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
    }

    /**
     * Steps:
     * 
     * 1. alice restrict to read only access.
     * 
     * 2. bob try to create inconsistency (set Text)
     * 
     * 3. alice grants write access to bob
     * 
     * 4. bob immediately begins to write it.
     * 
     * Expected Results:
     * 
     * 2. inconsistency should occur by bob.
     * 
     * 4. no inconsistency occur by bob.
     * 
     */
    @Test
    public void testFollowModeByOpenClassbyAlice() throws RemoteException {

        alice.superBot().views().sarosView().selectParticipant(bob.getJID())
            .restrictToReadOnlyAccess();
        bob.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        bob.remoteBot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        bob.superBot().views().sarosView().waitUntilIsInconsistencyDetected();

        assertTrue(bob.remoteBot().view(VIEW_SAROS)
            .toolbarButtonWithRegex(TB_INCONSISTENCY_DETECTED + ".*")
            .isEnabled());
        bob.superBot().views().sarosView().inconsistencyDetected();

        alice.superBot().views().sarosView().selectParticipant(bob.getJID())
            .grantWriteAccess();
        bob.remoteBot().editor(CLS1_SUFFIX).setTextWithoutSave(CP2);
        bob.remoteBot().sleep(5000);
        assertFalse(bob.remoteBot().view(VIEW_SAROS)
            .toolbarButton(TB_NO_INCONSISTENCIES).isEnabled());
    }
}
