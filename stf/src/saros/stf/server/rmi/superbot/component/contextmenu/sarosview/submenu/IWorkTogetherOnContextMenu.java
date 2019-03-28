package saros.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu;

import java.rmi.Remote;
import java.rmi.RemoteException;
import saros.net.xmpp.JID;

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
