package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view;

import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IPackageExplorerView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.ISarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.whiteboard.ISarosWhiteboardView;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IViews extends Remote {

  /**
   * Use this method to force the STF to use the HTML-GUI. This can be removed when the old GUI is
   * fully replaced.
   *
   * @throws RemoteException
   */
  public void runTestsOnHtmlGui() throws RemoteException;

  public ISarosView sarosView() throws RemoteException;

  public ISarosWhiteboardView sarosWhiteboardView() throws RemoteException;

  public IConsoleView consoleView() throws RemoteException;

  public IPackageExplorerView packageExplorerView() throws RemoteException;

  public IProgressView progressView() throws RemoteException;
}
