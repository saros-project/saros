package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/** Represent an HTML select and makes it controllable via RMI. */
public interface IRemoteHTMLMultiSelect extends Remote {

  /** return a list of values of the selected options */
  public List<String> getSelection() throws RemoteException;

  /** select all options which has the given values */
  public void select(List<String> value) throws RemoteException;

  /** get the size of how many options the select has */
  public int size() throws RemoteException;

  /** return all a list of all options */
  public List<String> options() throws RemoteException;
}
