package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteBotMenu extends Remote {

    public void click() throws RemoteException;

    public RemoteBotMenuImp contextMenu(String text) throws RemoteException;

    public void waitUntilIsEnabled() throws RemoteException;

    public String getToolTipText() throws RemoteException;

    public String getText() throws RemoteException;

    public boolean isChecked() throws RemoteException;

    public boolean isActive() throws RemoteException;

    public boolean isVisible() throws RemoteException;

    public boolean isEnabled() throws RemoteException;

    public void setFocus() throws RemoteException;

    public RemoteBotMenu menu(String menuName) throws RemoteException;

}
