package saros.stf.server.rmi.superbot.component.view;

import java.rmi.Remote;
import java.rmi.RemoteException;
import saros.stf.server.rmi.superbot.component.view.eclipse.IConsoleView;
import saros.stf.server.rmi.superbot.component.view.eclipse.IPackageExplorerView;
import saros.stf.server.rmi.superbot.component.view.eclipse.IProgressView;
import saros.stf.server.rmi.superbot.component.view.saros.ISarosView;

public interface IViews extends Remote {

  public ISarosView sarosView() throws RemoteException;

  public IConsoleView consoleView() throws RemoteException;

  public IPackageExplorerView packageExplorerView() throws RemoteException;

  public IProgressView progressView() throws RemoteException;
}
