package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.WindowPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PEViewComponent;

/**
 * This interface contains convenience API to perform a action using the
 * submenus of the contextmenu "Saros" by right clicking on a treeItem(ie,
 * project, class, file...)in the package explorer view. then you can start off
 * as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Musician} object in your junit-test.
 * (How to do it please look at the javadoc in class {@link TestPattern} or read
 * the user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * then you can use the object pEV initialized in {@link Musician} to access the
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
public interface SarosPEViewComponent extends PEViewComponent {

    /**
     * Perform one of the actions "Share project",
     * "Share project with VCS support",
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
     * run
     * {@link SarosPEViewComponent#shareProjectWith(String, String, String[])},
     * and then the invited users confirm the popup window
     * {@link SarosPEViewComponent#confirmWizardSessionInvitationUsingWhichProject(String, String, int)}
     * . Since the share session process is very often used, so a convenient
     * method {@link Musician#shareProjectWithDone(String, String, Musician...)}
     * is defined, which build the sharing session completely.</li>
     * </ol>
     * 
     * @param projectName
     *            the name of the project located in the package explorer view,
     *            which you want to share with other peers.
     * @param howToShareProject
     *            with the parameter you can tell the method how to share your
     *            project with "Share project","Share project with VCS support",
     *            "Share project partially (experimental)..." or "Add to session
     *            (experimental)...".
     * @param inviteeBaseJIDs
     *            the base JIDs of the users with whom you want to share your
     *            project.
     * @throws RemoteException
     * @see Musician#shareProjectWithDone(String, String, Musician...)
     */
    public void shareProjectWith(String projectName, String howToShareProject,
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
     * run {@link SarosPEViewComponent#shareProject(String, String...)}, and
     * then the invited users confirm the popup window
     * {@link SarosPEViewComponent#confirmWizardSessionInvitationUsingWhichProject(String, String, int)}
     * . Since the share session process is very often used, so a convenient
     * method {@link Musician#shareProjectWithDone(String, String, Musician...)}
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
     * @see Musician#shareProjectWithDone(String, String, Musician...)
     */
    public void shareProject(String projectName, String... inviteeBaseJIDS)
        throws RemoteException;

    /**
     * 
     * @return <tt>true</tr>, if the popup window with the title "Invitation cancel" is active
     * @throws RemoteException
     * @see WindowPart#isShellActive(String)
     */
    public boolean isWindowInvitationCancelledActive() throws RemoteException;

    /**
     * close the popup window with the title "Invitation cancel".
     * 
     * @throws RemoteException
     * @see WindowPart#closeShell(String)
     */
    public void closeWindowInvitaitonCancelled() throws RemoteException;

    /**
     * waits until the popup window with the title "Invitation cancel" is
     * active.
     * 
     * @throws RemoteException
     * @see WindowPart#waitUntilShellActive(String)
     */
    public void waitUntilIsWindowInvitationCnacelledActive()
        throws RemoteException;

    /**
     * 
     * @return <tt><true</tt>, if the popup window with the title
     *         "Session Invitation" is active
     * @throws RemoteException
     * @see WindowPart#isShellActive(String)
     */
    public boolean isWIndowSessionInvitationActive() throws RemoteException;

    /**
     * close the popup window with the title "Session Invitation".
     * 
     * @throws RemoteException
     * @see WindowPart#closeShell(String)
     */
    public void closeWIndowSessionInvitation() throws RemoteException;

    /**
     * waits until the popup window with the title "Session Invitation" is
     * active.
     * 
     * @throws RemoteException
     * @see WindowPart#waitUntilShellActive(String)
     */
    public void waitUntilWIndowSessionInvitationActive() throws RemoteException;

    /**
     * After host canceled the invitation process of a invitee, a popup window
     * with the title "Invitation canceled" should be appeared by the invtee,
     * who confirm the canceled inivation using this method.
     * 
     * @throws RemoteException
     */
    public void confirmWindowInvitationCancelled() throws RemoteException;

    /**
     * After clicking one of the sub menu of the context menu "Saros" in the
     * package explorer view host will get the popup window with the title
     * "Invitation". This method confirm the popup window.
     * 
     * @param baseJIDOfinvitees
     *            the base JID of the users with whom you want to share your
     *            project.
     * @throws RemoteException
     */
    public void confirmWindowInvitation(String... baseJIDOfinvitees)
        throws RemoteException;

    /**
     * After the {@link SarosPEViewComponent#confirmWindowInvitation(String...)}
     * the popup wizard with the title "Session Invitation" should be appeared
     * by the invitees' side. This method confirm the first page of the wizard.
     * 
     * @throws RemoteException
     */
    public void confirmFirstPageOfWizardSessionInvitation()
        throws RemoteException;

    /**
     * After the {@link SarosPEViewComponent#confirmWindowInvitation(String...)}
     * the popup wizard with the title "Session Invitation" should be appeared
     * by the invitees' side. This method confirm the second page of the wizard,
     * whereas check the radio button "Create new project".
     * 
     * @param projectName
     *            the name of the project located in the packageExplorer view
     *            which you want to share with others.
     * @throws RemoteException
     */
    public void confirmSecondPageOfWizardSessionInvitationUsingNewproject(
        String projectName) throws RemoteException;

    /**
     * After the {@link SarosPEViewComponent#confirmWindowInvitation(String...)}
     * the popup wizard with the title "Session Invitation" should be appeared
     * by the invitees' side. This method confirm the second page of the wizard,
     * whereas check the radio button "Use existing project".
     * 
     * @param projectName
     *            the name of the project located in the packageExplorer view
     *            which you want to share with others.
     * @throws RemoteException
     */
    public void confirmSecondPageOfWizardSessionInvitationUsingExistProject(
        String projectName) throws RemoteException;

    /**
     * After the {@link SarosPEViewComponent#confirmWindowInvitation(String...)}
     * the popup wizard with the title "Session Invitation" should be appeared
     * by the invitees' side. This method confirm the second page of the wizard,
     * whereas check the radio button "Use existing project" and the checkbox
     * "Create copy for working distributed".
     * 
     * @param projectName
     *            the name of the project located in the packageExplorer view
     *            which you want to share with others.
     * @throws RemoteException
     */
    public void confirmPageTwoOfWizardSessionInvitationUsingExistProjectWithCopy(
        String projectName) throws RemoteException;

    /**
     * After the {@link SarosPEViewComponent#confirmWindowInvitation(String...)}
     * the popup wizard with the title "Session Invitation" should be appeared
     * by the invitees' side. This method confirm the second page of the wizard,
     * whereas check the radio button "Use existing project".
     * 
     * TODO the method is not completely implemented yet. The invitation session
     * is not finished yet after clicking finish button and confirm the popup
     * window with the title "Warning: local changes will be deleted" with
     * cancel.
     * 
     * @param projectName
     *            the name of the project located in the packageExplorer view
     *            which you want to share with others.
     * @throws RemoteException
     */
    public void confirmPageTwoOfWizardSessionInvitationUsingExistProjectWithCancelLocalChange(
        String projectName) throws RemoteException;

    /**
     * After the {@link SarosPEViewComponent#confirmWindowInvitation(String...)}
     * the popup wizard with the title "Session Invitation" should be appeared
     * by the invitees' side. This method confirm the wizard
     * "Session Invitation" using a new project.
     * 
     * @throws RemoteException
     */
    public void confirmWirzardSessionInvitationWithNewProject(String projectname)
        throws RemoteException;

    /**
     * After the {@link SarosPEViewComponent#confirmWindowInvitation(String...)}
     * the popup wizard with the title "Session Invitation" should be appeared
     * by the invitees' side. This method confirm the wizard
     * "Session Invitation" using a exist project.
     * 
     * @throws RemoteException
     */
    public void confirmWizardSessionInvitationUsingExistProject(
        String projectName) throws RemoteException;

    /**
     * After the {@link SarosPEViewComponent#confirmWindowInvitation(String...)}
     * the popup wizard with the title "Session Invitation" should be appeared
     * by the invitees' side. This method confirm the wizard
     * "Session Invitation" using a exist project with clicking the button
     * browser->confirming popup window -> clicking the button "finish" ->
     * conforming the local change
     * 
     * @throws RemoteException
     */
    public void confirmWizardSessionInvitationUsingExistProjectWithCancelLocalChange(
        String projectName) throws RemoteException;

    /**
     * After the {@link SarosPEViewComponent#confirmWindowInvitation(String...)}
     * the popup wizard with the title "Session Invitation" should be appeared
     * by the invitees' side. This method confirm the wizard
     * "Session Invitation" using a exist project with copy
     * 
     * @throws RemoteException
     */
    public void confirmWizardSessionInvitationUsingExistProjectWithCopy(
        String projectName) throws RemoteException;

    /**
     * After the {@link SarosPEViewComponent#confirmWindowInvitation(String...)}
     * the popup wizard with the title "Session Invitation" should be appeared
     * by the invitees' side. This method confirm the wizard
     * "Session Invitation" using a new project or a existed project according
     * the passed parameter "usingWhichProject".
     * 
     * 
     * @param baseJIDOfInvitee
     *            the base JID of the invitee, who confirm the invitation
     *            session
     * @throws RemoteException
     */
    public void confirmWizardSessionInvitationUsingWhichProject(
        String baseJIDOfInvitee, String projectName, int usingWhichProject)
        throws RemoteException;

    /**
     * waits until the popup window with the title "Rroblem occurred" is active
     * 
     * @throws RemoteException
     */
    public void waitUntilIsWindowProblemOccurredActive() throws RemoteException;

    /**
     * 
     * @return the second label text of the popup window "Problem occurredss"
     * @throws RemoteException
     */
    public String getSecondLabelOfWindowProblemOccurred()
        throws RemoteException;
}
