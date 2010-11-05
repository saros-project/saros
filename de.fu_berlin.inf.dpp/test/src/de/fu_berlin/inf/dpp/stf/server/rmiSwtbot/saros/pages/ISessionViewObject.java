package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.pages;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.ViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.noGUI.ISarosState;

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
public interface ISessionViewObject extends Remote {

    /**
     * @throws RemoteException
     * @see ViewObject#closeViewById(String)
     */
    public void closeSessionView() throws RemoteException;

    /**
     * @return <tt>true</tt> if session view is active.
     * 
     * @throws RemoteException
     * @see ViewObject#isViewActive(String)
     */
    public boolean isSessionViewActive() throws RemoteException;

    /**
     * 
     * @return <tt>true</tt> if all the opened views contains the session view.
     * 
     * @throws RemoteException
     * @see ViewObject#isViewOpen(String)
     */
    public boolean isSessionViewOpen() throws RemoteException;

    /**
     * @throws RemoteException
     * @see ViewObject#openViewById(String)
     */
    public void openSessionView() throws RemoteException;

    /**
     * @see ViewObject#setFocusOnViewByTitle(String)
     * @throws RemoteException
     */
    public void setFocusOnSessionView() throws RemoteException;

    /**
     * Using this function host can perform the action "Give driver Role" which
     * should be activated by clicking the context menu "Give driver Role" of
     * the tableItem with itemText "inviteeJID" in session view.
     * <p>
     * <b>Attention:</b> <br>
     * Make sure, the session view is open and active. <br>
     * Waits until the shell "Progress Information" is closed. It guarantee that
     * the "Give driver Role" action is completely done.
     * 
     * @param inviteeJID
     *            itemText of the tableItem which you want to selected
     * @throws RemoteException
     */
    public void giveDriverRole(String inviteeJID) throws RemoteException;

    /**
     * Using this function host can perform the action
     * "Give exclusive driver Role" which should be activated by clicking the
     * context menu "Give exclusive driver Role" of the tableItem with itemText
     * "inviteeJID" in session view.
     * <p>
     * <b>Attention:</b> <br>
     * Make sure, the session view is open and active. <br>
     * Waits until the shell "Progress Information" is closed. It guarantee that
     * the "Give driver Role" action is completely done.
     * 
     * @param inviteeJID
     *            itemText of the tableItem which you want to selected
     * @throws RemoteException
     */
    public void giveExclusiveDriverRole(String inviteeJID)
        throws RemoteException;

    /**
     * Test if you are now in a session. <br>
     * This function check if the tool bar button "Leave the session" in the
     * session view is enabled. You can also use another function
     * {@link ISarosState#isInSession()}, which test the session state without
     * GUI.
     * 
     * <p>
     * <b>Attention:</b> <br>
     * Make sure, the session view is open and active. <br>
     * Try to use the {@link ISessionViewObject#isInSession()} and
     * {@link ISarosState#isInSession()} together in your junittests.
     * 
     * 
     * @return <tt>true</tt> if the tool bar button "Leave the session" is
     *         enabled.
     * 
     * @throws RemoteException
     */
    public boolean isInSession() throws RemoteException;

    public boolean isContactInSessionView(String Contact)
        throws RemoteException;

    public boolean isFollowing() throws RemoteException;

    public void clickTBShareYourScreenWithSelectedUserInSPSView()
        throws RemoteException;

    public void clickTBStopSessionWithUserInSPSView(String name)
        throws RemoteException;

    public void clickTBSendAFileToSelectedUserInSPSView(String inviteeJID)
        throws RemoteException;

    public void clickTBOpenInvitationInterfaceInSPSView()
        throws RemoteException;

    public void clickTBStartAVoIPSessionInSPSView() throws RemoteException;

    public void clickTBNoInconsistenciesInSPSView() throws RemoteException;

    public void clickTBRemoveAllRriverRolesInSPSView() throws RemoteException;

    public void clickTBEnableDisableFollowModeInSPSView()
        throws RemoteException;

    public void clickTBLeaveTheSessionInSPSView() throws RemoteException;

    public void clickCMJumpToPositionOfSelectedUserInSPSView(
        String participantJID, String sufix) throws RemoteException;

    public void clickCMStopFollowingThisUserInSPSView(ISarosState state, JID jid)
        throws RemoteException;

    public void clickCMgiveExclusiveDriverRoleInSPSView(String inviteeJID)
        throws RemoteException;

    public void clickCMRemoveDriverRoleInSPSView(String inviteeJID)
        throws RemoteException;

}
