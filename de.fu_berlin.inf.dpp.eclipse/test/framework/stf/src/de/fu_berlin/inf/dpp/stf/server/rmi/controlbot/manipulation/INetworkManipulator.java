package de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface gives you control to manipulate the Saros and XMPP network access. All methods
 * provided by the interface are <b>not</b> thread safe.
 *
 * @author Stefan Rossbach
 */
public interface INetworkManipulator extends Remote {

  /**
   * Blocks incoming packets that are received from the given JID over XMPP. Packets will be
   * dispatched after the connection is unblocked.
   *
   * @param jid the JID to block
   * @throws RemoteException
   */
  public void blockIncomingXMPPPackets(JID jid) throws RemoteException;

  /**
   * Blocks outgoing packets that are send to the given JID over XMPP. Packets will be dispatched
   * after the connection is unblocked.
   *
   * @param jid the JID to block
   * @throws RemoteException
   */
  public void blockOutgoingXMPPPackets(JID jid) throws RemoteException;

  /**
   * Unblocks the given JID and continue to dispatch packets from this JID over XMPP.
   *
   * @param jid the JID to unblock
   * @throws RemoteException
   */
  public void unblockIncomingXMPPPackets(JID jid) throws RemoteException;

  /**
   * Unblocks the given JID and continues to send packets to this JID over XMPP.
   *
   * @param jid the JID to unblock
   * @throws RemoteException
   */
  public void unblockOutgoingXMPPPackets(JID jid) throws RemoteException;

  /**
   * Blocks all incoming packet transfer for the current XMPP connection.
   *
   * @throws RemoteException
   */
  public void unblockIncomingXMPPPackets() throws RemoteException;

  /**
   * Unblocks all incoming packet transfer for the current XMPP connection.
   *
   * @throws RemoteException
   */
  public void unblockOutgoingXMPPPackets() throws RemoteException;

  /**
   * Controls whether all packets that are received from this JID should be discarded or not.
   *
   * @param jid the JID which packets should be discarded
   * @param discard <code>true</code> if all packets should be discarded, <code>false</code>
   *     otherwise
   * @throws RemoteException
   */
  public void setDiscardIncomingXMPPPackets(JID jid, boolean discard) throws RemoteException;

  /**
   * Controls whether all packets that are send to this JID should be discarded or not.
   *
   * @param jid the JID which packets should be discarded
   * @param discard <code>true</code> if all packets should be discarded, <code>false</code>
   *     otherwise
   * @throws RemoteException
   */
  public void setDiscardOutgoingXMPPPackets(JID jid, boolean discard) throws RemoteException;

  /**
   * Blocks incoming packets that are received from the given JID in the current Saros Session.
   * Packets will be dispatched after the connection is unblocked.
   *
   * @param jid the JID to block
   * @throws RemoteException
   */
  public void blockIncomingSessionPackets(JID jid) throws RemoteException;

  /**
   * Blocks outgoing packets that are send to the given JID in the current Saros Session. Packets
   * will be dispatched after the connection is unblocked.
   *
   * @param jid the JID to block
   * @throws RemoteException
   */
  public void blockOutgoingSessionPackets(JID jid) throws RemoteException;

  /**
   * Unblocks the given JID and continue to dispatch packets from this JID in the current Saros
   * Session.
   *
   * @param jid the JID to unblock
   * @throws RemoteException
   */
  public void unblockIncomingSessionPackets(JID jid) throws RemoteException;

  /**
   * Unblocks the given JID and continues to send packets to this JID in the current Saros Session.
   *
   * @param jid the JID to unblock
   * @throws RemoteException
   */
  public void unblockOutgoingSessionPackets(JID jid) throws RemoteException;

  /**
   * Blocks all incoming packet transfer for the current Saros session.
   *
   * @throws RemoteException
   */
  public void blockIncomingSessionPackets() throws RemoteException;

  /**
   * Blocks all outgoing packet transfer for the current Saros Session.
   *
   * @throws RemoteException
   */
  public void blockOutgoingSessionPackets() throws RemoteException;

  /**
   * Unblocks all incoming packet transfer for the current Saros session.
   *
   * @throws RemoteException
   */
  public void unblockIncomingSessionPackets() throws RemoteException;

  /**
   * Unblocks all outgoing packet transfer for the current Saros Session.
   *
   * @throws RemoteException
   */
  public void unblockOutgoingSessionPackets() throws RemoteException;

  /**
   * Controls whether all packets that are received from this JID should be discarded or not.
   *
   * @param jid the JID which packets should be discarded
   * @param discard <code>true</code> if all packets should be discarded, <code>false</code>
   *     otherwise
   * @throws RemoteException
   */
  public void setDiscardIncomingSessionPackets(JID jid, boolean discard) throws RemoteException;

  /**
   * Controls whether all packets that are send to this JID should be discarded or not.
   *
   * @param jid the JID which packets should be discarded
   * @param discard <code>true</code> if all packets should be discarded, <code>false</code>
   *     otherwise
   * @throws RemoteException
   */
  public void setDiscardOutgoingSessionPackets(JID jid, boolean discard) throws RemoteException;

  /**
   * Synchronizes on the activity queue for the given JID. When this method returns all outstanding
   * activities are guaranteed to have been executed before this call was made.
   *
   * @param jid the JID to synchronize on
   * @param timeout the timeout how long this method should wait before giving up
   * @throws RemoteException if the timeout exceeded or there is no active session
   */
  public void synchronizeOnActivityQueue(JID jid, long timeout) throws RemoteException;
}
