package saros.stf.server.rmi.htmlbot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Represent an React Tree Element and makes it controllable via RMI.. */
public interface IRemoteHTMLTree extends Remote {

  /** Check the node of the tree with the given title */
  public void check(String title) throws RemoteException;

  /** Uncheck the node of the tree with the given title */
  public void uncheck(String title) throws RemoteException;

  /** Get the node state of the tree with the given title */
  public boolean isChecked(String title) throws RemoteException;
}
