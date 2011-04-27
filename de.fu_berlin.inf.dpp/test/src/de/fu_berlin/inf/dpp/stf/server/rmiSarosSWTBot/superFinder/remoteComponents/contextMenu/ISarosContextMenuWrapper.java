package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISarosContextMenuWrapper extends Remote {

    /**********************************************
     * 
     * contextMenus showed in sarosview
     * 
     **********************************************/

    public void stopSarosSession() throws RemoteException;

}
