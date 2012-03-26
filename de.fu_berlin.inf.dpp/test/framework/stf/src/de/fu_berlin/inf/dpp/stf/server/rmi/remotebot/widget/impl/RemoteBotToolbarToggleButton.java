package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarToggleButton;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarToggleButton;

public final class RemoteBotToolbarToggleButton extends StfRemoteObject
    implements IRemoteBotToolbarToggleButton {

    private static final RemoteBotToolbarToggleButton INSTANCE = new RemoteBotToolbarToggleButton();

    private SWTBotToolbarToggleButton widget;

    public static RemoteBotToolbarToggleButton getInstance() {
        return INSTANCE;
    }

    public IRemoteBotToolbarToggleButton setWidget(
        SWTBotToolbarToggleButton toolbarToggleButton) {
        this.widget = toolbarToggleButton;
        return this;
    }

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
    }

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

    public void waitUntilIsEnabled() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(
            Conditions.widgetIsEnabled(widget));
    }

}
