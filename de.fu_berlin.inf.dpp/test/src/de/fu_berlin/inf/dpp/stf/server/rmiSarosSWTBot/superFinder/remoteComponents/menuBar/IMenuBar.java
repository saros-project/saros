package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus.ISarosM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus.IWindowM;

public interface IMenuBar extends Remote {

    public ISarosM saros() throws RemoteException;

    public IWindowM window() throws RemoteException;

}