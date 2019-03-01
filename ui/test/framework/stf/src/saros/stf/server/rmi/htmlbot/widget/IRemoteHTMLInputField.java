package saros.stf.server.rmi.htmlbot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Represent an HTML input field and makes it controllable via RMI. */
public interface IRemoteHTMLInputField extends Remote {

  /** enter text to the field. */
  public void enter(String text) throws RemoteException;

  /** get the value of the field */
  public String getValue() throws RemoteException;

  /** clear the field so that value is empty */
  public void clear() throws RemoteException;
}
