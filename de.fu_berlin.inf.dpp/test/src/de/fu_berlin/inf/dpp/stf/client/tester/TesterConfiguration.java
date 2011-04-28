package de.fu_berlin.inf.dpp.stf.client.tester;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.MakeOperationConcurrently;

public class TesterConfiguration {
    private final static Logger log = Logger.getLogger(TesterConfiguration.class);

    protected static AbstractTester newTester(String name, String jid, String pw,
        String host, int port) throws RemoteException {
        log.trace("new " + name);
        AbstractTester tester = new RealTester(new JID(jid), pw, host, port);
        try {
            log.trace("initBot");
            tester.getRegistriedRmiObject();
        } catch (AccessException e) {
            log.debug("", e);
        } catch (NotBoundException e) {
            log.debug("", e);
        }
        return tester;
    }

    public final static AbstractTester newAlice() throws RemoteException {
        return newTester("alice", JID_ALICE, PASSWORD_ALICE, HOST_ALICE,
            PORT_ALICE);
    }

    public final static AbstractTester newBob() throws RemoteException {
        return newTester("bob", JID_BOB, PASSWORD_BOB, HOST_BOB, PORT_BOB);
    }

    public final static AbstractTester newCarl() throws RemoteException {
        return newTester("carl", JID_CARL, PASSWORD_CARL, HOST_CARL, PORT_CARL);
    }

    public final static AbstractTester newDave() throws RemoteException {
        return newTester("dave", JID_DAVE, PASSWORD_DAVE, HOST_DAVE, PORT_DAVE);
    }

    public final static AbstractTester newEdna() throws RemoteException {
        return newTester("edna", JID_EDNA, PASSWORD_EDNA, HOST_EDNA, PORT_EDNA);
    }

    public static List<AbstractTester> initAliceBobCarlConcurrently()
        throws InterruptedException {
        // pool = Executors.newFixedThreadPool(3);
        List<Callable<AbstractTester>> initTasks = new ArrayList<Callable<AbstractTester>>();
        initTasks.add(newAliceCallable());
        initTasks.add(newBobCallable());
        initTasks.add(newCarlCallable());
        return MakeOperationConcurrently.workAll(initTasks);
    }

    public static List<AbstractTester> initMusiciansConcurrently(int... ports)
        throws InterruptedException {
        List<Callable<AbstractTester>> initTasks = new ArrayList<Callable<AbstractTester>>();
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

    public static Callable<AbstractTester> newAliceCallable() {
        return new Callable<AbstractTester>() {
            public AbstractTester call() throws Exception {
                return newAlice();
            }
        };
    }

    public static Callable<AbstractTester> newBobCallable() {
        return new Callable<AbstractTester>() {
            public AbstractTester call() throws Exception {
                return newBob();
            }
        };
    }

    public static Callable<AbstractTester> newCarlCallable() {
        return new Callable<AbstractTester>() {
            public AbstractTester call() throws Exception {
                return newCarl();
            }
        };
    }

    public static Callable<AbstractTester> newDaveCallable() {
        return new Callable<AbstractTester>() {
            public AbstractTester call() throws Exception {
                return newDave();
            }
        };
    }

    public static Callable<AbstractTester> newEdnaCallable() {
        return new Callable<AbstractTester>() {
            public AbstractTester call() throws Exception {
                return newEdna();
            }
        };
    }

    public final static boolean DEVELOPMODE = true;

    public final static accountType WHICH_ACCOUNT = accountType.USER_STF_WITH_SAROS_CON_SERVER;

    public enum accountType {
        USER1_FU_WITH_JABBER_CCC_SERVER, USER_STF_WITH_SAROS_CON_SERVER, USER1_FU_WITH_SAROS_CON_SERVER
    }

    /**
     * VM test accounts
     */
    private final static String JID_ALICE_VM = ("testuser@saros-con.imp.fu-berlin.de");
    private final static String PASSWORD_ALICE_VM = "test";
    private final static String HOST_ALICE_VM = "192.168.66.71";
    private final static int PORT_ALICE_VM = 1099;
    private final static String JID_BOB_VM = ("testuser2@saros-con.imp.fu-berlin.de");
    private final static String PASSWORD_BOB_VM = "test";
    private final static String HOST_BOB_VM = "192.168.66.72";
    private final static int PORT_BOB_VM = 1099;
    private final static String JID_CARL_VM = ("unconfigured");
    private final static String PASSWORD_CARL_VM = "unconfigured";
    private final static String HOST_CARL_VM = "unconfigured";
    private final static int PORT_CARL_VM = 1099;

    private final static String JID_DAVE_VM = ("unconfigured");
    private final static String PASSWORD_DAVE_VM = "unconfigured";
    private final static String HOST_DAVE_VM = "unconfigured";
    private final static int PORT_DAVE_VM = 1099;

    private final static String JID_EDNA_VM = ("unconfigured");
    private final static String PASSWORD_EDNA_VM = "unconfigured";
    private final static String HOST_EDNA_VM = "unconfigured";
    private final static int PORT_EDNA_VM = 1099;

    /**
     * Local test accounts
     */
    private final static String JID_ALICE_LOCAL_WITH_SAROS_CON = ("alice1_fu@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    private final static String JID2_ALICE_LOCAL_WITH_SAROS_CON = ("alice_stf@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    private final static String JID_ALICE_LOCAL_WITH_JABBER_CCC = ("alice1_fu@jabber.ccc.de/" + Saros.RESOURCE);
    private final static String PASSWORD_ALICE_LOCAL = "dddfffggg";
    private final static String HOST_ALICE_LOCAL = "localhost";
    private final static int PORT_ALICE_LOCAL = 12345;

    private final static String JID_BOB_LOCAL_WITH_SAROS_CON = ("bob1_fu@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    private final static String JID2_BOB_LOCAL_WITH_SAROS_CON = ("bob_stf@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    private final static String JID_BOB_LOCAL_WITH_JABBER_CCC = ("bob1_fu@jabber.ccc.de/" + Saros.RESOURCE);
    private final static String PASSWORD_BOB_LOCAL = "dddfffggg";
    private final static String HOST_BOB_LOCAL = "localhost";
    private final static int PORT_BOB_LOCAL = 12346;

    private final static String JID_CARL_LOCAL_WITH_SAROS_CON = ("carl1_fu@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    private final static String JID2_CARL_LOCAL_WITH_SAROS_CON = ("carl1_stf@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    private final static String JID_CARL_LOCAL_WITH_JABBER_CCC = ("carl1_fu@jabber.ccc.de/" + Saros.RESOURCE);
    private final static String PASSWORD_CARL_LOCAL = "dddfffggg";
    private final static String HOST_CARL_LOCAL = "localhost";
    private final static int PORT_CARL_LOCAL = 12347;

    private final static String JID_DAVE_LOCAL_WITH_SAROS_CON = ("dave1_fu@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    private final static String JID2_DAVE_LOCAL_WITH_SAROS_CON = ("dave_stf@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    private final static String JID_DAVE_LOCAL_WIHT_JABBER_CCC = ("dave1_fu@jabber.ccc.de/" + Saros.RESOURCE);
    private final static String PASSWORD_DAVE_LOCAL = "dddfffggg";
    private final static String HOST_DAVE_LOCAL = "localhost";
    private final static int PORT_DAVE_LOCAL = 12348;

    private final static String JID_EDNA_LOCAL_WITH_SAROS_CON = ("edna1_fu@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    private final static String JID2_EDNA_LOCAL_WITH_SAROS_CON = ("edna_stf@saros-con.imp.fu-berlin.de/" + Saros.RESOURCE);
    private final static String JID_EDNA_LOCAL_WITH_JABBER_CCC = ("edna1_fu@jabber.ccc.de/" + Saros.RESOURCE);
    private final static String PASSWORD_EDNA_LOCAL = "dddfffggg";
    private final static String HOST_EDNA_LOCAL = "localhost";
    private final static int PORT_EDNA_LOCAL = 12349;

    /**
     * test accounts
     */
    public final static String JID_ALICE;
    public final static String PASSWORD_ALICE;
    public final static String HOST_ALICE;
    public final static int PORT_ALICE;

    public final static String JID_BOB;
    public final static String PASSWORD_BOB;
    public final static String HOST_BOB;
    public final static int PORT_BOB;

    public final static String JID_CARL;
    public final static String PASSWORD_CARL;
    public final static String HOST_CARL;
    public final static int PORT_CARL;

    public final static String JID_DAVE;
    public final static String PASSWORD_DAVE;
    public final static String HOST_DAVE;
    public final static int PORT_DAVE;

    public final static String JID_EDNA;
    public final static String PASSWORD_EDNA;
    public final static String HOST_EDNA;
    public final static int PORT_EDNA;

    static {
        if (DEVELOPMODE) {
            switch (WHICH_ACCOUNT) {
            case USER_STF_WITH_SAROS_CON_SERVER:
                JID_ALICE = JID2_ALICE_LOCAL_WITH_SAROS_CON;
                JID_BOB = JID2_BOB_LOCAL_WITH_SAROS_CON;
                JID_CARL = JID2_CARL_LOCAL_WITH_SAROS_CON;
                JID_DAVE = JID2_DAVE_LOCAL_WITH_SAROS_CON;
                JID_EDNA = JID2_EDNA_LOCAL_WITH_SAROS_CON;
                break;
            case USER1_FU_WITH_JABBER_CCC_SERVER:
                JID_ALICE = JID_ALICE_LOCAL_WITH_JABBER_CCC;
                JID_BOB = JID_BOB_LOCAL_WITH_JABBER_CCC;
                JID_CARL = JID_CARL_LOCAL_WITH_JABBER_CCC;
                JID_DAVE = JID_DAVE_LOCAL_WIHT_JABBER_CCC;
                JID_EDNA = JID_EDNA_LOCAL_WITH_JABBER_CCC;
                break;
            default:
                JID_ALICE = JID_ALICE_LOCAL_WITH_SAROS_CON;
                JID_BOB = JID_BOB_LOCAL_WITH_SAROS_CON;
                JID_CARL = JID_CARL_LOCAL_WITH_SAROS_CON;
                JID_DAVE = JID_DAVE_LOCAL_WITH_SAROS_CON;
                JID_EDNA = JID_EDNA_LOCAL_WITH_SAROS_CON;
                break;
            }

            PASSWORD_ALICE = PASSWORD_ALICE_LOCAL;
            HOST_ALICE = HOST_ALICE_LOCAL;
            PORT_ALICE = PORT_ALICE_LOCAL;

            PASSWORD_BOB = PASSWORD_BOB_LOCAL;
            HOST_BOB = HOST_BOB_LOCAL;
            PORT_BOB = PORT_BOB_LOCAL;

            PASSWORD_CARL = PASSWORD_CARL_LOCAL;
            HOST_CARL = HOST_CARL_LOCAL;
            PORT_CARL = PORT_CARL_LOCAL;

            PASSWORD_DAVE = PASSWORD_DAVE_LOCAL;
            HOST_DAVE = HOST_DAVE_LOCAL;
            PORT_DAVE = PORT_DAVE_LOCAL;

            PASSWORD_EDNA = PASSWORD_EDNA_LOCAL;
            HOST_EDNA = HOST_EDNA_LOCAL;
            PORT_EDNA = PORT_EDNA_LOCAL;

        } else {
            JID_ALICE = JID_ALICE_VM;
            PASSWORD_ALICE = PASSWORD_ALICE_VM;
            HOST_ALICE = HOST_ALICE_VM;
            PORT_ALICE = PORT_ALICE_VM;
            JID_BOB = JID_BOB_VM;
            PASSWORD_BOB = PASSWORD_BOB_VM;
            HOST_BOB = HOST_BOB_VM;
            PORT_BOB = PORT_BOB_VM;
            JID_CARL = JID_CARL_VM;
            PASSWORD_CARL = PASSWORD_CARL_VM;
            HOST_CARL = HOST_CARL_VM;
            PORT_CARL = PORT_CARL_VM;
            JID_DAVE = JID_DAVE_VM;
            PASSWORD_DAVE = PASSWORD_DAVE_VM;
            HOST_DAVE = HOST_DAVE_VM;
            PORT_DAVE = PORT_DAVE_VM;
            JID_EDNA = JID_EDNA_VM;
            PASSWORD_EDNA = PASSWORD_EDNA_VM;
            HOST_EDNA = HOST_EDNA_VM;
            PORT_EDNA = PORT_EDNA_VM;
        }
    }

}
