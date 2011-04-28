package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;

public class RemoteBotCheckBox extends AbstractRmoteWidget implements
    IRemoteBotCheckBox {

    private static transient RemoteBotCheckBox self;

    private SWTBotCheckBox widget;

    /**
     * {@link RemoteBotCheckBox} is a singleton, but inheritance is possible.
     */
    public static RemoteBotCheckBox getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotCheckBox();
        return self;
    }

    public IRemoteBotCheckBox setWidget(SWTBotCheckBox checkBox) {
        this.widget = checkBox;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finders
     * 
     **********************************************/

    /**
     * 
     * @see SWTBotCheckBox#contextMenu(String)
     */
    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    /**
     * 
     * @see SWTBotCheckBox#click()
     */
    public void click() throws RemoteException {
        widget.click();
    }

    /**
     * 
     * @see SWTBotCheckBox#select()
     */
    public void select() throws RemoteException {
        widget.select();
    }

    /**
     * 
     * @see SWTBotCheckBox#deselect()
     */
    public void deselect() throws RemoteException {
        widget.deselect();

    }

    /**
     * 
     * @see SWTBotCheckBox#setFocus()
     */
    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    /**
     * 
     * @see SWTBotCheckBox#isEnabled()
     */
    public boolean isEnabled() throws RemoteException {
        return widget.isEnabled();
    }

    /**
     * 
     * @see SWTBotCheckBox#isVisible()
     */
    public boolean isVisible() throws RemoteException {
        return widget.isVisible();
    }

    /**
     * 
     * @see SWTBotCheckBox#isActive()
     */
    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    /**
     * 
     * @see SWTBotCheckBox#isChecked()
     */
    public boolean isChecked() throws RemoteException {
        return widget.isChecked();
    }

    /**
     * 
     * @see SWTBotCheckBox#getText()
     */
    public String getText() throws RemoteException {
        return widget.getText();
    }

    /**
     * 
     * @see SWTBotCheckBox#getToolTipText()
     */
    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

}
