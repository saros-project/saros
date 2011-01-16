package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;

/**
 * This interface contains convenience API to perform actions in the package
 * explorer view (API to perform the specifically defined actions for saros
 * would be separately located in the sub-interface {@link SarosPEView}
 * ) , then you can start off as follows:
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
 * alice.pEV.deleteProject(...);
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface PEView extends Remote {

    /**********************************************
     * 
     * open/close/activate the package explorer view
     * 
     **********************************************/

    /**
     * open the package explorer view using the view ID
     * 
     * @throws RemoteException
     * @see ViewPart#openViewById(String)
     */
    public void openPEView() throws RemoteException;

    /**
     * 
     * @return <tt>true</tt> if all the opened views contains the package
     *         explorer view.
     * 
     * @throws RemoteException
     * @see ViewPart#isViewOpen(String)
     */
    public boolean isPEViewOpen() throws RemoteException;

    /**
     * close the package explorer view using the view ID
     * 
     * @throws RemoteException
     * @see ViewPart#closeViewById(String)
     */
    public void closePEView() throws RemoteException;

    /**
     * set focus on the package explorer view
     * 
     * @see ViewPart#setFocusOnViewByTitle(String)
     * @throws RemoteException
     */
    public void setFocusOnPEView() throws RemoteException;

    /**
     * 
     * @return <tt>true</tt>, if the package explorer view is active
     * @throws RemoteException
     */
    public boolean isPEViewActive() throws RemoteException;

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "New"
     * 
     **********************************************/

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Open"
     * 
     **********************************************/

    /**
     * Performs the action "open file" which should be done with the following
     * steps:
     * 
     * <ol>
     * <li>if the file is already open, return.</li>
     * <li>selects the file, which you want to open, and then click the context
     * menu "Open".</li>
     * </ol>
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.{Foo_Saros,
     *            myFolder, myFile.xml}
     * @throws RemoteException
     */
    public void openFile(String... fileNodes) throws RemoteException;

    /**
     * 
     * Performs the action "open class file" which should be done with the
     * following steps:
     * 
     * <ol>
     * <li>if the class file is already open, return.</li>
     * <li>selects the class file, which you want to open, and then click the
     * context menu "Open".</li>
     * </ol>
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class, e.g. MyClass
     * @throws RemoteException
     */
    public void openClass(String projectName, String pkg, String className)
        throws RemoteException;

    /**
     * 
     * @param whichEditor
     *            the name of the editor, with which you want to open the file.
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class, e.g. MyClass
     * @throws RemoteException
     * @see PEView#openFileWith(String, String...)
     */
    public void openClassWith(String whichEditor, String projectName,
        String pkg, String className) throws RemoteException;

    /**
     * Performs the action "open file with" which should be done with the
     * following steps:
     * 
     * <ol>
     * <li>selects the file, which you want to open, and then click the context
     * menu "Open with -> Other..."</li>
     * <li>choose the given editor for opening the file</li>
     * <li>click "OK" to confirm the rename</li>
     * </ol>
     * 
     * @param whichEditor
     *            the name of the editor, with which you want to open the file.
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g. {Foo_Saros,
     *            myFolder, myFile.xml}
     * 
     * @throws RemoteException
     */
    public void openFileWith(String whichEditor, String... fileNodes)
        throws RemoteException;

    /**
     * open class with system editor using
     * Program.launch(resource.getLocation().toString()
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class, e.g. MyClass
     * @throws RemoteException
     */
    public void openClassWithSystemEditor(String projectName, String pkg,
        String className) throws RemoteException;

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
     * @param projectName
     *            the name of the project located in the package explorer view,
     *            which will be shared under SVN.
     * @param repositoryURL
     *            the repository location
     * @throws RemoteException
     */
    public void shareProjectWithSVN(String projectName, String repositoryURL)
        throws RemoteException;

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
     * @param projectName
     *            the name of the project located in the package explorer view,
     *            which will be shared under SVN.
     * @param repositoryURL
     *            the repository location
     * @throws RemoteException
     */
    public void shareProjectWithSVNWhichIsConfiguredWithSVNInfos(
        String projectName, String repositoryURL) throws RemoteException;

    /**
     * using this method you can import a project from SVN (Other variant to
     * import a project from SVN is in the method
     * {@link PEView#importProjectFromSVN(String)} defined). which
     * should be done with the following steps:
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
    public void shareProjectWithSVNUsingSpecifiedFolderName(String projectName,
        String repositoryURL, String specifiedFolderName)
        throws RemoteException;

    /**
     * Using this method you can import a project from SVN (Other variant to
     * import a project from SVN is in the method
     * {@link PEView#shareProjectWithSVNUsingSpecifiedFolderName(String, String, String)}
     * defined). which should be done with the following steps:
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
    public void disConnect(String projectName) throws RemoteException;

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
    public void revertProject(String projectName) throws RemoteException;

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
     * 
     * @param projectName
     *            name of the java project, e.g. Foo-Saros.
     * @param versionID
     *            the ID of the revision to which you want to switch
     * @throws RemoteException
     */
    public void updateProject(String projectName, String versionID)
        throws RemoteException;

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
     * 
     * @param projectName
     *            name of the java project, e.g. Foo-Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @param versionID
     *            the ID of the revision to which you want to switch
     * @throws RemoteException
     */
    public void updateClass(String projectName, String pkg, String className,
        String versionID) throws RemoteException;

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
     * Uses Copy and Paste to create a copy of a project.<br>
     * Warning: This method is not thread safe if the threads run on the same
     * host because it uses the global clipboard to copy the project.
     * 
     * @param target
     *            The name of the copy to be created.
     * @param source
     *            The project to create the copy from.
     * @throws RemoteException
     */
    public void copyProject(String target, String source)
        throws RemoteException;

}
