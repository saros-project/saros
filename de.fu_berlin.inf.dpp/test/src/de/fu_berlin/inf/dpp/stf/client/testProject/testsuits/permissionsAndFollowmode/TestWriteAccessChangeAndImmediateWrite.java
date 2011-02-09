package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.permissionsAndFollowmode;

import static org.junit.Assert.assertFalse;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestWriteAccessChangeAndImmediateWrite extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestWriteAccessChangeAndImmediateWrite.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
    }

    /**
     * Steps:
     * 
     * 1. alice grants write access to bob
     * 
     * 2. bob immediately begins to write it.
     * 
     * Expected Results:
     * 
     * 2. No inconsistency should occur.
     * 
     */
    @Test
    public void testFollowModeByOpenClassbyAlice() throws RemoteException {

        bob.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);
        bob.workbench.sleep(5000);
        assertFalse(bob.toolbarButton.isToolbarButtonInViewEnabled(
            VIEW_SAROS_SESSION, TB_INCONSISTENCY_DETECTED));
        alice.editor.setTextInJavaEditorWithoutSave(CP1_CHANGE, PROJECT1, PKG1,
            CLS1);
        bob.workbench.sleep(5000);
        assertFalse(bob.toolbarButton.isToolbarButtonInViewEnabled(
            VIEW_SAROS_SESSION, TB_INCONSISTENCY_DETECTED));

    }
}
