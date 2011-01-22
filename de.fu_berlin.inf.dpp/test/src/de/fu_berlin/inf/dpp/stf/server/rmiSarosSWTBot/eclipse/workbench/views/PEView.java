package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.contextMenu.SarosC;

/**
 * This interface contains convenience API to perform actions in the package
 * explorer view (API to perform the specifically defined actions for saros
 * would be separately located in the sub-interface {@link SarosC} ) , then you
 * can start off as follows:
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

    public void selectProject(String projectName) throws RemoteException;

    public void selectPkg(String projectName, String pkg)
        throws RemoteException;

    public void selectClass(String projectName, String pkg, String className)
        throws RemoteException;

    public void selectFolder(String... pathToFolder) throws RemoteException;

    public void selectFile(String... pathToFile) throws RemoteException;

}
