package de.fu_berlin.inf.dpp.stf.client.test.testcases.rolesAndFollowmode;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestFollowMode {
    private static final String CLS1 = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;
    private static final String CLS3 = BotConfiguration.CLASSNAME3;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String PROJECT = BotConfiguration.PROJECTNAME;

    protected static Musician alice;
    protected static Musician bob;

    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS1);
        alice.buildSessionSequential(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        if (bob.state.isFollowing())
            bob.sessionV.clickCMStopFollowingThisUserInSPSView(alice.state,
                alice.jid);
        if (alice.state.isFollowing())
            alice.sessionV.clickCMStopFollowingThisUserInSPSView(bob.state,
                bob.jid);
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @Test
    public void testBobFollowAlice() throws IOException, CoreException {
        alice.bot.setTextInJavaEditorWithSave(BotConfiguration.CONTENTPATH,
            PROJECT, PKG, CLS1);
        bob.bot.followUser(alice.state, alice.jid);
        bob.bot.waitUntilJavaEditorActive(CLS1);
        assertTrue(bob.state.isFollowing());
        assertTrue(bob.bot.isJavaEditorActive(CLS1));

        String clsContentOfAlice = alice.bot
            .getClassContent(PROJECT, PKG, CLS1);

        bob.bot.waitUntilClassContentsSame(PROJECT, PKG, CLS1,
            clsContentOfAlice);
        String clsContentOfBob = bob.bot.getClassContent(PROJECT, PKG, CLS1);
        assertTrue(clsContentOfBob.equals(clsContentOfAlice));

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
        alice.bot.setTextInJavaEditorWithSave(BotConfiguration.CONTENTPATH3,
            PROJECT, PKG, CLS3);
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
