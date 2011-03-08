package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotTableItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class ContextMenuWrapper extends Component implements
    IContextMenuWrapper {
    private static transient ContextMenuWrapper self;

    protected static TeamC teamC;
    protected static NewC newC;
    protected static RefactorC reafactorC;
    private static ShareWithC shareWithC;

    private IRemoteBotTreeItem treeItem;
    private IRemoteBotTree tree;
    private TreeItemType type;
    private IRemoteBotTableItem tableItem;

    /**
     * {@link ContextMenuWrapper} is a singleton, but inheritance is
     * possible.
     */
    public static ContextMenuWrapper getInstance() {
        if (self != null)
            return self;
        self = new ContextMenuWrapper();
        teamC = TeamC.getInstance();
        reafactorC = RefactorC.getInstance();
        newC = NewC.getInstance();
        shareWithC = ShareWithC.getInstance();
        return self;
    }

    public void setTreeItem(IRemoteBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    public void setTableItem(IRemoteBotTableItem tableItem) {
        this.tableItem = tableItem;
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
        treeItem.contextMenu(CM_OPEN).click();
    }

    public void copy() throws RemoteException {
        treeItem.contextMenu(MENU_COPY).click();
    }

    public void paste(String target) throws RemoteException {
        if (treeItem == null) {
            tree.contextMenu(MENU_PASTE).click();
            IRemoteBotShell shell = bot().shell(SHELL_COPY_PROJECT);
            shell.activate();
            shell.bot().textWithLabel("Project name:").setText(target);
            shell.bot().button(OK).click();
            bot().waitUntilShellIsClosed(SHELL_COPY_PROJECT);
            bot().sleep(1000);
        }
        // switch (type) {
        // case PROJECT:

        // break;
        // default:
        // break;
        // }
    }

    public void openWith(String editorType) throws RemoteException {
        treeItem.contextMenu(CM_OPEN_WITH, CM_OTHER).click();
        bot().waitUntilShellIsOpen(SHELL_EDITOR_SELECTION);
        IRemoteBotShell shell_bob = bot().shell(SHELL_EDITOR_SELECTION);
        shell_bob.activate();
        shell_bob.bot().table().getTableItem(editorType).select();
        shell_bob.bot().button(OK).waitUntilIsEnabled();
        shell_bob.confirm(OK);
    }

    public void delete() throws RemoteException {
        treeItem.contextMenu(CM_DELETE).click();
        switch (type) {
        case PROJECT:
            bot().shell(SHELL_DELETE_RESOURCE).confirmWithCheckBox(OK, true);
            bot().waitUntilShellIsClosed(SHELL_DELETE_RESOURCE);
            break;
        case JAVA_PROJECT:
            bot().shell(SHELL_DELETE_RESOURCE).confirmWithCheckBox(OK, true);
            bot().waitUntilShellIsClosed(SHELL_DELETE_RESOURCE);
            break;
        default:
            bot().waitUntilShellIsOpen(CONFIRM_DELETE);
            bot().shell(CONFIRM_DELETE).activate();
            bot().shell(CONFIRM_DELETE).bot().button(OK).click();
            bot().sleep(300);
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
