package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView;

import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.submenus.INewC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.submenus.IRefactorC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.submenus.IShareWithC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.submenus.ITeamC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.submenus.NewC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.submenus.RefactorC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.submenus.ShareWithC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.submenus.TeamC;

public class ContextMenusInPEView extends Component implements
    IContextMenusInPEView {
    private static transient ContextMenusInPEView self;

    protected static TeamC teamC;
    protected static NewC newC;
    protected static RefactorC reafactorC;
    private static ShareWithC shareWithC;

    private IRemoteBotTreeItem treeItem;
    private IRemoteBotTree tree;
    private TreeItemType type;

    /**
     * {@link ContextMenusInPEView} is a singleton, but inheritance is possible.
     */
    public static ContextMenusInPEView getInstance() {
        if (self != null)
            return self;
        self = new ContextMenusInPEView();
        teamC = TeamC.getInstance();
        reafactorC = RefactorC.getInstance();
        newC = NewC.getInstance();
        shareWithC = ShareWithC.getInstance();
        return self;
    }

    public void setTreeItem(IRemoteBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    public void setTreeItemType(TreeItemType type) {
        this.type = type;
    }

    public void setTree(IRemoteBotTree tree) {
        this.tree = tree;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    public IShareWithC shareWith() throws RemoteException {
        shareWithC.setTreeItem(treeItem);
        return shareWithC;
    }

    public INewC newC() throws RemoteException {
        newC.setTree(tree);
        newC.setTreeItem(treeItem);
        return newC;
    }

    public ITeamC team() throws RemoteException {
        teamC.setTreeItem(treeItem);
        return teamC;
    }

    public IRefactorC refactor() throws RemoteException {
        reafactorC.setTreeItem(treeItem);
        reafactorC.setTreeItemType(type);
        return reafactorC;
    }

    public void open() throws RemoteException {
        treeItem.contextMenus(CM_OPEN).click();
    }

    public void copy() throws RemoteException {
        treeItem.contextMenus(MENU_COPY).click();
    }

    public void paste(String target) throws RemoteException {
        if (treeItem == null) {
            tree.contextMenu(MENU_PASTE).click();
            IRemoteBotShell shell = remoteBot().shell(SHELL_COPY_PROJECT);
            shell.activate();
            shell.bot().textWithLabel("Project name:").setText(target);
            shell.bot().button(OK).click();
            remoteBot().waitUntilShellIsClosed(SHELL_COPY_PROJECT);
            remoteBot().sleep(1000);
        }
        // switch (type) {
        // case PROJECT:

        // break;
        // default:
        // break;
        // }
    }

    public void openWith(String editorType) throws RemoteException {
        treeItem.contextMenus(CM_OPEN_WITH, CM_OTHER).click();
        remoteBot().waitUntilShellIsOpen(SHELL_EDITOR_SELECTION);
        IRemoteBotShell shell_bob = remoteBot().shell(SHELL_EDITOR_SELECTION);
        shell_bob.activate();
        shell_bob.bot().table().getTableItem(editorType).select();
        shell_bob.bot().button(OK).waitUntilIsEnabled();
        shell_bob.confirm(OK);
    }

    public void delete() throws RemoteException {
        treeItem.contextMenus(CM_DELETE).click();
        switch (type) {
        case PROJECT:
            remoteBot().shell(SHELL_DELETE_RESOURCE).confirmWithCheckBox(OK, true);
            remoteBot().waitUntilShellIsClosed(SHELL_DELETE_RESOURCE);
            break;
        case JAVA_PROJECT:
            remoteBot().shell(SHELL_DELETE_RESOURCE).confirmWithCheckBox(OK, true);
            remoteBot().waitUntilShellIsClosed(SHELL_DELETE_RESOURCE);
            break;
        default:
            remoteBot().waitUntilShellIsOpen(CONFIRM_DELETE);
            remoteBot().shell(CONFIRM_DELETE).activate();
            remoteBot().shell(CONFIRM_DELETE).bot().button(OK).click();
            remoteBot().sleep(300);
            break;
        }
        tree.waitUntilItemNotExists(treeItem.getText());
    }

    public boolean existsWithRegex(String name) throws RemoteException {
        if (treeItem == null) {
            for (String item : tree.getTextOfItems()) {
                if (item.matches(name + ".*"))
                    return true;
            }
            return false;
        } else {
            for (String item : treeItem.getTextOfItems()) {
                if (item.matches(name + ".*"))
                    return true;
            }
            return false;
        }
    }

    public boolean exists(String name) throws RemoteException {
        if (treeItem == null) {
            return tree.getTextOfItems().contains(name);
        } else {
            return treeItem.getTextOfItems().contains(name);
        }
    }

    public List<String> getTextOfTreeItems() throws RemoteException {
        if (treeItem == null) {
            return tree.getTextOfItems();
        } else {
            return treeItem.getTextOfItems();
        }
    }

}
