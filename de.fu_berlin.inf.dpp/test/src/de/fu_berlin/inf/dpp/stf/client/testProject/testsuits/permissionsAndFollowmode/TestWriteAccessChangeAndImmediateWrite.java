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

        alice.sarosBot().views().sessionView().selectBuddy(bob.jid)
            .restrictToReadOnlyAccess();
        bob.sarosBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        bob.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        bob.sarosBot().views().sessionView().waitUntilIsInconsistencyDetected();

        assertTrue(bob.bot().view(VIEW_SAROS_SESSION)
            .toolbarButtonWithRegex(TB_INCONSISTENCY_DETECTED + ".*")
            .isEnabled());
        bob.sarosBot().views().sessionView().inconsistencyDetected();

        alice.sarosBot().views().sessionView().selectBuddy(bob.jid)
            .grantWriteAccess();
        bob.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP2);
        bob.bot().sleep(5000);
        assertFalse(bob.bot().view(VIEW_SAROS_SESSION)
            .toolbarButton(TB_NO_INCONSISTENCIES).isEnabled());
    }
}
