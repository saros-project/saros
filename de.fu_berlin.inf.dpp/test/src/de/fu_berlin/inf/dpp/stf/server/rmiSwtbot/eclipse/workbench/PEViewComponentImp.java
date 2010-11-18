package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.widgets.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class PEViewComponentImp extends EclipseComponent implements
    PEViewComponent {

    /*
     * View infos
     */
    protected final static String VIEWNAME = SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER;
    // protected final static String VIEWID = SarosConstant.ID_

    /*
     * title of shells which are pop up by performing the actions on the package
     * explorer view.
     */
    protected final static String DELETE_RESOURCE = SarosConstant.SHELL_TITLE_DELETE_RESOURCE;
    protected final static String CONFIRM_DELETE = SarosConstant.SHELL_TITLE_CONFIRM_DELETE;
    protected final static String EDITOR_SELECTION = "Editor Selection";
    protected final static String MOVE_TITLE = "Move";
    protected final static String CONFIRM_DISCONNECT_FROM_SVN = "Confirm Disconnect from SVN";
    protected final static String SAROS_RUNNING_VCS_OPERATION = "Saros running VCS operation";
    protected final static String SAVE_RESOURCE = "Save Resource";

    /*
     * Tool tip text of toolbar buttons on the package explorer view
     */

    // Context menu of the tree on the view
    protected final static String DELETE = "Delete";
    protected final static String OPEN_WITH = "Open With";
    protected final static String OTHER = "Other...";
    protected final static String REFACTOR = "Refactor";
    protected final static String MOVE = "Move...";
    protected final static String TEAM = "Team";
    protected final static String DISCONNECT = "Disconnect...";
    private final static String SHARE_PROJECT = "Share Project...";
    protected final static String SWITCH_TO_ANOTHER_BRANCH = "Switch to another Branch/Tag/Revision...";

    protected final static String SRC = "src";
    protected final static String SUFIX_JAVA = ".java";

    public void closePackageExplorerView() throws RemoteException {
        viewPart.closeViewByTitle(VIEWNAME);
    }

    public void setFocusOnPackageExplorerView() throws RemoteException {
        viewPart.setFocusOnViewByTitle(VIEWNAME);
    }

    public void deleteProjectGui(String projectName) throws RemoteException {
        precondition();
        // SWTBotTree tree = viewO.getTreeInView(VIEWNAME);
        // tree.select(projectName);
        // menuO.clickMenuWithTexts("Edit", DELETE);
        viewPart.clickContextMenuOfTreeInView(VIEWNAME, DELETE, projectName);
        windowPart.confirmWindowWithCheckBox(DELETE_RESOURCE, OK, true);
        windowPart.waitUntilShellClosed(DELETE_RESOURCE);
    }

    public void deleteFileGui(String... nodes) throws RemoteException {
        precondition();
        viewPart.clickContextMenuOfTreeInView(VIEWNAME, DELETE, nodes);
        windowPart.waitUntilShellActive(CONFIRM_DELETE);
        windowPart.confirmWindow(CONFIRM_DELETE, OK);
    }

    public boolean isClassExistGUI(String... matchTexts) throws RemoteException {
        workbenchC.activateEclipseShell();
        precondition();
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        return treePart.isTreeItemWithMatchTextExist(tree, matchTexts);
    }

    public void openClass(String projectName, String packageName,
        String className) throws RemoteException {
        if (!editorC.isClassOpen(className)) {
            viewPart.openFileInView(VIEWNAME, projectName, SRC, packageName,
                className + SUFIX_JAVA);
            bot.sleep(sleepTime);
        }
    }

    public void openFile(String... filePath) throws RemoteException {
        if (!editorC.isFileOpen(filePath[filePath.length - 1])) {
            viewPart.openFileInView(VIEWNAME, filePath);
            bot.sleep(sleepTime);
        }
    }

    public void openClassWith(String whichEditor, String projectName,
        String packageName, String className) throws RemoteException {
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        tree.expandNode(projectName, SRC, packageName, className + SUFIX_JAVA)
            .select();
        ContextMenuHelper.clickContextMenu(tree, OPEN_WITH, OTHER);
        windowPart.waitUntilShellActive(EDITOR_SELECTION);
        SWTBotTable table = bot.table();
        table.select(whichEditor);
        basicPart.waitUntilButtonIsEnabled(OK);
        windowPart.confirmWindow(EDITOR_SELECTION, OK);
    }

    public void showViewPackageExplorer() throws RemoteException {
        viewPart.openViewWithName(VIEWNAME, "Java", "Package Explorer");
    }

    public void moveClassTo(String projectName, String pkg, String className,
        String targetProject, String targetPkg) throws RemoteException {
        showViewPackageExplorer();
        setFocusOnPackageExplorerView();
        String[] matchTexts = helperPart.changeToRegex(projectName, SRC, pkg,
            className);
        log.info("matchTexts: " + matchTexts);
        viewPart.clickContextMenusOfTreeItemInView(VIEWNAME, matchTexts,
            REFACTOR, MOVE);
        windowPart.waitUntilShellActive(MOVE_TITLE);
        windowPart.confirmWindowWithTree(MOVE_TITLE, OK, targetProject, SRC,
            targetPkg);
    }

    public void disConnectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewPart.clickContextMenusOfTreeItemInView(VIEWNAME, matchTexts, TEAM,
            DISCONNECT);
        windowPart.confirmWindow(CONFIRM_DISCONNECT_FROM_SVN, YES);
    }

    public void connectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewPart.clickContextMenusOfTreeItemInView(VIEWNAME, matchTexts, TEAM,
            SHARE_PROJECT);
        windowPart.confirmWindowWithTable("Share Project", "SVN", NEXT);
        bot.button(FINISH).click();
    }

    public void switchToOtherRevision() throws RemoteException {
        precondition();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewPart.clickContextMenusOfTreeItemInView(VIEWNAME, matchTexts, TEAM,
            SWITCH_TO_ANOTHER_BRANCH);
        windowPart.waitUntilShellActive("Switch");
        bot.checkBox("Switch to HEAD revision").click();
        bot.textWithLabel("Revision:").setText("115");
        bot.button(OK).click();
        windowPart.waitUntilShellClosed("SVN Switch");
    }

    public void switchToOtherRevision(String CLS_PATH) throws RemoteException {
        precondition();
        String[] matchTexts = CLS_PATH.split("/");
        for (int i = 0; i < matchTexts.length; i++) {
            matchTexts[i] = matchTexts[i] + ".*";
        }
        // String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewPart.clickContextMenusOfTreeItemInView(VIEWNAME, matchTexts, TEAM,
            SWITCH_TO_ANOTHER_BRANCH);
        windowPart.waitUntilShellActive("Switch");
        bot.checkBox("Switch to HEAD revision").click();
        bot.textWithLabel("Revision:").setText("116");
        bot.button(OK).click();
        windowPart.waitUntilShellClosed("SVN Switch");
    }

    public void revert() throws RemoteException {
        showViewPackageExplorer();
        setFocusOnPackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewPart.clickContextMenusOfTreeItemInView(VIEWNAME, matchTexts, TEAM,
            "Revert...");
        windowPart.confirmWindow("Revert", OK);
        windowPart.waitUntilShellClosed("Revert");
    }

    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException {
        renameFile(newName, projectName, SRC, pkg, className);
    }

    public void renameFile(String newName, String... texts)
        throws RemoteException {
        precondition();
        String[] matchTexts = helperPart.changeToRegex(texts);
        viewPart.clickContextMenusOfTreeItemInView(VIEWNAME, matchTexts,
            REFACTOR, "Rename...");
        windowPart.activateShellWithText("Rename Compilation Unit");
        bot.textWithLabel("New name:").setText(newName);
        basicPart.waitUntilButtonIsEnabled(FINISH);
        bot.button(FINISH).click();
        windowPart.waitUntilShellClosed("Rename Compilation Unit");
    }

    public void renameFolder(String projectName, String oldPath, String newPath)
        throws RemoteException {
        precondition();
        String[] nodes = { projectName, oldPath };
        viewPart.clickContextMenusOfTreeItemInView(VIEWNAME, nodes, REFACTOR,
            "Rename...");
        windowPart.waitUntilShellActive("Rename Resource");
        bot.textWithLabel("New name:").setText(newPath);
        basicPart.waitUntilButtonIsEnabled(OK);
        bot.button(OK).click();
        windowPart.waitUntilShellClosed("Rename Resource");
    }

    public void renamePkg(String newName, String... texts)
        throws RemoteException {
        precondition();
        String[] matchTexts = helperPart.changeToRegex(texts);
        viewPart.clickContextMenusOfTreeItemInView(VIEWNAME, matchTexts,
            REFACTOR, "Rename...");
        windowPart.activateShellWithText("Rename Package");
        bot.textWithLabel("New name:").setText(newName);
        basicPart.waitUntilButtonIsEnabled(OK);
        bot.button(OK).click();
        windowPart.waitUntilShellClosed("Rename Package");
    }

    public void switchToTag() throws RemoteException {
        precondition();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewPart.clickContextMenusOfTreeItemInView(VIEWNAME, matchTexts, TEAM,
            SWITCH_TO_ANOTHER_BRANCH);
        windowPart.waitUntilShellActive("Switch");
        bot.button("Select...").click();
        windowPart.confirmWindowWithTree("Repository Browser", OK, "tags",
            "eclipsecon2009");
        bot.button(OK).click();
        windowPart.waitUntilShellClosed("SVN Switch");
    }

    public void importProjectFromSVN(String path) throws RemoteException {
        workbenchC.activateEclipseShell();
        menuPart.clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
            SarosConstant.MENU_TITLE_IMPORT);
        windowPart.confirmWindowWithTreeWithFilterText(
            SarosConstant.SHELL_TITLE_IMPORT, "SVN",
            "Checkout Projects from SVN", NEXT);
        if (bot.table().containsItem(path)) {
            windowPart.confirmWindowWithTable("Checkout from SVN",
                BotConfiguration.SVN_URL, NEXT);
        } else {
            bot.radio("Create a new repository location").click();
            bot.button(NEXT).click();
            bot.comboBoxWithLabel("Url:").setText(path);
            bot.button(NEXT).click();
            windowPart.waitUntilShellActive("Checkout from SVN");
        }
        windowPart.confirmWindowWithTree("Checkout from SVN", FINISH, path,
            "trunk", "examples");
        windowPart.waitUntilShellActive("SVN Checkout");
        SWTBotShell shell2 = bot.shell("SVN Checkout");
        windowPart.waitUntilShellCloses(shell2);
    }

    protected List<String> getAllProjects() {
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        List<String> projectNames = new ArrayList<String>();
        for (int i = 0; i < tree.getAllItems().length; i++) {
            projectNames.add(tree.getAllItems()[i].getText());
        }
        return projectNames;
    }

    @Override
    protected void precondition() throws RemoteException {
        showViewPackageExplorer();
        setFocusOnPackageExplorerView();
    }

    public void waitUntilSarosRunningVCSOperationClosed()
        throws RemoteException {
        windowPart.waitUntilShellClosed(SAROS_RUNNING_VCS_OPERATION);
    }

}
