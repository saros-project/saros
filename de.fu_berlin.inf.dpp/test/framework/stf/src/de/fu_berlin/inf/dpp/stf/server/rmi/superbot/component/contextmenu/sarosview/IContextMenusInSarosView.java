package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IContextMenusInSarosView extends Remote {

    /**********************************************
     * 
     * contextMenus showed in sarosview
     * 
     **********************************************/

    public void stopSarosSession() throws RemoteException;

}
