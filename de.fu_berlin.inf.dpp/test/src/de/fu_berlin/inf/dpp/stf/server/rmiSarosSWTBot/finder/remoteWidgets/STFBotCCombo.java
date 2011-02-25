package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface STFBotCCombo extends Remote {

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

    public void setSelection(int indexOfSelection) throws RemoteException;

    public String selection() throws RemoteException;

    public int selectionIndex() throws RemoteException;

    public void setSelection(String text) throws RemoteException;

    public void setText(String text) throws RemoteException;

    public void setFocus() throws RemoteException;

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isEnabled() throws RemoteException;

    public boolean isVisible() throws RemoteException;

    public boolean isActive() throws RemoteException;

    public String getText() throws RemoteException;

    public String getToolTipText() throws RemoteException;

    public int itemCount() throws RemoteException;

    public String[] items() throws RemoteException;

    public int textLimit() throws RemoteException;
}
