package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.Perspective;

/**
 * This interface contains convenience API to perform a action using main menu
 * widgets. You can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link AbstractTester} object in your
 * junit-test. (How to do it please read the user guide in TWiki
 * https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * then you can use the object mainMenu initialized in {@link AbstractTester} to
 * access the APIs :), e.g.
 * 
 * <pre>
 * alice.mainMenu.clickMenuPreferences();
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface IWindowMenu extends Remote {

    /**********************************************
     * 
     * change setting with preferences dialog
     * 
     **********************************************/

    /**
     * selects a new text file line delimiter on the preference page which
     * should be done with the following steps:
     * <ol>
     * <li>Click menu "Window" -> "Preferences"</li>
     * <li>In the preference dialog select "General" -> "Workspace"</li>
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
    public void setNewTextFileLineDelimiter(String whichOS)
        throws RemoteException;

    /**
     * 
     * @return the used text file line delimiter
     * @throws RemoteException
     */
    public String getTextFileLineDelimiter() throws RemoteException;

    /**
     * if OS is windows: click menu "Window" -> "Preferences".<br/>
     * if OS is MAC: click menu "Eclipse" -> "Preferences".
     * 
     * @throws RemoteException
     */
    public void clickMenuPreferences() throws RemoteException;

    /**********************************************
     * 
     * show view with main menu
     * 
     **********************************************/
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

    /**
     * Open a view using menus Window->Show View->Other... . which should be
     * done with the following steps:
     * <ol>
     * <li>If the view is already open, return.</li>
     * <li>Activate the saros-instance workbench(alice / bob / carl). If the
     * workbench isn't active, bot can't find the main menus.</li>
     * <li>Click main menus Window -> Show View -> Other...</li>
     * <li>Confirm the pop-up window "Show View".</li>
     * </ol>
     * 
     * @param category
     *            example: "General"
     * @param nodeName
     *            example: "Console"
     * 
     */
    public void showViewWithName(String category, String nodeName)
        throws RemoteException;

    /**********************************************
     * 
     * open perspectives
     * 
     **********************************************/

    /**
     * open the default perspective which is defined in
     * {@link Perspective#WHICH_PERSPECTIVE}.
     */
    public void openPerspective() throws RemoteException;

    /**
     * Open the perspective "Resource" with id
     * 
     * @throws RemoteException
     */
    public void openPerspectiveResource() throws RemoteException;

    /**
     * Open the perspective "Java" with id
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
     * Open the perspective "Debug" with Id
     * 
     * @throws RemoteException
     */
    public void openPerspectiveDebug() throws RemoteException;

    /**
     * @return <tt>true</tt>, if the debug perspective is active.
     * @throws RemoteException
     */
    public boolean isDebugPerspectiveActive() throws RemoteException;
}
