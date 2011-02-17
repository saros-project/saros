package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface STFBotList extends Remote {

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public String itemAt(int index) throws RemoteException;

    public int itemCount() throws RemoteException;

    public int indexOf(String item) throws RemoteException;

    public void select(String item) throws RemoteException;

    public void select(int... indices) throws RemoteException;

    public void select(int index) throws RemoteException;

    public void select(String... items) throws RemoteException;

    public void selectionCount() throws RemoteException;

    public void unselect() throws RemoteException;

    public void setFocus() throws RemoteException;

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public String[] getItems() throws RemoteException;

    public boolean isEnabled() throws RemoteException;

    public boolean isVisible() throws RemoteException;

    public boolean isActive() throws RemoteException;

    public String getText() throws RemoteException;

    public String getToolTipText() throws RemoteException;
}
