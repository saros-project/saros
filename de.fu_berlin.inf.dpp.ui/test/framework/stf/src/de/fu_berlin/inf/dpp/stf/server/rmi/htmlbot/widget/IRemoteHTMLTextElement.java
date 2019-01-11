package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface represent an HTML Element which contains simple text (div, span, ...) and makes it
 * controllable via RMI.
 */
public interface IRemoteHTMLTextElement extends Remote {

  /** Get the innerHTML of the element */
  public String getText() throws RemoteException;

  /** Set the innerHTML of the element */
  public void setText(String text) throws RemoteException;
}
