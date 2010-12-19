package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
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
    public final static String MENU_OTHER = "Other...";
    public final static String MENU_SHOW_VIEW = "Show View";

    /* title of shells which are pop up by clicking the main menus */
    private static final String SHELL_PREFERNCES = "Preferences";

    /* treeItems in Preferences dialog */
    private static final String GENERAL = "General";
    private static final String WORKSPACE = "Workspace";

    /* treeItems in Show View dialog */
    private static final String V_GENERAL = "General";
    private static final String V_PROBLEM = "Problems";
    private static final String V_PROJECT_EXPLORER = "Project Explorer";

    /***********************************************************************
     * 
     * exported functions
     * 
     ***********************************************************************/

    /**********************************************
     * 
     * TreeItem: General->Workspaces in preferences dialog
     * 
     **********************************************/

    public void newTextFileLineDelimiter(String OS) throws RemoteException {
        clickMenuPreferences();
        SWTBotTree tree = bot.tree();
        tree.expandNode(GENERAL).select(WORKSPACE);

        if (OS.equals("Default")) {
            bot.radioInGroup("Default", "New text file line delimiter").click();
        } else {
            bot.radioInGroup("Other:", "New text file line delimiter").click();
            bot.comboBoxInGroup("New text file line delimiter")
                .setSelection(OS);
        }
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public String getTextFileLineDelimiter() throws RemoteException {
        clickMenuPreferences();
        SWTBotTree tree = bot.tree();
        tree.expandNode(GENERAL).select(WORKSPACE);
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
        openViewWithName(V_GENERAL, V_PROBLEM);
    }

    public void showViewProjectExplorer() throws RemoteException {
        openViewWithName(V_GENERAL, V_PROJECT_EXPLORER);
    }

    /**********************************************
     * 
     * all related actions with perspective
     * 
     **********************************************/
    public void openPerspectiveJava() throws RemoteException {
        openPerspectiveWithId(ID_JAVA_PERSPECTIVE);
    }

    public boolean isJavaPerspectiveActive() throws RemoteException {
        return isPerspectiveActive(ID_JAVA_PERSPECTIVE);
    }

    public void openPerspectiveDebug() throws RemoteException {
        openPerspectiveWithId(ID_DEBUG_PERSPECTIVE);
    }

    public boolean isDebugPerspectiveActive() throws RemoteException {
        return isPerspectiveActive(ID_DEBUG_PERSPECTIVE);
    }

    /**
     * Open a perspective using Window->Open Perspective->Other... The method is
     * defined as helper method for other openPerspective* methods and should
     * not be exported using rmi.
     * 
     * 1. if the perspective already exist, return.
     * 
     * 2. activate the saros-instance-window(alice / bob / carl). If the
     * workbench isn't active, delegate can't find the main menus.
     * 
     * 3. click main menus Window -> Open perspective -> Other....
     * 
     * 4. confirm the pop-up window "Open Perspective".
     * 
     * @param persID
     *            example: "org.eclipse.jdt.ui.JavaPerspective"
     */
    public void openPerspectiveWithId(final String persID)
        throws RemoteException {
        if (!isPerspectiveActive(persID)) {
            workbenchC.activateEclipseShell();
            try {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        final IWorkbench wb = PlatformUI.getWorkbench();
                        IPerspectiveDescriptor[] descriptors = wb
                            .getPerspectiveRegistry().getPerspectives();
                        for (IPerspectiveDescriptor per : descriptors) {
                            log.debug("installed perspective id:" + per.getId());
                        }
                        final IWorkbenchWindow win = wb
                            .getActiveWorkbenchWindow();
                        try {
                            wb.showPerspective(persID, win);
                        } catch (WorkbenchException e) {
                            log.debug("couldn't open perspective wit ID"
                                + persID, e);
                        }
                    }
                });
            } catch (IllegalArgumentException e) {
                log.debug("Couldn't initialize perspective with ID" + persID,
                    e.getCause());
            }

        }

    }

    public boolean isPerspectiveActive(String id) {
        return bot.perspectiveById(id).isActive();
    }

    public boolean isPerspectiveOpen(String title) {
        return getPerspectiveTitles().contains(title);
    }

    protected List<String> getPerspectiveTitles() {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotPerspective perspective : bot.perspectives())
            list.add(perspective.getLabel());
        return list;
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
        if (getOS() == TypeOfOS.MAC)
            clickMenuWithTexts("Eclipse", "Preferences...");
        else
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
        clickMenuWithTexts(MENU_WINDOW, MENU_SHOW_VIEW, MENU_OTHER);
        shellC.confirmShellWithTreeWithFilterText(MENU_SHOW_VIEW, category,
            nodeName, OK);

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
