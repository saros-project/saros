package de.fu_berlin.inf.dpp.stf.client.test.testcases.enteringAndExitingSession;

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

public class TestHostAsDriverInvitesBelatedly {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;
    private static final String CP = BotConfiguration.CONTENTPATH;
    private static final String CP_CHANGE = BotConfiguration.CONTENTCHANGEPATH;
    private static final String CP2 = BotConfiguration.CONTENTPATH2;
    private static final String CP2_CHANGE = BotConfiguration.CONTENTCHANGEPATH2;

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
     * <li>All observers enable follow mode</li>
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
            BotConfiguration.PORT_CARL);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);

        alice.eclipseMainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.eclipseMainMenu.newClass(PROJECT, PKG, CLS2);
        bob.eclipseMainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        bob.eclipseMainMenu.newClass(PROJECT, PKG, CLS2);

        /*
         * alice build session with carl and is followed by carl.
         */
        bob.typeOfSharingProject = SarosConstant.USE_EXISTING_PROJECT;
        alice.buildSessionSequential(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, carl);
        alice.followedBy(carl);
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
        bob.bot.resetSaros();
        carl.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @Before
    public void setFollowMode() throws RemoteException, InterruptedException {
        /*
         * bob, carl and dave follow alice.
         */

    }

    /**
     * make sure,all opened pop up windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        alice.bot.resetWorkbench();

    }

    /**
     * Steps:
     * 
     * 1. alice edits the file CLS but don't saves it.
     * 
     * 2. bob edits the file CLS in the project currently used and saves it.
     * 
     * 3. alice edits the file CLS2 but don't saves it.
     * 
     * 4. bob edits the file CLS2 in the project currently used and don't saves
     * it.
     * 
     * 5. alice invites bob.
     * 
     * 6. The question about the changed files at bob is answered with YES.
     * 
     * 7. bob accepts and uses project from test X.
     * 
     * Expected Results:
     * 
     * 7. bob has the same project like host.
     * 
     * @throws CoreException
     * @throws IOException
     */
    @Test
    public void testFollowModeByOpenClassbyAlice() throws IOException,
        CoreException {
        alice.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        bob.bot.setTextInJavaEditorWithSave(CP_CHANGE, PROJECT, PKG, CLS);

        alice.bot.setTextInJavaEditorWithoutSave(CP2, PROJECT, PKG, CLS2);
        bob.bot.setTextInJavaEditorWithoutSave(CP2_CHANGE, PROJECT, PKG, CLS2);

        alice.bot.invitateUser(bob.getBaseJid());

        bob.popupWindow.confirmSessionInvitationWindowStep1();
        bob.popupWindow
            .confirmSessionInvitationWindowStep2UsingExistProject(PROJECT);

        String CLSContentOfAlice = alice.eclipseState.getClassContent(PROJECT,
            PKG, CLS);
        String CLS2ContentOfAlice = alice.eclipseState.getClassContent(PROJECT,
            PKG, CLS2);

        String CLSContentOfBob = bob.eclipseState.getClassContent(PROJECT, PKG,
            CLS);
        String CLS2ContentOfBob = bob.eclipseState.getClassContent(PROJECT,
            PKG, CLS2);

        assertTrue(CLSContentOfAlice.equals(CLSContentOfBob));
        assertTrue(CLS2ContentOfAlice.equals(CLS2ContentOfBob));

    }
}
