package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.IRSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.ISarosView;

public interface IViews extends Remote {

    public ISarosView sarosView() throws RemoteException;

    public IRSView remoteScreenView() throws RemoteException;

    public IConsoleView consoleView() throws RemoteException;

    public IPEView packageExplorerView() throws RemoteException;

    public IProgressView progressView() throws RemoteException;

}