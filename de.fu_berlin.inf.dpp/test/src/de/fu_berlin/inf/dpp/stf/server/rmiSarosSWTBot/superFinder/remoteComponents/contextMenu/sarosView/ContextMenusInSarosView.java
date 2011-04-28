package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.sarosView;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.ISarosView;

public class ContextMenusInSarosView extends Component implements
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
        superBot().confirmShellLeavingClosingSession();
    }
}
