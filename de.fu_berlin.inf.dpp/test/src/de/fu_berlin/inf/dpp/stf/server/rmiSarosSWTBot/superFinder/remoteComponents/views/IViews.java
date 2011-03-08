package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.IBuddiesView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.IChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.IRSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.ISessionView;

public interface IViews extends Remote {

    public IChatView chatView() throws RemoteException;

    public IBuddiesView buddiesView() throws RemoteException;

    public IRSView remoteScreenView() throws RemoteException;

    public ISessionView sessionView() throws RemoteException;

    public IConsoleView consoleView() throws RemoteException;

    public IPEView packageExplorerView() throws RemoteException;

    public IProgressView progressView() throws RemoteException;

}