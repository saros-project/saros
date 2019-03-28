package saros.stf.server.rmi.superbot.component.menubar.menu;

import java.rmi.Remote;
import java.rmi.RemoteException;
import saros.net.xmpp.JID;
import saros.stf.server.rmi.superbot.component.menubar.menu.submenu.ISarosPreferences;

public interface ISarosMenu extends Remote {

  public ISarosPreferences preferences() throws RemoteException;

  /**
   * Creates an account with GUI, which should be done with the following steps:
   *
   * <ol>
   *   <li>Click menu "Saros" -> "Create Account"
   *   <li>confirm the popup window "Create New User Account" with the given parameters
   * </ol>
   *
   * @param jid a JID which is used to identify the users of the Jabber network, more about it
   *     please see {@link JID}.
   * @param password the password of the new account.
   *     <p>TODO not implement yet.
   * @throws RemoteException
   */
  public void createAccount(JID jid, String password) throws RemoteException;

  public void addContact(JID jid) throws RemoteException;

  public void addContactsToSession(String... jidOfInvitees) throws RemoteException;

  public void shareProjects(String projectName, JID... jids) throws RemoteException;

  public void shareProjectFiles(String projectName, String[] files, JID... jids)
      throws RemoteException;

  public void shareProjects(String[] projectNames, JID... jids) throws RemoteException;

  /**
   * Adds the files of the given project to the current session.
   *
   * @param project the name of the project that should be added to the current session
   * @param files the files of the project that should be added
   * @throws RemoteException
   */
  public void addProject(String project, String[] files) throws RemoteException;

  public void addProjects(String... projectNames) throws RemoteException;

  public void stopSession() throws RemoteException;
}
