package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarToggleButton;

public class RemoteBotToolbarToggleButton extends AbstractRmoteWidget implements
    IRemoteBotToolbarToggleButton {

    private static transient RemoteBotToolbarToggleButton self;

    private SWTBotToolbarToggleButton widget;

    /**
     * {@link RemoteBotToolbarToggleButton} is a singleton, but inheritance is
     * possible.
     */
    public static RemoteBotToolbarToggleButton getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotToolbarToggleButton();
        return self;
    }

    public IRemoteBotToolbarToggleButton setWidget(
        SWTBotToolbarToggleButton toolbarToggleButton) {
        this.widget = toolbarToggleButton;
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
    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void click() throws RemoteException {
        widget.click();
    }

    public void deselect() throws RemoteException {
        widget.deselect();
    }

    public IRemoteBotToolbarToggleButton toggle() throws RemoteException {
        return setWidget(widget.toggle());
    }

    public void select() throws RemoteException {
        widget.select();
    }

    public void clickAndWait() throws RemoteException {
        waitUntilIsEnabled();
        click();
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isEnabled() throws RemoteException {
        return widget.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return widget.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    public String getText() throws RemoteException {
        return widget.getText();
    }

    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

    public boolean isChecked() throws RemoteException {
        return widget.isChecked();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsEnabled() throws RemoteException {
        stfBot.waitUntil(Conditions.widgetIsEnabled(widget));
    }

}
