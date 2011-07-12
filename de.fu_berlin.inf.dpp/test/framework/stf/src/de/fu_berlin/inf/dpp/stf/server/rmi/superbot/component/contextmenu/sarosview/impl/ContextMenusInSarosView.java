package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInSarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.ISarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

public abstract class ContextMenusInSarosView extends StfRemoteObject implements
    IContextMenusInSarosView {

    protected SWTBotTreeItem treeItem;
    protected SWTBotTree tree;

    protected ISarosView sarosView;

    public void setTreeItem(SWTBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    public void setTree(SWTBotTree tree) {
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
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_STOP_SAROS_SESSION);
        SuperBot.getInstance().confirmShellLeavingClosingSession();
    }
}
