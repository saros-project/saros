package de.fu_berlin.inf.dpp.stf.test.chatview;

import static org.junit.Assert.assertTrue;

import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

/**
 * Precondition:
 * 
 * 2 Users
 * 
 * Users have joined a shared project session Steps:
 * <ol>
 * <li>Switch to ChatView</li>
 * <li>User A sends IM to user B (both users should have beep button active by
 * default)</li>
 * </ol>
 * 
 * Result:
 * <ol>
 * <li>The chat should have automatically connected. You should see
 * "[You (xx:xx)]: has joined chat" in the ChatView and perhaps the log of the
 * MUC if someone is already in the chatroom.</li>
 * <li>Only user B hears a beep when receiving IM.</li>
 * </ol>
 * 
 * TODO: replace bob.sleep() by a better condition and do we need the
 * comperator.class for other tests?
 */

public class TestChatViewFunctions {

    String message = "Hello Bob";

    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String PROJECT = BotConfiguration.PROJECTNAME;

    protected static Musician alice;
    protected static Musician bob;

    @BeforeClass
    public static void initMusicans() throws AccessException, RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
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
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @Test
    public void testChat() throws RemoteException {
        assertTrue(alice.bot.compareChatMessage("You", "joined the chat."));
        alice.bot.sendChatMessage(message);
        bob.bot.waitUntilGetChatMessage(alice.getName(), message);
        assertTrue(bob.bot.compareChatMessage(alice.getName(), message));

    }
}
