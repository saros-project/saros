package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.ISessionView;

public interface ISarosContextMenuWrapper extends Remote {

    /**********************************************
     * 
     * contextMenus showed in session View
     * 
     **********************************************/

    /**
     * Grant the selected buddy a write access by clicking his contextMenu
     * {@link STF#CM_GRANT_WRITE_ACCESS}.
     */
    public void grantWriteAccess() throws RemoteException;

    /**
     * Restrict the selected buddy to read only access by clicking his
     * contextMenu {@link STF#CM_RESTRICT_TO_READ_ONLY_ACCESS}
     * 
     * @throws RemoteException
     */
    public void restrictToReadOnlyAccess() throws RemoteException;

    /**
     * Follow the selected buddy by clicking his contextMenu
     * {@link STF#CM_FOLLOW_THIS_BUDDY}
     * 
     * @throws RemoteException
     */
    public void followThisBuddy() throws RemoteException;

    /**
     * Stop following the selected buddy by clicking his contextMenu
     * {@link STF#CM_STOP_FOLLOWING_THIS_BUDDY}
     * 
     * @throws RemoteException
     */
    public void stopFollowingThisBuddy() throws RemoteException;

    /**
     * Jump to the position of the selected buddy by clicking his contextMenu
     * {@link STF#CM_JUMP_TO_POSITION_SELECTED_BUDDY}
     * 
     * @throws RemoteException
     */
    public void jumpToPositionOfSelectedBuddy() throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if the selected buddy has write access.
     * @throws RemoteException
     */
    public boolean hasWriteAccess() throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if the selected buddy has read only access.
     * @throws RemoteException
     */
    public boolean hasReadOnlyAccess() throws RemoteException;

    /**
     * 
     * @return<tt>true</tt>, if the local user is following the selected buddy
     * @throws RemoteException
     */
    public boolean isFollowingThisBuddy() throws RemoteException;

    /**
     * wait until the condition {@link ISarosContextMenuWrapper#hasWriteAccess()}
     * is true . This method should be used after performing the action
     * {@link ISarosContextMenuWrapper#grantWriteAccess()} to guarantee the
     * invitee has really got {@link User.Permission#WRITE_ACCESS} .
     * 
     * @throws RemoteException
     */
    public void waitUntilHasWriteAccess() throws RemoteException;

    /**
     * waits until the condition
     * {@link ISarosContextMenuWrapper#hasReadOnlyAccess()} is true. This method
     * should be used after performing the action
     * {@link ISarosContextMenuWrapper#restrictToReadOnlyAccess()} or
     * {@link ISessionView#restrictInviteesToReadOnlyAccess()} to guarantee
     * invitee has only read access.
     * 
     * @throws RemoteException
     */
    public void waitUntilHasReadOnlyAccess() throws RemoteException;

    /**
     * Wait until the condition
     * {@link ISarosContextMenuWrapper#isFollowingThisBuddy()} is true.
     * 
     * @throws RemoteException
     */
    public void waitUntilIsFollowingThisBuddy() throws RemoteException;

    /**
     * Wait until the condition
     * {@link ISarosContextMenuWrapper#isFollowingThisBuddy()} is false.
     * 
     * @throws RemoteException
     */
    public void waitUntilIsNotFollowingThisBuddy() throws RemoteException;

    /**********************************************
     * 
     * contextMenus showed in buddies View
     * 
     **********************************************/
    /**
     * Delete the selected buddy by clicking his contextMenu
     * {@link STF#CM_DELETE}
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the roster view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the context menu..</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void delete() throws RemoteException;

    /**
     * Rename the selected buddy'name to the passed newName
     * 
     * @param newBuddyName
     *            the new nickName
     * @throws RemoteException
     */
    public void rename(String newBuddyName) throws RemoteException;

    /**
     * Invite the selected buddy by clicking his contextMenu
     * {@link STF#CM_INVITE_BUDDY}
     * 
     * @throws RemoteException
     */
    public void inviteBuddy() throws RemoteException;

}
