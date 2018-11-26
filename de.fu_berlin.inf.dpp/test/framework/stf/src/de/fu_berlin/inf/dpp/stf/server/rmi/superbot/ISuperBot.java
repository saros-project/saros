package de.fu_berlin.inf.dpp.stf.server.rmi.superbot;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.IMenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.IViews;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.internal.IInternal;
import de.fu_berlin.inf.dpp.stf.shared.Constants;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ISuperBot extends Remote {

  public IInternal internal() throws RemoteException;

  /* *********************************************
   *
   * finders
   *
   * ********************************************
   */

  public IViews views() throws RemoteException;

  public IMenuBar menuBar() throws RemoteException;

  public void setJID(JID jid) throws RemoteException;

  /* *********************************************
   *
   * Shells
   *
   * ********************************************
   */

  /**
   * The shell with the title {@link StfRemoteObject#SHELL_ADD_PROJECTS} should be appeared by the
   * invitees' side during sharing session. This method confirm the shell using a new project.
   *
   * @throws RemoteException
   */
  public void confirmShellAddProjectWithNewProject(String projectname) throws RemoteException;

  /**
   * The shell with the title {@link StfRemoteObject#SHELL_ADD_PROJECTS} should be appeared by the
   * invitees' side during sharing session. This method confirm the shell using an existed project.
   *
   * @throws RemoteException
   */
  public void confirmShellAddProjectUsingExistProject(String projectName) throws RemoteException;

  /**
   * The shell with the title {@link StfRemoteObject#SHELL_ADD_PROJECTS} should be appeared by the
   * invitees' side during sharing session. This method confirm the shell using an existed project
   * with copy.
   *
   * @throws RemoteException
   */
  public void confirmShellAddProjectUsingExistProjectWithCopy(String projectName)
      throws RemoteException;

  /**
   * The shell with the title {@link StfRemoteObject#SHELL_ADD_PROJECTS} should be appeared by the
   * invitees' side during sharing session. This method confirm the shell. with the passed parameter
   * "usingWhichProject" to decide using which project.
   *
   * @throws RemoteException
   */
  public void confirmShellAddProjectUsingWhichProject(
      String projectName, TypeOfCreateProject usingWhichProject) throws RemoteException;

  /**
   * Confirm the shell with title {@link StfRemoteObject#SHELL_EDIT_XMPP_JABBER_ACCOUNT} activated
   * by clicking button {@link StfRemoteObject#BUTTON_EDIT_ACCOUNT} in saros preference.
   *
   * @param newXmppJabberID
   * @param newPassword
   * @throws RemoteException
   */
  public void confirmShellEditXMPPAccount(String newXmppJabberID, String newPassword)
      throws RemoteException;

  /**
   * Confirm the shell with title {@link StfRemoteObject#SHELL_CREATE_XMPP_JABBER_ACCOUNT}
   *
   * @param jid {@link JID}
   * @param password password of the new XMPP account
   * @throws RemoteException
   */
  public void confirmShellCreateNewXMPPAccount(JID jid, String password) throws RemoteException;

  /**
   * confirm the shell with title {@link StfRemoteObject#SHELL_ADD_XMPP_JABBER_ACCOUNT}
   *
   * @param jid {@link JID}
   * @param password password of the new XMPP account
   * @throws RemoteException
   */
  public void confirmShellAddXMPPAccount(JID jid, String password) throws RemoteException;

  /**
   * Confirms the shell with title {@link StfRemoteObject#SHELL_ADD_CONTACT_WIZARD}.
   *
   * @param baseJIDOfinvitees
   * @throws RemoteException
   */
  public void confirmShellAddContactsToSession(String... baseJIDOfinvitees) throws RemoteException;

  /**
   * Confirm the shell with title {@link StfRemoteObject#SHELL_CLOSING_THE_SESSION}
   *
   * @throws RemoteException
   */
  public void confirmShellClosingTheSession() throws RemoteException;

  /**
   * Confirm the shell with title {@link StfRemoteObject#SHELL_ADD_CONTACT_WIZARD}
   *
   * @param jid {@link JID}
   * @throws RemoteException
   */
  public void confirmShellAddContact(JID jid) throws RemoteException;

  /**
   * confirm the shell with title {@link StfRemoteObject#SHELL_SHARE_PROJECT}
   *
   * @param projectName the name of shared project
   * @param jids {@link JID}s of all invitees
   * @throws RemoteException
   */
  public void confirmShellShareProjects(String projectName, JID... jids) throws RemoteException;

  /**
   * confirm the shell with title {@link StfRemoteObject#SHELL_SHARE_PROJECT}
   *
   * @param projectNames a {@link List} containing the names of shared projects
   * @param jids the {@link JID}s of all invitees
   * @throws RemoteException
   */
  public void confirmShellShareProjects(String[] projectNames, JID... jids) throws RemoteException;

  public void confirmShellAddProjectsToSession(String... projectNames) throws RemoteException;

  /**
   * Confirms the shell with title {@link Constants#SHELL_ADD_PROJECTS_TO_SESSION}
   *
   * @param project the name of the project that should be added to the current session
   * @param files the files of the project that should be added
   * @throws RemoteException
   */
  public void confirmShellAddProjectToSession(String project, String[] files)
      throws RemoteException;

  /**
   * confirm the shell with title {@link StfRemoteObject#SHELL_SESSION_INVITATION} and also the
   * following shell with title {@link StfRemoteObject#SHELL_ADD_PROJECTS}
   *
   * @param projectName the name of shared project
   * @param usingWhichProject if invitee has same project locally, he can decide use new or existed
   *     project.
   * @throws RemoteException
   */
  public void confirmShellSessionInvitationAndShellAddProject(
      String projectName, TypeOfCreateProject usingWhichProject) throws RemoteException;

  public void confirmShellRequestOfSubscriptionReceived() throws RemoteException;

  public void confirmShellLeavingClosingSession() throws RemoteException;

  public void confirmShellNewSharedFile(String decision) throws RemoteException;

  public void confirmShellNeedBased(String decsision, boolean remember) throws RemoteException;
}
