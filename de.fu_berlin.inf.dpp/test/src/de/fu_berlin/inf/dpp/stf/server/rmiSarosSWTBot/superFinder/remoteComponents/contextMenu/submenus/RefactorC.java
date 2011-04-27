package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.submenus;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class RefactorC extends Component implements IRefactorC {

    private static transient RefactorC refactorImp;

    private IRemoteBotTreeItem treeItem;
    private TreeItemType type;

    /**
     * {@link NewC} is a singleton, but inheritance is possible.
     */
    public static RefactorC getInstance() {
        if (refactorImp != null)
            return refactorImp;
        refactorImp = new RefactorC();
        return refactorImp;
    }

    public void setTreeItem(IRemoteBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    public void setTreeItemType(TreeItemType type) {
        this.type = type;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void moveClassTo(String targetProject, String targetPkg)
        throws RemoteException {
        moveTo(SHELL_MOVE, OK, getPkgNodes(targetProject, targetPkg));
    }

    public void rename(String newName) throws RemoteException {
        switch (type) {
        case JAVA_PROJECT:
            rename(SHELL_RENAME_JAVA_PROJECT, OK, newName);
            break;
        case PKG:
            rename(SHELL_RENAME_PACKAGE, OK, newName);
            break;
        case CLASS:
            rename(SHELL_RENAME_COMPiIATION_UNIT, FINISH, newName);
            break;
        default:
            rename(SHELL_RENAME_RESOURCE, OK, newName);
            break;
        }
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/
    private void rename(String shellTitle, String buttonName, String newName)
        throws RemoteException {
        treeItem.contextMenus(MENU_REFACTOR, MENU_RENAME).click();
        IRemoteBotShell shell = bot().shell(shellTitle);
        shell.activate();
        shell.bot().textWithLabel(LABEL_NEW_NAME).setText(newName);
        bot().shell(shellTitle).bot().button(buttonName).waitUntilIsEnabled();
        shell.bot().button(buttonName).click();
        if (bot().isShellOpen("Rename Compilation Unit")) {
            bot().shell("Rename Compilation Unit").bot().button(buttonName)
                .waitUntilIsEnabled();
            bot().shell("Rename Compilation Unit").bot().button(buttonName)
                .click();
        }
        if (bot().isShellOpen(shellTitle))
            bot().waitUntilShellIsClosed(shellTitle);
    }

    private void moveTo(String shellTitle, String buttonName, String... nodes)
        throws RemoteException {
        bot().menu(MENU_REFACTOR).menu(MENU_MOVE).click();
        bot().shell(shellTitle).confirmWithTree(buttonName, nodes);
        bot().waitUntilShellIsClosed(shellTitle);
    }

}
