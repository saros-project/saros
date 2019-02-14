package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/** Represent an HTML radio button group and makes it controllable via RMI. */
public interface IRemoteHTMLRadioGroup extends Remote {

  /** return the value of the selected radio button */
  public String getSelected() throws RemoteException;

  /** select the radio button which has the given value */
  public void select(String value) throws RemoteException;

  /** get the size of the radio group */
  public int size() throws RemoteException;

  /** return all possible values of the radio group */
  public List<String> values() throws RemoteException;
}
