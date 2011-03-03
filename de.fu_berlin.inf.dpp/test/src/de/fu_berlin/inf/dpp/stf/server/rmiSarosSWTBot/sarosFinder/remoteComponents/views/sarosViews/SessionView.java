package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.SarosBot;
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
     * performs the action "Leave the session" which should be activated by
     * clicking the tool bar button with the toolTip text "Leave the session" on
     * the session view. After clicking the button you will get different popUp
     * window depending on whether you are host or not.
     * 
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

    /**
     * @param contactJID
     *            the JID of a user, who is sharing a session with you.
     * @return the text of the table item specified with the given parameter
     *         "contactJID" which listed in the session view.
     * @throws RemoteException
     */
    public String getParticipantLabel(JID contactJID) throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if you have write access.
     * @throws RemoteException
     */
    public boolean hasWriteAccess() throws RemoteException;

    /**
     * 
     * @param buddiesJIDs
     *            the list of buddies'JIDs, which write access would be checked
     * @return<tt>true</tt>, if the given buddies have write access.
     * @throws RemoteException
     */
    public boolean hasWriteAccessBy(JID... buddiesJIDs) throws RemoteException;

    public boolean hasWriteAccessBy(String... tableItemTexts)
        throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if the you have read only access.
     * @throws RemoteException
     */
    public boolean hasReadOnlyAccess() throws RemoteException;

    /**
     * 
     * @param buddiesJIDs
     *            the list of buddies'JIDs, which read access would be checked
     * @return<tt>true</tt>, if the context menu "Restrict to read only access"
     *                       isn't enabled by given participants
     * @throws RemoteException
     */
    public boolean hasReadOnlyAccessBy(JID... buddiesJIDs)
        throws RemoteException;

    public boolean hasReadOnlyAccessBy(String... jids) throws RemoteException;

    /**
     * Test if you are now in a session. <br>
     * This function check if the tool bar button "Leave the session" in the
     * session view is enabled. You can also use another function
     * 
     * 
     * @return <tt>true</tt> if the tool bar button "Leave the session" is
     *         enabled.
     * 
     * @throws RemoteException
     */
    public boolean isInSession() throws RemoteException;

    /**
     * Test if a participant
     * 
     * 
     * exists in the contact list in the session view.
     * 
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @param contactJID
     *            JID of a contact whose nick name listed in the session view.
     *            e.g. "You" or "bob_stf@saros-con.imp.fu-berlin.de" or
     *            "bob_stf@saros-con.imp.fu-berlin.de" or
     *            "(nickNameOfBob (bob_stf@saros-con.imp.fu-berlin.de)" .
     * @return <tt>true</tt> if the passed contactName is in the contact list in
     *         the session view.
     * @throws RemoteException
     */
    public boolean existsParticipant(JID contactJID) throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if name of the first tableItem in the saros session
     *                       view is equal with the label infos of the local
     *                       user.
     * @throws RemoteException
     */
    public boolean isHost() throws RemoteException;

    /**
     * 
     * @param jidOfParticipant
     * @return<tt>true</tt>, if name of the first tableItem in the saros session
     *                       view is equal with the label infos of the given
     *                       participant.
     * @throws RemoteException
     */
    public boolean isHost(JID jidOfParticipant) throws RemoteException;

    /**
     * 
     * @return <tt>true</tt>, if the local user is in the session view
     * @throws RemoteException
     */
    public boolean isParticipant() throws RemoteException;

    /**
     * @return <tt>true</tt>, if the participant with the given JID exists in
     *         the session view
     * 
     */
    public boolean isParticipant(JID jid) throws RemoteException;

    /**
     * 
     * @param jidOfParticipants
     * @return <tt>true</tt>, if the participants with the given JIDs exist in
     *         the session view
     * @throws RemoteException
     */
    public boolean areParticipants(List<JID> jidOfParticipants)
        throws RemoteException;

    public boolean isFollowing() throws RemoteException;

    public boolean isFollowingBuddy(JID buddyJID) throws RemoteException;

    /**
     * 
     * It get all participant names listed in the session view
     * 
     * @return list, which contain all the contact names.
     * @throws RemoteException
     */
    public List<String> getAllParticipantsInSessionView()
        throws RemoteException;

    /**
     * @return the first label text on the session view, which should be showed
     *         if there are no session.
     * @throws RemoteException
     */
    public String getFirstLabelTextInViewContent() throws RemoteException;

    /**
     * 
     * @return the JID of the local user
     * @throws RemoteException
     */
    public JID getJID() throws RemoteException;

    /**
     * @return the JID of the followed user or null if currently no user is
     *         followed.
     * 
     */
    public JID getFollowedBuddy() throws RemoteException;

    public void waitUntilIsInconsistencyDetected() throws RemoteException;

    /**
     * waits until the session is open.
     * 
     * @throws RemoteException
     */
    public void waitUntilIsInSession() throws RemoteException;

    /**
     * waits until the session by the defined peer is open.
     * 
     * @throws RemoteException
     */
    public void waitUntilIsInviteeInSession(SarosBot sarosBot)
        throws RemoteException;

    /**
     * Waits until the {@link SarosSessionManager#getSarosSession()} is null.
     * <p>
     * <b>Attention</b>:<br/>
     * After a action is performed, you immediately try to assert a condition is
     * true/false or perform a following action which based on that the current
     * performed action is completely finished, e.g. alice.state.isInSession is
     * false after alice leave the session by running the
     * {@link SessionViewImp#leaveTheSession()} and confirming the appeared pop
     * up window without this waitUntil. In this case, you may get the
     * AssertException, because alice should not really leave the session yet
     * during asserting the condition or performing a following action. So it is
     * recommended that you wait until the session is completely closed before
     * you run the assertion or perform a following action.
     * 
     * @throws RemoteException
     */
    public void waitUntilIsNotInSession() throws RemoteException;

    /**
     * Waits until the {@link SarosSessionManager#getSarosSession()} is null.
     * <p>
     * <b>Attention</b>:<br/>
     * After a action is performed, you immediately try to assert a condition is
     * true/false or perform a following action which based on that the current
     * performed action is completely finished, e.g.
     * assertFalse(alice.state.hasWriteAccess(bob.jid)) after bob leave the
     * session by running the {@link SessionViewImp#leaveTheSession()} and
     * confirming the appeared pop up window without this waitUntil. In this
     * case, you may get the AssertException, because bob should not really
     * leave the session yet during asserting a condition or performing a
     * following action. So it is recommended that you wait until the session by
     * the defined peer is completely closed before you run a assertion or
     * perform a following action.
     * 
     * 
     * @throws RemoteException
     */
    public void waitUntilIsInviteeNotInSession(SarosBot sarosBot)
        throws RemoteException;

    /**
     * waits until the local user has {@link User.Permission#WRITE_ACCESS} after
     * host grants him {@link User.Permission#WRITE_ACCESS}. This method should
     * be used after performing the action
     * 
     * 
     * @throws RemoteException
     */
    public void waitUntilHasWriteAccess() throws RemoteException;

    /**
     * waits until the given user has {@link User.Permission#WRITE_ACCESS} after
     * host grant him {@link User.Permission#WRITE_ACCESS}. This method should
     * be used after performing the action
     * 
     * 
     * @throws RemoteException
     */
    public void waitUntilHasWriteAccessBy(final JID jid) throws RemoteException;

    public void waitUntilHasWriteAccessBy(final String tableItemText)
        throws RemoteException;

    /**
     * waits until the local user has no more
     * {@link User.Permission#WRITE_ACCESS} after host has
     * {@link User.Permission#READONLY_ACCESS}. This method should be used after
     * performing the action
     * 
     * 
     * @throws RemoteException
     */
    public void waitUntilHasReadOnlyAccess() throws RemoteException;

    public void waitUntilHasReadOnlyAccessBy(final JID jid)
        throws RemoteException;

    public void waitUntilHasReadOnlyAccessBy(final String tableItemText)
        throws RemoteException;

    public void waitUntilIsFollowingBuddy(final JID followedBuddyJID)
        throws RemoteException;

    public void waitUntilIsNotFollowingBuddy(final JID foolowedBuddyJID)
        throws RemoteException;

    public void waitUntilAllPeersLeaveSession(
        final List<JID> jidsOfAllParticipants) throws RemoteException;
}
