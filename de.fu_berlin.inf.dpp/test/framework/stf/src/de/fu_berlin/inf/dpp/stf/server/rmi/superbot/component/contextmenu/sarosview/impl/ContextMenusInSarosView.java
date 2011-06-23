package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInSarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.ISarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

public abstract class ContextMenusInSarosView extends StfRemoteObject implements
    IContextMenusInSarosView {

    protected IRemoteBotTreeItem treeItem;
    protected IRemoteBotTree tree;

    protected ISarosView sarosView;

    public void setTreeItem(IRemoteBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    public void setTree(IRemoteBotTree tree) {
        this.tree = tree;
    }

    public void setSarosView(ISarosView sarosView) {
        this.sarosView = sarosView;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    public void stopSarosSession() throws RemoteException {
        treeItem.contextMenus(CM_STOP_SAROS_SESSION).click();
        SuperBot.getInstance().confirmShellLeavingClosingSession();
    }
}
