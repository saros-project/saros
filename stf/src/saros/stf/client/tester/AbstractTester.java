package saros.stf.client.tester;

import java.rmi.RemoteException;
import saros.net.xmpp.JID;
import saros.stf.server.rmi.controlbot.IControlBot;
import saros.stf.server.rmi.htmlbot.IHTMLBot;
import saros.stf.server.rmi.htmlbot.IHTMLWorkbenchBot;
import saros.stf.server.rmi.remotebot.IRemoteBot;
import saros.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import saros.stf.server.rmi.superbot.ISuperBot;

/**
 * Interface of {@link InvalidTester} and {@link RealTester}. It define all methods supported by
 * {@link AbstractTester}
 */
public interface AbstractTester {

  /**
   * get remote registered objects bot {@link IRemoteBot} and superBot {@link ISuperBot}
   *
   * @throws RemoteException
   * @throws NotBoundException
   * @throws AccessException
   */

  /** @Return the name segment of {@link JID}. */
  public String getName();

  /** @Return the JID without resource qualifier. */
  public String getBaseJid();

  /** @Return the resource qualified {@link JID}. */
  public String getRqJid();

  /** @return the domain of {@link JID} */
  public String getDomain();

  /** @return {@link JID} */
  public JID getJID();

  /** @return password of tester account. */
  public String getPassword();

  /**
   * @return the simple {@link IRemoteBot}, with which tester can remotely access widgets of
   *     saros-instance
   */
  public IRemoteWorkbenchBot remoteBot() throws RemoteException;

  /**
   * @return the super {@link ISuperBot}, which encapsulate some often used actions e.g.
   *     shareProject, connect, leaveSession.., which can be also done with {@link IRemoteBot}
   * @throws RemoteException
   */
  public ISuperBot superBot() throws RemoteException;

  /**
   * @return the control {@link IControlBot}, which is changing the behavior of how Saros operates.
   * @throws RemoteException
   */
  public IControlBot controlBot() throws RemoteException;

  /**
   * @return <code>true</code> iff the remote Saros instance has enabled and exposed its HTML GUI
   */
  public boolean usesHtmlGui();

  /**
   * @return the {@link IHTMLWorkbenchBot}, which gives control over the IDE-specific GUI part that
   *     contains the main view of the HTML GUI
   * @throws RemoteException
   */
  public IHTMLWorkbenchBot htmlViewBot() throws RemoteException;

  /**
   * @return the {@link IHTMLBot}, which gives control over the HTML GUI itself
   * @throws RemoteException
   */
  public IHTMLBot htmlBot() throws RemoteException;
}
