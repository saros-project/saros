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
        alice.eclipseMainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.eclipseEditor.closejavaEditorWithoutSave(CLS);

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
        bob.bot.resetSaros();
        carl.bot.resetSaros();
        dave.bot.resetSaros();
        alice.bot.resetSaros();
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
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        dave.bot.resetWorkbench();
        alice.bot.resetWorkbench();

    }

    @Test
    public void testFollowModeByOpenClassbyAlice() throws RemoteException {
        assertFalse(bob.eclipseEditor.isClassOpen(CLS));
        assertFalse(carl.eclipseEditor.isClassOpen(CLS));
        assertFalse(dave.eclipseEditor.isClassOpen(CLS));

        alice.packageExplorerV.openClass(PROJECT, PKG, CLS);
        bob.eclipseEditor.waitUntilJavaEditorOpen(CLS);
        carl.eclipseEditor.waitUntilJavaEditorOpen(CLS);
        dave.eclipseEditor.waitUntilJavaEditorOpen(CLS);

        assertTrue(bob.eclipseEditor.isClassOpen(CLS));
        assertTrue(carl.eclipseEditor.isClassOpen(CLS));
        assertTrue(dave.eclipseEditor.isClassOpen(CLS));

    }

    /**
     * TODO it takes a lot of time to run the test, codes need to be optimized.
     * 
     * @throws RemoteException
     */
    @Test
    public void testFollowModeByEditingClassByAlice() throws RemoteException {
        alice.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        String dirtyClsContentOfAlice = alice.eclipseEditor
            .getTextOfJavaEditor(PROJECT, PKG, CLS);

        bob.eclipseEditor.waitUntilJavaEditorContentSame(
            dirtyClsContentOfAlice, PROJECT, PKG, CLS);
        assertTrue(bob.eclipseEditor.isJavaEditorActive(CLS));
        assertTrue(bob.eclipseEditor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertTrue(bob.eclipseEditor.getTextOfJavaEditor(PROJECT, PKG, CLS)
            .equals(dirtyClsContentOfAlice));

        carl.eclipseEditor.waitUntilJavaEditorContentSame(
            dirtyClsContentOfAlice, PROJECT, PKG, CLS);
        assertTrue(carl.eclipseEditor.isJavaEditorActive(CLS));
        assertTrue(carl.eclipseEditor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertTrue(carl.eclipseEditor.getTextOfJavaEditor(PROJECT, PKG, CLS)
            .equals(dirtyClsContentOfAlice));

        dave.eclipseEditor.waitUntilJavaEditorContentSame(
            dirtyClsContentOfAlice, PROJECT, PKG, CLS);
        assertTrue(dave.eclipseEditor.isJavaEditorActive(CLS));
        assertTrue(dave.eclipseEditor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertTrue(dave.eclipseEditor.getTextOfJavaEditor(PROJECT, PKG, CLS)
            .equals(dirtyClsContentOfAlice));
        bob.eclipseEditor.closeJavaEditorWithSave(CLS);
        carl.eclipseEditor.closeJavaEditorWithSave(CLS);
        dave.eclipseEditor.closeJavaEditorWithSave(CLS);
        alice.eclipseEditor.closeJavaEditorWithSave(CLS);
    }

    @Test
    public void testFollowModeByClosingEditorByAlice() throws IOException,
        CoreException {
        alice.packageExplorerV.openClass(PROJECT, PKG, CLS);
        bob.eclipseEditor.waitUntilJavaEditorOpen(CLS);
        carl.eclipseEditor.waitUntilJavaEditorOpen(CLS);
        dave.eclipseEditor.waitUntilJavaEditorOpen(CLS);
        alice.bot.setTextInJavaEditorWithoutSave(CP_CHANGE, PROJECT, PKG, CLS);

        alice.eclipseEditor.closeJavaEditorWithSave(CLS);
        String clsContentOfAlice = alice.eclipseState.getClassContent(PROJECT,
            PKG, CLS);
        bob.eclipseEditor.waitUntilJavaEditorClosed(CLS);
        assertFalse(bob.eclipseEditor.isClassOpen(CLS));
        carl.eclipseEditor.waitUntilJavaEditorClosed(CLS);
        assertFalse(carl.eclipseEditor.isClassOpen(CLS));
        dave.eclipseEditor.waitUntilJavaEditorClosed(CLS);
        assertFalse(dave.eclipseEditor.isClassOpen(CLS));
        assertTrue(bob.eclipseState.getClassContent(PROJECT, PKG, CLS).equals(
            clsContentOfAlice));
        assertTrue(carl.eclipseState.getClassContent(PROJECT, PKG, CLS).equals(
            clsContentOfAlice));
        assertTrue(bob.eclipseState.getClassContent(PROJECT, PKG, CLS).equals(
            clsContentOfAlice));
    }
}
