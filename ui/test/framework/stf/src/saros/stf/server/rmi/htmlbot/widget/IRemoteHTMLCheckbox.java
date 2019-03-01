package saros.stf.server.rmi.htmlbot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Represent an HTML checkbox and makes it controllable via RMI. */
public interface IRemoteHTMLCheckbox extends Remote {

  /** return the checked status */
  public boolean isChecked() throws RemoteException;

  /** set the status to checked */
  public void check() throws RemoteException;

  /** set the status to unchecked */
  public void uncheck() throws RemoteException;
}
