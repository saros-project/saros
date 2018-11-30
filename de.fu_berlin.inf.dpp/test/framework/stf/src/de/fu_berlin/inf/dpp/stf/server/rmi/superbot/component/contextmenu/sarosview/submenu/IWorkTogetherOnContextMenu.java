package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IWorkTogetherOnContextMenu extends Remote {

  /**
   * ********************************************
   *
   * <p>actions
   *
   * <p>********************************************
   */
  public void multipleProjects(String projectName, JID... baseJIDOfInvitees) throws RemoteException;

  public void project(String projectName) throws RemoteException;
}
