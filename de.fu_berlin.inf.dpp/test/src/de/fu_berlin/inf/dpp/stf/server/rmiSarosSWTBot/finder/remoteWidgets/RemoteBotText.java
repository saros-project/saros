package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteBotText extends Remote {

    /**********************************************
     * 
     * finders
     * 
     **********************************************/

    public RemoteBotMenu contextMenu(String text) throws RemoteException;

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public RemoteBotText selectAll() throws RemoteException;

    public void setFocus() throws RemoteException;

    public RemoteBotText setText(String text) throws RemoteException;

    public RemoteBotText typeText(String text) throws RemoteException;

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
