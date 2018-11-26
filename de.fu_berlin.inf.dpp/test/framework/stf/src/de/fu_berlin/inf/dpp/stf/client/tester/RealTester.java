package de.fu_berlin.inf.dpp.stf.client.tester;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.IControlBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.IHTMLBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.IHTMLWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.apache.log4j.Logger;

/**
 * Tester simulate a real Saros-tester. He/She can takes use of all RMI interfaces to help test
 * writers with Bot {@link IRemoteBot} and SuperBot {@link ISuperBot}to write their STF tests
 * nicely. STF is short for Saros Test Framework.
 */
class RealTester implements AbstractTester {

  private static final Logger log = Logger.getLogger(RealTester.class);

  private IRemoteWorkbenchBot bot;
  private ISuperBot superBot;
  private IControlBot controlBot;
  private IHTMLWorkbenchBot htmlViewBot;
  private IHTMLBot htmlBot;

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

    // TODO Don't handle this situation with exceptions
    try {
      htmlViewBot = (IHTMLWorkbenchBot) registry.lookup("htmlViewBot");
      htmlBot = (IHTMLBot) registry.lookup("htmlBot");
    } catch (NotBoundException e) {
      log.info(
          "Did not find bots for controlling HTML GUI. "
              + "Make sure to enable the HTML GUI if you want to test it.");
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
  public boolean usesHtmlGui() {
    return htmlViewBot != null && htmlBot != null;
  }

  @Override
  public IHTMLWorkbenchBot htmlViewBot() throws RemoteException {
    return htmlViewBot;
  }

  @Override
  public IHTMLBot htmlBot() throws RemoteException {
    return htmlBot;
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
