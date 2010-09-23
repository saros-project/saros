package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

public class TestShareProjectUsingExistingProject {
    // bots
    protected static Musician alice;
    protected static Musician bob;

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
    public static void configureAlice() throws RemoteException,
        NotBoundException {
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        alice.initRmi();
        alice.initBot();

    }

    @BeforeClass
    public static void configureBob() throws RemoteException, NotBoundException {
        bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bob.initRmi();
        bob.initBot();
    }

    @Before
    public void setUpAlice() throws RemoteException {
        alice.newProjectWithClass(PROJECT, PKG, CLS);
    }

    @Before
    public void setUpBob() throws RemoteException {
        bob.newProjectWithClass(PROJECT, PKG, CLS2);
    }

    @After
    public void cleanupBob() throws RemoteException {
        bob.leaveSession();
        bob.bot.deleteResource(PROJECT);
    }

    @After
    public void cleanupAlice() throws RemoteException {
        alice.leaveSession();
        alice.bot.deleteResource(PROJECT);
    }

    @Test
    public void testShareProjectUsingExistingProject() throws RemoteException {
        assertTrue(bob.bot.isFileExist(CLS2_PATH_IN_PROJECT));
        alice.buildSession(bob, PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT,
            SarosConstant.USE_EXISTING_PROJECT);
        assertFalse(bob.bot.isFileExist(CLS2_PATH_IN_PROJECT));
        assertFalse(bob.bot.isJavaProjectExist(PROJECT2));

    }

    @Test
    public void testShareProjectUsingExistingProjectWithCancelLocalChange()
        throws RemoteException {
        alice.buildSession(bob, PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT,
            SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE);
        assertTrue(bob.bot
            .isShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
        bob.bot
            .confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(PROJECT);

        assertTrue(bob.bot.isJavaProjectExist(PROJECT));
        assertTrue(bob.bot.isFileExist(CLS2_PATH_IN_PROJECT));
        assertTrue(bob.bot.isJavaProjectExist(PROJECT2));
        assertTrue(bob.bot.isFileExist(CLS_PATH_IN_PROJECT2));
        bob.bot.deleteResource(PROJECT2);

    }

    @Test
    public void testShareProjectUsingExistingProjectWithCopy()
        throws RemoteException {
        alice.buildSession(bob, BotConfiguration.PROJECTNAME,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT,
            SarosConstant.USE_EXISTING_PROJECT_WITH_COPY);
        assertTrue(bob.bot.isJavaProjectExist(PROJECT));
        assertTrue(bob.bot.isFileExist(CLS2_PATH_IN_PROJECT));
        assertTrue(bob.bot.isJavaProjectExist(PROJECT2));
        assertTrue(bob.bot.isFileExist(CLS_PATH_IN_PROJECT2));
        bob.bot.deleteResource(PROJECT2);

    }
}
