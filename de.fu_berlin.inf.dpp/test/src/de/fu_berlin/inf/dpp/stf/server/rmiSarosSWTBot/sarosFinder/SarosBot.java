package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosC;
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

    /**
     * waits until the window with the title "Saros running VCS operation" is
     * closed
     * 
     * @throws RemoteException
     */
    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException;

    /**
     * 
     * @param prjectName
     *            the name of the project
     * @return <tt>true</tt>, if the given project is under SVN control
     * @throws RemoteException
     */
    public boolean isProjectManagedBySVN(String prjectName)
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
     * @return the revision id of the given resource.
     * @throws RemoteException
     */
    public String getRevision(String fullPath) throws RemoteException;

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
     * @param fullPath
     *            the full path of the local resource, e.g.
     *            "example_project/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * 
     * @return the VCS specific URL information for the given resource specified
     *         by the passed parameter"fullPath".
     * @throws RemoteException
     */
    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException;

    /**
     * After host canceled the invitation process of a invitee, a popup window
     * with the title "Invitation canceled" should be appeared by the invtee,
     * who confirm the canceled inivation using this method.
     * 
     * @throws RemoteException
     */
    public void confirmWindowInvitationCancelled() throws RemoteException;

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

    /**
     * Perform the action "switch to another Branch/Tag" which should be done
     * with the following steps:
     * 
     * <ol>
     * <li>Select the given project and click "Team" >
     * "Switch to another Branch/Tag/Revision..."</li>
     * <li>Enter the given URL to the combobox text field with the title
     * "To URL:"</li>
     * <li>click "OK" to confirm the switch</li>
     * <li>Waits until the shell "SVN Switch" is closed. It guarantee that the
     * "switch to another Branch/Tag/revision" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "switch to another Branch/Tag" . I mean, after clicking the sub menu you
     * need to treat the following popup window too.</li>
     * 
     * 
     * @param projectName
     *            the name of the project located in the package explorer view,
     *            which you want to share with other peers.
     * @param url
     *            Update working copy to the url.
     * @throws RemoteException
     */
    public void switchProject(String projectName, String url)
        throws RemoteException;

    public void switchResource(String fullPath, String url, String revision)
        throws RemoteException;

    public void switchResource(String fullPath, String url)
        throws RemoteException;

    /**
     * Delete the project using FileUntil.delete(resource). This delete-method
     * costs less time than the method using GUI
     * 
     * @param projectName
     *            name of the project, which you want to delete.
     */
    public void deleteProjectNoGUI(String projectName) throws RemoteException;

    /**
     * Delete the specified folder using FileUntil.delete(resource).
     * 
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     */
    public void deleteFolderNoGUI(String... folderNodes) throws RemoteException;

    /**
     * Delete the specified package using FileUntil.delete(resource).
     * 
     * @param projectName
     *            name of the project, which package you want to delete.
     * @param pkg
     *            name of the package, which you want to delete.
     */
    public void deletePkgNoGUI(String projectName, String pkg)
        throws RemoteException;

    /**
     * Performs the action "delete file" which should be done with the following
     * steps:
     * 
     * <ol>
     * <li>click the menu Delete.</li>
     * <li>confirms the popup-window "Confirm Delete".</li>
     * <li>waits until the popup-window is closed.</li>
     * </ol>
     * 
     * 
     */
    // public void deleteFile() throws RemoteException;

    /**
     * Delete a class of the specified java project using
     * FileUntil.delete(resource).
     * 
     * @param projectName
     *            name of the project, which class you want to delete.
     * @param pkg
     *            name of the package, which class you want to delete.
     * @param className
     *            name of the class, which you want to delete.
     */
    public void deleteClassNoGUI(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * Uses Copy and Paste to create a copy of a project.<br>
     * Warning: This method is not thread safe if the threads run on the same
     * host because it uses the global clipboard to copy the project.
     * 
     * @param target
     *            The name of the copy to be created.
     * 
     * @throws RemoteException
     */
    // public void copyProject(String target) throws RemoteException;

    /**
     * Delete all the projects in this workspace.
     * 
     * @throws RemoteException
     */
    public void deleteAllProjectsNoGUI() throws RemoteException;
}