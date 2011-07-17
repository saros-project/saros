package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCCombo;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotCCombo;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;

public final class RemoteBotCCombo extends StfRemoteObject implements
    IRemoteBotCCombo {

    private static final RemoteBotCCombo INSTANCE = new RemoteBotCCombo();

    private SWTBotCCombo widget;

    public static RemoteBotCCombo getInstance() {
        return INSTANCE;
    }

    public IRemoteBotCCombo setWidget(SWTBotCCombo ccomb) {
        this.widget = ccomb;
        return this;
    }

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
    }

    public void setSelection(int indexOfSelection) throws RemoteException {
        widget.setSelection(indexOfSelection);
    }

    public String selection() throws RemoteException {
        return widget.selection();
    }

    public int selectionIndex() throws RemoteException {
        return widget.selectionIndex();
    }

    public void setSelection(String text) throws RemoteException {
        widget.setSelection(text);
    }

    public void setText(String text) throws RemoteException {
        widget.setText(text);
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

    public int itemCount() throws RemoteException {
        return widget.itemCount();
    }

    public String[] items() throws RemoteException {
        return widget.items();
    }

    public int textLimit() throws RemoteException {
        return widget.textLimit();
    }

}
