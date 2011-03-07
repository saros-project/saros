package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteBotPerspective extends Remote {

    public void activate() throws RemoteException;

    public String getLabel() throws RemoteException;

    public boolean isActive() throws RemoteException;
}
