package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PEViewComponent extends Remote {

    public void closePackageExplorerView() throws RemoteException;

    public void setFocusOnPackageExplorerView() throws RemoteException;

    /**
     * Delete the selected project in view "Package Explorer" using GUI-method
     * <ol>
     * <li>if view "Package Explorer" isn't open, open it.</li>
     * <li>if view "Package Explorer" isn't active, activate it.</li>
     * <li>select the project,which you want to delete, and then click the main
     * menu Edit->Delete.</li>
     * <li>confirm the popup-window "Delete Resources" and make sure the
     * checkbox is clicked.</li>
     * <li>delegate wait so long until the popup-window is closed.</li>
     * 
     * @param projectName
     *            the treeitem's name of the tree in the view "Package Explorer"
     */
    public void deleteProjectGui(String projectName) throws RemoteException;

    /**
     * Delete the selected file in the view "Package Explorer" using GUI-method
     * 
     * <ol>
     * <li>if view "Package Explorer" isn't open, open it.</li>
     * <li>if view "Package Explorer" isn't active, activate it.</li>
     * <li>select the file,which you want to delete, and then click the menu
     * Edit->Delete.</li>
     * <li>confirm the popup-window "Confirm Delete".</li>
     * <li>bot wait so long until the popup-window is closed.</li>
     * </ol>
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     */
    public void deleteFileGui(String... nodes) throws RemoteException;

    public boolean isClassExistGUI(String... matchTexts) throws RemoteException;

    /**
     * Open a class
     * 
     * 1. if the class file is already open, return.
     * 
     * 2. select the class file.
     * 
     * 3. then click the context menu open.
     * 
     * @param projectName
     *            name of the project, e.g. Foo-Saros.
     * @param packageName
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     */
    public void openClass(String projectName, String packageName,
        String className) throws RemoteException;

    public void openFile(String... filePath) throws RemoteException;

    public void openClassWith(String whichEditor, String projectName,
        String packageName, String className) throws RemoteException;

    /**
     * Open the view "Package Explorer". The name of the method is defined the
     * same as the menu names. The name "showViewPackageExplorer" then means:
     * hello guy, please click main menus Window -> Show view ->
     * PackageExplorer.
     * 
     */
    public void showViewPackageExplorer() throws RemoteException;

    public void moveClassTo(String projectName, String pkg, String className,
        String targetProject, String targetPkg) throws RemoteException;

    public void importProjectFromSVN(String path) throws RemoteException;

    public void renamePkg(String newName, String... texts)
        throws RemoteException;

    public void renameFolder(String projectName, String oldPath, String newPath)
        throws RemoteException;

    public void renameFile(String newName, String... texts)
        throws RemoteException;

    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException;

    public void revert() throws RemoteException;

    public void switchToOtherRevision(String CLS_PATH) throws RemoteException;

    public void switchToOtherRevision() throws RemoteException;

    public void connectSVN() throws RemoteException;

    public void disConnectSVN() throws RemoteException;

    public void switchToTag() throws RemoteException;

    public void waitUntilSarosRunningVCSOperationClosed()
        throws RemoteException;

}
