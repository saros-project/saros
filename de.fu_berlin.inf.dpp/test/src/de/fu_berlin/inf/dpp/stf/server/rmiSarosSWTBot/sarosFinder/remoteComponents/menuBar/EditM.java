package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface EditM extends EclipseComponent {

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
    public void deleteProjectNoGUI(String projectName) throws RemoteException;

    /**
     * Delete all the projects existed in the package explorer view.
     * 
     * @throws RemoteException
     */
    public void deleteAllProjects(String viewTitle) throws RemoteException;

    /**
     * Perform the action "delete project" which should be done with the
     * following steps:
     * <ol>
     * <li>click the menu "Delete".</li>
     * <li>confirm the popup-window "Delete Resources" and make sure the
     * checkbox is clicked.</li>
     * <li>wait until the popup-window is closed.</li>
     * 
     * 
     */
    public void deleteProject() throws RemoteException;

    public void deleteAllItemsOfJavaProject(String viewTitle, String projectName)
        throws RemoteException;

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
    public void deleteFile() throws RemoteException;

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
    public void copyProject(String target) throws RemoteException;

    /**
     * Delete all the projects in this workspace.
     * 
     * @throws RemoteException
     */
    public void deleteAllProjectsNoGUI() throws RemoteException;
}
