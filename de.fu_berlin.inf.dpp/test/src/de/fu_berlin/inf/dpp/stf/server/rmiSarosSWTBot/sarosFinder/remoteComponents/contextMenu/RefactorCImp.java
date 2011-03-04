package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.Component;

public class RefactorCImp extends Component implements RefactorC {

    private static transient RefactorCImp refactorImp;

    private STFBotTreeItem treeItem;
    private TreeItemType type;

    /**
     * {@link NewCImp} is a singleton, but inheritance is possible.
     */
    public static RefactorCImp getInstance() {
        if (refactorImp != null)
            return refactorImp;
        refactorImp = new RefactorCImp();
        return refactorImp;
    }

    public void setTreeItem(STFBotTreeItem treeItem) {
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
            rename("Rename Java Project", OK, newName);
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
        treeItem.contextMenu(MENU_REFACTOR, MENU_RENAME).click();
        STFBotShell shell = bot().shell(shellTitle);
        shell.activate();
        shell.bot().textWithLabel(LABEL_NEW_NAME).setText(newName);
        bot().shell(shellTitle).bot().button(buttonName).waitUntilIsEnabled();
        shell.bot().button(buttonName).click();
        if (bot().isShellOpen("Rename Compilation Unit")) {
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
