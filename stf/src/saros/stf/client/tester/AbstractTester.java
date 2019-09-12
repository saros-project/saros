package saros.stf.client.tester;

import java.rmi.RemoteException;
import saros.net.xmpp.JID;
import saros.stf.server.rmi.controlbot.IControlBot;
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
}
