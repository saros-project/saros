package de.fu_berlin.inf.dpp.stf.client.test.testcases.rolesAndFollowmode;

import static org.junit.Assert.assertFalse;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestDriverChangeAndImmediateWrite {
    private static final Logger log = Logger
        .getLogger(TestDriverChangeAndImmediateWrite.class);

    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CP = BotConfiguration.CONTENTPATH;
    private static final String CP_CHANGE = BotConfiguration.CONTENTCHANGEPATH;
    private static Musician alice;
    private static Musician bob;

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusican() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        alice.mainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.shareProjectWithDone(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    }

    /**
     * make sure, all opened xmppConnects, pop up windows and editor should be
     * closed.
     * <p>
     * make sure, all existed projects should be deleted.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * make sure,all opened pop up windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
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
        alice.sessionV.giveDriverRole(bob.state);
        bob.editor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        bob.basic.sleep(5000);
        assertFalse(bob.sessionV.isInconsistencyDetectedEnabled());

        alice.editor.setTextInJavaEditorWithoutSave(CP_CHANGE, PROJECT,
            PKG, CLS);

        bob.basic.sleep(5000);
        assertFalse(bob.sessionV.isInconsistencyDetectedEnabled());

    }
}
