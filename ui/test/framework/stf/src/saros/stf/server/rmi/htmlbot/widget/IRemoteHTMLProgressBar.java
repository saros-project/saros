package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Represent an HTML progressbar and makes it controllable via RMI. */
public interface IRemoteHTMLProgressBar extends Remote {

  /** return the value of the progressbar */
  public int getValue() throws RemoteException;

  /** set the value of the progressbar */
  public void setValue(int value) throws RemoteException;
}
