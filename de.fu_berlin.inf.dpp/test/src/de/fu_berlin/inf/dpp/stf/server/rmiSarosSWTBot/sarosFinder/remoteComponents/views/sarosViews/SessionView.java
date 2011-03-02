package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosContextMenuWrapper;
import de.fu_berlin.inf.dpp.ui.actions.RestrictInviteesToReadOnlyAccessAction;

/**
 * This interface contains convenience API to perform a action using widgets in
 * session view. then you can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide</li>
 * <li>
 * then you can use the object sessionV initialized in {@link Tester} to access
 * the API :), e.g.
 * 
 * <pre>
 * alice.sessionV.openSharedSessionView();
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface SessionView extends Remote {
    public SarosContextMenuWrapper selectBuddy(final JID participantJID)
        throws RemoteException;

    public void shareYourScreenWithSelectedBuddy(JID jidOfPeer)
        throws RemoteException;

    /**********************************************
     * 
     * Actions
     * 
     **********************************************/
    /**
     * Performs the action "Follow this buddy" which should be activated by
     * clicking the context menu "Follow this buddy" of the tableItem with
     * itemText e.g. "alice1_fu@jabber.ccc.de" in the session view.
     * 
     * 
     * @param followedBuddyJID
     *            the {@link JID} of the user whom you want to follow.
     * @throws RemoteException
     */
    // public void followThisBuddy(JID followedBuddyJID) throws RemoteException;

    /**
     * performs the action "Share your screen with selected buddy" which should
     * be activated by clicking the tool bar button with the tooltip text
     * "Share your screen with selected user" on the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>All iterative triggered events by the action should be handled in the
     * method(exclude remote triggered events). E.g. a popup window.</li>
     * </ol>
     * 
     * @param jidOfPeer
     *            the {@link JID} of the user with whom you want to share your
     *            screen.
     * @throws RemoteException
     */
    // public void shareYourScreenWithSelectedBuddy(JID jidOfPeer)
    // throws RemoteException;

    /**
     * performs the action "Stop share session with buddy" which should be
     * activated by clicking the tool bar button with the tooltip text
     * "Stop share session with buddy" on the session view, This toolbar button
     * is only visible after clicking the
     * {@link SessionView#shareYourScreenWithSelectedBuddy(JID)}
     * 
     * 
     * @param jidOfPeer
     *            the {@link JID} of the user with whom you want to stop the
     *            screen session.
     * @throws RemoteException
     */
    public void stopSessionWithBuddy(JID jidOfPeer) throws RemoteException;

    /**
     * performs the action "Send a file to selected buddy" which should be
     * activated by clicking the tool bar button with the tooltip text
     * "Send a file to selected buddy" on the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>All iterative triggered events by the action should be handled in the
     * method(exclude remote triggered events). E.g. a popup window.</li>
     * </ol>
     * 
     * TODO: this function isn't complete yet. SWTBot don't support native
     * dialog, So the action on the "Select the file to send" dialog can be
     * performed.
     * 
     * @param selectedBuddyJID
     *            the {@link JID} of the user with whom you want to share your
     *            screen.
     * @throws RemoteException
     */
    public void sendAFileToSelectedBuddy(JID selectedBuddyJID)
        throws RemoteException;

    /**
     * performs the action "Start a VoIP session" which should be activated by
     * clicking the tool bar button with the tooltip text "Start a VoIP session"
     * on the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>All iterative triggered events by the action should be handled in the
     * method(exclude remote triggered events). E.g. a popup window.</li>
     * </ol>
     * 
     * TODO: this function isn't complete yet.
     * 
     * 
     * @param selectedBuddyJID
     *            the {@link JID} of the user with whom you want to share your
     *            screen.
     * @throws RemoteException
     */
    public void startAVoIPSessionWithSelectedBuddy(JID selectedBuddyJID)
        throws RemoteException;

    /**
     * Performs the action "Jump to position of selected buddy" which should be
     * activated by clicking the context menu
     * "Jump to position of selected buddy" of a tableItem with the itemText
     * e.g. "alice1_fu@jabber.ccc.de" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @param selectedBuddyJID
     *            the {@link JID} of the user whom you want to stop following.
     * @throws RemoteException
     */
    // public void jumpToPositionOfSelectedBuddy(JID selectedBuddyJID)
    // throws RemoteException;

    /**
     * performs the action "Leave the session" which should be activated by
     * clicking the tool bar button with the toolTip text "Leave the session" on
     * the session view. After clicking the button you will get different popUp
     * window depending on whether you are host or not. So if you are peer,
     * please use this one, otherwise use
     * {@link SessionView#leaveTheSessionByHost()}
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>All iterative triggered events by the action should be handled in the
     * method(exclude remote triggered events). E.g. a popup window.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void leaveTheSession() throws RemoteException;

    /**
     * Perform the action "Grant write access" which should be activated by
     * clicking the context menu "Grant write access" of a tableItem with
     * itemText e.g. "bob1_fu@jabber.ccc.de" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>Wait until the shell "Progress Information" is closed. It guarantee
     * that the "Grant write access" action is completely done.</li>
     * </ol>
     * 
     * @param participantJID
     *            the {@link JID} of the user whom you want to grant
     *            {@link User.Permission#WRITE_ACCESS}.
     * @throws RemoteException
     */
    // public void grantWriteAccess(final JID participantJID)
    // throws RemoteException;

    /**
     * performs the {@link RestrictToReadOnlyAccessAction} which should be
     * activated by clicking the context menu
     * {@link RestrictToReadOnlyAccessAction} of the tableItem with itemText
     * e.g. "bob1_fu@jabber.ccc.de" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * <li>Waits until the shell "Progress Information" is closed. It guarantee
     * that the {@link RestrictToReadOnlyAccessAction} is completely done.</li>
     * </ol>
     * 
     * @param participantJID
     *            the {@link JID} of the user whom you want to remove drive
     *            {@link User.Permission}.
     * @throws RemoteException
     */
    // public void restrictToReadOnlyAccess(final JID participantJID)
    // throws RemoteException;

    /**
     * performs the action "Open invitation interface" which should be activated
     * by clicking the tool bar button with the tooltip text
     * "Open invitation interface" on the session view. The button is only
     * enabled, if you are host.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>All iterative triggered events by the action should be handled in the
     * method(exclude remote triggered events). E.g. a popup window.</li>
     * </ol>
     * 
     * @param inviteesJIDs
     *            the buddies whom you want to invite to your session.
     * @throws RemoteException
     */
    public void openInvitationInterface(String... inviteesJIDs)
        throws RemoteException;

    /**
     * set the JID Of the lcoal user
     * 
     * @param localJID
     * @throws RemoteException
     */
    // public void setJID(JID localJID) throws RemoteException;

    /**
     * This function do same as the
     * {@link SessionView#stopFollowingThisBuddy(JID)} except you don't need to
     * pass the followed buddy to the function. It is very useful, if you don't
     * exactly know whom you are now following.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    // public void stopFollowing() throws RemoteException;

    /**
     * Performs the action "Stop following this buddy" which should be activated
     * by clicking the context menu "Stop following this buddy" of a tableItem
     * with the itemText e.g. "alice1_fu@jabber.ccc.de" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @param followedBuddyJID
     *            the {@link JID} of the user whom you want to stop following.
     * @throws RemoteException
     */
    // public void stopFollowingThisBuddy(JID followedBuddyJID)
    // throws RemoteException;

    /**
     * performs the action "inconsistency detected in ..." which should be
     * activated by clicking the tool bar button with the toolTip text
     * "inconsistency detected in ..." on the session view. The button is only
     * enabled, if there are inconsistency detected in a file.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>All iterative triggered events by the action should be handled in the
     * method(exclude remote triggered events). E.g. a popup window.</li>
     * </ol>
     * 
     * TODO: this function isn't complete yet.
     * 
     * @throws RemoteException
     */
    public void inconsistencyDetected() throws RemoteException;

    /**
     * performs the {@link RestrictInviteesToReadOnlyAccessAction} which should
     * be activated by clicking the toolBar button with the toolTip text
     * {@link RestrictInviteesToReadOnlyAccessAction} on the session view. The
     * button is only enabled, if there users with
     * {@link User.Permission#WRITE_ACCESS} existed in the session.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>All iterative triggered events by the action should be handled in the
     * method(exclude remote triggered events). E.g. a popup window.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void restrictInviteesToReadOnlyAccess() throws RemoteException;

}
