package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu;

import de.fu_berlin.inf.dpp.stf.shared.Constants;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface contains convenience API to perform actions activated by clicking subMenus of
 * contextMenu {@link Constants#CM_RUN_AS} in the package explorer view.
 */
public interface IRunAsContextMenu extends Remote {

  /**
   * Runs the selected item as java application. The selected item has to be a runnable java class.
   */
  public void javaApplication() throws RemoteException;
}
