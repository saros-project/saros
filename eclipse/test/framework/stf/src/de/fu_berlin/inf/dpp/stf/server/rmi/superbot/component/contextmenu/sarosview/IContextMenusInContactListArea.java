package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.submenu.IWorkTogetherOnContextMenu;
import java.rmi.RemoteException;

public interface IContextMenusInContactListArea extends IContextMenusInSarosView {

  public void delete() throws RemoteException;

  public void rename(String nickname) throws RemoteException;

  public void addToSarosSession() throws RemoteException;

  public void addContact(JID jid) throws RemoteException;

  public IWorkTogetherOnContextMenu workTogetherOn() throws RemoteException;
}
