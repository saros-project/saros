package de.fu_berlin.inf.dpp.stf.test;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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

    public static class MusicianConfiguration {
        public MusicianConfiguration(String jidString, String password,
            String host, int port) {
            this.jidString = jidString;
            this.password = password;
            this.host = host;
            this.port = port;
        }

        public String jidString;
        public String password;
        public String host;
        public int port;
    }

    public static List<Musician> initAliceBobCarlConcurrently()
        throws InterruptedException {
        // pool = Executors.newFixedThreadPool(3);
        List<Callable<Musician>> initTasks = new ArrayList<Callable<Musician>>();
        initTasks.add(newAliceCallable());
        initTasks.add(newBobCallable());
        initTasks.add(newCarlCallable());
        return MakeOperationConcurrently.workAll(initTasks);

    }

    public static Callable<Musician> newAliceCallable() {
        return new Callable<Musician>() {
            public Musician call() throws Exception {
                return newAlice();
            }
        };
    }

    public static Callable<Musician> newBobCallable() {
        return new Callable<Musician>() {
            public Musician call() throws Exception {
                return newBob();
            }
        };
    }

    public static Callable<Musician> newCarlCallable() {
        return new Callable<Musician>() {
            public Musician call() throws Exception {
                return newCarl();
            }
        };
    }

}
