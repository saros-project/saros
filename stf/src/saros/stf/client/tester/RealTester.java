package saros.stf.client.tester;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import saros.net.xmpp.JID;
import saros.stf.server.rmi.controlbot.IControlBot;
import saros.stf.server.rmi.remotebot.IRemoteBot;
import saros.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import saros.stf.server.rmi.superbot.ISuperBot;

/**
 * Tester simulate a real Saros-tester. He/She can takes use of all RMI interfaces to help test
 * writers with Bot {@link IRemoteBot} and SuperBot {@link ISuperBot}to write their STF tests
 * nicely. STF is short for Saros Test Framework.
 */
class RealTester implements AbstractTester {

  private IRemoteWorkbenchBot bot;
  private ISuperBot superBot;
  private IControlBot controlBot;

  private JID jid;
  private String password;

  public RealTester(JID jid, String password, String host, int port)
      throws IOException, NotBoundException {
    this.jid = jid;
    this.password = password;

    Registry registry = LocateRegistry.getRegistry(host, port);
    bot = (IRemoteWorkbenchBot) registry.lookup("workbenchBot");
    superBot = (ISuperBot) registry.lookup("superBot");
    controlBot = (IControlBot) registry.lookup("controlBot");
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
  public String getRqJid() {
    return jid.toString();
  }

  @Override
  public String getDomain() {
    return jid.getDomain();
  }

  @Override
  public IRemoteWorkbenchBot remoteBot() throws RemoteException {
    bot.resetBot();
    return bot;
  }

  @Override
  public ISuperBot superBot() throws RemoteException {
    superBot.setJID(jid);
    return superBot;
  }

  @Override
  public IControlBot controlBot() throws RemoteException {
    return controlBot;
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
