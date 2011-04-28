package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.IMenuBar;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.IViews;
import de.fu_berlin.inf.dpp.stf.stfMessages.STFMessages;

public interface ISuperBot extends Remote {

    /**********************************************
     * 
     * finders
     * 
     **********************************************/

    public IViews views() throws RemoteException;

    public IMenuBar menuBar() throws RemoteException;

    public void setJID(JID jid) throws RemoteException;

    /**********************************************
     * 
     * Shells
     * 
     **********************************************/

    /**
     * The shell with the title {@link STFMessages#SHELL_ADD_PROJECT} should be appeared
     * by the invitees' side during sharing session. This method confirm the
     * shell using a new project.
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectWithNewProject(String projectname)
        throws RemoteException;

    /**
     * The shell with the title {@link STFMessages#SHELL_ADD_PROJECT} should be appeared
     * by the invitees' side during sharing session. This method confirm the
     * shell using an existed project.
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingExistProject(String projectName)
        throws RemoteException;

    /**
     * The shell with the title {@link STFMessages#SHELL_ADD_PROJECT} should be appeared
     * by the invitees' side during sharing session. This method confirm the
     * shell using an existed project.
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingExistProjectWithCopyAfterCancelLocalChange(
        String projectName) throws RemoteException;

    /**
     * The shell with the title {@link STFMessages#SHELL_ADD_PROJECT} should be appeared
     * by the invitees' side during sharing session. This method confirm the
     * shell using an existed project with copy.
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingExistProjectWithCopy(
        String projectName) throws RemoteException;

    /**
     * The shell with the title {@link STFMessages#SHELL_ADD_PROJECT} should be appeared
     * by the invitees' side during sharing session. This method confirm the
     * shell. with the passed parameter "usingWhichProject" to decide using
     * which project.
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingWhichProject(String projectName,
        TypeOfCreateProject usingWhichProject) throws RemoteException;

    /**
     * Confirm the shell with title {@link STFMessages#SHELL_EDIT_XMPP_JABBER_ACCOUNT}
     * activated by clicking button {@link STFMessages#BUTTON_EDIT_ACCOUNT} in saros
     * preference.
     * 
     * @param newXmppJabberID
     * @param newPassword
     * @throws RemoteException
     */
    public void confirmShellEditXMPPJabberAccount(String newXmppJabberID,
        String newPassword) throws RemoteException;

    /**
     * Confirm the shell with title {@link STFMessages#SHELL_CREATE_XMPP_JABBER_ACCOUNT}
     * 
     * @param jid
     *            {@link JID}
     * @param password
     *            password of the new XMPP/Jabber account
     * @throws RemoteException
     */
    public void confirmShellCreateNewXMPPJabberAccount(JID jid, String password)
        throws RemoteException;

    /**
     * confirm the shell with title {@link STFMessages#SHELL_ADD_XMPP_JABBER_ACCOUNT}
     * 
     * @param jid
     *            {@link JID}
     * @param password
     *            password of the new XMPP/Jabber account
     * @throws RemoteException
     */
    public void confirmShellAddXMPPJabberAccount(JID jid, String password)
        throws RemoteException;

    /**
     * confirm the shell with title {@link STFMessages#SHELL_ADD_BUDDY}
     * 
     * @param baseJIDOfinvitees
     * @throws RemoteException
     */
    public void confirmShellAddBuddyToSession(String... baseJIDOfinvitees)
        throws RemoteException;

    /**
     * Confirm the shell with title {@link STFMessages#SHELL_CLOSING_THE_SESSION}
     * 
     * @throws RemoteException
     */
    public void confirmShellClosingTheSession() throws RemoteException;

    /**
     * Confirm the shell with title {@link STFMessages#SHELL_REMOVAL_OF_SUBSCRIPTION}
     * which would be appeared if someone delete your contact from his buddies
     * list.
     * 
     * @throws RemoteException
     */
    public void confirmShellRemovelOfSubscription() throws RemoteException;

    /**
     * Confirm the shell with title {@link STFMessages#SHELL_ADD_BUDDY}
     * 
     * @param jid
     *            {@link JID}
     * @throws RemoteException
     */
    public void confirmShellAddBuddy(JID jid) throws RemoteException;

    /**
     * confirm the shell with title {@link STFMessages#SHELL_SHARE_PROJECT}
     * 
     * @param projectName
     *            the name of shared project
     * @param jids
     *            {@link JID}s of all invitees
     * @throws RemoteException
     */
    public void confirmShellShareProject(String projectName, JID... jids)
        throws RemoteException;

    public void confirmShellAddProjectsToSession(String... projectNames)
        throws RemoteException;

    /**
     * confirm the shell with title {@link STFMessages#SHELL_SESSION_INVITATION} and
     * also the following shell with title {@link STFMessages#SHELL_ADD_PROJECT}
     * 
     * @param projectName
     *            the name of shared project
     * @param usingWhichProject
     *            if invitee has same project locally, he can decide use new or
     *            existed project.
     * @throws RemoteException
     */
    public void confirmShellSessionInvitationAndShellAddProject(
        String projectName, TypeOfCreateProject usingWhichProject)
        throws RemoteException;

    public void confirmShellAddProjects(String projectName,
        TypeOfCreateProject usingWhichProject) throws RemoteException;

    public void confirmShellRequestOfSubscriptionReceived()
        throws RemoteException;

    public void confirmShellLeavingClosingSession() throws RemoteException;

}