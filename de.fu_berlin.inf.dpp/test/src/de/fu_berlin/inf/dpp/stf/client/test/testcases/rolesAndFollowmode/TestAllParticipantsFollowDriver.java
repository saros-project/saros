package de.fu_berlin.inf.dpp.stf.client.test.testcases.rolesAndFollowmode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestAllParticipantsFollowDriver {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CP = BotConfiguration.CONTENTPATH;
    private static final String CP_CHANGE = BotConfiguration.CONTENTCHANGEPATH;

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;
    private static Musician dave;

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
     * <li>Dave (Observer)</li>
     * <li>All observers enable followmode</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void initMusican() throws AccessException, RemoteException,
        InterruptedException {
        /*
         * initialize the musicians simultaneously
         */
        List<Musician> musicians = InitMusician.initMusiciansConcurrently(
            BotConfiguration.PORT_ALICE, BotConfiguration.PORT_BOB,
            BotConfiguration.PORT_CARL, BotConfiguration.PORT_DAVE);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        dave = musicians.get(3);
        alice.mainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.editor.closejavaEditorWithoutSave(CLS);

        /*
         * build session with bob, carl and dave simultaneously
         */
        alice.buildSessionConcurrently(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob, carl, dave);
        // alice.bot.waitUntilNoInvitationProgress();

    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        carl.workbench.resetSaros();
        dave.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @Before
    public void setFollowMode() throws RemoteException, InterruptedException {
        /*
         * bob, carl and dave follow alice.
         */
        alice.followedBy(bob, carl, dave);
    }

    /**
     * make sure,all opened popup windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        dave.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();

    }

    @Test
    public void testFollowModeByOpenClassbyAlice() throws RemoteException {
        assertFalse(bob.editor.isClassOpen(CLS));
        assertFalse(carl.editor.isClassOpen(CLS));
        assertFalse(dave.editor.isClassOpen(CLS));

        alice.pEV.openClass(PROJECT, PKG, CLS);
        bob.editor.waitUntilJavaEditorOpen(CLS);
        carl.editor.waitUntilJavaEditorOpen(CLS);
        dave.editor.waitUntilJavaEditorOpen(CLS);

        assertTrue(bob.editor.isClassOpen(CLS));
        assertTrue(carl.editor.isClassOpen(CLS));
        assertTrue(dave.editor.isClassOpen(CLS));

    }

    /**
     * TODO it takes a lot of time to run the test, codes need to be optimized.
     * 
     * @throws RemoteException
     */
    @Test
    public void testFollowModeByEditingClassByAlice() throws RemoteException {
        alice.editor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG,
            CLS);
        String dirtyClsContentOfAlice = alice.editor
            .getTextOfJavaEditor(PROJECT, PKG, CLS);

        bob.editor.waitUntilJavaEditorContentSame(
            dirtyClsContentOfAlice, PROJECT, PKG, CLS);
        assertTrue(bob.editor.isJavaEditorActive(CLS));
        assertTrue(bob.editor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertTrue(bob.editor.getTextOfJavaEditor(PROJECT, PKG, CLS)
            .equals(dirtyClsContentOfAlice));

        carl.editor.waitUntilJavaEditorContentSame(
            dirtyClsContentOfAlice, PROJECT, PKG, CLS);
        assertTrue(carl.editor.isJavaEditorActive(CLS));
        assertTrue(carl.editor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertTrue(carl.editor.getTextOfJavaEditor(PROJECT, PKG, CLS)
            .equals(dirtyClsContentOfAlice));

        dave.editor.waitUntilJavaEditorContentSame(
            dirtyClsContentOfAlice, PROJECT, PKG, CLS);
        assertTrue(dave.editor.isJavaEditorActive(CLS));
        assertTrue(dave.editor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertTrue(dave.editor.getTextOfJavaEditor(PROJECT, PKG, CLS)
            .equals(dirtyClsContentOfAlice));
        bob.editor.closeJavaEditorWithSave(CLS);
        carl.editor.closeJavaEditorWithSave(CLS);
        dave.editor.closeJavaEditorWithSave(CLS);
        alice.editor.closeJavaEditorWithSave(CLS);
    }

    @Test
    public void testFollowModeByClosingEditorByAlice() throws IOException,
        CoreException {
        alice.pEV.openClass(PROJECT, PKG, CLS);
        bob.editor.waitUntilJavaEditorOpen(CLS);
        carl.editor.waitUntilJavaEditorOpen(CLS);
        dave.editor.waitUntilJavaEditorOpen(CLS);
        alice.editor.setTextInJavaEditorWithoutSave(CP_CHANGE, PROJECT,
            PKG, CLS);

        alice.editor.closeJavaEditorWithSave(CLS);
        String clsContentOfAlice = alice.state.getClassContent(PROJECT, PKG,
            CLS);
        bob.editor.waitUntilJavaEditorClosed(CLS);
        assertFalse(bob.editor.isClassOpen(CLS));
        carl.editor.waitUntilJavaEditorClosed(CLS);
        assertFalse(carl.editor.isClassOpen(CLS));
        dave.editor.waitUntilJavaEditorClosed(CLS);
        assertFalse(dave.editor.isClassOpen(CLS));
        assertTrue(bob.state.getClassContent(PROJECT, PKG, CLS).equals(
            clsContentOfAlice));
        assertTrue(carl.state.getClassContent(PROJECT, PKG, CLS).equals(
            clsContentOfAlice));
        assertTrue(bob.state.getClassContent(PROJECT, PKG, CLS).equals(
            clsContentOfAlice));
    }
}
