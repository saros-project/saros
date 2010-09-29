package de.fu_berlin.inf.dpp.stf.test.chatview;

import static org.junit.Assert.assertTrue;

import java.rmi.AccessException;
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

    static Musician alice;
    static Musician bob;

    @BeforeClass
    public static void configureAlice() throws AccessException,
        RemoteException, NotBoundException {
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        alice.initBot();
        alice.bot.newJavaProject(BotConfiguration.PROJECTNAME);

        alice.bot.newClass(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
    }

    @BeforeClass
    public static void configureBob() throws AccessException, RemoteException,
        NotBoundException {
        bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bob.initBot();
        if (bob.bot.isJavaProjectExist(BotConfiguration.PROJECTNAME))
            bob.bot.deleteProject(BotConfiguration.PROJECTNAME);
        bob.bot.xmppConnect(bob.jid, bob.password);
    }

    @After
    public void cleanUp() throws RemoteException {
        bob.bot.xmppDisconnect();
        bob.bot.deleteProject(BotConfiguration.PROJECTNAME);
        alice.bot.xmppDisconnect();
        alice.bot.deleteProject(BotConfiguration.PROJECTNAME);
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @Before
    public void shareProjekt() throws RemoteException {
        alice.buildSessionSequential(BotConfiguration.PROJECTNAME,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    }

    @Test
    public void testChat() throws RemoteException {
        alice.bot.openChatView();
        assertTrue(alice.bot.compareChatMessage("You", "joined the chat."));
        alice.bot.sendChatMessage(message);
        bob.bot.openChatView();
        // wait until the chat message arrived
        bob.bot.sleep(1000);
        assertTrue(bob.bot.compareChatMessage(alice.getName(), message));

    }
}
