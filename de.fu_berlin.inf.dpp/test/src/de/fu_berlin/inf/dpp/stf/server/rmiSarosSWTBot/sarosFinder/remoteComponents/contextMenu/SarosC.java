package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfShareProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.SarosComponent;

/**
 * This interface contains convenience API to perform a action using the
 * submenus of the contextmenu "Saros" by right clicking on a treeItem(ie,
 * project, class, file...)in the package explorer view. then you can start off
 * as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * then you can use the object pEV initialized in {@link Tester} to access the
 * API :), e.g.
 * 
 * <pre>
 * alice.pEV.shareProject();
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface SarosC extends SarosComponent {

    /**
     * Perform one of the actions "Share project",
     * "Share project partially (experimental)..." and "Add to session
     * (experimental)..." according to the passed parameter "howToShareProject"
     * which should be activated by clicking the corresponding sub menu of the
     * context menu "Saros" of the given project in the package explorer view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions showed
     * by host side, which are activated or indirectly activated by the clicking
     * one of the sub menus . I mean, after clicking the sub menu e.g.
     * "Share project" you need to treat the following popup window too.</li>
     * <li>The share session process is only completely done after host first
     * run {@link SarosC#shareProjectWith(String, TypeOfShareProject, String[])}
     * , and then the invited users confirm the popup window
     * {@link SarosC#confirmShellAddProjectUsingWhichProject(String, TypeOfCreateProject)}
     * . Since the share session process is very often used, so a convenient
     * method
     * {@link Tester#buildSessionDoneSequentially(String, TypeOfShareProject, TypeOfCreateProject, Tester...)}
     * is defined, which build the sharing session completely.</li>
     * </ol>
     * 
     * @param projectName
     *            the name of the project located in the package explorer view,
     *            which you want to share with other peers.
     * @param howToshareProject
     *            with the parameter you can tell the method how to share your
     *            project with "Share project",
     *            "Share project partially (experimental)..." or "Add to session
     *            (experimental)...".
     * @param inviteeBaseJIDs
     *            the base JIDs of the users with whom you want to share your
     *            project.
     * @throws RemoteException
     * @see Tester#buildSessionDoneSequentially(String, TypeOfShareProject,
     *      TypeOfCreateProject, Tester...)
     */
    public void shareProjectWith(String projectName, String howToshareProject,
        String[] inviteeBaseJIDs) throws RemoteException;

    /**
     * Perform the action "Share project" which should be activated by clicking
     * the corresponding sub menu "Share project" of the context menu "Saros" of
     * the given project in the package explorer view.
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions showed
     * by host side, which are activated or indirectly activated by the clicking
     * one of the sub menus . I mean, after clicking the sub menu
     * "Share project" you need to treat the following popup window too.</li>
     * <li>The share session process is only completely done after host first
     * run {@link SarosC#shareProject(String, String...)}, and then the invited
     * users confirm the popup window
     * {@link SarosC#confirmWizardSessionInvitationUsingWhichProject(String, String, int)}
     * . Since the share session process is very often used, so a convenient
     * method {@link Tester#buildSessionSequentially(String, String, Tester...)}
     * is defined, which build the sharing session completely.</li>
     * </ol>
     * 
     * @param projectName
     *            the name of the project located in the package explorer view,
     *            which you want to share with other peers.
     * @param inviteeBaseJIDS
     *            the base JIDs of the users with whom you want to share your
     *            project.
     * @throws RemoteException
     * @see Tester#buildSessionSequentially(String, String, Tester...)
     */
    public void shareProject(String projectName, String... inviteeBaseJIDS)
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

}
