package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import java.rmi.RemoteException;

public interface IContextMenusInSessionArea extends IContextMenusInSarosView {

  /**
   * ********************************************
   *
   * <p>contextMenus showed in session-area
   *
   * <p>********************************************
   */

  /**
   * Grant the selected participant a write access by clicking his contextMenu {@link
   * StfRemoteObject#CM_GRANT_WRITE_ACCESS}.
   */
  public void grantWriteAccess() throws RemoteException;

  /**
   * Restrict the selected participant to read only access by clicking his contextMenu {@link
   * StfRemoteObject#CM_RESTRICT_TO_READ_ONLY_ACCESS}
   *
   * @throws RemoteException
   */
  public void restrictToReadOnlyAccess() throws RemoteException;

  /**
   * Follow the selected participant by clicking his contextMenu {@link
   * StfRemoteObject#CM_FOLLOW_PARTICIPANT}
   *
   * @throws RemoteException
   */
  public void followParticipant() throws RemoteException;

  /**
   * Stop following the selected participant by clicking his contextMenu {@link
   * StfRemoteObject#CM_STOP_FOLLOWING}
   *
   * @throws RemoteException
   */
  public void stopFollowing() throws RemoteException;

  /**
   * Jump to the position of the selected participant by clicking his contextMenu {@link
   * StfRemoteObject#CM_JUMP_TO_POSITION_OF_PARTICIPANT}
   *
   * @throws RemoteException
   */
  public void jumpToPositionOfSelectedParticipant() throws RemoteException;

  public void addProjects(String... projectNames) throws RemoteException;

  public void addContactsToSession(String... jidOfInvitees) throws RemoteException;

  public void shareProjects(String projectName, JID... jids) throws RemoteException;

  /**
   * @return<tt>true</tt>, if the selected participant has write access.
   * @throws RemoteException
   */
  public boolean hasWriteAccess() throws RemoteException;

  /**
   * @return<tt>true</tt>, if the selected participant has read only access.
   * @throws RemoteException
   */
  public boolean hasReadOnlyAccess() throws RemoteException;

  /**
   * @return<tt>true</tt>, if the local user is following the selected participant
   * @throws RemoteException
   */
  public boolean isFollowing() throws RemoteException;

  /**
   * wait until the condition {@link IContextMenusInSessionArea#hasWriteAccess()} is true . This
   * method should be used after performing the action {@link
   * IContextMenusInSessionArea#grantWriteAccess()} to guarantee the invitee has really got {@link
   * Permission#WRITE_ACCESS} .
   *
   * @throws RemoteException
   */
  public void waitUntilHasWriteAccess() throws RemoteException;

  /**
   * waits until the condition {@link IContextMenusInSessionArea#hasReadOnlyAccess()} is true. This
   * method should be used after performing the action {@link
   * IContextMenusInSessionArea#restrictToReadOnlyAccess()}
   *
   * @throws RemoteException
   */
  public void waitUntilHasReadOnlyAccess() throws RemoteException;

  /**
   * Wait until the condition {@link IContextMenusInSessionArea#isFollowing()} is true.
   *
   * @throws RemoteException
   */
  public void waitUntilIsFollowing() throws RemoteException;

  /**
   * Wait until the condition {@link IContextMenusInSessionArea#isFollowing()} is false.
   *
   * @throws RemoteException
   */
  public void waitUntilIsNotFollowing() throws RemoteException;
}
