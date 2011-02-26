package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.PEView;

public interface OpenC extends EclipseComponent {

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
    // public void openFile(String viewTitle, String... fileNodes)
    // throws RemoteException;

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
    // public void openClass(String viewTitle, String projectName, String pkg,
    // String className) throws RemoteException;

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
    public void openClassWith(String viewTitle, String whichEditor,
        String projectName, String pkg, String className)
        throws RemoteException;

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
    public void openFileWith(String viewTitle, String whichEditor,
        String... fileNodes) throws RemoteException;

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
    public void openClassWithSystemEditorNoGUI(String projectName, String pkg,
        String className) throws RemoteException;

}
