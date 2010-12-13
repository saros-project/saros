package de.fu_berlin.inf.dpp.stf.client.testProject.helpers;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.TesterConfigurationInfos;

public class InitMusician extends TesterConfigurationInfos {
    private final static Logger log = Logger.getLogger(InitMusician.class);

    protected static Tester newMusician(String name, String jid, String pw,
        String host, int port) throws RemoteException {
        log.trace("new " + name);
        Tester alice = new Tester(new JID(jid), pw, host, port);
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

    public final static Tester newAlice() throws RemoteException {
        return newMusician("alice", JID_ALICE, PASSWORD_ALICE, HOST_ALICE,
            PORT_ALICE);
    }

    public final static Tester newBob() throws RemoteException {
        return newMusician("bob", JID_BOB, PASSWORD_BOB, HOST_BOB, PORT_BOB);
    }

    public final static Tester newCarl() throws RemoteException {
        return newMusician("carl", JID_CARL, PASSWORD_CARL, HOST_CARL,
            PORT_CARL);
    }

    public final static Tester newDave() throws RemoteException {
        return newMusician("dave", JID_DAVE, PASSWORD_DAVE, HOST_DAVE,
            PORT_DAVE);
    }

    public final static Tester newEdna() throws RemoteException {
        return newMusician("edna", JID_EDNA, PASSWORD_EDNA, HOST_EDNA,
            PORT_EDNA);
    }

    public static List<Tester> initAliceBobCarlConcurrently()
        throws InterruptedException {
        // pool = Executors.newFixedThreadPool(3);
        List<Callable<Tester>> initTasks = new ArrayList<Callable<Tester>>();
        initTasks.add(newAliceCallable());
        initTasks.add(newBobCallable());
        initTasks.add(newCarlCallable());
        return MakeOperationConcurrently.workAll(initTasks);
    }

    public static List<Tester> initMusiciansConcurrently(int... ports)
        throws InterruptedException {
        List<Callable<Tester>> initTasks = new ArrayList<Callable<Tester>>();
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
        return MakeOperationConcurrently.workAll(initTasks);
    }

    public static void addContacts(List<Tester> musicians)
        throws RemoteException {
        for (int i = 0; i < musicians.size(); i++) {
            Tester musican = musicians.get(i);
            for (int j = i + 1; j < musicians.size(); j++) {
                Tester addedMuscian = musicians.get(j);
                if (!musican.rosterV.hasBuddy(addedMuscian.jid))
                    musican.addBuddyGUIDone(addedMuscian);
            }
        }
    }

    public static Callable<Tester> newAliceCallable() {
        return new Callable<Tester>() {
            public Tester call() throws Exception {
                return newAlice();
            }
        };
    }

    public static Callable<Tester> newBobCallable() {
        return new Callable<Tester>() {
            public Tester call() throws Exception {
                return newBob();
            }
        };
    }

    public static Callable<Tester> newCarlCallable() {
        return new Callable<Tester>() {
            public Tester call() throws Exception {
                return newCarl();
            }
        };
    }

    public static Callable<Tester> newDaveCallable() {
        return new Callable<Tester>() {
            public Tester call() throws Exception {
                return newDave();
            }
        };
    }

    public static Callable<Tester> newEdnaCallable() {
        return new Callable<Tester>() {
            public Tester call() throws Exception {
                return newEdna();
            }
        };
    }

}
