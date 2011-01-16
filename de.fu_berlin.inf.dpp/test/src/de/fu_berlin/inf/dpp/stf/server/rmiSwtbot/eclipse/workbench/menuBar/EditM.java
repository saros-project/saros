package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.menuBar;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EditM extends Remote {

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
     * Delete all the projects existed in the package explorer view.
     * 
     * @throws RemoteException
     */
    public void deleteAllProjectsWithGUI() throws RemoteException;

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
}
