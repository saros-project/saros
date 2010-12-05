package de.fu_berlin.inf.dpp.stf.client.test.helpers;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.MusicianConfigurationInfos;

public class InitMusician extends MusicianConfigurationInfos {
    private final static Logger log = Logger.getLogger(InitMusician.class);

    protected static Musician newMusician(String name, String jid, String pw,
        String host, int port) throws RemoteException {
        log.trace("new " + name);
        Musician alice = new Musician(new JID(jid), pw, host, port);
        try {
            log.trace("initBot");
            alice.initBot();
        } catch (AccessException e) {
            log.debug("", e);
        } catch (NotBoundException e) {
            log.debug("", e);
        }
        return alice;
    }

    public final static Musician newAlice() throws RemoteException {
        return newMusician("alice", JID_ALICE, PASSWORD_ALICE, HOST_ALICE,
            PORT_ALICE);
    }

    public final static Musician newBob() throws RemoteException {
        return newMusician("bob", JID_BOB, PASSWORD_BOB, HOST_BOB, PORT_BOB);
    }

    public final static Musician newCarl() throws RemoteException {
        return newMusician("carl", JID_CARL, PASSWORD_CARL, HOST_CARL,
            PORT_CARL);
    }

    public final static Musician newDave() throws RemoteException {
        return newMusician("dave", JID_DAVE, PASSWORD_DAVE, HOST_DAVE,
            PORT_DAVE);
    }

    public final static Musician newEdna() throws RemoteException {
        return newMusician("edna", JID_EDNA, PASSWORD_EDNA, HOST_EDNA,
            PORT_EDNA);
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
                if (!musican.rosterV.hasBuddy(addedMuscian.jid))
                    musican.addBuddyGUIDone(addedMuscian);
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
