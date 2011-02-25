package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface STFBotToolbarButton extends Remote {

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    public STFBotMenu contextMenu(String text) throws RemoteException;

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public abstract void click() throws RemoteException;

    public abstract void clickAndWait() throws RemoteException;

    public abstract void setFocus() throws RemoteException;

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public abstract boolean isEnabled() throws RemoteException;

    public abstract boolean isVisible() throws RemoteException;

    public abstract boolean isActive() throws RemoteException;

    public abstract String getText() throws RemoteException;

    public abstract String getToolTipText() throws RemoteException;

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public abstract void waitUntilIsEnabled() throws RemoteException;

}
