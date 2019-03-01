package de.fu_berlin.inf.dpp.stf.client.tester;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.IControlBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.IHTMLBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.IHTMLWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;
import java.rmi.RemoteException;

/** Tester that could not be initialized. */
class InvalidTester implements AbstractTester {

  RuntimeException exception;
  String password;
  JID jid;

  public InvalidTester(JID jid, String password, RuntimeException exception) {
    this.jid = jid;
    this.password = password;
    this.exception = exception;
  }

  /** @Return the name segment of {@link JID}. */
  @Override
  public String getName() {
    return jid.getName();
  }

  /** @Return the JID without resource qualifier. */
  @Override
  public String getBaseJid() {
    return jid.getBase();
  }

  /** @Return the resource qualified {@link JID}. */
  @Override
  public String getRqJid() {
    return jid.toString();
  }

  @Override
  public String getDomain() {
    return jid.getDomain();
  }

  @Override
  public IRemoteWorkbenchBot remoteBot() {
    throw exception;
  }

  @Override
  public ISuperBot superBot() throws RemoteException {
    throw exception;
  }

  @Override
  public IControlBot controlBot() throws RemoteException {
    throw exception;
  }

  @Override
  public boolean usesHtmlGui() {
    throw exception;
  }

  @Override
  public IHTMLWorkbenchBot htmlViewBot() throws RemoteException {
    throw exception;
  }

  @Override
  public IHTMLBot htmlBot() throws RemoteException {
    throw exception;
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
