package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.MenuPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosWorkbenchComponentImp;

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

    public final static String MENU_TITLE_OTHER = "Other...";
    public final static String MENU_TITLE_SHOW_VIEW = "Show View";
    public final static String MENU_TITLE_WINDOW = "Window";

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
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public String getTextFileLineDelimiter() throws RemoteException {
        clickMenuPreferences();
        SWTBotTree tree = bot.tree();
        tree.expandNode("General").select("Workspace");
        if (bot.radioInGroup("Default", "New text file line delimiter")
            .isSelected()) {
            shellC.closeShell(SHELL_PREFERNCES);
            return "Default";
        } else if (bot.radioInGroup("Other:", "New text file line delimiter")
            .isSelected()) {
            SWTBotCombo combo = bot
                .comboBoxInGroup("New text file line delimiter");
            String itemName = combo.items()[combo.selectionIndex()];
            shellC.closeShell(SHELL_PREFERNCES);
            return itemName;
        }
        shellC.closeShell(SHELL_PREFERNCES);
        return "";
    }

    /**********************************************
     * 
     * show view with main menu
     * 
     **********************************************/
    public void showViewProblems() throws RemoteException {
        openViewWithName("General", "Problems");
    }

    public void showViewProjectExplorer() throws RemoteException {
        openViewWithName("General", "Project Explorer");
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

    public void clickMenuWithTexts(String... texts) throws RemoteException {
        precondition();
        SWTBotMenu selectedmenu = null;
        for (String text : texts) {
            try {
                if (selectedmenu == null) {
                    selectedmenu = bot.menu(text);
                } else {
                    selectedmenu = selectedmenu.menu(text);
                }
            } catch (WidgetNotFoundException e) {
                log.error("menu \"" + text + "\" not found!");
                throw e;
            }
        }
        if (selectedmenu != null)
            selectedmenu.click();
    }

    public void clickMenuPreferences() throws RemoteException {
        clickMenuWithTexts(MENU_WINDOW, MENU_PREFERENCES);
    }

    /**
     * Open a view using menus Window->Show View->Other... The method is defined
     * as helper method and should not be exported by rmi. <br/>
     * Operational steps:
     * <ol>
     * <li>If the view is already open, return.</li>
     * <li>Activate the saros-instance workbench(alice / bob / carl). If the
     * workbench isn't active, bot can't find the main menus.</li>
     * <li>Click main menus Window -> Show View -> Other....</li>
     * <li>Confirm the pop-up window "Show View".</li>
     * </ol>
     * 
     * @param title
     *            the title on the view tab.
     * @param category
     *            example: "General"
     * @param nodeName
     *            example: "Console"
     * @see SarosWorkbenchComponentImp#activateEclipseShell()
     * @see MenuPart#clickMenuWithTexts(String...)
     * 
     */
    public void openViewWithName(String category, String nodeName)
        throws RemoteException {
        workbenchC.activateEclipseShell();
        clickMenuWithTexts(MENU_TITLE_WINDOW, MENU_TITLE_SHOW_VIEW,
            MENU_TITLE_OTHER);
        shellC.confirmShellWithTreeWithFilterText(MENU_TITLE_SHOW_VIEW,
            category, nodeName, OK);

    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    protected void precondition() throws RemoteException {
        workbenchC.activateEclipseShell();
    }

}
