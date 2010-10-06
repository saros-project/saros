package de.fu_berlin.inf.dpp.stf.test.chatview;

import static org.junit.Assert.assertTrue;

import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.junit.AfterClass;
import org.junit.Before;
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
 * Users have joined a shared project session Steps: 1. Switch to ChatView 2.
 * User A sends IM to user B (both users should have beep button active by
 * default)
 * 
 * Result: 1. The chat should have automatically connected. You should see
 * "[You (xx:xx)]: has joined chat" in the ChatView and perhaps the log of the
 * MUC if someone is already in the chatroom.
 * 
 * 2. Only user B hears a beep when receiving IM.
 * 
 * TODO: replace bob.sleep() by a better condition and do we need the
 * comperator.class for other tests?
 */

public class TestChatViewFunctions {

    String message = "Hello Bob";

    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String PROJECT = BotConfiguration.PROJECTNAME;

    protected static Musician alice = InitMusician.newAlice();
    protected static Musician bob = InitMusician.newBob();

    @BeforeClass
    public static void configureAlice() throws AccessException, RemoteException {
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
    }

    @AfterClass
    public static void afterClass() throws RemoteException {
        bob.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @Before
    public void shareProjekt() throws RemoteException {
        alice.buildSessionSequential(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    }

    @Test
    public void testChat() throws RemoteException {
        assertTrue(alice.bot.compareChatMessage("You", "joined the chat."));
        alice.bot.sendChatMessage(message);
        bob.bot.sleep(1000);
        assertTrue(bob.bot.compareChatMessage(alice.getName(), message));

    }
}
