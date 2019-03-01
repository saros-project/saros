package saros.stf.server.rmi.superbot.component.contextmenu.sarosview;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IContextMenusInSarosView extends Remote {

  /** This function opens a chat by selecting the respective context menu item. */
  public void openChat() throws RemoteException;
}
