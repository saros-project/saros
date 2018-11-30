package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot;

import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.IRemoteHTMLView;
import de.fu_berlin.inf.dpp.ui.View;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This interface is part of the HTML GUI test framework. It provides methods to remotely emulate
 * user input by executing Javascript and to query the currently rendered state.
 */
public interface IHTMLBot extends Remote {
  /**
   * Get a remote representation of a conceptual part of the Saros GUI.
   *
   * @param view The part of the Saros GUI on which user input should be emulated and/or of which
   *     the current state should be queried.
   * @throws RemoteException
   */
  IRemoteHTMLView view(View view) throws RemoteException;

  /**
   * Returns the currently displayed list of accounts.
   *
   * @return a list of strings in the form 'user@domain'
   * @throws RemoteException
   */
  List<String> getAccountList() throws RemoteException;

  /**
   * Returns the currently displayed list of contacts.
   *
   * @param view The part of the Saros GUI on which the contact list is displayed.
   * @return a list of diplayNames of contacts
   * @throws RemoteException
   */
  List<String> getContactList(View view) throws RemoteException;
}
