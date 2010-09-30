package de.fu_berlin.inf.dpp.stf.test;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;

public class InitMusician {
    private final static Logger log = Logger.getLogger(InitMusician.class);

    public final static Musician newAlice() {
        log.trace("new alice");
        Musician alice = new Musician(new JID(BotConfiguration.JID_ALICE),
            BotConfiguration.PASSWORD_ALICE, BotConfiguration.HOST_ALICE,
            BotConfiguration.PORT_ALICE);
        try {
            log.trace("initBot");
            alice.initBot();
        } catch (AccessException e) {
            log.debug("", e);
        } catch (RemoteException e) {
            log.debug("", e);
        } catch (NotBoundException e) {
            log.debug("", e);
        }
        try {
            alice.bot.deleteAllProjects();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            log.debug("", e);
        }
        return alice;
    }

    public final static Musician newBob() {
        Musician bob = new Musician(new JID(BotConfiguration.JID_BOB),
            BotConfiguration.PASSWORD_BOB, BotConfiguration.HOST_BOB,
            BotConfiguration.PORT_BOB);
        try {
            bob.initBot();
        } catch (AccessException e) {
            log.debug("", e);
        } catch (RemoteException e) {
            log.debug("", e);
        } catch (NotBoundException e) {
            log.debug("", e);
        }
        try {
            bob.bot.deleteAllProjects();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            log.debug("", e);
        }
        return bob;
    }

    public final static Musician newCarl() {
        Musician carl = new Musician(new JID(BotConfiguration.JID_CARL),
            BotConfiguration.PASSWORD_CARL, BotConfiguration.HOST_CARL,
            BotConfiguration.PORT_CARL);
        try {
            carl.initBot();
        } catch (AccessException e) {
            log.debug("", e);
        } catch (RemoteException e) {
            log.debug("", e);
        } catch (NotBoundException e) {
            log.debug("", e);
        }
        try {
            carl.bot.deleteAllProjects();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            log.debug("", e);
        }
        return carl;
    }

}
