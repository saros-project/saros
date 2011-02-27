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
    // public void deleteProject() throws RemoteException;

    public void deleteAllItemsOfJavaProject(String viewTitle, String projectName)
        throws RemoteException;

}
