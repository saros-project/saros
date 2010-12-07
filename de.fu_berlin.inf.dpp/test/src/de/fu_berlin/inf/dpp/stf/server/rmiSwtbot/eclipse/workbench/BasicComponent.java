package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BasicComponent extends Remote {

    public void sleep(long millis) throws RemoteException;

    public void captureScreenshot(String filename) throws RemoteException;

    public boolean isTextWithLabelEqualWithText(String label, String text)
        throws RemoteException;

    public void clickButton(String mnemonicText) throws RemoteException;

    /**
     * TODO don't work now
     * 
     * @return the path, in which the screenshot located.
     * @throws RemoteException
     */
    public String getPathToScreenShot() throws RemoteException;
}
