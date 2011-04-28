package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.sarosView;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.sarosView.submenus.IWorkTogetherOnC;

public interface IContextMenusInBuddiesArea extends IContextMenusInSarosView {

    /**********************************************
     * 
     * contextMenus showed in buddies View
     * 
     **********************************************/

    public void delete() throws RemoteException;

    public void rename(String newBuddyName) throws RemoteException;

    public void addToSarosSession() throws RemoteException;

    public void addBuddy(JID jid) throws RemoteException;

    public IWorkTogetherOnC workTogetherOn() throws RemoteException;

}