package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IProgressView extends Remote {

  /**
   * remove the progress. ie. Click the gray clubs delete icon.
   *
   * @throws RemoteException
   */
  public void removeProgress() throws RemoteException;

  public void removeProcess(int index) throws RemoteException;
}
