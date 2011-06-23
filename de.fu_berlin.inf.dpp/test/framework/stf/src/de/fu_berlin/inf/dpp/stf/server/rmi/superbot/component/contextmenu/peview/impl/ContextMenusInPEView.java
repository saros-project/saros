package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.impl;

import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.IContextMenusInPEView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.INewC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IRefactorC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IShareWithC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.ITeamC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.NewC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.RefactorC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.ShareWithC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl.TeamC;

public final class ContextMenusInPEView extends StfRemoteObject implements
    IContextMenusInPEView {

    private static final ContextMenusInPEView INSTANCE = new ContextMenusInPEView();

    private IRemoteBotTreeItem treeItem;
    private IRemoteBotTree tree;
    private TreeItemType type;

    public static ContextMenusInPEView getInstance() {
        return INSTANCE;
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
        ShareWithC.getInstance().setTreeItem(treeItem);
        return ShareWithC.getInstance();
    }

    public INewC newC() throws RemoteException {
        NewC.getInstance().setTree(tree);
        NewC.getInstance().setTreeItem(treeItem);
        return NewC.getInstance();
    }

    public ITeamC team() throws RemoteException {
        TeamC.getInstance().setTreeItem(treeItem);
        return TeamC.getInstance();
    }

    public IRefactorC refactor() throws RemoteException {
        RefactorC.getInstance().setTreeItem(treeItem);
        RefactorC.getInstance().setTreeItemType(type);
        return RefactorC.getInstance();
    }

    public void open() throws RemoteException {
        treeItem.contextMenus(CM_OPEN).click();
    }

    public void copy() throws RemoteException {
        treeItem.contextMenus(MENU_COPY).click();
    }

    public void refresh() throws RemoteException {
        treeItem.contextMenus(MENU_REFRESH).click();
    }

    public void paste(String target) throws RemoteException {
        if (treeItem == null) {
            tree.contextMenu(MENU_PASTE).click();
            IRemoteBotShell shell = RemoteWorkbenchBot.getInstance().shell(
                SHELL_COPY_PROJECT);
            shell.activate();
            shell.bot().textWithLabel("Project name:").setText(target);
            shell.bot().button(OK).click();
            RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(
                SHELL_COPY_PROJECT);
            RemoteWorkbenchBot.getInstance().sleep(1000);
        }
    }

    public void openWith(String editorType) throws RemoteException {
        treeItem.contextMenus(CM_OPEN_WITH, CM_OTHER).click();
        RemoteWorkbenchBot.getInstance().waitUntilShellIsOpen(
            SHELL_EDITOR_SELECTION);
        IRemoteBotShell shell_bob = RemoteWorkbenchBot.getInstance().shell(
            SHELL_EDITOR_SELECTION);
        shell_bob.activate();
        shell_bob.bot().table().getTableItem(editorType).select();
        shell_bob.bot().button(OK).waitUntilIsEnabled();
        shell_bob.confirm(OK);
    }

    public void delete() throws RemoteException {
        treeItem.contextMenus(CM_DELETE).click();
        switch (type) {
        case PROJECT:
            RemoteWorkbenchBot.getInstance().shell(SHELL_DELETE_RESOURCE)
                .confirmWithCheckBox(OK, true);
            RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(
                SHELL_DELETE_RESOURCE);
            break;
        case JAVA_PROJECT:
            RemoteWorkbenchBot.getInstance().shell(SHELL_DELETE_RESOURCE)
                .confirmWithCheckBox(OK, true);
            RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(
                SHELL_DELETE_RESOURCE);
            break;
        default:
            RemoteWorkbenchBot.getInstance().waitUntilShellIsOpen(
                CONFIRM_DELETE);
            RemoteWorkbenchBot.getInstance().shell(CONFIRM_DELETE).activate();
            RemoteWorkbenchBot.getInstance().shell(CONFIRM_DELETE).bot()
                .button(OK).click();
            RemoteWorkbenchBot.getInstance().sleep(300);
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
