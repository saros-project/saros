package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotList;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;

public final class RemoteBotList extends StfRemoteObject implements
    IRemoteBotList {

    private static final RemoteBotList INSTANCE = new RemoteBotList();

    private SWTBotList widget;

    public static RemoteBotList getInstance() {
        return INSTANCE;
    }

    public IRemoteBotList setWidget(SWTBotList list) {
        this.widget = list;
        return this;
    }

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
    }

    public void select(String item) throws RemoteException {
        widget.select(item);
    }

    public void select(int... indices) throws RemoteException {
        widget.select(indices);
    }

    public void select(int index) throws RemoteException {
        widget.select(index);
    }

    public String[] selection() throws RemoteException {
        return widget.selection();
    }

    public void select(String... items) throws RemoteException {
        widget.select(items);
    }

    public int selectionCount() throws RemoteException {
        return widget.selectionCount();
    }

    public void unselect() throws RemoteException {
        widget.unselect();
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    public String itemAt(int index) throws RemoteException {
        return widget.itemAt(index);
    }

    public int itemCount() throws RemoteException {
        return widget.itemCount();
    }

    public int indexOf(String item) throws RemoteException {
        return widget.indexOf(item);
    }

    public String[] getItems() throws RemoteException {
        return widget.getItems();
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

}
