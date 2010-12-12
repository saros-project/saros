package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BasicComponent extends Remote {

    public void sleep(long millis) throws RemoteException;

    public void captureScreenshot(String filename) throws RemoteException;

    public boolean isTextWithLabelEqualWithText(String label, String text)
        throws RemoteException;

    public void clickButton(String mnemonicText) throws RemoteException;

    public boolean isShellOpen(String title) throws RemoteException;

    public boolean isShellActive(String title) throws RemoteException;

    public void closeShell(String title) throws RemoteException;

    /**
     * TODO don't work now
     * 
     * @return the path, in which the screenshot located.
     * @throws RemoteException
     */
    public String getPathToScreenShot() throws RemoteException;
}
