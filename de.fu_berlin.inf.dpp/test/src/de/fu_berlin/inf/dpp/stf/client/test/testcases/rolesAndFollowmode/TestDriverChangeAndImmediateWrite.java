package de.fu_berlin.inf.dpp.stf.client.test.testcases.rolesAndFollowmode;

import static org.junit.Assert.assertFalse;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestDriverChangeAndImmediateWrite extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestDriverChangeAndImmediateWrite.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
        setUpSession(alice, bob);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException {
        resetSaros();
        resetWorkbenches();
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        resetWorkbenches();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetWorkbenches();
    }

    /**
     * Steps:
     * 
     * 1. alice passes driver role to bob
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
        alice.sessionV.giveDriverRoleGUI(bob.sessionV);
        bob.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);
        bob.basic.sleep(5000);
        assertFalse(bob.sessionV.isInconsistencyDetectedEnabled());

        alice.editor.setTextInJavaEditorWithoutSave(CP1_CHANGE, PROJECT1, PKG1,
            CLS1);

        bob.basic.sleep(5000);
        assertFalse(bob.sessionV.isInconsistencyDetectedEnabled());

    }
}
