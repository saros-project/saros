package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MainMenuComponent extends Remote {

    public void preference() throws RemoteException;

    public void newTextFileLineDelimiter(String OS) throws RemoteException;

    public String getTextFileLineDelimiter() throws RemoteException;

    /**
     * Open the view "Problems". The name of the method is defined the same as
     * the menu names. The name "showViewProblem" then means: hello guy, please
     * click main menus Window -> Show view -> Problems.
     * 
     */
    public void showViewProblems() throws RemoteException;

    /**
     * Open the view "Project Explorer". The name of the method is defined the
     * same as the menu names. The name "showViewProblem" then means: hello guy,
     * please click main menus Window -> Show view -> Project Explorer.
     * 
     */
    public void showViewProjectExplorer() throws RemoteException;

    /**
     * Open the perspective "Java". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveJava" then means: hello guy,
     * please click main menus Window -> Open perspective -> Java.
     * 
     */
    public void openPerspectiveJava() throws RemoteException;

    /**
     * test, if the java perspective is active.
     */
    public boolean isJavaPerspectiveActive() throws RemoteException;

    /**
     * Open the perspective "Debug". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveDebug" then means: hello guy,
     * please click main menus Window -> Open perspective -> Debug.
     * 
     */
    public void openPerspectiveDebug() throws RemoteException;

    /**
     * test, if the debug perspective is active.
     */
    public boolean isDebugPerspectiveActive() throws RemoteException;

    public void importProjectFromSVN(String path) throws RemoteException;

}
