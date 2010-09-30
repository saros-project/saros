package de.fu_berlin.inf.dpp.stf.test.rolesAndFollowmode;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestFollowMode {
    private static final String CLS1 = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;
    private static final String CLS3 = BotConfiguration.CLASSNAME3;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String PROJECT = BotConfiguration.PROJECTNAME;

    protected static Musician alice = InitMusician.newAlice();
    protected static Musician bob = InitMusician.newBob();

    @BeforeClass
    public static void configureInvitee() throws RemoteException {
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS1);
        alice.buildSessionSequential(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);

    }

    @AfterClass
    public static void cleanupBob() throws RemoteException {
        bob.bot.resetSaros();
    }

    @AfterClass
    public static void cleanupAlice() throws RemoteException {
        alice.bot.resetSaros();
    }

    @After
    public void StopFollowMode() throws RemoteException {
        if (bob.state.isFollowing())
            bob.bot.clickCMStopFollowingThisUserInSPSView(alice.state,
                alice.jid);
        if (alice.state.isFollowing())
            alice.bot.clickCMStopFollowingThisUserInSPSView(bob.state, bob.jid);
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @Test
    public void testBobFollowAlice() throws RemoteException {
        alice.bot.setTextInJavaEditor(BotConfiguration.CONTENTPATH, PROJECT,
            PKG, CLS1);
        bob.bot.followUser(alice.state, alice.jid);
        bob.bot.waitUntilJavaEditorActive(CLS1);
        assertTrue(bob.state.isFollowing());
        assertTrue(bob.bot.isJavaEditorActive(CLS1));

        String textFromInviter = alice.bot.getTextOfJavaEditor(PROJECT, PKG,
            CLS1);

        bob.bot.waitUntilFileEqualWithFile(PROJECT, PKG, CLS1, textFromInviter);
        // bob.waitUntilFileEqualWithFile(PROJECT, PKG, CLS1, textFromInviter);
        String textFormInvitee = bob.bot
            .getTextOfJavaEditor(PROJECT, PKG, CLS1);
        assertTrue(textFromInviter.equals(textFormInvitee));

        alice.bot.newClass(PROJECT, PKG, CLS2);
        bob.bot.waitUntilJavaEditorActive(CLS2);
        assertTrue(bob.bot.isJavaEditorActive(CLS2));

        alice.bot.followUser(bob.state, bob.jid);
        bob.bot.activateJavaEditor(CLS1);
        alice.bot.waitUntilJavaEditorActive(CLS1);
        assertTrue(alice.state.isFollowing());
        assertTrue(alice.bot.isJavaEditorActive(CLS1));

        bob.bot.followUser(alice.state, alice.jid);
        alice.bot.newClass(PROJECT, PKG, CLS3);
        alice.bot.waitUntilJavaEditorActive(CLS3);
        alice.bot.setTextInJavaEditor(BotConfiguration.CONTENTPATH3, PROJECT,
            PKG, CLS3);
        alice.bot.setBreakPoint(13, PROJECT, PKG, CLS3);
        // alice.debugJavaFile(BotConfiguration.PROJECTNAME,
        // BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME3);
        // bob.waitUntilJavaEditorActive(BotConfiguration.CLASSNAME3);
        // assertFalse(bob.isDebugPerspectiveActive());
        // alice.openJavaPerspective();
        // bob.sleep(1000);
        // int lineFromAlice = alice.getJavaCursorLinePosition(
        // BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
        // BotConfiguration.CLASSNAME3);
        // int lineFromBob = bob.getJavaCursorLinePosition(
        // BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
        // BotConfiguration.CLASSNAME3);
        // assertEquals(lineFromAlice, lineFromBob);
        // alice.waitUntilShellActive("Confirm Perspective Switch");
        // assertTrue(alice.isShellActive("Confirm Perspective Switch"));
    }
}
