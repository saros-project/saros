package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface STFBotViewMenu extends Remote {

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void click() throws RemoteException;

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public String getToolTipText() throws RemoteException;

    public String getText() throws RemoteException;

    public boolean isChecked() throws RemoteException;
}
