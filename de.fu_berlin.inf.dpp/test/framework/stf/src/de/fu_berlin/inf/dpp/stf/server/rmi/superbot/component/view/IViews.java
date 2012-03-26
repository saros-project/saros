package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IPackageExplorerView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.IRSView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.ISarosView;

public interface IViews extends Remote {

    public ISarosView sarosView() throws RemoteException;

    public IRSView remoteScreenView() throws RemoteException;

    public IConsoleView consoleView() throws RemoteException;

    public IPackageExplorerView packageExplorerView() throws RemoteException;

    public IProgressView progressView() throws RemoteException;

}