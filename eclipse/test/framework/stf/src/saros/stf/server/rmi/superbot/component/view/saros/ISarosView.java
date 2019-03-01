package saros.stf.server.rmi.superbot.component.view.saros;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import saros.net.xmpp.JID;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.superbot.ISuperBot;
import saros.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInContactListArea;
import saros.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInSessionArea;

/**
 * This interface contains convenience API to perform actions in the Saros view. If not mentioned
 * otherwise all offered methods fulfill their contract by interacting only with the GUI.
 *
 * @author lchen
 */
public interface ISarosView extends Remote {

  /* *********************************************
   *
   * Actions
   *
   * ********************************************
   */
  /**
   * Connects with the given JID if not already connected with the given JID and waits until the
   * tester is connected to the server.
   *
   * @param jid see {@link JID}
   * @param password
   * @param forceReconnect if set to <code>true</code> the current connection will be disconnected
   *     first regardless of the JID to use
   * @throws RemoteException
   */
  public void connectWith(JID jid, String password, boolean forceReconnect) throws RemoteException;

  /**
   * Connects with the current active account and waits until the tester is connected to the server.
   * If a connection is already established the connection will be disconnected first.
   *
   * @throws RemoteException
   */
  public void connect() throws RemoteException;

  /**
   * Clicks the tool bar button "Disconnect" and waits until the tester is disconnected from the
   * server.
   *
   * @throws RemoteException
   */
  public void disconnect() throws RemoteException;

  /**
   * Adds the given contact to the contact list of the currently logged in tester. Does nothing if
   * the contact is already in the contact list.
   *
   * @param jid the {@link JID} of the contact to add
   * @throws RemoteException
   */
  public void addContact(JID jid) throws RemoteException;

  /**
   * Selects the contact specified with the given JID which is located under node "Contacts".
   *
   * @param jid the {@link JID} of the contact
   * @throws RemoteException
   */
  public IContextMenusInContactListArea selectContact(JID jid) throws RemoteException;

  /** Selects the tree node "Contacts" */
  public IContextMenusInContactListArea selectContacts() throws RemoteException;

  /** Selects the tree node "Session" */
  public IContextMenusInSessionArea selectSession() throws RemoteException;

  /** Selects the tree node "No Session Running" */
  public IContextMenusInSessionArea selectNoSessionRunning() throws RemoteException;

  /**
   * Selects the session user with the given JID in the "Session" node.
   *
   * @param jid the JID of the user
   * @throws IllegalStateException if the user is not in a session
   * @throws WidgetNotFoundException if the user could not be found
   * @throws RemoteException
   */
  public IContextMenusInSessionArea selectUser(JID jid) throws RemoteException;

  /*
   * Chat stuff
   */

  /**
   * Selects the chat room with the given title.
   *
   * @param name the name of the chat (as displayed in the chat tab)
   * @return an {@link IChatroom} interface
   * @throws RemoteException if the chat room does not exist
   */
  public IChatroom selectChatroom(String name) throws RemoteException;

  /**
   * Selects the chat room with the given regular expression.
   *
   * @param regex the regular expression to find the chat
   * @return an {@link IChatroom} interface for the first found match
   * @throws RemoteException if no matches are found
   */
  public IChatroom selectChatroomWithRegex(String regex) throws RemoteException;

  /**
   * Closes the chat room with the given title.
   *
   * @param name the name of the chat room (as displayed in the chat tab)
   * @throws RemoteException if the chat room does not exist
   */
  public void closeChatroom(String name) throws RemoteException;

  /**
   * Closes all chat rooms that matches the given regular expression.
   *
   * @param regex the regular expression to find the chat(s)
   * @throws RemoteException if no matches are found
   */
  public void closeChatroomWithRegex(String regex) throws RemoteException;

  /**
   * Checks if one or multiple chat rooms are currently opened in the Saros view.
   *
   * @return <code>true</code> if at least one chat room is open in the current Saros view, <code>
   *     false</code> otherwise
   * @throws RemoteException
   */
  public boolean hasOpenChatrooms() throws RemoteException;

  /* *********************************************
   *
   * States
   *
   * ********************************************
   */

  /**
   * @return<tt>true</tt>, if the toolBarbutton with the toolTip text "Disconnect.*" is visible.
   * @throws RemoteException
   */
  public boolean isConnected() throws RemoteException;

  /**
   * Checks if the contact given by the JID exists in the contact list of the currently logged in
   * tester.
   *
   * @param jid {@link JID} of the contact
   * @return <code>true</code>, if the contact specified with the given {@link JID} exists, <code>
   *     false</code> otherwise
   * @throws RemoteException
   */
  public boolean isInContactList(JID jid) throws RemoteException;

  /**
   * Returns the nickname of a contact from the contact list of the currently logged in
   * tester. @Note The implementation may bypass the GUI.
   *
   * @param jid {@link JID} of the contact
   * @return the nickname of the given contact or <code>null</code> if there is no nickname for that
   *     contact
   * @throws RemoteException
   */
  public String getNickname(JID jid) throws RemoteException;

  /**
   * Check if a nickname for the contact exists in the contact list of the currently logged in
   * tester. @Note The implementation may bypass the GUI.
   *
   * @param jid {@link JID} of the contact
   * @return <code>true</code>, if the given contact has a nickname, <code>false</code> otherwise
   * @throws RemoteException
   */
  public boolean hasNickName(JID jid) throws RemoteException;

  /**
   * Returns a list of all contacts names that are available in the contact list of the currently
   * logged in tester.
   *
   * @return list of all contacts names as <b>displayed</b> in the contact list tree node
   * @throws RemoteException
   */
  public List<String> getContacts() throws RemoteException;

  /* *********************************************
   *
   * Wait until
   *
   * ********************************************
   */

  /**
   * Waits until the tester is connected to the XMPP server.
   *
   * @throws RemoteException
   */
  public void waitUntilIsConnected() throws RemoteException;

  /**
   * Wait until the tester is disconnected from the XMPP server.
   *
   * @throws RemoteException
   */
  public void waitUntilIsDisconnected() throws RemoteException;

  /* *********************************************
   *
   * Actions
   *
   * ********************************************
   */

  /**
   * Performs the action "Send a file to selected contact" which should be activated by clicking the
   * tool bar button with the tooltip text {@link
   * StfRemoteObject#TB_SEND_A_FILE_TO_SELECTED_CONTACT} on the session view.
   *
   * <p><b>Attention:</b>
   *
   * <ol>
   *   <li>Makes sure, the session view is open and active.
   *   <li>All iterative triggered events by the action should be handled in the method(exclude
   *       remote triggered events). E.g. a popup window.
   * </ol>
   *
   * TODO: this function isn't complete yet. SWTBot don't support native dialog, so the action on
   * the "Select the file to send" dialog can be performed.
   *
   * @param jid the {@link JID} of the user with whom you want to share your screen.
   * @throws RemoteException
   */
  public void sendFileToUser(JID jid) throws RemoteException;

  /**
   * performs the action "Leave the session" which should be activated by clicking the tool bar
   * button with the toolTip text {@link StfRemoteObject#TB_LEAVE_SESSION} on the session view.
   *
   * <p><b>Attention:</b>
   *
   * <ol>
   *   <li>Makes sure, the session view is open and active.
   *   <li>All iterative triggered events by the action should be handled in the method(exclude
   *       remote triggered events). E.g. a popup window.
   * </ol>
   *
   * @throws RemoteException
   */
  public void leaveSession() throws RemoteException;

  /**
   * Performs the action "inconsistency detected in ..." which should be activated by clicking the
   * tool bar button with the toolTip text {@link StfRemoteObject#TB_INCONSISTENCY_DETECTED} on the
   * session view. The button is only enabled, if there are inconsistencies detected in a file.
   *
   * <p><b>Attention:</b>
   *
   * <ol>
   *   <li>Makes sure, the session view is open and active.
   *   <li>All iterative triggered events by the action should be handled in the method(exclude
   *       remote triggered events). E.g. a popup window.
   * </ol>
   *
   * TODO: this function isn't complete yet.
   *
   * @throws RemoteException
   */
  public void resolveInconsistency() throws RemoteException;

  /* *********************************************
   *
   * States
   *
   * ********************************************
   */

  /**
   * Checks if the tester is in a session.
   *
   * @return <code>true</code> if the tester is currently in a session, <code>false</code> otherwise
   * @throws RemoteException
   */
  public boolean isInSession() throws RemoteException;

  /**
   * Checks if a contact with the given JID is a user in the current session.
   *
   * @param jid {@link JID}
   * @return <code>true</code> if the contact is a user in the current session, <code>false</code>
   *     otherwise
   * @throws RemoteException
   */
  public boolean existsUser(JID jid) throws RemoteException;

  /**
   * Checks if the tester is host of the current session.
   *
   * @return <code>true</code> if the tester is the host of the current session, <code>false</code>
   *     otherwise
   * @throws RemoteException
   */
  public boolean isHost() throws RemoteException;

  /**
   * Check if the tester is following another user in the current session. @Note The implementation
   * may bypass the GUI.
   *
   * @return <code>true</code> if the tester is following another session user, <code>false</code>
   *     otherwise
   * @throws RemoteException
   */
  public boolean isFollowing() throws RemoteException;

  /**
   * Returns a list of all user names of the current session.
   *
   * @return list of all user names as <b>displayed</b> in the session tree node
   * @throws RemoteException
   */
  public List<String> getUsers() throws RemoteException;

  /**
   * Returns the JID of the user that the tester is currently following. @Note The implementation
   * may bypass the GUI.
   *
   * @return the {@link JID} of the followed user or <code>null</code> if the tester is currently
   *     not following anyone.
   */
  public JID getFollowedUser() throws RemoteException;

  /* *********************************************
   *
   * Wait until
   *
   * ********************************************
   */

  /**
   * Wait until the toolbarButton {@link StfRemoteObject#TB_INCONSISTENCY_DETECTED} is enabled.
   *
   * @throws RemoteException
   */
  public void waitUntilIsInconsistencyDetected() throws RemoteException;

  /**
   * Wait until the condition {@link ISarosView#isInSession()} is true.
   *
   * @throws RemoteException
   */
  public void waitUntilIsInSession() throws RemoteException;

  /**
   * wait until the condition {@link ISarosView#isInSession()} by given user is true.
   *
   * @param superBot {@link ISuperBot} of the invitee, whose session status you want to know.
   * @throws RemoteException
   */
  public void waitUntilIsInviteeInSession(ISuperBot superBot) throws RemoteException;

  /**
   * Wait until the condition {@link ISarosView#isInSession()} is false.
   *
   * <p><b>Attention</b>:<br>
   * Some actions need too much time to complete, so you will get assertError if you immediately
   * assert the after-state caused by such actions. E.g. you will get assertError with the assertion
   * assertTrue(alice.views().sessionviews().isInsession()) if alice leaves the session without
   * waitUntil the condition {@link ISarosView#isInSession()}. So it is recommended that you wait
   * until the session is completely closed before you run the assertion or perform a following
   * action.
   *
   * @throws RemoteException
   */
  public void waitUntilIsNotInSession() throws RemoteException;

  /**
   * Wait until the condition {@link ISarosView#existsUser(JID)} for all participants is false.
   *
   * @param jidsOfAllParticipants
   * @throws RemoteException
   */
  public void waitUntilAllPeersLeaveSession(final List<JID> jidsOfAllParticipants)
      throws RemoteException;
}
