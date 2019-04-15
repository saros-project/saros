package saros.stf.server.rmi.superbot.component.contextmenu.sarosview;

import java.rmi.RemoteException;
import saros.net.xmpp.JID;
import saros.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.IWorkTogetherOnContextMenu;

public interface IContextMenusInContactListArea extends IContextMenusInSarosView {

  public void delete() throws RemoteException;

  public void rename(String nickname) throws RemoteException;

  public void addToSarosSession() throws RemoteException;

  public void addContact(JID jid) throws RemoteException;

  public IWorkTogetherOnContextMenu workTogetherOn() throws RemoteException;
}
