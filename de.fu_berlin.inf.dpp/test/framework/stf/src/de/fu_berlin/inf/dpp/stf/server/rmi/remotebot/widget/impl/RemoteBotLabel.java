package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;

public final class RemoteBotLabel extends StfRemoteObject implements
    IRemoteBotLabel {

    private static final RemoteBotLabel INSTANCE = new RemoteBotLabel();

    private SWTBotLabel widget;

    public static RemoteBotLabel getInstance() {
        return INSTANCE;
    }

    public IRemoteBotLabel setWidget(SWTBotLabel label) {
        this.widget = label;
        return this;
    }

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    public int alignment() throws RemoteException {
        return widget.alignment();
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

    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

    public String getText() throws RemoteException {
        return widget.getText();
    }

}
