package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.EditM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.FileM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.RefactorM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.SarosM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar.WindowM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.PEView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RosterView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionView;

public interface SarosBot extends Remote {

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    public FileM file() throws RemoteException;

    public EditM edit() throws RemoteException;

    public RefactorM refactor() throws RemoteException;

    public SarosM saros() throws RemoteException;

    public WindowM window() throws RemoteException;

    public ChatView chatView() throws RemoteException;

    public RosterView buddiesView() throws RemoteException;

    public RSView remoteScreenView() throws RemoteException;

    public SessionView sessionView() throws RemoteException;

    public ConsoleView consoleView() throws RemoteException;

    public PEView packageExplorerView() throws RemoteException;

    public ProgressView progressView() throws RemoteException;

    public void setJID(JID jid) throws RemoteException;

    /**********************************************
     * 
     * shells
     * 
     **********************************************/
    public void confirmShellEditorSelection(String editorType)
        throws RemoteException;

    /**
     * After clicking one of the sub menu of the context menu "Saros" in the
     * package explorer view host will get the popup window with the title
     * "Invitation". This method confirm the popup window.
     * 
     * @param inviteesBaseJIDs
     *            the base JID of the users with whom you want to share your
     *            project.
     * @throws RemoteException
     */
    public void confirmShellInvitation(String... inviteesBaseJIDs)
        throws RemoteException;

    /**
     * After host canceled the invitation process of a invitee, a popup window
     * with the title "Invitation canceled" should be appeared by the invtee,
     * who confirm the canceled inivation using this method.
     * 
     * @throws RemoteException
     */
    public void confirmShellInvitationCancelled() throws RemoteException;

    /**
     * After the {@link SarosC#confirmShellInvitation(String...)} the popup
     * wizard with the title "Session Invitation" should be appeared by the
     * invitees' side. This method confirm the first page of the wizard.
     * 
     * @throws RemoteException
     */
    public void confirmShellSessionnInvitation() throws RemoteException;

    /**
     * After the {@link SarosC#confirmShellInvitation(String...)} the popup
     * wizard with the title "Session Invitation" should be appeared by the
     * invitees' side. This method confirm the wizard "Session Invitation" using
     * a new project.
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectWithNewProject(String projectname)
        throws RemoteException;

    /**
     * After the {@link SarosC#confirmShellInvitation(String...)} the popup
     * wizard with the title "Session Invitation" should be appeared by the
     * invitees' side. This method confirm the wizard "Session Invitation" using
     * a exist project.
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingExistProject(String projectName)
        throws RemoteException;

    /**
     * After the {@link SarosC#confirmShellInvitation(String...)} the popup
     * wizard with the title "Session Invitation" should be appeared by the
     * invitees' side. This method confirm the wizard "Session Invitation" using
     * a exist project with clicking the button browser->confirming popup window
     * -> clicking the button "finish" -> conforming the local change
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingExistProjectWithCopyAfterCancelLocalChange(
        String projectName) throws RemoteException;

    /**
     * After the {@link SarosC#confirmShellInvitation(String...)} the popup
     * wizard with the title "Session Invitation" should be appeared by the
     * invitees' side. This method confirm the wizard "Session Invitation" using
     * a exist project with copy
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingExistProjectWithCopy(
        String projectName) throws RemoteException;

    /**
     * After the {@link SarosC#confirmShellInvitation(String...)} the popup
     * wizard with the title "Session Invitation" should be appeared by the
     * invitees' side. This method confirm the wizard "Session Invitation" using
     * a new project or a existed project according the passed parameter
     * "usingWhichProject".
     * 
     * @throws RemoteException
     */
    public void confirmShellAddProjectUsingWhichProject(String projectName,
        TypeOfCreateProject usingWhichProject) throws RemoteException;

    public void confirmShellShareProject(String repositoryURL)
        throws RemoteException;

    public void confirmShellImport(String repositoryURL) throws RemoteException;

    public void confirmShellRevert() throws RemoteException;

    public void confirmShellSwitch(String versionID) throws RemoteException;

    /**
     * waits until the window with the title "Saros running VCS operation" is
     * closed
     * 
     * @throws RemoteException
     */
    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException;

    /**
     * waits until the given project is in SVN control
     * 
     * @param projectName
     *            the name of the project
     * @throws RemoteException
     */
    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException;

    /**
     * waits until the given project is not under SVN control
     * 
     * @param projectName
     *            the name of the project
     * @throws RemoteException
     */
    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException;

    /**
     * 
     * @param fullPath
     *            the full path of the local resource, e.g.
     *            "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * @param revisionID
     *            the expected revision.
     * @throws RemoteException
     */
    public void waitUntilRevisionIsSame(String fullPath, String revisionID)
        throws RemoteException;

    /**
     * 
     * @param fullPath
     *            the full path of the local resource, e.g.
     *            "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * @param url
     *            the expected URL of the remote resource, e.g.
     *            "http://myhost.com/svn/trunk/.../MyFirstTest01.java".
     * @throws RemoteException
     */
    public void waitUntilUrlIsSame(String fullPath, String url)
        throws RemoteException;

    /**
     * using this method you can import a project from SVN (Other variant to
     * import a project from SVN is in the method
     * {@link PEView#importProjectFromSVN(String)} defined). which should be
     * done with the following steps:
     * 
     * <ol>
     * <li>Select the given project and click "Team" > "Share project"</li>
     * <li>Select the repository type "SVN" and click next</li>
     * <li>In the next page select the repositoryURL if the repositoryURL
     * already exists,otherwise create a new one and click the next button</li>
     * <li>In the next page activate the radio button
     * "Use specified folder name", insert the given folder name in the text
     * field and click finish to confirm the import prorject with SVN process.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "share project" . I mean, after clicking the sub menu you need to treat
     * the following popup window too.</li>
     * 
     * @param projectName
     *            the name of the project located in the package explorer view,
     *            which will be shared under SVN.
     * @param repositoryURL
     *            the repository location
     * @param specifiedFolderName
     *            the name of the folder, which already exists in the
     *            repository, e.g. trunk/examples
     * @throws RemoteException
     */
    public void shareProjectWithSVNUsingSpecifiedFolderName(String viewTitle,
        String projectName, String repositoryURL, String specifiedFolderName)
        throws RemoteException;

}