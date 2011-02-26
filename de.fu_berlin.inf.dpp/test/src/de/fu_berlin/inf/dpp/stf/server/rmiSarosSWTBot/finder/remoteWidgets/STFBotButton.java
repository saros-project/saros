package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;

public interface STFBotButton extends Remote {

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    /**
     * @see SWTBotButton#contextMenu(String)
     */
    public STFBotMenu contextMenu(String text) throws RemoteException;

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    /**
     * @see SWTBotButton#click()
     */
    public void click() throws RemoteException;

    /**
     * Click the button until it is enabled
     * 
     * @throws RemoteException
     */
    public void clickAndWait() throws RemoteException;

    /**
     * @see SWTBotButton#setFocus()
     * @throws RemoteException
     */
    public void setFocus() throws RemoteException;

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    /**
     * 
     * 
     * @see SWTBotButton#isEnabled()
     * @throws RemoteException
     */
    public boolean isEnabled() throws RemoteException;

    /**
     * 
     * @see SWTBotButton#isVisible()
     * @throws RemoteException
     */
    public boolean isVisible() throws RemoteException;

    /**
     * 
     * @see SWTBotButton#isActive()
     * @throws RemoteException
     */
    public boolean isActive() throws RemoteException;

    /**
     * 
     * @see SWTBotButton#getText()
     * @throws RemoteException
     */
    public String getText() throws RemoteException;

    /**
     * 
     * @see SWTBotButton#getToolTipText()
     * @throws RemoteException
     */
    public String getToolTipText() throws RemoteException;

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    /**
     * Waits until the button is enabled.
     */
    public void waitUntilIsEnabled() throws RemoteException;

    public void waitLongUntilIsEnabled() throws RemoteException;

}
