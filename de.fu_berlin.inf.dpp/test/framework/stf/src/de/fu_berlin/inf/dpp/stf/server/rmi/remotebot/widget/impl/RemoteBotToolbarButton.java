package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarButton;

public final class RemoteBotToolbarButton extends StfRemoteObject implements
    IRemoteBotToolbarButton {

    private static final RemoteBotToolbarButton INSTANCE = new RemoteBotToolbarButton();

    private SWTBotToolbarButton toolbarButton;

    public static RemoteBotToolbarButton getInstance() {
        return INSTANCE;
    }

    public IRemoteBotToolbarButton setWidget(SWTBotToolbarButton toolbarButton) {
        this.toolbarButton = toolbarButton;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return RemoteBotMenu.getInstance().setWidget(
            toolbarButton.contextMenu(text));

    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void click() throws RemoteException {
        toolbarButton.click();
    }

    public void clickAndWait() throws RemoteException {
        waitUntilIsEnabled();
        click();
    }

    public void setFocus() throws RemoteException {
        toolbarButton.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public boolean isEnabled() throws RemoteException {
        return toolbarButton.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return toolbarButton.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return toolbarButton.isActive();
    }

    public String getText() throws RemoteException {
        return toolbarButton.getText();
    }

    public String getToolTipText() throws RemoteException {
        return toolbarButton.getToolTipText();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsEnabled() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(
            Conditions.widgetIsEnabled(toolbarButton));
    }

}
