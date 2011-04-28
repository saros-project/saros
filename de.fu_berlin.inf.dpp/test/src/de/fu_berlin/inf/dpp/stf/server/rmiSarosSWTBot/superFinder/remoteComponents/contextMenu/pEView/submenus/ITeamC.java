package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.submenus;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.stfMessages.STFMessages;

/**
 * This interface contains convenience API to perform actions activated by
 * clicking subMenus of contextMenu {@link STFMessages#CM_TEAM} in the package explorer
 * view. STF users would start off as follows:
 * 
 * <pre>
 * //
 * // init alice and bob
 * //
 * initTesters(TypeOfTester.ALICE, Tester.BOB);
 * 
 * //
 * // clean up workbench
 * //
 * setUpWorkbench();
 * 
 * //
 * // open sarosViews, connect...
 * //
 * setUpSaros();
 * 
 * //
 * // alice create a new java project with name Foo_bar
 * //
 * alice.superBot().views().packageExplorerView().tree().newC()
 *     .javaProject(&quot;Foo_bar&quot;);
 * 
 * //
 * // alice check out a project with SVN and update its reversion to 115.
 * //
 * alice
 *     .superBot()
 *     .views()
 *     .packageExplorerView()
 *     .selectProject(&quot;Foo_bar&quot;)
 *     .team()
 *     .shareProjectUsingSpecifiedFolderName(
 *         &quot;http://saros-build.imp.fu-berlin.de/svn/saros&quot;,
 *         &quot;stf_tests/stf_test_project&quot;);
 * alice.superBot().views().packageExplorerView().selectProject((&quot;Foo_bar&quot;)
 *     .team().update(&quot;115&quot;);
 * </pre>
 * 
 * More informations about how to write STF-Tests please read the user guide.
 * 
 * @author lchen
 */
public interface ITeamC extends Remote {

    /**
     * Perform the action "share project with SVN" which should be done with the
     * following steps:
     * 
     * <ol>
     * <li>Select the given project and click "Team" > "Share project"</li>
     * <li>Select the repository type "SVN"</li>
     * <li>If the given repository URL is already existed, then select the URL
     * and confirm the popup window</li>
     * <li>Otherwise check the checkbox "Create a new repository location",
     * enter the given repository URL and click "Finish" to confirm the share
     * project process</li>
     * <li>Waits until the shell is closed. It guarantee that the share project
     * action is completely done.</li>
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
     * 
     * @param repositoryURL
     *            the repository location
     * @throws RemoteException
     */
    public void shareProject(String repositoryURL) throws RemoteException;

    /**
     * Perform the action
     * "share project with SVN, which is already configured with SVN repository information"
     * which should be done with the following steps:
     * 
     * <ol>
     * <li>Select the given project and click "Team" > "Share project"</li>
     * <li>Select the repository type "SVN"</li>
     * <li>click "Finish" to confirm the share proejct process</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "share project" . I mean, after clicking the sub menu you need to treat
     * the following popup window too.</li>
     * <li>this method is only suitable for such project, which still include
     * the SVN meta information.</li>
     * 
     * 
     * @param repositoryURL
     *            the repository location
     * @throws RemoteException
     */
    public void shareProjectConfiguredWithSVNInfos(String repositoryURL)
        throws RemoteException;

    /**
     * using this method you can import a project from SVN (Other variant to
     * import a project from SVN is in the method which should be done with the
     * following steps:
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
     * 
     * @param repositoryURL
     *            the repository location
     * @param specifiedFolderName
     *            the name of the folder, which already exists in the
     *            repository, e.g. trunk/examples
     * @throws RemoteException
     */
    public void shareProjectUsingSpecifiedFolderName(String repositoryURL,
        String specifiedFolderName) throws RemoteException;

    /**
     * Using this method you can import a project from SVN (Other variant to
     * import a project from SVN is in the method . which should be done with
     * the following steps:
     * 
     * <ol>
     * <li>Clicks main menu "File" > "Import..."</li>
     * <li>Selects SVN -> checkout projects from SVN and click the button "next"
     * </li>
     * <li>In the next page select the repositoryURL if the repositoryURL
     * already exists,otherwise create a new one and click the next button</li>
     * <li>In the next page select the folder to be checked out from SVN and
     * click finish to confirm the import project with SVN process.</li>
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
     * @param repositoryURL
     *            the repository location
     * 
     * @throws RemoteException
     */
    public void importProjectFromSVN(String repositoryURL)
        throws RemoteException;

    /**
     * Perform the action "Disconnect from SVN" which should be done with the
     * following steps:
     * 
     * <ol>
     * <li>Select the given project and click "Team" > "Disconnect..."</li>
     * <li>click "Yes" to confirm the disconnect</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "Disconnect..." . I mean, after clicking the sub menu you need to treat
     * the following popup window too.</li>
     * 
     * @throws RemoteException
     */
    public void disConnect() throws RemoteException;

    /**
     * Perform the action "revert" which should be done with the following
     * steps:
     * 
     * <ol>
     * <li>Select the given project and click "Team" > "Revert..."</li>
     * <li>click "OK" to confirm the revert</li>
     * <li>Waits until the shell "Revert" is closed. It guarantee that the
     * "Revert" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "Revert..." . I mean, after clicking the sub menu you need to treat the
     * following popup window too.</li>
     * 
     * @throws RemoteException
     */
    public void revert() throws RemoteException;

    /**
     * Perform the action "switch to another revision" which should be done with
     * the following steps:
     * 
     * <ol>
     * <li>Select the given project and click "Team" >
     * "Switch to another Branch/Tag/Revision..."</li>
     * <li>uncheckt the checkbox with the title "Switch to HEAD version"</li>
     * <li>Enter the given versionID to the text field with the title
     * "Revision:"</li>
     * <li>click "OK" to confirm the switch</li>
     * <li>Waits until the shell "SVN Switch" is closed. It guarantee that the
     * "switch to another revision" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "switch to another revision" . I mean, after clicking the sub menu you
     * need to treat the following popup window too.</li>
     * 
     * @param versionID
     *            the ID of the revision to which you want to switch
     * @throws RemoteException
     */
    public void update(String versionID) throws RemoteException;

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

}
