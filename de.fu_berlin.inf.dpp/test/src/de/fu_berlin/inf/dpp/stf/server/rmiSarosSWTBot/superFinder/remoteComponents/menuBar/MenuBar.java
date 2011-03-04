package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MenuBar extends Remote {

    public SarosM saros() throws RemoteException;

    public WindowM window() throws RemoteException;

}