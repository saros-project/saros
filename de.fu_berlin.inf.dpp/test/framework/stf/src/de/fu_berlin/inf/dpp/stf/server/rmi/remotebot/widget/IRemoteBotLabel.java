package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotLabel extends Remote {

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    public IRemoteBotMenu contextMenu(String text) throws RemoteException;

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void setFocus() throws RemoteException;

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public int alignment() throws RemoteException;

    public boolean isEnabled() throws RemoteException;

    public boolean isVisible() throws RemoteException;

    public boolean isActive() throws RemoteException;

    public String getToolTipText() throws RemoteException;

    public String getText() throws RemoteException;

}
