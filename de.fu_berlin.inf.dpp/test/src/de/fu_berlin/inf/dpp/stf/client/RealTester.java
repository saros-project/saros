package de.fu_berlin.inf.dpp.stf.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.SuperBot;

/**
 * Tester simulate a real saros-tester. He/She can takes use of all RMI
 * interfaces to help testwriters with Bot {@link STFBot} and SuperBot
 * {@link SuperBot}to write their STF tests nicely. STF is short for Saros Test
 * Framework.
 */
public class RealTester extends AbstractTester {

    private STFWorkbenchBot bot;

    private SuperBot superBot;

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
            bot = (STFWorkbenchBot) registry.lookup("workbenchBot");

            superBot = (SuperBot) registry.lookup("superBot");

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
    public STFWorkbenchBot bot() {
        return bot;
    }

    @Override
    public SuperBot superBot() throws RemoteException {
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