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
     * Waits until the button is enabled.
     * 
     * @param mnemonicText
     *            the mnemonicText on the widget.
     */
    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException;

    /**
     * Waits until the button is enabled.
     * 
     * @param tooltipText
     *            the tooltip on the widget.
     */
    public void waitUnitButtonWithTooltipIsEnabled(String tooltipText)
        throws RemoteException;

    public void setTextInTextWithLabel(String text, String label)
        throws RemoteException;

    public String getLabelText() throws RemoteException;

    /**
     * TODO don't work now
     * 
     * @return the path, in which the screenshot located.
     * @throws RemoteException
     */
    public String getPathToScreenShot() throws RemoteException;

    public boolean isButtonEnabled(String mnemonicText) throws RemoteException;
}
