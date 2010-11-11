package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.TableObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.ViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ISarosState;

/**
 * This interface contains convenience API to perform a action using widgets in
 * session view. then you can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Musician} object in your junit-test.
 * (How to do it please look at the javadoc in class {@link TestPattern} or read
 * the user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * then you can use the object sessionV initialized in {@link Musician} to
 * access all the useful API to do what you want :), e.g.
 * 
 * <pre>
 * alice.sessionV.openSharedSessionView();
 * bot.button(&quot;hello world&quot;).click();
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface SessionViewObject extends Remote {

    /**
     * Test if you are now in a session. <br>
     * This function check if the tool bar button "Leave the session" in the
     * session view is enabled. You can also use another function
     * {@link ISarosState#isInSession()}, which test the session state without
     * GUI.
     * 
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * <li>Try to use the {@link SessionViewObject#isInSession()} and
     * {@link ISarosState#isInSession()} together in your junittests.</li>
     * </ol>
     * 
     * @return <tt>true</tt> if the tool bar button "Leave the session" is
     *         enabled.
     * 
     * @throws RemoteException
     */
    public boolean isInSession() throws RemoteException;

    /**
     * @throws RemoteException
     * @see ViewObject#openViewById(String)
     */
    public void openSessionView() throws RemoteException;

    /**
     * 
     * @return <tt>true</tt> if all the opened views contains the session view.
     * 
     * @throws RemoteException
     * @see ViewObject#isViewOpen(String)
     */
    public boolean isSessionViewOpen() throws RemoteException;

    public void waitUntilSessionOpen() throws RemoteException;

    public void waitUntilSessionOpenBy(ISarosState state)
        throws RemoteException;

    /**
     * @see ViewObject#setFocusOnViewByTitle(String)
     * @throws RemoteException
     */
    public void setFocusOnSessionView() throws RemoteException;

    /**
     * @return <tt>true</tt> if session view is active.
     * 
     * @throws RemoteException
     * @see ViewObject#isViewActive(String)
     */
    public boolean isSessionViewActive() throws RemoteException;

    /**
     * @throws RemoteException
     * @see ViewObject#closeViewById(String)
     */
    public void closeSessionView() throws RemoteException;

    public void waitUntilSessionCloses() throws RemoteException;

    public void waitUntilSessionClosedBy(ISarosState state)
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
     * @param contactName
     *            the name, which listed in the session view. e.g. "You" or
     *            "alice1_fu@jabber.ccc.de (Driver)" or "Bob1_fu@jabber.ccc.de".
     * @return <tt>true</tt> if the passed contactName is in the contact list in
     *         the session view.
     * @throws RemoteException
     */
    public boolean isContactInSessionView(String contactName)
        throws RemoteException;

    /**
     * Perform the action "Give driver Role" which should be activated by
     * clicking the context menu "Give driver Role" of the tableItem with
     * itemText e.g. "bob1_fu@jabber.ccc.de" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * <li>Waits until the shell "Progress Information" is closed. It guarantee
     * that the "Give driver Role" action is completely done.</li>
     * <li>There are same function {@link Musician#giveDriverRole(Musician)}
     * defined In the {@link Musician} class, which reference to this. The goal
     * is to see, Whether the passed parameter is correct, if not, throws a
     * runtimeException. So for your tests you should only need to use the
     * function: {@link Musician#giveDriverRole(Musician)}.</li>
     * </ol>
     * 
     * @param stateOfInvitee
     *            the {@link ISarosState} of the user whom you want to give
     *            drive role.
     * @throws RemoteException
     */
    public void giveDriverRole(ISarosState stateOfInvitee)
        throws RemoteException;

    /**
     * Using this function host can perform the action
     * "Give exclusive driver Role" which should be activated by clicking the
     * context menu "Give exclusive driver Role" of the tableItem with itemText
     * e.g. "bob1_fu@jabber.ccc.de" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * <li>Waits until the shell "Progress Information" is closed. It guarantee
     * that the "Give exclusive driver Role" action is completely done.</li>
     * <li>There are same function
     * {@link Musician#giveExclusiveDriverRole(Musician)} defined In the
     * {@link Musician} class, which reference to this. The goal is to see,
     * Whether the passed parameter is correct, if not, throws a
     * runtimeException. So for your tests you should only need to use the
     * function: {@link Musician#giveExclusiveDriverRole(Musician)}.</li>
     * </ol>
     * 
     * @param inviteeBaseJID
     *            the {@link JID#getBase()} of the user whom you want to give
     *            exclusive drive role.
     * @throws RemoteException
     */
    public void giveExclusiveDriverRole(String inviteeBaseJID)
        throws RemoteException;

    /**
     * Using this function host can perform the action "Remove driver Role"
     * which should be activated by clicking the context menu
     * "Remove driver Role" of the tableItem with itemText e.g.
     * "bob1_fu@jabber.ccc.de (Driver)" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * <li>Waits until the shell "Progress Information" is closed. It guarantee
     * that the "Remove driver Role" action is completely done.</li>
     * <li>There are same function {@link Musician#removeDriverRole(Musician)}
     * defined In the {@link Musician} class, which reference to this. The goal
     * is to see, Whether the passed parameter is correct, if not, throws a
     * runtimeException. So for your tests you should only need to use the
     * function: {@link Musician#removeDriverRole(Musician)}.</li>
     * </ol>
     * 
     * @param inviteeBaseJID
     *            the {@link JID#getBase()} of the user whose drive role you
     *            want to remove.
     * @throws RemoteException
     */
    public void removeDriverRole(String inviteeBaseJID) throws RemoteException;

    /**
     * Using this function host can perform the action "Follow this user" which
     * should be activated by clicking the context menu "Follow this user" of
     * the tableItem with itemText e.g. "alice1_fu@jabber.ccc.de (Driver)" in
     * the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @param stateOfFollowedUser
     *            the {@link ISarosState} of the user whom you want to follow.
     * @throws RemoteException
     */
    public void followThisUser(ISarosState stateOfFollowedUser)
        throws RemoteException;

    /**
     * Test if you are in follow mode. <br>
     * This function check if the context menu "Stop following this user" of
     * every contact listed in the session view exists and is enabled. You can
     * also use another function {@link ISarosState#isInFollowMode()}, which
     * test the following state without GUI.
     * 
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * <li>Try to use only the function{@link ISarosState#isInSession()} for
     * your junittests, because the method
     * {@link TableObject#existContextOfTableItem(String, String)} need to be
     * still optimized.</li>
     * </ol>
     * 
     * @return <tt>true</tt> if the tool bar button "Leave the session" is
     *         enabled.
     * 
     * @throws RemoteException
     */
    public boolean isInFollowMode() throws RemoteException;

    /**
     * This function do same as the
     * {@link SessionViewObject#stopFollowingThisUser(ISarosState)} except you
     * don't need to pass the {@link ISarosState} of the user followed by you to
     * the function. It is very useful, if you don't exactly know whom you are
     * now following. Instead, we get the followed user JID using the method
     * {@link ISarosState#getFollowedUserJID()}.
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
     * Using this function host can perform the action
     * "Stop following this user" which should be activated by clicking the
     * context menu "Stop following this user" of the tableItem with itemText
     * e.g. "alice1_fu@jabber.ccc.de (Driver)" in the session view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the session view is open and active.</li>
     * </ol>
     * 
     * @param stateOfFollowedUser
     *            the {@link ISarosState} of the user whom you want to stop
     *            following.
     * @throws RemoteException
     */
    public void stopFollowingThisUser(ISarosState stateOfFollowedUser)
        throws RemoteException;

    /**
     * check if the context menu "Stop following this user" of a contact listed
     * in the session view is enabled. It would be used by
     * {@link SessionViewObjectImp#isInFollowMode()}.
     * 
     * @param contactName
     *            the name, which listed in the session view. e.g. "You" or
     *            "alice1_fu@jabber.ccc.de (Driver)" or "Bob1_fu@jabber.ccc.de".
     * @return <tt>true</tt> if the context menu "Following this user" of the
     *         passed contactName listed in the session view is enabled.
     * @throws RemoteException
     */
    public boolean isStopFollowingThisUserEnabled(String contactName)
        throws RemoteException;

    /**
     * check if the context menu "Stop following this user" of a contact listed
     * in the session view is visible.
     * 
     * @param contactName
     *            the name, which listed in the session view. e.g. "You" or
     *            "alice1_fu@jabber.ccc.de (Driver)" or "Bob1_fu@jabber.ccc.de".
     * @return <tt>true</tt> if the context menu "Following this user" of the
     *         passed contactName listed in the session view is visible.
     * @throws RemoteException
     */
    public boolean isStopFollowingThisUserVisible(String contactName)
        throws RemoteException;

    public void waitUntilFollowed(String plainJID) throws RemoteException;

    public void shareYourScreenWithSelectedUser(ISarosState respondentState)
        throws RemoteException;

    public void stopSessionWithUser(String name) throws RemoteException;

    public void sendAFileToSelectedUser(String inviteeJID)
        throws RemoteException;

    public void openInvitationInterface() throws RemoteException;

    public void startAVoIPSession() throws RemoteException;

    public void noInconsistencies() throws RemoteException;

    public void removeAllRriverRoles() throws RemoteException;

    public void enableDisableFollowMode() throws RemoteException;

    public void leaveTheSession() throws RemoteException;

    public void waitUntilAllPeersLeaveSession(List<JID> jids)
        throws RemoteException;

    public void jumpToPositionOfSelectedUser(String participantJID, String sufix)
        throws RemoteException;

    public boolean isToolbarNoInconsistenciesEnabled() throws RemoteException;

    public void invitateUser(String inviteeJID) throws RemoteException;

    public void leaveSessionByHost() throws RemoteException;

    public void leaveSessionByPeer() throws RemoteException;

}
