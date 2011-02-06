package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.ui.actions.RestrictInviteesToReadOnlyAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.RestrictToReadOnlyAccessAction;

/**
 * This interface contains convenience API to perform a action using widgets in
 * session view. then you can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
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
    /**
     * Test if you are now in a session. <br>
     * You can also use another function {@link SessionView#isInSession()} ,
     * which test the session state with GUI.
     * 
     * <p>
     * <b>Attention:</b> <br>
     * Try to use the {@link SessionView#isInSession()} and
     * {@link SarosState#isInSessionNoGUI()} together in your junittests.
     * 
     * 
     * @return <tt>true</tt> if {@link SarosSessionManager#getSarosSession()} is
     *         not null.
     * 
     * @throws RemoteException
     * @see SarosSessionManager#getSarosSession()
     */
    public boolean isInSessionNoGUI() throws RemoteException;

    /**
     * Test if you are now in a session. <br>
     * This function check if the tool bar button "Leave the session" in the
     * session view is enabled. You can also use another function
     * {@link SarosState#isInSessionNoGUI()}, which test the session state
     * without GUI.
     * 
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * <li>Try to use the {@link SessionView#isInSession()} and
     * {@link SarosState#isInSessionNoGUI()} together in your junit tests.</li>
     * </ol>
     * 
     * @return <tt>true</tt> if the tool bar button "Leave the session" is
     *         enabled.
     * 
     * @throws RemoteException
     */
    public boolean isInSession() throws RemoteException;

    /**
     * waits until the session is open.
     * 
     * @throws RemoteException
     * @see SarosConditions#isInSession(SarosState)
     */
    public void waitUntilIsInSession() throws RemoteException;

    /**
     * waits until the session by the defined peer is open.
     * 
     * @param sessionV
     *            the {@link SessionView} of the defined peer.
     * @throws RemoteException
     */
    public void waitUntilIsInviteeInSession(final SessionView sessionV)
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
     * @param sessionV
     *            the {@link SessionView} of the user, whose session should be
     *            closed.
     * @throws RemoteException
     */
    public void waitUntilIsInviteeNotInSession(final SessionView sessionV)
        throws RemoteException;

    /**
     * Test if a contact exists in the contact list in the session view.
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
     * @return <tt>true</tt> if the local client has
     *         {@link User.Permission#WRITE_ACCESS} of this shared project.
     *         false otherwise.
     * @throws RemoteException
     */
    public boolean hasWriteAccessNoGUI() throws RemoteException;

    /**
     * @return <tt>true</tt> if the given {@link JID} has
     *         {@link User.Permission#WRITE_ACCESS} in this
     *         {@link SharedProject}.
     * @throws RemoteException
     */
    public boolean hasWriteAccessByNoGUI(JID jid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if all given {@link JID}s have
     *         {@link User.Permission#WRITE_ACCESS} for this
     *         {@link SharedProject}.
     * @throws RemoteException
     */
    public boolean haveWriteAccessByNoGUI(List<JID> jids)
        throws RemoteException;

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
     * @return <tt>true</tt>, if the local user is a host.
     * @throws RemoteException
     */
    public boolean isHostNoGUI() throws RemoteException;

    /**
     * 
     * @param jid
     *            the JID of the user.
     * @return <tt>true</tt>, if the user specified by the given jid is a host.
     * @throws RemoteException
     */
    public boolean isHostNoGUI(JID jid) throws RemoteException;

    /**
     * 
     * @param jids
     * @return<tt>true</tt>, if the context menu "Restrict to read only access"
     *                       isn't enabled by given participants
     * @throws RemoteException
     */
    public boolean hasReadOnlyAccessBy(JID... jids) throws RemoteException;

    /**
     * @return <tt>true</tt>, if the local user has read-only access in this
     *         {@link SharedProject}.
     */
    public boolean hasReadOnlyAccessNoGUI() throws RemoteException;

    /**
     * @return <tt>true</tt>, if the given {@link JID} has read-only access in
     *         this {@link SharedProject}.
     */
    public boolean hasReadOnlyAccessNoGUI(JID jid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if all given {@link JID} have read-only of the
     *         project.
     */
    public boolean haveReadOnlyAccessNoGUI(List<JID> jids)
        throws RemoteException;

    /**
     * 
     * @return <tt>true</tt>, if the local user is in the session view
     * @throws RemoteException
     */
    public boolean isParticipant() throws RemoteException;

    /**
     * 
     * @return <tt>true</tt>, if the local user is a participant of the
     *         {@link SharedProject}.
     * @throws RemoteException
     */
    public boolean isParticipantNoGUI() throws RemoteException;

    /**
     * @return <tt>true</tt>, if the participant with the given JID exists in
     *         the session view
     * 
     */
    public boolean isParticipant(JID jid) throws RemoteException;

    /**
     * @return <tt>true</tt>, if the given {@link JID} is a participant of the
     *         {@link SharedProject}.
     */
    public boolean isParticipantNoGUI(JID jid) throws RemoteException;

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
     * @return <tt>true</tt>, if all given jids are participants of the project.
     */
    public boolean areParticipantsNoGUI(List<JID> jids) throws RemoteException;

    /**
     * Perform the action "Grant write access" which should be activated by
     * clicking the context menu "Grant write access" of a tableItem with
     * itemText e.g. "bob1_fu@jabber.ccc.de" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>Waits until the shell "Progress Information" is closed. It guarantee
     * that the "Grant write access" action is completely done.</li>
     * </ol>
     * 
     * @param participantJID
     *            the {@link JID} of the user whom you want to grant
     *            {@link User.Permission#WRITE_ACCESS}.
     * @throws RemoteException
     */
    public void grantWriteAccess(final JID participantJID)
        throws RemoteException;

    /**
     * waits until the local user has {@link User.Permission#WRITE_ACCESS} after
     * host grants him {@link User.Permission#WRITE_ACCESS}. This method should
     * be used after performing the action
     * {@link SessionView#grantWriteAccess(SessionView)} to guarantee the
     * invitee has really got {@link User.Permission#WRITE_ACCESS}.
     * 
     * @throws RemoteException
     */
    public void waitUntilHasWriteAccess() throws RemoteException;

    /**
     * waits until the given user has {@link User.Permission#WRITE_ACCESS} after
     * host grant him {@link User.Permission#WRITE_ACCESS}. This method should
     * be used after performing the action
     * {@link SessionView#grantWriteAccess(SessionView)} to guarantee the
     * invitee has really got the {@link User.Permission#WRITE_ACCESS}.
     * 
     * @throws RemoteException
     */
    public void waitUntilHasWriteAccessBy(final JID jid) throws RemoteException;

    /**
     * waits until the local user has no more
     * {@link User.Permission#WRITE_ACCESS} after host has
     * {@link User.Permission#READONLY_ACCESS}. This method should be used after
     * performing the action
     * {@link SessionView#restrictToReadOnlyAccess(SessionView)} or
     * {@link SessionView#restrictInviteesToReadOnlyAccess()} to guarantee the
     * invitee's {@link User.Permission#WRITE_ACCESS} is really removed
     * 
     * @throws RemoteException
     */
    public void waitUntilHasReadOnlyAccess() throws RemoteException;

    public void waitUntilHasReadOnlyAccessBy(final JID jid)
        throws RemoteException;

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
    public void restrictToReadOnlyAccess(final JID participantJID)
        throws RemoteException;

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
     * It get all participant names listed in the session view
     * 
     * @return list, which contain all the contact names.
     * @throws RemoteException
     */
    public List<String> getAllParticipantsInSessionView()
        throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if there are some label texts existed in the
     *                       session view. You can only see the label texts when
     *                       you are not in a session.
     * @throws RemoteException
     */
    public boolean existsLabelTextInSessionView() throws RemoteException;

    public boolean hasWriteAccess() throws RemoteException;

    public boolean hasWriteAccessBy(JID... jids) throws RemoteException;

    public boolean hasReadOnlyAccess() throws RemoteException;

    public boolean hastReadOnlyAccessBy(JID... jids) throws RemoteException;

    /**
     * @return the first label text on the session view, which should be showed
     *         if there are no session.
     * @throws RemoteException
     */
    public String getFirstLabelTextInSessionview() throws RemoteException;

    /**
     * Performs the action "Follow this user" which should be activated by
     * clicking the context menu "Follow this user" of the tableItem with
     * itemText e.g. "alice1_fu@jabber.ccc.de" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @param jidOfFollowedUser
     *            the {@link JID} of the user whom you want to follow.
     * @throws RemoteException
     */
    public void followThisBuddy(JID jidOfFollowedUser) throws RemoteException;

    /**
     * @return <tt>true</tt>, if you are currently following another user.
     * @throws RemoteException
     * @see EditorManager#isFollowing
     */
    public boolean isInFollowModeNoGUI() throws RemoteException;

    /**
     * 
     * @param baseJID
     *            the baseJID of the user, whom you are currently following
     * @return <tt>true</tt>, if you are currently following the given user.
     * @throws RemoteException
     */
    public boolean isFollowingBuddyNoGUI(String baseJID) throws RemoteException;

    /**
     * This function do same as the
     * {@link SessionView#stopFollowingThisBuddy(SarosState)} except you don't
     * need to pass the {@link SarosState} of the followed user to the function.
     * It is very useful, if you don't exactly know whom you are now following.
     * Instead, we get the followed user JID from the method
     * {@link SarosState#getFollowedBuddyJIDNoGUI()}.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void stopFollowing() throws RemoteException;

    /**
     * Performs the action "Stop following this user" which should be activated
     * by clicking the context menu "Stop following this user" of a tableItem
     * with the itemText e.g. "alice1_fu@jabber.ccc.de" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @param jidOfFollowedUser
     *            the {@link JID} of the user whom you want to stop following.
     * @throws RemoteException
     */
    public void stopFollowingThisBuddy(JID jidOfFollowedUser)
        throws RemoteException;

    /**
     * Waits until the {@link EditorManager#getFollowedUser()} is same as the
     * given user.
     * <p>
     * <b>Attention</b>:<br/>
     * After a action is performed, you immediately try to assert a condition is
     * true/false or perform a following action which based on that the current
     * performed action is completely finished, e.g. assert bob's workbench
     * state after bob follow the given user by running the
     * {@link SessionViewImp#followThisBuddy(SarosState)}without this waitUntil.
     * In this case, you may get the AssertException, because bob should not
     * really in the follow mode yet during asserting a condition or performing
     * a following action. So it is recommended that you wait until the session
     * is completely closed before you run the assertion or perform a following
     * action.
     * 
     * @param baseJIDOfFollowedUser
     * @throws RemoteException
     */
    public void waitUntilIsFollowingBuddy(String baseJIDOfFollowedUser)
        throws RemoteException;

    /**
     * performs the action "Share your screen with selected user" which should
     * be activated by clicking the tool bar button with the tooltip text
     * "Share your screen with selected user" on the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @param jidOfPeer
     *            the {@link JID} of the user with whom you want to share your
     *            screen.
     * @throws RemoteException
     */
    public void shareYourScreenWithSelectedBuddy(JID jidOfPeer)
        throws RemoteException;

    /**
     * performs the action "Stop share session with user" which should be
     * activated by clicking the tool bar button with the tooltip text
     * "Stop share session with user" on the session view, This toolbar button
     * is only visible after clicking the
     * {@link SessionView#shareYourScreenWithSelectedBuddy(JID)}
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @param jidOfPeer
     *            the {@link JID} of the user with whom you want to stop the
     *            screen session.
     * @throws RemoteException
     */
    public void stopSessionWithBuddy(JID jidOfPeer) throws RemoteException;

    /**
     * performs the action "Send a file to selected user" which should be
     * activated by clicking the tool bar button with the tooltip text
     * "Send a file to selected user" on the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>If there are some following actions which are activated by the action
     * "Send a file to selected user", e.g. a popup window. In this case, the
     * method should handle the popup window too.</li>
     * </ol>
     * 
     * TODO: this function isn't complete yet. SWTBot don't support native
     * dialog, So the action on the "Select the file to send" dialog can be
     * performed.
     * 
     * @param jidOfPeer
     *            the {@link JID} of the user with whom you want to share your
     *            screen.
     * @throws RemoteException
     */
    public void sendAFileToSelectedUserGUI(JID jidOfPeer)
        throws RemoteException;

    /**
     * performs the action "Start a VoIP session" which should be activated by
     * clicking the tool bar button with the tooltip text "Start a VoIP session"
     * on the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>If there are some following actions which are activated by the action
     * "Start a VoIP session", e.g. a popup window. In this case, e.g. a popup
     * window. In this case, the method should handle the popup window too.</li>
     * </ol>
     * 
     * TODO: this function isn't complete yet.
     * 
     * 
     * @param jidOfPeer
     *            the {@link JID} of the user with whom you want to share your
     *            screen.
     * @throws RemoteException
     */
    public void startAVoIPSession(JID jidOfPeer) throws RemoteException;

    /**
     * performs the action "inconsistency detected in ..." which should be
     * activated by clicking the tool bar button with the tooltip text
     * "inconsistency detected in ..." on the session view. The button is only
     * enabled, if there are inconsistency detected in a file.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>If there are some following actions which are activated by the action
     * "inconsistency detected in ...", e.g. a popup window. In this case, the
     * method should handle the popup window too.</li>
     * </ol>
     * 
     * TODO: this function isn't complete yet.
     * 
     * @throws RemoteException
     */
    public void inconsistencyDetected() throws RemoteException;

    /**
     * performs the {@link RestrictInviteesToReadOnlyAccessAction} which should
     * be activated by clicking the tool bar button with the tooltip text
     * {@link RestrictInviteesToReadOnlyAccessAction} on the session view. The
     * button is only enabled, if there users with
     * {@link User.Permission#WRITE_ACCESS} existed in the session.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>If there are some following actions which are activated by the action
     * "inconsistency detected in ...", e.g. a popup window. In this case, the
     * method should handle the popup window too.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void restrictInviteesToReadOnlyAccess() throws RemoteException;

    /**
     * Waits until all the given users are not in the session.
     * <p>
     * <b>Attention</b>:<br/>
     * After a action is performed, you immediately try to assert a condition is
     * true/false or perform a following action which based on that the current
     * performed action is completely finished, e.g. alice(host)leave the
     * session immediately after bob and carl leave the session without this
     * waitUntil. In this case, you may get a popup window what you did not
     * expected, because bob and carl should not really leave the session yet
     * during performing a following action. So it is recommended that you wait
     * until all the peers completely leave the session before you run a
     * assertion or perform a following action.
     * 
     * @param jids
     * @throws RemoteException
     * @see Tester#leaveSessionPeersFirstDone(Tester...)
     */
    public void waitUntilAllPeersLeaveSession(List<JID> jids)
        throws RemoteException;

    /**
     * Performs the action "Jump to position of selected user" which should be
     * activated by clicking the context menu
     * "SJump to position of selected user" of a tableItem with the itemText
     * e.g. "alice1_fu@jabber.ccc.de" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @param jidOfselectedUser
     *            the {@link JID} of the user whom you want to stop following.
     * @throws RemoteException
     */
    public void jumpToPositionOfSelectedBuddy(JID jidOfselectedUser)
        throws RemoteException;

    /**
     * waits until the toolbar button "inconsistency detected..." is enabled.
     * 
     * @throws RemoteException
     */
    public void waitUntilInconsistencyDetected() throws RemoteException;

    /**
     * performs the action "Open invitation interface" which should be activated
     * by clicking the tool bar button with the tooltip text
     * "Open invitation interface" on the session view. The button is only
     * enabled, if you are host.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>If there are some following actions which are activated by the action
     * "Open invitation interface", e.g. a popup window. In this case, the
     * method should handle the popup window too.</li>
     * </ol>
     * 
     * @param jidOfInvitees
     *            the users whom you want to invite to your session.
     * @throws RemoteException
     */
    public void openInvitationInterface(String... jidOfInvitees)
        throws RemoteException;

    /**
     * set the JID Of the lcoal user
     * 
     * @param localJID
     * @throws RemoteException
     */
    public void setJID(JID localJID) throws RemoteException;

    /**
     * 
     * @return the JID of the local user
     * @throws RemoteException
     */
    public JID getJID() throws RemoteException;

    /**
     * performs the action "Leave the session" which should be activated by
     * clicking the tool bar button with the tooltip text "Leave the session" on
     * the session view. After clicking the button you will get different popup
     * window depending on whether you are host or not. So if you are host,
     * please use this one, otherwise use
     * {@link SessionView#leaveTheSessionByPeer()}
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>If there are some following actions which are activated by the action
     * "Open invitation interface", e.g. a popup window. In this case, the
     * method should handle the popup window too.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void leaveTheSessionByHost() throws RemoteException;

    /**
     * performs the action "Leave the session" which should be activated by
     * clicking the tool bar button with the tooltip text "Leave the session" on
     * the session view. After clicking the button you will get different popup
     * window depending on whether you are host or not. So if you are peer,
     * please use this one, otherwise use
     * {@link SessionView#leaveTheSessionByHost()}
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the session view is open and active.</li>
     * <li>If there are some following actions which are activated by the action
     * "Open invitation interface", e.g. a popup window. In this case, the
     * method should handle the popup window too.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void leaveTheSessionByPeer() throws RemoteException;

    public void confirmShellClosingTheSession() throws RemoteException;

}
