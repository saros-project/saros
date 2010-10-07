package de.fu_berlin.inf.dpp.stf.test.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestShareProjectUsingExistingProject {

    private static Musician alice;
    private static Musician bob;

    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;
    private static final String PROJECT2 = BotConfiguration.PROJECTNAME + " 2";

    private static final String CLS2_PATH_IN_PROJECT = PROJECT + "/src/my/pkg/"
        + CLS2 + ".java";
    private static final String CLS_PATH_IN_PROJECT2 = PROJECT2
        + "/src/my/pkg/" + CLS + ".java";

    @BeforeClass
    public static void initMusicians() {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
    }

    @Before
    public void setUpAlice() throws RemoteException {
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
    }

    @Before
    public void setUpBob() throws RemoteException {
        bob.bot.newJavaProjectWithClass(PROJECT, PKG, CLS2);
    }

    @After
    public void cleanUp() throws RemoteException, InterruptedException {
        alice.leaveSessionFirst(bob);
        alice.bot.deleteProject(PROJECT);
        bob.bot.deleteProject(PROJECT);
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @Test
    public void testShareProjectUsingExistingProject() throws RemoteException {
        assertTrue(bob.bot.isResourceExist(CLS2_PATH_IN_PROJECT));
        bob.typeOfSharingProject = SarosConstant.USE_EXISTING_PROJECT;
        alice.buildSessionSequential(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        assertFalse(bob.bot.isResourceExist(CLS2_PATH_IN_PROJECT));
        assertFalse(bob.bot.isJavaProjectExist(PROJECT2));

    }

    @Test
    public void testShareProjectUsingExistingProjectWithCancelLocalChange()
        throws RemoteException {
        bob.typeOfSharingProject = SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE;
        alice.buildSessionSequential(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        assertTrue(bob.bot
            .isShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
        bob.bot
            .confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(PROJECT);

        assertTrue(bob.bot.isJavaProjectExist(PROJECT));
        assertTrue(bob.bot.isResourceExist(CLS2_PATH_IN_PROJECT));
        assertTrue(bob.bot.isJavaProjectExist(PROJECT2));
        assertTrue(bob.bot.isResourceExist(CLS_PATH_IN_PROJECT2));
        bob.bot.deleteProject(PROJECT2);

    }

    @Test
    public void testShareProjectUsingExistingProjectWithCopy()
        throws RemoteException {
        bob.typeOfSharingProject = SarosConstant.USE_EXISTING_PROJECT_WITH_COPY;
        alice.buildSessionSequential(BotConfiguration.PROJECTNAME,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        assertTrue(bob.bot.isJavaProjectExist(PROJECT));
        assertTrue(bob.bot.isResourceExist(CLS2_PATH_IN_PROJECT));
        assertTrue(bob.bot.isJavaProjectExist(PROJECT2));
        assertTrue(bob.bot.isResourceExist(CLS_PATH_IN_PROJECT2));
        bob.bot.deleteProject(PROJECT2);

    }
}
