package de.fu_berlin.inf.dpp.stf.test;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class TestSVN {
    protected static Musician alice;
    protected static Musician bob;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        alice = initBot(BotConfiguration.JID_ALICE,
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);

        bob = initBot(BotConfiguration.JID_BOB, BotConfiguration.PASSWORD_BOB,
            BotConfiguration.HOST_BOB, BotConfiguration.PORT_BOB);
    }

    private static Musician initBot(String jid, String password, String host,
        int port) throws RemoteException, NotBoundException, AccessException {
        Musician bot = new Musician(new JID(jid), password, host, port);
        bot.initRmi();

        if (bot.isViewOpen("Welcome"))
            bot.closeViewByTitle("Welcome");

        bot.xmppConnect();
        bot.waitForConnect();
        return bot;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        alice.xmppDisconnect();
        bob.xmppDisconnect();
    }

    @Before
    public void setUp() throws Exception {
        // Make sure Alice has project "test" which is connected to SVN.
        // Make sure Alice is switched to trunk.
        // Make sure there is a file "src/main/Main.java" in the project.
        // Make sure Bob has no project named "test".
    }

    @Test
    public void testCheckout() {
        // Alice shares project "test" with VCS support with Bob.
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Make sure Bob has checked out project test from SVN.
    }

    @Test
    public void testSwitch() {
        // Alice shares project "test" with VCS support with Bob
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Alice switches to branch "testing".
        // Make sure Bob is switched to branch "testing".
    }

    @Test
    public void testDisconnect() {
        // Alice shares project "test" with VCS support with Bob
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Alice disconnects project "test" from SVN.
        // Make sure Bob is disconnected.
    }

    @Test
    public void testConnect() {
        // Alice shares project "test" with VCS support with Bob
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Alice disconnects project "test" from SVN.
        // Make sure both Alice and Bob are disconnected from SVN.
        // Alice connects project "test" to SVN.
        // Make sure both Alice and Bob are connected to SVN.
    }

    @Test
    public void testUpdate() {
        // Alice shares project "test" with VCS support with Bob
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Alice updates the entire project to the older revision Y (< HEAD).
        // Make sure Bob's revision of "test" is Y.
    }

    @Test
    public void testUpdateSingleFile() {
        // Alice shares project "test" with VCS support with Bob
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Alice updates the file Main.java to the older revision Y (< HEAD).
        // Make sure Bob's revision of file "src/main/Main.java" is Y.
        // Make sure Bob's revision of project "test" is HEAD.
    }

    @Test
    public void testRevert() {
        // Alice shares project "test" with VCS support with Bob
        // Bob accepts invitation, new project "test".
        // Make sure Bob has joined the session.
        // Alice deletes the file Main.java.
        // Make sure Bob has no file Main.java.
        // Alice reverts the project "test".
        // Make sure Bob has the file Main.java.
    }
}
