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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;

public class PackageExplorerViewObjectImp extends EclipseObject implements
    ExPackageExplorerViewObject {

    private static transient PackageExplorerViewObjectImp self;

    private final static String VIEWNAME = SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER;

    /**
     * {@link EclipseBasicObjectImp} is a singleton, but inheritance is
     * possible.
     */
    public static PackageExplorerViewObjectImp getInstance() {
        if (self != null)
            return self;
        self = new PackageExplorerViewObjectImp();
        return self;
    }

    public void closePackageExplorerView() throws RemoteException {
        viewO.closeViewByTitle(VIEWNAME);
    }

    public void closeWelcomeView() throws RemoteException {
        viewO.closeViewByTitle(SarosConstant.VIEW_TITLE_WELCOME);
    }

    public void activatePackageExplorerView() throws RemoteException {
        viewO.setFocusOnViewByTitle(VIEWNAME);
    }

    /**
     * Delete the selected project in view "Package Explorer" using GUI-method
     * 
     * 1. if view "Package Explorer" isn't open, open it.
     * 
     * 2. if view "Package Explorer" isn't active, activate it.
     * 
     * 3. select the project,which you want to delete, and then click the menu
     * Edit->Delete.
     * 
     * 4. confirm the popup-window "Delete Resources" and make sure the checkbox
     * is clicked.
     * 
     * 5. delegate wait so long until the popup-window is closed.
     * 
     * @param projectName
     *            the treeitem's name of the tree in view "Package Explorer"
     */
    public void deleteProjectGui(String projectName) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        SWTBotTree tree = viewO.getTreeInView(VIEWNAME);
        tree.select(projectName);
        menuO.clickMenuWithTexts("Edit", "Delete");
        exWindowO.confirmWindowWithCheckBox(
            SarosConstant.SHELL_TITLE_DELETE_RESOURCE, SarosConstant.BUTTON_OK,
            true);
        windowO.waitUntilShellClosed(SarosConstant.SHELL_TITLE_DELETE_RESOURCE);
    }

    /**
     * Delete the selected file in view "Package Explorer" using GUI-method
     * 
     * 1. if view "Package Explorer" isn't open, open it.
     * 
     * 2. if view "Package Explorer" isn't active, activate it.
     * 
     * 3. select the file,which you want to delete, and then click the menu
     * Edit->Delete.
     * 
     * 4. confirm the popup-window "Confirm Delete".
     * 
     * 5. delegate wait so long until the popup-window is closed.
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     */
    public void deleteFileGui(String... nodes) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        viewO.clickContextMenuOfTreeInView(VIEWNAME, "Delete", nodes);
        windowO.waitUntilShellActive(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
        exWindowO.confirmWindow(SarosConstant.SHELL_TITLE_CONFIRM_DELETE,
            SarosConstant.BUTTON_OK);
    }

    public boolean isClassExistGUI(String... matchTexts) throws RemoteException {
        exWorkbenchO.activateEclipseShell();
        showViewPackageExplorer();
        activatePackageExplorerView();
        SWTBotTree tree = viewO.getTreeInView(VIEWNAME);
        return treeO.isTreeItemWithMatchTextExist(tree, matchTexts);
    }

    /**
     * Open a class
     * 
     * 1. if the class file is already open, return.
     * 
     * 2. select the class file.
     * 
     * 3. then click the context menu open.
     * 
     * @param projectName
     *            name of the project, e.g. Foo-Saros.
     * @param packageName
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     */
    public void openClass(String projectName, String packageName,
        String className) throws RemoteException {
        if (!exEditorO.isClassOpen(className)) {
            viewO.openFileInView(VIEWNAME, projectName, "src", packageName,
                className + ".java");
            bot.sleep(sleepTime);
        }
    }

    public void openFile(String... filePath) throws RemoteException {
        if (!exEditorO.isFileOpen(filePath[filePath.length - 1])) {
            viewO.openFileInView(VIEWNAME, filePath);
            bot.sleep(sleepTime);
        }
    }

    public void openClassWith(String whichEditor, String projectName,
        String packageName, String className) throws RemoteException {
        SWTBotTree tree = viewO.getTreeInView(VIEWNAME);
        tree.expandNode(projectName, "src", packageName, className + ".java")
            .select();
        ContextMenuHelper.clickContextMenu(tree, "Open With", "Other...");
        windowO.waitUntilShellActive("Editor Selection");
        SWTBotTable table = bot.table();
        table.select(whichEditor);
        basicO.waitUntilButtonIsEnabled(SarosConstant.BUTTON_OK);
        exWindowO.confirmWindow("Editor Selection", SarosConstant.BUTTON_OK);
    }

    /**
     * Open the view "Package Explorer". The name of the method is defined the
     * same as the menu names. The name "showViewPackageExplorer" then means:
     * hello guy, please click main menus Window -> Show view ->
     * PackageExplorer.
     * 
     */
    public void showViewPackageExplorer() throws RemoteException {
        viewO.openViewWithName(VIEWNAME, "Java", "Package Explorer");
    }

    public void moveClassTo(String projectName, String pkg, String className,
        String targetProject, String targetPkg) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = helperO.changeToRegex(projectName, "src", pkg,
            className);
        log.info("matchTexts: " + matchTexts);
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            "Refactor", "Move...");
        windowO.waitUntilShellActive("Move");
        windowO.confirmWindowWithTree("Move", SarosConstant.BUTTON_OK,
            targetProject, "src", targetPkg);
    }

    public void disConnectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            "Team", "Disconnect...");
        exWindowO.confirmWindow("Confirm Disconnect from SVN",
            SarosConstant.BUTTON_YES);
    }

    public void connectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Share Project...");
        exWindowO.confirmWindowWithTable("Share Project", "SVN",
            SarosConstant.BUTTON_NEXT);
        bot.button(SarosConstant.BUTTON_FINISH).click();
    }

    public void switchToOtherRevision() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        windowO.waitUntilShellActive("Switch");
        bot.checkBox("Switch to HEAD revision").click();
        bot.textWithLabel("Revision:").setText("115");
        bot.button(SarosConstant.BUTTON_OK).click();
        windowO.waitUntilShellClosed("SVN Switch");
    }

    public void switchToOtherRevision(String CLS_PATH) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();

        String[] matchTexts = CLS_PATH.split("/");
        for (int i = 0; i < matchTexts.length; i++) {
            matchTexts[i] = matchTexts[i] + ".*";
        }
        // String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        windowO.waitUntilShellActive("Switch");
        bot.checkBox("Switch to HEAD revision").click();
        bot.textWithLabel("Revision:").setText("116");
        bot.button(SarosConstant.BUTTON_OK).click();
        windowO.waitUntilShellClosed("SVN Switch");
    }

    public void revert() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Revert...");
        exWindowO.confirmWindow("Revert", SarosConstant.BUTTON_OK);
        windowO.waitUntilShellClosed("Revert");
    }

    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException {
        renameFile(newName, projectName, "src", pkg, className);
    }

    public void renameFile(String newName, String... texts)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = helperO.changeToRegex(texts);
        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Refactor",
            "Rename...");
        windowO.activateShellWithText("Rename Compilation Unit");
        bot.textWithLabel("New name:").setText(newName);
        basicO.waitUntilButtonIsEnabled(SarosConstant.BUTTON_FINISH);
        bot.button(SarosConstant.BUTTON_FINISH).click();
        windowO.waitUntilShellClosed("Rename Compilation Unit");
    }

    public void renameFolder(String projectName, String oldPath, String newPath)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName, oldPath };
        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, nodes, "Refactor",
            "Rename...");
        windowO.waitUntilShellActive("Rename Resource");
        bot.textWithLabel("New name:").setText(newPath);
        basicO.waitUntilButtonIsEnabled(SarosConstant.BUTTON_OK);
        bot.button(SarosConstant.BUTTON_OK).click();
        windowO.waitUntilShellClosed("Rename Resource");
    }

    public void renamePkg(String newName, String... texts)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = helperO.changeToRegex(texts);
        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Refactor",
            "Rename...");
        windowO.activateShellWithText("Rename Package");
        bot.textWithLabel("New name:").setText(newName);
        basicO.waitUntilButtonIsEnabled(SarosConstant.BUTTON_OK);
        bot.button(SarosConstant.BUTTON_OK).click();
        windowO.waitUntilShellClosed("Rename Package");
    }

    public void switchToTag() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        windowO.waitUntilShellActive("Switch");
        bot.button("Select...").click();
        windowO.confirmWindowWithTree("Repository Browser",
            SarosConstant.BUTTON_OK, "tags", "eclipsecon2009");
        bot.button(SarosConstant.BUTTON_OK).click();
        windowO.waitUntilShellClosed("SVN Switch");
    }

    public void importProjectFromSVN(String path) throws RemoteException {
        exWorkbenchO.activateEclipseShell();
        menuO.clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
            SarosConstant.MENU_TITLE_IMPORT);
        exWindowO.confirmWindowWithTreeWithFilterText(
            SarosConstant.SHELL_TITLE_IMPORT, "SVN",
            "Checkout Projects from SVN", SarosConstant.BUTTON_NEXT);
        if (bot.table().containsItem(path)) {
            exWindowO.confirmWindowWithTable("Checkout from SVN",
                BotConfiguration.SVN_URL, SarosConstant.BUTTON_NEXT);
        } else {
            bot.radio("Create a new repository location").click();
            bot.button(SarosConstant.BUTTON_NEXT).click();
            bot.comboBoxWithLabel("Url:").setText(path);
            bot.button(SarosConstant.BUTTON_NEXT).click();
            windowO.waitUntilShellActive("Checkout from SVN");
        }
        windowO.confirmWindowWithTree("Checkout from SVN",
            SarosConstant.BUTTON_FINISH, path, "trunk", "examples");
        windowO.waitUntilShellActive("SVN Checkout");
        SWTBotShell shell2 = bot.shell("SVN Checkout");
        windowO.waitUntilShellCloses(shell2);
    }

    public void shareProject(String projectName) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperO.changeToRegex(nodes);

        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT);
    }

    public void shareprojectWithVCSSupport(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperO.changeToRegex(nodes);
        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS);
    }

    public void shareProjectPartically(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperO.changeToRegex(nodes);
        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY);
    }

    public void addToSession(String projectName) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperO.changeToRegex(nodes);
        viewO.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_ADD_TO_SESSION);
    }

    public void clickShareProjectWith(String projectName,
        String shareProjectWith) throws RemoteException {
        if (shareProjectWith.equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT)) {
            shareProject(projectName);
        } else if (shareProjectWith
            .equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS))
            shareprojectWithVCSSupport(projectName);
        else if (shareProjectWith
            .equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY))
            shareProjectPartically(projectName);
        else
            addToSession(projectName);
    }

    public void shareProject(String projectName, List<String> inviteeJIDS)
        throws RemoteException {
        shareProject(projectName);
        windowO.waitUntilShellActive(SarosConstant.SHELL_TITLE_INVITATION);
        tableO.selectCheckBoxsInTable(inviteeJIDS);
        basicO.waitUntilButtonIsEnabled(SarosConstant.BUTTON_FINISH);
        bot.button(SarosConstant.BUTTON_FINISH).click();
    }

    protected List<String> getAllProjects() {
        SWTBotTree tree = viewO.getTreeInView(VIEWNAME);
        List<String> projectNames = new ArrayList<String>();
        for (int i = 0; i < tree.getAllItems().length; i++) {
            projectNames.add(tree.getAllItems()[i].getText());
        }
        return projectNames;
    }
}
