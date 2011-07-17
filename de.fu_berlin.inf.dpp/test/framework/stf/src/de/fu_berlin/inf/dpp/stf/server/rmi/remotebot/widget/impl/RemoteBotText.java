package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotText;

public final class RemoteBotText extends StfRemoteObject implements
    IRemoteBotText {

    private static final RemoteBotText INSTANCE = new RemoteBotText();

    private SWTBotText widget;

    public static RemoteBotText getInstance() {
        return INSTANCE;
    }

    public IRemoteBotText setWidget(SWTBotText text) {
        this.widget = text;
        return this;
    }

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));

    }

    public IRemoteBotText selectAll() throws RemoteException {
        return setWidget(widget.selectAll());
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    public IRemoteBotText setText(String text) throws RemoteException {
        return setWidget(widget.setText(text));
    }

    public IRemoteBotText typeText(String text) throws RemoteException {
        return setWidget(widget.typeText(text));
    }

    public String getText() throws RemoteException {
        return widget.getText();
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

}
