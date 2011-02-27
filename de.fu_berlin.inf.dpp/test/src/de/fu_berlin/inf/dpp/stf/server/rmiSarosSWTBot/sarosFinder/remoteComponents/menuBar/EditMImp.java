package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class EditMImp extends EclipseComponentImp implements EditM {

    private static transient EditMImp self;

    /**
     * {@link EditMImp} is a singleton, but inheritance is possible.
     */
    public static EditMImp getInstance() {
        if (self != null)
            return self;
        self = new EditMImp();
        return self;
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

    public void deleteAllProjects(String viewTitle) throws RemoteException {
        STFBotTree tree = bot().view(viewTitle).bot().tree();
        List<String> allTreeItems = tree.getTextOfItems();

        if (allTreeItems != null) {
            for (String item : allTreeItems) {
                tree.selectTreeItem(item).contextMenu(MENU_DELETE).click();
                STFBotShell shell = bot().shell(SHELL_DELETE_RESOURCE);

                shell.confirmWithCheckBox(OK, true);
                bot().waitsUntilShellIsClosed(SHELL_DELETE_RESOURCE);
            }
        }
    }

    public void deleteAllItemsOfJavaProject(String viewTitle, String projectName)
        throws RemoteException {
        STFBotTreeItem treeItem = bot().view(viewTitle).bot().tree()
            .selectTreeItem(projectName, SRC);
        for (String item : treeItem.getTextOfItems()) {
            bot().view(viewTitle).bot().tree()
                .selectTreeItem(projectName, SRC, item).contextMenu(CM_DELETE)
                .click();

            bot().waitUntilShellIsOpen(CONFIRM_DELETE);
            bot().shell(CONFIRM_DELETE).activate();
            bot().shell(CONFIRM_DELETE).bot().button(OK).click();
        }
    }

}
