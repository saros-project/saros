package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionView;

public interface SarosContextMenuWrapper extends Remote {

    public void grantWriteAccess() throws RemoteException;

    public void restrictToReadOnlyAccess() throws RemoteException;

    public void followThisBuddy() throws RemoteException;

    public void stopFollowingThisBuddy() throws RemoteException;

    public void jumpToPositionOfSelectedBuddy() throws RemoteException;

    /**
     * performs the action "Delete contact" which should be activated by
     * clicking the context menu with the text "Delete contact" on the roster
     * view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Make sure, the roster view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the context menu. I
     * mean, after clicking the menu you need to treat the following popup
     * window too.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void delete() throws RemoteException;

    /**
     * rename the buddy'name specified with the given baseJID to the given
     * newName
     * 
     * @param newBuddyName
     *            the new name , to which the contact should be changed
     * @throws RemoteException
     */
    public void rename(String newBuddyName) throws RemoteException;

    public void inviteBuddy() throws RemoteException;

    public boolean hasWriteAccess() throws RemoteException;

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

    /**
     * 
     * @return<tt>true</tt>, if the you have read only access.
     * @throws RemoteException
     */
    public boolean hasReadOnlyAccess() throws RemoteException;

    public boolean isFollowingThisBuddy() throws RemoteException;

    public void waitUntilIsFollowingThisBuddy() throws RemoteException;

    public void waitUntilIsNotFollowingThisBuddy() throws RemoteException;

}
