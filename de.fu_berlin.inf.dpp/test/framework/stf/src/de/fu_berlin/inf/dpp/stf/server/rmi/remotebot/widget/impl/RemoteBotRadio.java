package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotRadio;

public final class RemoteBotRadio extends StfRemoteObject implements
    IRemoteBotRadio {

    private static final RemoteBotRadio INSTANCE = new RemoteBotRadio();

    private SWTBotRadio swtBotRadio;

    public static RemoteBotRadio getInstance() {
        return INSTANCE;
    }

    public IRemoteBotRadio setWidget(SWTBotRadio radio) {
        this.swtBotRadio = radio;
        return this;

    }

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return RemoteBotMenu.getInstance().setWidget(
            swtBotRadio.contextMenu(text));

    }

    public void click() throws RemoteException {
        swtBotRadio.click();
    }

    public void clickAndWait() throws RemoteException {
        waitUntilIsEnabled();
        click();
    }

    public void setFocus() throws RemoteException {
        swtBotRadio.setFocus();
    }

    public boolean isEnabled() throws RemoteException {
        return swtBotRadio.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return swtBotRadio.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return swtBotRadio.isActive();
    }

    public String getText() throws RemoteException {
        return swtBotRadio.getText();
    }

    public String getToolTipText() throws RemoteException {
        return swtBotRadio.getText();
    }

    public boolean isSelected() throws RemoteException {
        return swtBotRadio.isSelected();
    }

    public void waitUntilIsEnabled() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(
            Conditions.widgetIsEnabled(swtBotRadio));
    }
}
