package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class MainMenuComponentImp extends EclipseComponent implements
    MainMenuComponent {

    public final static String ID_JAVA_PERSPECTIVE = "org.eclipse.jdt.ui.JavaPerspective";
    public final static String ID_DEBUG_PERSPECTIVE = "org.eclipse.debug.ui.DebugPerspective";

    public void preference() throws RemoteException {
        workbenchC.activateEclipseShell();
        menuPart.clickMenuWithTexts("Window", "Preferences");
    }

    public void newTextFileLineDelimiter(String OS) throws RemoteException {
        preference();
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
        bot.button("OK").click();
        windowPart.waitUntilShellClosed("Preferences");
    }

    public String getTextFileLineDelimiter() throws RemoteException {
        preference();
        SWTBotTree tree = bot.tree();
        tree.expandNode("General").select("Workspace");
        if (bot.radioInGroup("Default", "New text file line delimiter")
            .isSelected()) {
            windowPart.closeShell("Preferences");
            return "Default";
        } else if (bot.radioInGroup("Other:", "New text file line delimiter")
            .isSelected()) {
            SWTBotCombo combo = bot
                .comboBoxInGroup("New text file line delimiter");
            String itemName = combo.items()[combo.selectionIndex()];
            windowPart.closeShell("Preferences");
            return itemName;
        }
        windowPart.closeShell("Preferences");
        return "";
    }

    /**
     * Open the view "Problems". The name of the method is defined the same as
     * the menu names. The name "showViewProblem" then means: hello guy, please
     * click main menus Window -> Show view -> Problems.
     * 
     */
    public void showViewProblems() throws RemoteException {
        menuPart.openViewWithName("General", "Problems");
    }

    /**
     * Open the view "Project Explorer". The name of the method is defined the
     * same as the menu names. The name "showViewProblem" then means: hello guy,
     * please click main menus Window -> Show view -> Project Explorer.
     * 
     */
    public void showViewProjectExplorer() throws RemoteException {
        menuPart.openViewWithName("General", "Project Explorer");
    }

    /**
     * Open the perspective "Java". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveJava" then means: hello guy,
     * please click main menus Window -> Open perspective -> Java.
     * 
     */
    public void openPerspectiveJava() throws RemoteException {
        perspectivePart.openPerspectiveWithId(ID_JAVA_PERSPECTIVE);
    }

    /**
     * test, if the java perspective is active.
     */
    public boolean isJavaPerspectiveActive() throws RemoteException {
        return perspectivePart.isPerspectiveActive(ID_JAVA_PERSPECTIVE);
    }

    /**
     * Open the perspective "Debug". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveDebug" then means: hello guy,
     * please click main menus Window -> Open perspective -> Debug.
     * 
     */
    public void openPerspectiveDebug() throws RemoteException {
        perspectivePart.openPerspectiveWithId(ID_DEBUG_PERSPECTIVE);
    }

    /**
     * test, if the debug perspective is active.
     */
    public boolean isDebugPerspectiveActive() throws RemoteException {
        return perspectivePart.isPerspectiveActive(ID_DEBUG_PERSPECTIVE);
    }

    private static final String MENU_FILE = "File";
    private static final String MENU_IMPORT = "Import...";
    private static final String SHELL_IMPORT = "Import";
    private static final String REPOSITORY_TYPE_SVN = "SVN";
    private static final String IMPORT_SOURCE = "Checkout Projects from SVN";

    public void importProjectFromSVN(String repository, String path)
        throws RemoteException {
        precondition();
        menuPart.clickMenuWithTexts(MENU_FILE, MENU_IMPORT);
        windowPart.confirmWindowWithTreeWithFilterText(SHELL_IMPORT,
            REPOSITORY_TYPE_SVN, IMPORT_SOURCE, NEXT);
        if (bot.table().containsItem(repository)) {
            windowPart.confirmWindowWithTable("Checkout from SVN", repository,
                NEXT);
        } else {
            bot.radio("Create a new repository location").click();
            bot.button(NEXT).click();
            bot.comboBoxWithLabel("Url:").setText(repository);
            bot.button(NEXT).click();
            windowPart.waitUntilShellActive("Checkout from SVN");
        }
        // Note that the character '"' is illegal in URLs
        String[] nodes = (repository.replaceAll("/", "\"") + path).split("/");
        nodes[0] = nodes[0].replaceAll("\"", "/");
        windowPart.confirmWindowWithTreeWithWaitingExpand("Checkout from SVN",
            FINISH, nodes);
        // windowPart.waitUntilShellActive("SVN Checkout");
        SWTBotShell shell2 = bot.shell("SVN Checkout");
        windowPart.waitUntilShellCloses(shell2);
    }

    @Override
    protected void precondition() throws RemoteException {
        workbenchC.activateEclipseShell();
    }

}
