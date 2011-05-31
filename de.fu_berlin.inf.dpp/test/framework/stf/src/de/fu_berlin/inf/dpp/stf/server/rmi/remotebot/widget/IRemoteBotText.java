package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotText extends Remote {

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
    public IRemoteBotText selectAll() throws RemoteException;

    public void setFocus() throws RemoteException;

    public IRemoteBotText setText(String text) throws RemoteException;

    public IRemoteBotText typeText(String text) throws RemoteException;

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public String getText() throws RemoteException;

    public boolean isEnabled() throws RemoteException;

    public boolean isVisible() throws RemoteException;

    public boolean isActive() throws RemoteException;

    public String getToolTipText() throws RemoteException;
}
