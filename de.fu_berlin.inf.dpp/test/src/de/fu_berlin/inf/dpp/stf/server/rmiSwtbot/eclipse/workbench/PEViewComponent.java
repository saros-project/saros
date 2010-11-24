package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ViewPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosPEViewComponent;

/**
 * This interface contains convenience API to perform actions in the package
 * explorer view (API to perform the specifically defined actions for saros
 * would be separately located in the sub-interface {@link SarosPEViewComponent}
 * ) , then you can start off as follows:
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
 * alice.pEV.deleteProject(...);
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface PEViewComponent extends Remote {

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

    /**
     * Performs the action "create a project" which should be done with the
     * following steps:
     * 
     * <ol>
     * <li>if the project already exist, return</li>
     * <li>click context menu: New -> Project</li>
     * <li>confirm the pop-up window "New Project"</li>
     * <li>wait until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Project..." on the package Explorer view.</li>
     * </ol>
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * 
     */
    public void newProject(String projectName) throws RemoteException;

    /**
     * Performs the action "create a java project" which should be done with the
     * following steps:
     * <ol>
     * <li>if the java project already exist, return.</li>
     * <li>click context menu: New -> Java Project</li>
     * <li>confirm the pop-up window "New Java Project"</li>
     * <li>wait until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Java Project" on the package Explorer view.</li>
     * </ol>
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     */
    public void newJavaProject(String projectName) throws RemoteException;

    /**
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @return <tt>true</tt>, if the given project is exist
     */
    public boolean isProjectExist(String projectName) throws RemoteException;

    /**
     * Performs the action "create a new folder" which should be done with the
     * following steps:
     * <ol>
     * <li>If the folder already exists, return</li>
     * <li>Click sub menu: New -> Folder</li>
     * <li>Confirm pop-up window "New Folder"</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Folder" on the package Explorer view.</li>
     * </ol>
     * 
     * @param newFolderName
     *            the name of the new created folder which should be located in
     *            the parent folder specified by the parentNodes array
     *            parameter. e.g. "myFolder"
     * @param parentNodes
     *            node path to expand. Attempts to expand all parent nodes along
     *            the path specified by the parent node array parameter.e.g.
     *            {"Foo-saros","parentFolder" }
     */
    public void newFolder(String newFolderName, String... parentNodes)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the given folder already exists.
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     */
    public boolean isFolderExist(String... folderNodes) throws RemoteException;

    /**
     * waits until the specified folder is exist
     * 
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     */
    public void waitUntilFolderExist(String... folderNodes)
        throws RemoteException;

    /**
     * Performs the action "create a new package" which should be done with the
     * following steps:
     * <ol>
     * <li>if the package already exist, return</li>
     * <li>Click sub menu: New -> Package</li>
     * <li>Confirm pop-up window "New Java Package"</li>
     * <li>waits until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Package" on the package Explorer view.</li>
     * </ol>
     * 
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * 
     */
    public void newPackage(String projectName, String pkg)
        throws RemoteException;

    /**
     * @return <tt>true</tt>, if the specified package already exists.
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @throws RemoteException
     */
    public boolean isPkgExist(String projectName, String pkg)
        throws RemoteException;

    /**
     * wait until the given package is exist
     * 
     * @param projectName
     *            name of the java project, e.g. Foo-Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @throws RemoteException
     */
    public void waitUntilPkgExist(String projectName, String pkg)
        throws RemoteException;

    /**
     * waits until the given package isn't exist
     ** 
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @throws RemoteException
     */
    public void waitUntilPkgNotExist(String projectName, String pkg)
        throws RemoteException;

    /**
     * Performs the action "create a new file" which should be done with the
     * following steps:
     * <ol>
     * <li>if the package already exist, return</li>
     * <li>Click sub menu: New -> File</li>
     * <li>Confirm pop-up window "New File"</li>
     * <li>waits until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "File" on the package Explorer view.</li>
     * </ol>
     * 
     * @param filePath
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder", "myFile.xml"}
     * 
     */
    public void newFile(String... filePath) throws RemoteException;

    /**
     * please use the method
     * {@link EclipseComponent#getClassPath(String, String, String)}to get the
     * class path and the method {@link EclipseComponent#getPath(String...)} to
     * get the file path.
     * 
     * @param filePath
     *            path of the file, e.g. "Foo_Saros/myFolder/myFile.xml" or path
     *            of a class file, e.g. "Foo_Saros/src/my/pkg/myClass.java
     * 
     */
    public boolean isFileExist(String filePath) throws RemoteException;

    /**
     * Performs the action "create a new class" which should be done with the
     * following steps:
     * 
     * <ol>
     * <li>if the class already exist, return</li>
     * <li>Click sub menu: New -> Class</li>
     * <li>Confirm pop-up window "New Java Class"</li>
     * <li>waits until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Class" on the package Explorer view.</li>
     * </ol>
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * 
     */
    public void newClass(String projectName, String pkg, String className)
        throws RemoteException;

    /**
     * waits until the specified class is exist
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @throws RemoteException
     */
    public void waitUntilClassExist(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * waits until the specified class isn't exist
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @throws RemoteException
     */
    public void waitUntilClassNotExist(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * Performs the action "create a new class that implements runnable" which
     * should be done with the following steps:
     * 
     * <ol>
     * <li>if the class already exist, return</li>
     * <li>Click sub menu: New -> Class</li>
     * <li>Confirm pop-up window "New Java Class"</li>
     * <li>waits until the pop-up window is closed</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The name of the method consists of the context menu's name "New" and
     * the sub menu's name "Class" + extra actions on the package Explorer view.
     * </li>
     * </ol>
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * 
     */
    public void newClassImplementsRunnable(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * Create a java project and a class in the project. The two functions
     * newJavaProject and newClass are often used, so i put them together to
     * simplify the junit-tests.
     * 
     * Attention: after creating a project bot need to sleep a moment until he
     * is allowed to create class. so if you want to create a project with a
     * class, please use this method, otherwise you should get
     * WidgetNotfoundException.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class, e.g. MyClass
     */
    public void newJavaProjectWithClass(String projectName, String pkg,
        String className) throws RemoteException;

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
     * <li>if the class file is already open, return.</li>
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

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Delete"
     * 
     **********************************************/

    /**
     * Delete the project using FileUntil.delete(resource). This delete-method
     * costs less time than the method using GUI
     * 
     * @param projectName
     *            name of the project, which you want to delete.
     */
    public void deleteProject(String projectName) throws RemoteException;

    /**
     * Perform the action "delete project" which should be done with the
     * following steps:
     * <ol>
     * <li>select the project,which you want to delete, and then click the
     * context menu "Delete".</li>
     * <li>confirm the popup-window "Delete Resources" and make sure the
     * checkbox is clicked.</li>
     * <li>wait until the popup-window is closed.</li>
     * 
     * @param projectName
     *            the name of the project, which you want to delete.
     */
    public void deleteProjectWithGUI(String projectName) throws RemoteException;

    /**
     * Delete the specified folder using FileUntil.delete(resource).
     * 
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     */
    public void deleteFolder(String... folderNodes) throws RemoteException;

    /**
     * Delete the specified package using FileUntil.delete(resource).
     * 
     * @param projectName
     *            name of the project, which package you want to delete.
     * @param pkg
     *            name of the package, which you want to delete.
     */
    public void deletePkg(String projectName, String pkg)
        throws RemoteException;

    /**
     * Performs the action "delete file" which should be done with the following
     * steps:
     * 
     * <ol>
     * <li>selects the file,which you want to delete, and then click the context
     * menu Delete.</li>
     * <li>confirms the popup-window "Confirm Delete".</li>
     * <li>waits until the popup-window is closed.</li>
     * </ol>
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     */
    public void deleteFile(String... nodes) throws RemoteException;

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
    public void deleteClass(String projectName, String pkg, String className)
        throws RemoteException;

    /**
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","src" "my.pkg", "myClass.java"}
     * @return <tt>true</tt>, if the file specified by the node array parameter
     *         exists
     * @throws RemoteException
     */
    public boolean isFileExistWithGUI(String... nodes) throws RemoteException;

    /**
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g. {"Foo-saros",
     *            "myFolder", "myFile.xml"}
     */
    public void waitUntilFileExist(String... fileNodes) throws RemoteException;

    /**
     * Performs the action "move class to another package" which should be done
     * with the following steps:
     * 
     * <ol>
     * <li>selects the class, which you want to move, and then click the context
     * menu "Refactor -> Move..."</li>
     * <li>choose the package specified by the passed parameter "targetPkg"</li>
     * <li>click "OK" to confirm the move</li>
     * </ol>
     * 
     * @param sourceProject
     *            name of the project, e.g. Foo-Saros.
     * @param sourcePkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @param targetProject
     * @param targetPkg
     * @throws RemoteException
     */
    public void moveClassTo(String sourceProject, String sourcePkg,
        String className, String targetProject, String targetPkg)
        throws RemoteException;

    /**
     * Perform the action "rename package" which should be done with the
     * following steps:
     * <ol>
     * <li>Select the given package with the given node path and click
     * "Refactor" > "Rename..."</li>
     * <li>Enter the given new name to the text field with the title "New name:"
     * </li>
     * <li>click "OK" to confirm the rename</li>
     * <li>Waits until the shell "Rename package" is closed. It guarantee that
     * the "rename package" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "rename..." . I mean, after clicking the sub menu you need to treat the
     * following popup window too.</li>
     * 
     * 
     * @param newName
     *            the new name of the given package.
     * 
     * @throws RemoteException
     */
    public void renamePkg(String newName, String projectName, String pkg)
        throws RemoteException;

    /**
     * Perform the action "rename folder" which should be done with the
     * following steps:
     * <ol>
     * <li>Select the given folder with the given node path and click "Refactor"
     * > "Rename..."</li>
     * <li>Enter the given new name to the text field with the title "New name:"
     * </li>
     * <li>click "OK" to confirm the rename</li>
     * <li>Waits until the shell "Rename resource" is closed. It guarantee that
     * the "rename folder" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "rename..." . I mean, after clicking the sub menu you need to treat the
     * following popup window too.</li>
     * 
     * 
     * @param newName
     *            the new name of the given folder.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.{"Foo-saros",
     *            "myFolder"}
     * @throws RemoteException
     */
    public void renameFolder(String newName, String... nodes)
        throws RemoteException;

    /**
     * Perform the action "rename file" which should be done with the following
     * steps:
     * <ol>
     * <li>Select the given file with the given node path and click "Refactor" >
     * "Rename..."</li>
     * <li>Enter the given new name to the text field with the title "New name:"
     * </li>
     * <li>click "OK" to confirm the rename</li>
     * <li>Waits until the shell "Rename Compilation Unit" is closed. It
     * guarantee that the "rename file" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "rename..." . I mean, after clicking the sub menu you need to treat the
     * following popup window too.</li>
     * 
     * 
     * @param newName
     *            the new name of the given file.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.{"Foo-saros",
     *            "myFolder", "myFile.xml"}
     * @throws RemoteException
     */
    public void renameFile(String newName, String... nodes)
        throws RemoteException;

    /**
     * Perform the action "rename class" which should be done with the following
     * steps:
     * <ol>
     * <li>Select the given class with the given node path and click "Refactor"
     * > "Rename..."</li>
     * <li>Enter the given new name to the text field with the title "New name:"
     * </li>
     * <li>click "OK" to confirm the rename</li>
     * <li>Waits until the shell "Rename Compilation Unit" is closed. It
     * guarantee that the "rename file" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "rename..." . I mean, after clicking the sub menu you need to treat the
     * following popup window too.</li>
     * 
     * 
     * @param newName
     *            the new name of the given class.
     * @param projectName
     *            name of the java project, e.g. Foo-Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * @throws RemoteException
     */
    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException;

    public void renameJavaProject(String newName, String... nodes)
        throws RemoteException;

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
    public void revert(String projectName) throws RemoteException;

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
     * {@link PEViewComponent#importProjectFromSVN(String)} defined). which
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
     * {@link PEViewComponent#shareProjectWithSVNUsingSpecifiedFolderName(String, String, String)}
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
     * Perform the action "switch to another Reversion" which should be done
     * with the following steps:
     * 
     * <ol>
     * <li>Select the given project and click "Team" >
     * "Switch to another Branch/Tag/Revision..."</li>
     * <li>uncheckt the checkbox with the title "Switch to HEAD version"</li>
     * <li>Enter the given versionID to the text field with the title
     * "Revision:"</li>
     * <li>click "OK" to confirm the switch</li>
     * <li>Waits until the shell "SVN Switch" is closed. It guarantee that the
     * "switch to another Reversion" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "switch to another Reversion" . I mean, after clicking the sub menu you
     * need to treat the following popup window too.</li>
     * 
     * 
     * @param projectName
     *            name of the java project, e.g. Foo-Saros.
     * @param versionID
     *            the ID of the reversion to which you want to switch
     * @throws RemoteException
     */
    public void switchProjectToAnotherRevision(String projectName,
        String versionID) throws RemoteException;

    /**
     * Perform the action "switch to another Reversion" which should be done
     * with the following steps:
     * 
     * <ol>
     * <li>Select the given project and click "Team" >
     * "Switch to another Branch/Tag/Revision..."</li>
     * <li>uncheckt the checkbox with the title "Switch to HEAD version"</li>
     * <li>Enter the given versionID to the text field with the title
     * "Revision:"</li>
     * <li>click "OK" to confirm the switch</li>
     * <li>Waits until the shell "SVN Switch" is closed. It guarantee that the
     * "switch to another Reversion" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "switch to another Reversion" . I mean, after clicking the sub menu you
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
     *            the ID of the reversion to which you want to switch
     * @throws RemoteException
     */
    public void switchClassToAnotherRevision(String projectName, String pkg,
        String className, String versionID) throws RemoteException;

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
     * "switch to another Branch/Tag/Reversion" action is completely done.</li>
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
    public void switchToAnotherBranchOrTag(String projectName, String url)
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
    public boolean isInSVN(String prjectName) throws RemoteException;

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
     *            the path of the resource, e.g.
     *            "examples/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * @return the reversion id of the given resource.
     * @throws RemoteException
     */
    public String getReversion(String fullPath) throws RemoteException;

    /**
     * 
     * @param fullPath
     *            the path of the resource, e.g.
     *            "examples/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * @param reversionID
     *            the ID of the reversion,with which your reversion should
     *            compare
     * @throws RemoteException
     */
    public void waitUntilReversionIsSame(String fullPath, String reversionID)
        throws RemoteException;

    /**
     * @param fullPath
     *            the path of the resource, e.g.
     *            "examples/src/org/eclipsecon/swtbot/example/MyFirstTest01.java"
     * 
     * @return the VCS specific URL information for the given resource specified
     *         by the passed parameter"fullPath".
     * @throws RemoteException
     */
    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException;

}
