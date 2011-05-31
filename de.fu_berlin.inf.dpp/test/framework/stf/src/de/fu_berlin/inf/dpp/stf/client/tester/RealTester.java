package de.fu_berlin.inf.dpp.stf.client.tester;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;

/**
 * Tester simulate a real saros-tester. He/She can takes use of all RMI
 * interfaces to help testwriters with Bot {@link IRemoteBot} and SuperBot
 * {@link ISuperBot}to write their STF tests nicely. STF is short for Saros Test
 * Framework.
 */
class RealTester implements AbstractTester {

    private IRemoteWorkbenchBot bot;

    private ISuperBot superBot;

    private JID jid;
    private String password;

    public RealTester(JID jid, String password, String host, int port)
        throws IOException, NotBoundException {
        this.jid = jid;
        this.password = password;

        Registry registry = LocateRegistry.getRegistry(host, port);
        bot = (IRemoteWorkbenchBot) registry.lookup("workbenchBot");
        superBot = (ISuperBot) registry.lookup("superBot");
    }

    public String getName() {
        return jid.getName();
    }

    public String getBaseJid() {
        return jid.getBase();
    }

    public String getRqJid() {
        return jid.toString();
    }

    public String getDomain() {
        return jid.getDomain();
    }

    public IRemoteWorkbenchBot remoteBot() {
        return bot;
    }

    public ISuperBot superBot() throws RemoteException {
        superBot.setJID(jid);
        return superBot;
    }

    public JID getJID() {
        return jid;
    }

    public String getPassword() {
        return password;
    }

}