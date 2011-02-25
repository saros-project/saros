package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface STFBotText extends Remote {

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
    public STFBotText selectAll() throws RemoteException;

    public void setFocus() throws RemoteException;

    public STFBotText setText(String text) throws RemoteException;

    public STFBotText typeText(String text) throws RemoteException;

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
