package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.ISarosView;

public class SarosContextMenuWrapper extends Component implements
    ISarosContextMenuWrapper {

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

    public void leaveSarosSession() throws RemoteException {
        if (!sarosView.isHost()) {
            treeItem.contextMenus(CM_STOP_SAROS_SESSION).click();
            bot().waitUntilShellIsOpen(SHELL_CONFIRM_LEAVING_SESSION);
            bot().shell(SHELL_CONFIRM_LEAVING_SESSION).activate();
            bot().shell(SHELL_CONFIRM_LEAVING_SESSION).confirm(YES);
        } else {
            treeItem.contextMenus(CM_STOP_SAROS_SESSION).click();
            bot().waitUntilShellIsOpen(SHELL_CONFIRM_CLOSING_SESSION);
            bot().shell(SHELL_CONFIRM_CLOSING_SESSION).activate();
            bot().shell(SHELL_CONFIRM_CLOSING_SESSION).confirm(YES);
        }
        sarosView.waitUntilIsNotInSession();

    }
}
