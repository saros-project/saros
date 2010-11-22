package de.fu_berlin.inf.dpp.stf.client.test.helpers;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.MusicianConfigurationInfos;
import de.fu_berlin.inf.dpp.stf.client.Musician;

public class InitMusician {
    private final static Logger log = Logger.getLogger(InitMusician.class);

    public final static Musician newAlice() {
        log.trace("new alice");
        Musician alice = new Musician(new JID(MusicianConfigurationInfos.JID_ALICE),
            MusicianConfigurationInfos.PASSWORD_ALICE, MusicianConfigurationInfos.HOST_ALICE,
            MusicianConfigurationInfos.PORT_ALICE);
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
        return alice;
    }

    public final static Musician newBob() {
        Musician bob = new Musician(new JID(MusicianConfigurationInfos.JID_BOB),
            MusicianConfigurationInfos.PASSWORD_BOB, MusicianConfigurationInfos.HOST_BOB,
            MusicianConfigurationInfos.PORT_BOB);
        try {
            bob.initBot();
        } catch (AccessException e) {
            log.debug("", e);
        } catch (RemoteException e) {
            log.debug("", e);
        } catch (NotBoundException e) {
            log.debug("", e);
        }
        return bob;
    }

    public final static Musician newCarl() {
        Musician carl = new Musician(new JID(MusicianConfigurationInfos.JID_CARL),
            MusicianConfigurationInfos.PASSWORD_CARL, MusicianConfigurationInfos.HOST_CARL,
            MusicianConfigurationInfos.PORT_CARL);
        try {
            carl.initBot();
        } catch (AccessException e) {
            log.debug("", e);
        } catch (RemoteException e) {
            log.debug("", e);
        } catch (NotBoundException e) {
            log.debug("", e);
        }
        return carl;
    }

    public final static Musician newDave() {
        Musician dave = new Musician(new JID(MusicianConfigurationInfos.JID_DAVE),
            MusicianConfigurationInfos.PASSWORD_DAVE, MusicianConfigurationInfos.HOST_DAVE,
            MusicianConfigurationInfos.PORT_DAVE);
        try {
            dave.initBot();
        } catch (AccessException e) {
            log.debug("", e);
        } catch (RemoteException e) {
            log.debug("", e);
        } catch (NotBoundException e) {
            log.debug("", e);
        }
        return dave;
    }

    public final static Musician newEdna() {
        Musician edna = new Musician(new JID(MusicianConfigurationInfos.JID_EDNA),
            MusicianConfigurationInfos.PASSWORD_EDNA, MusicianConfigurationInfos.HOST_EDNA,
            MusicianConfigurationInfos.PORT_EDNA);
        try {
            edna.initBot();
        } catch (AccessException e) {
            log.debug("", e);
        } catch (RemoteException e) {
            log.debug("", e);
        } catch (NotBoundException e) {
            log.debug("", e);
        }
        return edna;
    }

    public static List<Musician> initAliceBobCarlConcurrently()
        throws InterruptedException {
        // pool = Executors.newFixedThreadPool(3);
        List<Callable<Musician>> initTasks = new ArrayList<Callable<Musician>>();
        initTasks.add(newAliceCallable());
        initTasks.add(newBobCallable());
        initTasks.add(newCarlCallable());
        return MakeOperationConcurrently.workAll(initTasks, 3);

    }

    public static List<Musician> initMusiciansConcurrently(int... ports)
        throws InterruptedException {
        List<Callable<Musician>> initTasks = new ArrayList<Callable<Musician>>();
        for (int port : ports) {
            switch (port) {
            case 12345:
                initTasks.add(newAliceCallable());
                break;
            case 12346:
                initTasks.add(newBobCallable());
                break;
            case 12347:
                initTasks.add(newCarlCallable());
                break;
            case 12348:
                initTasks.add(newDaveCallable());
                break;
            case 12349:
                initTasks.add(newEdnaCallable());
                break;
            default:
                break;
            }
        }
        return MakeOperationConcurrently.workAll(initTasks, initTasks.size());
    }

    public static void addContacts(List<Musician> musicians)
        throws RemoteException {
        for (int i = 0; i < musicians.size(); i++) {
            Musician musican = musicians.get(i);
            for (int j = i + 1; j < musicians.size(); j++) {
                Musician addedMuscian = musicians.get(j);
                if (!musican.rosterV.hasContactWith(addedMuscian.jid))
                    musican.addContactDone(addedMuscian);
            }
        }
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

    public static Callable<Musician> newDaveCallable() {
        return new Callable<Musician>() {
            public Musician call() throws Exception {
                return newDave();
            }
        };
    }

    public static Callable<Musician> newEdnaCallable() {
        return new Callable<Musician>() {
            public Musician call() throws Exception {
                return newEdna();
            }
        };
    }

}
