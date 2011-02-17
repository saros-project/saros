package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface STFBotCCombo extends Remote {

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public abstract void setSelection(int indexOfSelection)
        throws RemoteException;

    public abstract void selection() throws RemoteException;

    public abstract void selectionIndex() throws RemoteException;

    public abstract void setSelection(String text) throws RemoteException;

    public abstract void setText(String text) throws RemoteException;

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

    public abstract int itemCount() throws RemoteException;

    public abstract String[] items() throws RemoteException;

    public abstract int textLimit() throws RemoteException;
}
