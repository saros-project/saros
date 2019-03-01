package saros.stf.server.rmi.superbot.component.contextmenu.peview;

import java.rmi.Remote;
import java.rmi.RemoteException;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.INewC;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IRefactorC;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IRunAsContextMenu;
import saros.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IShareWithC;

public interface IContextMenusInPEView extends Remote {
  public IShareWithC shareWith() throws RemoteException;

  public void open() throws RemoteException;

  public void openWith(String editorType) throws RemoteException;

  public void delete() throws RemoteException;

  public INewC newC() throws RemoteException;

  public IRefactorC refactor() throws RemoteException;

  public IRunAsContextMenu runAs() throws RemoteException;

  public void refresh() throws RemoteException;

  public void copy() throws RemoteException;

  public void paste(String target) throws RemoteException;

  public boolean existsWithRegex(String name) throws RemoteException;

  public boolean exists(String name) throws RemoteException;
}
