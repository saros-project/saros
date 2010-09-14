package de.fu_berlin.inf.dpp.stf.test;

import static org.junit.Assert.assertTrue;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

public class TestEditDuringInvitation {
    /**
     * Steps: 1. Alice invites Bob 2. Bob accepts the invitation 3. Alice gives
     * Bob driver capability 4. Alice invites Carl 5. Bob changes data during
     * the running invtiation of Carl.
     * 
     * Expected Results: All changes that Bob has done should be on Carl's side.
     * There should not be an inconsistency.
     */
    // bots
    protected static Musician alice;
    protected static Musician bob;
    protected static Musician carl;

    @BeforeClass
    public static void configureCarl() throws RemoteException,
        NotBoundException {
        carl = new Musician(new JID(BotConfiguration.JID_CARL),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_CARL,
            BotConfiguration.PORT_CARL);
        carl.initBot();
    }

    @BeforeClass
    public static void configureBob() throws RemoteException, NotBoundException {
        bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        bob.initBot();
    }

    @BeforeClass
    public static void configureInviter() throws RemoteException,
        NotBoundException {
        alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        alice.initBot();
        alice.createProjectWithClass(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
    }

    @After
    public void cleanupBob() throws RemoteException {
        bob.xmppDisconnect();
        bob.removeProject(BotConfiguration.PROJECTNAME);
    }

    @After
    public void cleanupCarl() throws RemoteException {
        carl.xmppDisconnect();
        carl.removeProject(BotConfiguration.PROJECTNAME);
    }

    @After
    public void cleanupAlice() throws RemoteException {
        alice.xmppDisconnect();
    }

    @Test
    public void testShareProjectParallel() throws RemoteException {
        bob.waitForConnect();
        carl.waitForConnect();
        alice.waitForConnect();

        alice.buildSession(bob, BotConfiguration.PROJECTNAME,
            SarosConstant.SHARE_PROJECT, SarosConstant.CREATE_NEW_PROJECT);

        alice.giveDriverRole(bob);

        assertTrue(bob.isDriver(alice));

        bob.openFile(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);

        alice.inviteUser(carl, BotConfiguration.PROJECTNAME);

        carl.ackProjectStep1(alice);

        bob.setTextInClass(BotConfiguration.CONTENTPATH,
            BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
            BotConfiguration.CLASSNAME);

        carl.ackProjectStep2UsingNewproject(alice, BotConfiguration.PACKAGENAME);

        String textFromCarl = carl.getTextOfClass(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
        String textFormAlice = alice.getTextOfClass(
            BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
            BotConfiguration.CLASSNAME);
        assertTrue(textFromCarl.equals(textFormAlice));

    }
}
