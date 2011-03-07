package de.fu_berlin.inf.dpp.stf.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.RemoteBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.SuperRemoteBot;

/**
 * Tester simulate a real saros-tester. He/She can takes use of all RMI
 * interfaces to help testwriters with Bot {@link RemoteBot} and SuperBot
 * {@link SuperRemoteBot}to write their STF tests nicely. STF is short for Saros Test
 * Framework.
 */
public class RealTester extends AbstractTester {

    private RemoteWorkbenchBot bot;

    private SuperRemoteBot superBot;

    private JID jid;
    private String password;

    private String host;
    private int port;

    public RealTester(JID jid, String password, String host, int port) {
        super();
        this.jid = jid;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    @Override
    public void getRegistriedRmiObject() throws RemoteException,
        NotBoundException, AccessException {
        Registry registry = LocateRegistry.getRegistry(host, port);
        try {
            bot = (RemoteWorkbenchBot) registry.lookup("workbenchBot");

            superBot = (SuperRemoteBot) registry.lookup("superBot");

        } catch (java.rmi.ConnectException e) {
            throw new RuntimeException("Could not connect to RMI of bot " + jid
                + ", did you start the Eclipse instance?");
        }

    }

    @Override
    public String getName() {
        return jid.getName();
    }

    @Override
    public String getBaseJid() {
        return jid.getBase();
    }

    @Override
    public String getRQjid() {
        return jid.toString();
    }

    @Override
    public String getDomain() {
        return jid.getDomain();
    }

    @Override
    public RemoteWorkbenchBot bot() {
        return bot;
    }

    @Override
    public SuperRemoteBot superBot() throws RemoteException {
        superBot.setJID(jid);
        return superBot;
    }

    @Override
    public JID getJID() {
        return jid;
    }

    @Override
    public String getPassword() {
        return password;
    }

}