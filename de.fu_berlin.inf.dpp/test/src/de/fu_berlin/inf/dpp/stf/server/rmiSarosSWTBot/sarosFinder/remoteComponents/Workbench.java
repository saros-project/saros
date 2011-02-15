package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.SWTBot;

/**
 * This interface contains convenience APIs to perform actions in the workbench.
 * The main tasks are to setUp/cleanUp the saros_instance_workbench.
 * 
 * @author lchen
 */
public interface Workbench extends EclipseComponent {

    /**********************************************
     * 
     * action
     * 
     **********************************************/
    /**
     * 
     * @see SWTBot#sleep(long)
     * @throws RemoteException
     */
    public void sleep(long millis) throws RemoteException;

    /**
     * @see SWTBot#captureScreenshot(String)
     * @throws RemoteException
     */
    public void captureScreenshot(String fileName) throws RemoteException;

    /**
     * close all views which are not necessary to test saros.
     */
    public void closeUnnecessaryViews() throws RemoteException;

    /**
     * It's very recommend to clean up the test_workbench after every tests.
     * <ol>
     * <li>
     * open java perspective</li>
     * <li>close all unexpected popUp windows</li>
     * <li>close all opened editors</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void resetWorkbench() throws RemoteException;

    public void closeAllShells() throws RemoteException;

    /**
     * Activate the saros-instance.This method is very useful, wenn you test
     * saros under MAC
     * 
     * @throws RemoteException
     */
    public void activateWorkbench() throws RemoteException;

    /**
     * It's very recommend to clean up the test_workbench before every tests.
     * <ol>
     * <li>open java perspective</li>
     * <li>close all unexpected popUp windows</li>
     * <li>close all opened editors</li>
     * <li>delete all projects</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    public void setUpWorkbench() throws RemoteException;

    /**********************************************
     * 
     * state
     * 
     **********************************************/

    /**
     * TODO doesn't work now
     * 
     * @return the path, in which the screenShots located.
     * @throws RemoteException
     */
    public String getPathToScreenShot() throws RemoteException;

}
