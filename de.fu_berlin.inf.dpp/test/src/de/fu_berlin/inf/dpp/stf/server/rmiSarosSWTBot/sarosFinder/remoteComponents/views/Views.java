package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SarosBuddiesView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionView;

public interface Views extends Remote {

    public ChatView chatView() throws RemoteException;

    public SarosBuddiesView buddiesView() throws RemoteException;

    public RSView remoteScreenView() throws RemoteException;

    public SessionView sessionView() throws RemoteException;

    public ConsoleView consoleView() throws RemoteException;

    public PEView packageExplorerView() throws RemoteException;

    public ProgressView progressView() throws RemoteException;

}