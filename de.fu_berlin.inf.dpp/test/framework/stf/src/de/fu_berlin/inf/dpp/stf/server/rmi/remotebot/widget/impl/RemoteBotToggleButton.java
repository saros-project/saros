package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToggleButton;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToggleButton;

public final class RemoteBotToggleButton extends StfRemoteObject implements
    IRemoteBotToggleButton {

    private static final RemoteBotToggleButton INSTANCE = new RemoteBotToggleButton();

    private SWTBotToggleButton widget;

    public static RemoteBotToggleButton getInstance() {
        return INSTANCE;
    }

    public IRemoteBotToggleButton setWidget(SWTBotToggleButton toggleButton) {
        this.widget = toggleButton;
        return this;
    }

    public void click() throws RemoteException {
        widget.click();
    }

    public void press() throws RemoteException {
        widget.press();
    }

    public void clickAndWait() throws RemoteException {
        waitUntilIsEnabled();
        click();
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    public boolean isPressed() throws RemoteException {
        return widget.isPressed();
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

    public void waitUntilIsEnabled() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(
            Conditions.widgetIsEnabled(widget));
    }

}
