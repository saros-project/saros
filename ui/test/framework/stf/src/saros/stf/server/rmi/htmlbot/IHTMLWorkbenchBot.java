package saros.stf.server.rmi.htmlbot;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface is part of the GUI test framework and contains methods concerning the handling of
 * the IDE-specific part that contains the main view of Saros's HTML GUI. This interface needs to be
 * implemented individually for each supported IDE.
 */
public interface IHTMLWorkbenchBot extends Remote {

  /**
   * Opens the HTML view of Saros inside the IDE.
   *
   * @throws RemoteException
   */
  void openSarosBrowserView() throws RemoteException;

  /**
   * Closes the HTML view of Saros inside the IDE.
   *
   * @throws RemoteException
   */
  void closeSarosBrowserView() throws RemoteException;

  /**
   * Tests if the HTML view is already open.
   *
   * @return <code>true</code> iff it is open
   * @throws RemoteException
   */
  boolean isSarosBrowserViewOpen() throws RemoteException;
}
