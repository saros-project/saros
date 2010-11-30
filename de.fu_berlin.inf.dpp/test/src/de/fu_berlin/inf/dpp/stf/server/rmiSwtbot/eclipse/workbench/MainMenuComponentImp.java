package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class MainMenuComponentImp extends EclipseComponent implements
    MainMenuComponent {

    /* IDs of all the perspectives */
    public final static String ID_JAVA_PERSPECTIVE = "org.eclipse.jdt.ui.JavaPerspective";
    public final static String ID_DEBUG_PERSPECTIVE = "org.eclipse.debug.ui.DebugPerspective";

    /* name of all the main menus */
    private static final String MENU_WINDOW = "Window";
    private static final String MENU_PREFERENCES = "Preferences";
    private static final String MENU_FILE = "File";
    private static final String MENU_IMPORT = "Import...";

    /* title of shells which are pop up by clicking the main menus */
    private static final String SHELL_PREFERNCES = "Preferences";

    /***********************************************************************
     * 
     * exported functions
     * 
     ***********************************************************************/

    /**********************************************
     * 
     * all related actions with preferences
     * 
     **********************************************/

    public void newTextFileLineDelimiter(String OS) throws RemoteException {
        clickMenuPreferences();
        SWTBotTree tree = bot.tree();
        tree.expandNode("General").select("Workspace");

        if (OS.equals("Default")) {
            bot.radioInGroup("Default", "New text file line delimiter").click();
        } else {
            bot.radioInGroup("Other:", "New text file line delimiter").click();
            bot.comboBoxInGroup("New text file line delimiter")
                .setSelection(OS);
        }
        bot.button("Apply").click();
        bot.button(OK).click();
        windowPart.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public String getTextFileLineDelimiter() throws RemoteException {
        clickMenuPreferences();
        SWTBotTree tree = bot.tree();
        tree.expandNode("General").select("Workspace");
        if (bot.radioInGroup("Default", "New text file line delimiter")
            .isSelected()) {
            windowPart.closeShell(SHELL_PREFERNCES);
            return "Default";
        } else if (bot.radioInGroup("Other:", "New text file line delimiter")
            .isSelected()) {
            SWTBotCombo combo = bot
                .comboBoxInGroup("New text file line delimiter");
            String itemName = combo.items()[combo.selectionIndex()];
            windowPart.closeShell(SHELL_PREFERNCES);
            return itemName;
        }
        windowPart.closeShell(SHELL_PREFERNCES);
        return "";
    }

    /**********************************************
     * 
     * show view with main menu
     * 
     **********************************************/
    public void showViewProblems() throws RemoteException {
        menuPart.openViewWithName("General", "Problems");
    }

    public void showViewProjectExplorer() throws RemoteException {
        menuPart.openViewWithName("General", "Project Explorer");
    }

    /**********************************************
     * 
     * all related actions with perspective
     * 
     **********************************************/
    public void openPerspectiveJava() throws RemoteException {
        perspectivePart.openPerspectiveWithId(ID_JAVA_PERSPECTIVE);
    }

    public boolean isJavaPerspectiveActive() throws RemoteException {
        return perspectivePart.isPerspectiveActive(ID_JAVA_PERSPECTIVE);
    }

    public void openPerspectiveDebug() throws RemoteException {
        perspectivePart.openPerspectiveWithId(ID_DEBUG_PERSPECTIVE);
    }

    public boolean isDebugPerspectiveActive() throws RemoteException {
        return perspectivePart.isPerspectiveActive(ID_DEBUG_PERSPECTIVE);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

  
    protected void precondition() throws RemoteException {
        workbenchC.activateEclipseShell();
    }

    private void clickMenuPreferences() throws RemoteException {
        precondition();
        menuPart.clickMenuWithTexts(MENU_WINDOW, MENU_PREFERENCES);
    }

}
