package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public interface SarosWorkbenchComponent extends Remote {
    public void sleep(long millis) throws RemoteException;

    public void captureScreenshot(String filename) throws RemoteException;

    /**
     * TODO don't work now
     * 
     * @return the path, in which the screenshot located.
     * @throws RemoteException
     */
    public String getPathToScreenShot() throws RemoteException;

    public void openSarosViews() throws RemoteException;

    public void closeUnnecessaryViews() throws RemoteException;

    public void resetSaros() throws RemoteException;

    public SWTBotShell getEclipseShell() throws RemoteException;

    /**
     * Open Java perspective, close all editors and dialogs.
     */
    public void resetWorkbench() throws RemoteException;

    public void activateEclipseShell() throws RemoteException;

    public void closeWelcomeView() throws RemoteException;

    public void setUpWorkbench() throws RemoteException;

}
