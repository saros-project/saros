package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Perspective;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus.submenus.SarosPreferences;

public class WindowM extends SarosPreferences implements IWindowM {

    private static transient WindowM self;

    /**
     * {@link WindowM} is a singleton, but inheritance is possible.
     */
    public static WindowM getInstance() {
        if (self != null)
            return self;
        self = new WindowM();
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
    public void setNewTextFileLineDelimiter(String OS) throws RemoteException {
        clickMenuPreferences();
        IRemoteBotShell shell = remoteBot().shell(SHELL_PREFERNCES);
        IRemoteBotTree tree = shell.bot().tree();
        tree.expandNode(TREE_ITEM_GENERAL_IN_PRFERENCES).select(
            TREE_ITEM_WORKSPACE_IN_PREFERENCES);

        if (OS.equals("Default")) {
            shell.bot().radioInGroup("Default", "New text file line delimiter")
                .click();
        } else {
            shell.bot().radioInGroup("Other:", "New text file line delimiter")
                .click();
            shell.bot().comboBoxInGroup("New text file line delimiter")
                .setSelection(OS);
        }
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        remoteBot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void clickMenuPreferences() throws RemoteException {
        if (getOS() == TypeOfOS.MAC)
            remoteBot().menu("Eclipse").menu(MENU_PREFERENCES).click();
        else
            remoteBot().menu(MENU_WINDOW).menu(MENU_PREFERENCES).click();
    }

    public void showViewProblems() throws RemoteException {
        showViewWithName(TREE_ITEM_GENERAL_IN_SHELL_SHOW_VIEW,
            TREE_ITEM_PROBLEM_IN_SHELL_SHOW_VIEW);
    }

    public void showViewProjectExplorer() throws RemoteException {
        showViewWithName(TREE_ITEM_GENERAL_IN_SHELL_SHOW_VIEW,
            TREE_ITEM_PROJECT_EXPLORER_IN_SHELL_SHOW_VIEW);
    }

    public void showViewWithName(String parentNode, String node)
        throws RemoteException {
        remoteBot().activateWorkbench();
        remoteBot().menu(MENU_WINDOW).menu(MENU_SHOW_VIEW).menu(MENU_OTHER).click();
        remoteBot().shell(SHELL_SHOW_VIEW).confirmWithTreeWithFilterText(parentNode,
            node, OK);
    }

    public void openPerspective() throws RemoteException {
        switch (Perspective.WHICH_PERSPECTIVE) {
        case JAVA:
            openPerspectiveJava();
            break;
        case DEBUG:
            openPerspectiveDebug();
            break;
        case RESOURCE:
            openPerspectiveResource();
            break;
        default:
            openPerspectiveJava();
            break;
        }
    }

    public void openPerspectiveResource() throws RemoteException {
        remoteBot().openPerspectiveWithId(ID_RESOURCE_PERSPECTIVE);
    }

    public void openPerspectiveJava() throws RemoteException {
        remoteBot().openPerspectiveWithId(ID_JAVA_PERSPECTIVE);
    }

    public void openPerspectiveDebug() throws RemoteException {
        remoteBot().openPerspectiveWithId(ID_DEBUG_PERSPECTIVE);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public String getTextFileLineDelimiter() throws RemoteException {
        clickMenuPreferences();
        IRemoteBotShell shell = remoteBot().shell(SHELL_PREFERNCES);
        IRemoteBotTree tree = shell.bot().tree();
        tree.expandNode(TREE_ITEM_GENERAL_IN_PRFERENCES).select(
            TREE_ITEM_WORKSPACE_IN_PREFERENCES);
        if (shell.bot().radioInGroup("Default", "New text file line delimiter")
            .isSelected()) {
            shell.close();
            return "Default";
        } else if (shell.bot()
            .radioInGroup("Other:", "New text file line delimiter")
            .isSelected()) {
            IRemoteBotCombo combo = shell.bot().comboBoxInGroup(
                "New text file line delimiter");
            String itemName = combo.items()[combo.selectionIndex()];
            remoteBot().shell(SHELL_PREFERNCES).close();
            return itemName;
        }
        shell.close();
        return "";
    }

    public boolean isJavaPerspectiveActive() throws RemoteException {
        return remoteBot().isPerspectiveActive(ID_JAVA_PERSPECTIVE);
    }

    public boolean isDebugPerspectiveActive() throws RemoteException {
        return remoteBot().isPerspectiveActive(ID_DEBUG_PERSPECTIVE);
    }

}
