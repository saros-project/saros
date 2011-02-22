package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.Perspective;

public class WindowMImp extends SarosPreferencesImp implements WindowM {

    private static transient WindowMImp windowImp;

    /**
     * {@link FileMImp} is a singleton, but inheritance is possible.
     */
    public static WindowMImp getInstance() {
        if (windowImp != null)
            return windowImp;
        windowImp = new WindowMImp();
        return windowImp;
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
        SWTBotTree tree = bot.tree();
        tree.expandNode(TREE_ITEM_GENERAL_IN_PRFERENCES).select(
            TREE_ITEM_WORKSPACE_IN_PREFERENCES);

        if (OS.equals("Default")) {
            bot.radioInGroup("Default", "New text file line delimiter").click();
        } else {
            bot.radioInGroup("Other:", "New text file line delimiter").click();
            bot.comboBoxInGroup("New text file line delimiter")
                .setSelection(OS);
        }
        bot.button(APPLY).click();
        bot.button(OK).click();
        bot().waitsUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void clickMenuPreferences() throws RemoteException {
        if (getOS() == TypeOfOS.MAC)
            bot().menu("Eclipse").menu(MENU_PREFERENCES).click();
        else
            bot().menu(MENU_WINDOW).menu(MENU_PREFERENCES).click();
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
        bot().activateWorkbench();
        bot().menu(MENU_WINDOW).menu(MENU_SHOW_VIEW).menu(MENU_OTHER).click();
        bot().shell(SHELL_SHOW_VIEW).confirmShellWithTreeWithFilterText(
            parentNode, node, OK);
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
        bot().openPerspectiveWithId(ID_RESOURCE_PERSPECTIVE);
    }

    public void openPerspectiveJava() throws RemoteException {
        bot().openPerspectiveWithId(ID_JAVA_PERSPECTIVE);
    }

    public void openPerspectiveDebug() throws RemoteException {
        bot().openPerspectiveWithId(ID_DEBUG_PERSPECTIVE);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public String getTextFileLineDelimiter() throws RemoteException {
        clickMenuPreferences();
        SWTBotTree tree = bot.tree();
        tree.expandNode(TREE_ITEM_GENERAL_IN_PRFERENCES).select(
            TREE_ITEM_WORKSPACE_IN_PREFERENCES);
        if (bot.radioInGroup("Default", "New text file line delimiter")
            .isSelected()) {
            bot().shell(SHELL_PREFERNCES).close();
            return "Default";
        } else if (bot.radioInGroup("Other:", "New text file line delimiter")
            .isSelected()) {
            SWTBotCombo combo = bot
                .comboBoxInGroup("New text file line delimiter");
            String itemName = combo.items()[combo.selectionIndex()];
            bot().shell(SHELL_PREFERNCES).close();
            return itemName;
        }
        bot().shell(SHELL_PREFERNCES).close();
        return "";
    }

    public boolean isJavaPerspectiveActive() throws RemoteException {
        return bot().isPerspectiveActive(ID_JAVA_PERSPECTIVE);
    }

    public boolean isDebugPerspectiveActive() throws RemoteException {
        return bot().isPerspectiveActive(ID_DEBUG_PERSPECTIVE);
    }

    /**********************************************
     * 
     * inner functions
     * 
     **********************************************/
    private void precondition() throws RemoteException {
        bot().activateWorkbench();
    }

}
