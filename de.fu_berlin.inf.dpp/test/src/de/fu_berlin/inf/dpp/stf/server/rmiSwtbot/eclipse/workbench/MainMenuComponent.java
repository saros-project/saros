package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MainMenuComponent extends Remote {

    /**********************************************
     * 
     * all related actions with preferences
     * 
     **********************************************/

    /**
     * selects a new text file line delimiter on the preference page which
     * should be done with the following steps:
     * <ol>
     * <li>Click menu "Window" -> "Preferences"</li>
     * <li>In the preference page select "General" -> "Workspace"</li>
     * <li>modify the text file line delimiter with the passed parameter "OS"</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the saros-instance is active.</li>
     * </ol>
     * 
     * @param whichOS
     *            the name of the OS, which you want to select.
     * 
     */
    public void newTextFileLineDelimiter(String whichOS) throws RemoteException;

    /**
     * 
     * @return the used text file line delimiter
     * @throws RemoteException
     */
    public String getTextFileLineDelimiter() throws RemoteException;

    /**
     * Open the view "Problems" using GUI which should be done with the
     * following steps:
     * <ol>
     * <li>Click menu "Window" -> "show view" -> "other..."</li>
     * <li>Select "Gernaral" -> "Problems" and click "Finish" to confirm the
     * action</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the saros-instance is active.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void showViewProblems() throws RemoteException;

    /**
     * Open the view "Project Explorer" using GUI which should be done with the
     * following steps:
     * <ol>
     * <li>Click menu "Window" -> "show view" -> "other..."</li>
     * <li>Select "Gernaral" -> "Project Explorer" and click "Finish" to confirm
     * the action</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the saros-instance is active.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void showViewProjectExplorer() throws RemoteException;

    /**********************************************
     * 
     * open perspectives
     * 
     **********************************************/
    /**
     * Open the perspective "Java" using GUI which should be done with the
     * following steps:
     * <ol>
     * <li>Click menu "Window" -> "Open Perspective" -> "other..."</li>
     * <li>Select "Java (default)" and click "Finish" to confirm the action</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the saros-instance is active.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void openPerspectiveJava() throws RemoteException;

    /**
     * @return <tt>true</tt>, if the java perspective is active.
     * @throws RemoteException
     */
    public boolean isJavaPerspectiveActive() throws RemoteException;

    /**
     * Open the perspective "Java" using GUI which should be done with the
     * following steps:
     * <ol>
     * <li>Click menu "Window" -> "Open Perspective" -> "other..."</li>
     * <li>Select "Debug" and click "Finish" to confirm the action</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the saros-instance is active.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void openPerspectiveDebug() throws RemoteException;

    /**
     * @return <tt>true</tt>, if the debug perspective is active.
     * @throws RemoteException
     */
    public boolean isDebugPerspectiveActive() throws RemoteException;

    public void clickMenuWithTexts(String... texts) throws RemoteException;

    public void clickMenuPreferences() throws RemoteException;

    public void openViewWithName(String category, String nodeName)
        throws RemoteException;
}
