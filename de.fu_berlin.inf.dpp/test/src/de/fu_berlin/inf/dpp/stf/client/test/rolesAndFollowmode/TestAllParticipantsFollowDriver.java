package de.fu_berlin.inf.dpp.stf.client.test.rolesAndFollowmode;

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
import de.fu_berlin.inf.dpp.stf.client.test.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.SarosConstant;

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
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.bot.closejavaEditorWithoutSave(CLS);

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
        assertFalse(bob.bot.isClassOpen(CLS));
        assertFalse(carl.bot.isClassOpen(CLS));
        assertFalse(dave.bot.isClassOpen(CLS));

        alice.bot.openClass(PROJECT, PKG, CLS);
        bob.bot.waitUntilJavaEditorOpen(CLS);
        carl.bot.waitUntilJavaEditorOpen(CLS);
        dave.bot.waitUntilJavaEditorOpen(CLS);

        assertTrue(bob.bot.isClassOpen(CLS));
        assertTrue(carl.bot.isClassOpen(CLS));
        assertTrue(dave.bot.isClassOpen(CLS));

    }

    @Test
    public void testFollowModeByEditingClassByAlice() throws RemoteException {
        alice.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        String dirtyClsContentOfAlice = alice.bot.getTextOfJavaEditor(PROJECT,
            PKG, CLS);

        bob.bot.waitUntilJavaEditorContentSame(dirtyClsContentOfAlice, PROJECT,
            PKG, CLS);
        assertTrue(bob.bot.isJavaEditorActive(CLS));
        assertTrue(bob.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertTrue(bob.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
            dirtyClsContentOfAlice));

        carl.bot.waitUntilJavaEditorContentSame(dirtyClsContentOfAlice,
            PROJECT, PKG, CLS);
        assertTrue(carl.bot.isJavaEditorActive(CLS));
        assertTrue(carl.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertTrue(carl.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
            dirtyClsContentOfAlice));

        dave.bot.waitUntilJavaEditorContentSame(dirtyClsContentOfAlice,
            PROJECT, PKG, CLS);
        assertTrue(dave.bot.isJavaEditorActive(CLS));
        assertTrue(dave.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertTrue(dave.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
            dirtyClsContentOfAlice));
        bob.bot.closeJavaEditorWithSave(CLS);
        carl.bot.closeJavaEditorWithSave(CLS);
        dave.bot.closeJavaEditorWithSave(CLS);
        alice.bot.closeJavaEditorWithSave(CLS);
    }

    @Test
    public void testFollowModeByClosingEditorByAlice() throws IOException,
        CoreException {
        alice.bot.openClass(PROJECT, PKG, CLS);
        bob.bot.waitUntilJavaEditorOpen(CLS);
        carl.bot.waitUntilJavaEditorOpen(CLS);
        dave.bot.waitUntilJavaEditorOpen(CLS);
        alice.bot.setTextInJavaEditorWithoutSave(CP_CHANGE, PROJECT, PKG, CLS);

        alice.bot.closeJavaEditorWithSave(CLS);
        String clsContentOfAlice = alice.bot.getClassContent(PROJECT, PKG, CLS);
        bob.bot.waitUntilJavaEditorClosed(CLS);
        assertFalse(bob.bot.isClassOpen(CLS));
        carl.bot.waitUntilJavaEditorClosed(CLS);
        assertFalse(carl.bot.isClassOpen(CLS));
        dave.bot.waitUntilJavaEditorClosed(CLS);
        assertFalse(dave.bot.isClassOpen(CLS));
        assertTrue(bob.bot.getClassContent(PROJECT, PKG, CLS).equals(
            clsContentOfAlice));
        assertTrue(carl.bot.getClassContent(PROJECT, PKG, CLS).equals(
            clsContentOfAlice));
        assertTrue(bob.bot.getClassContent(PROJECT, PKG, CLS).equals(
            clsContentOfAlice));
    }
}
