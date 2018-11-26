package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/** Represent an HTML select and makes it controllable via RMI. */
public interface IRemoteHTMLSelect extends Remote {

  /** return the value of the selected option */
  public String getSelection() throws RemoteException;

  /** select the option which has the given value */
  public void select(String value) throws RemoteException;

  /** get the size of how many options the select has */
  public int size() throws RemoteException;

  /** return all a list of all options */
  public List<String> options() throws RemoteException;
}
