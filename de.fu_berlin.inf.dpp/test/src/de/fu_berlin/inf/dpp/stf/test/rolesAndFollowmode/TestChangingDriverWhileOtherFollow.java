package de.fu_berlin.inf.dpp.stf.test.rolesAndFollowmode;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestChangingDriverWhileOtherFollow {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CP = BotConfiguration.CONTENTPATH;

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

        /*
         * build session with bob, carl and dave simultaneously
         */
        alice.buildSessionConcurrently(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob, carl, dave);
        // alice.bot.waitUntilNoInvitationProgress();

        alice.followedBy(bob, carl, dave);

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

    /**
     * Steps:
     * <ol>
     * <li>Alice invites everyone else simultaneously.</li>
     * <li>Alice opens the Progress View and cancels Bob's invitation before Bob
     * accepts.</li>
     * <li>Carl accepts the invitation but does not choose a target project.</li>
     * <li>Alice opens the Progress View and cancels Carl's invitation before
     * Carl accepts</li>
     * <li>Dave accepts the invitation and chooses a target project.</li>
     * <li>Alice opens the Progress View and cancels Dave 's invitation during
     * the synchronisation.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li>Bob is notified of Alice's canceling the invitation.</li>
     * <li></li>
     * <li>Carl is notified of Alice's canceling the invitation.</li>
     * <li></li>
     * <li>Dave is notified of Alice's canceling the invitation.</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */

    @Test
    public void testExistDirtyFlagByDaveAndEdnaDuringAlicMakeChange()
        throws IOException, CoreException, InterruptedException {
        alice.bot.giveExclusiveDriverRole(carl.getPlainJid());
        // if (!alice.state.isFollowingUser(carl.getPlainJid()))
        // alice.bot.followUser(carl.state, carl.jid);
        // if (!bob.state.isFollowingUser(carl.getPlainJid()))
        // bob.bot.followUser(carl.state, carl.jid);
        // if (!dave.state.isFollowingUser(carl.getPlainJid()))
        // dave.bot.followUser(carl.state, carl.jid);
        // carl.stopFollowedBy(alice, bob, dave);
        // carl.followedBy(alice, bob, dave);
        carl.bot.closeAllOpenedEditors();
        carl.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        String dirtyClsContentOfCarl = carl.bot.getTextOfJavaEditor(PROJECT,
            PKG, CLS);

        alice.bot.waitUntilEditorContentSame(PROJECT, PKG, CLS,
            dirtyClsContentOfCarl);
        assertTrue(alice.bot.isJavaEditorActive(CLS));
        assertTrue(alice.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));

        bob.bot.waitUntilEditorContentSame(PROJECT, PKG, CLS,
            dirtyClsContentOfCarl);
        assertTrue(bob.bot.isJavaEditorActive(CLS));
        assertTrue(bob.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));

        dave.bot.waitUntilEditorContentSame(PROJECT, PKG, CLS,
            dirtyClsContentOfCarl);
        assertTrue(dave.bot.isJavaEditorActive(CLS));
        assertTrue(dave.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));

        carl.stopFollowedBy(alice, bob, dave);

    }
}
