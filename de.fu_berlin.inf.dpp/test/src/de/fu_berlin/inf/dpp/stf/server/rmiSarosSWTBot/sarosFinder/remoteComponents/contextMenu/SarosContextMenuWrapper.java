package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.Remote;
import java.rmi.RemoteException;

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
}
