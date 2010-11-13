package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.widgets.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;

public class PackageExplorerViewObjectImp extends EclipseObject implements
    PackageExplorerViewObject {
    // public static PackageExplorerViewObjectImp classVariable;

    private static transient PackageExplorerViewObjectImp self;

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
        viewObject.closeViewWithText(PEViewName);
    }

    public void closeWelcomeView() throws RemoteException {
        viewObject.closeViewWithText(SarosConstant.VIEW_TITLE_WELCOME);
    }

    public void activatePackageExplorerView() throws RemoteException {
        viewObject.setFocusOnViewByTitle(PEViewName);
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
        SWTBotTree tree = viewObject.getTreeInView(PEViewName);
        tree.select(projectName);
        menuObject.clickMenuWithTexts("Edit", "Delete");
        exportedWindowObject.confirmWindowWithCheckBox(
            SarosConstant.SHELL_TITLE_DELETE_RESOURCE, SarosConstant.BUTTON_OK,
            true);
        windowObject
            .waitUntilShellClosed(SarosConstant.SHELL_TITLE_DELETE_RESOURCE);
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
        viewObject.clickContextMenuOfTreeInView(PEViewName, "Delete", nodes);
        windowObject
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
        exportedWindowObject.confirmWindow(
            SarosConstant.SHELL_TITLE_CONFIRM_DELETE, SarosConstant.BUTTON_OK);
    }

    public boolean isClassExistGUI(String... matchTexts) throws RemoteException {
        workbenchObject.activateEclipseShell();
        showViewPackageExplorer();
        activatePackageExplorerView();
        SWTBotTree tree = viewObject.getTreeInView(PEViewName);
        return treeObject.isTreeItemWithMatchTextExist(tree, matchTexts);
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
        if (!exportedEditorObject.isClassOpen(className)) {
            viewObject.openFileInView(PEViewName, projectName, "src",
                packageName, className + ".java");
            bot.sleep(sleepTime);
        }
    }

    public void openFile(String... filePath) throws RemoteException {
        if (!exportedEditorObject.isFileOpen(filePath[filePath.length - 1])) {
            viewObject.openFileInView(PEViewName, filePath);
            bot.sleep(sleepTime);
        }
    }

    public void openClassWith(String whichEditor, String projectName,
        String packageName, String className) throws RemoteException {
        SWTBotTree tree = viewObject.getTreeInView(PEViewName);
        tree.expandNode(projectName, "src", packageName, className + ".java")
            .select();
        ContextMenuHelper.clickContextMenu(tree, "Open With", "Other...");
        windowObject.waitUntilShellActive("Editor Selection");
        SWTBotTable table = bot.table();
        table.select(whichEditor);
        basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_OK);
        exportedWindowObject.confirmWindow("Editor Selection",
            SarosConstant.BUTTON_OK);
    }

    /**
     * Open the view "Package Explorer". The name of the method is defined the
     * same as the menu names. The name "showViewPackageExplorer" then means:
     * hello guy, please click main menus Window -> Show view ->
     * PackageExplorer.
     * 
     */
    public void showViewPackageExplorer() throws RemoteException {
        viewObject.openViewWithName(PEViewName, "Java", "Package Explorer");
    }

    public void moveClassTo(String projectName, String pkg, String className,
        String targetProject, String targetPkg) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = helperObject.changeToRegex(projectName, "src",
            pkg, className);
        log.info("matchTexts: " + matchTexts);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(PEViewName,
            matchTexts, "Refactor", "Move...");
        windowObject.waitUntilShellActive("Move");
        windowObject.confirmWindowWithTree("Move", SarosConstant.BUTTON_OK,
            targetProject, "src", targetPkg);
    }

    public void disConnectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(PEViewName,
            matchTexts, "Team", "Disconnect...");
        exportedWindowObject.confirmWindow("Confirm Disconnect from SVN",
            SarosConstant.BUTTON_YES);
    }

    public void connectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Share Project...");
        exportedWindowObject.confirmWindowWithTable("Share Project", "SVN",
            SarosConstant.BUTTON_NEXT);
        bot.button(SarosConstant.BUTTON_FINISH).click();
    }

    public void switchToOtherRevision() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        windowObject.waitUntilShellActive("Switch");
        bot.checkBox("Switch to HEAD revision").click();
        bot.textWithLabel("Revision:").setText("115");
        bot.button(SarosConstant.BUTTON_OK).click();
        windowObject.waitUntilShellClosed("SVN Switch");
    }

    public void switchToOtherRevision(String CLS_PATH) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();

        String[] matchTexts = CLS_PATH.split("/");
        for (int i = 0; i < matchTexts.length; i++) {
            matchTexts[i] = matchTexts[i] + ".*";
        }
        // String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        windowObject.waitUntilShellActive("Switch");
        bot.checkBox("Switch to HEAD revision").click();
        bot.textWithLabel("Revision:").setText("116");
        bot.button(SarosConstant.BUTTON_OK).click();
        windowObject.waitUntilShellClosed("SVN Switch");
    }

    public void revert() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Revert...");
        exportedWindowObject.confirmWindow("Revert", SarosConstant.BUTTON_OK);
        windowObject.waitUntilShellClosed("Revert");
    }

    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException {
        renameFile(newName, projectName, "src", pkg, className);
    }

    public void renameFile(String newName, String... texts)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = helperObject.changeToRegex(texts);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Refactor",
            "Rename...");
        windowObject.activateShellWithText("Rename Compilation Unit");
        bot.textWithLabel("New name:").setText(newName);
        basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        bot.button(SarosConstant.BUTTON_FINISH).click();
        windowObject.waitUntilShellClosed("Rename Compilation Unit");
    }

    public void renameFolder(String projectName, String oldPath, String newPath)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName, oldPath };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, nodes, "Refactor",
            "Rename...");
        windowObject.waitUntilShellActive("Rename Resource");
        bot.textWithLabel("New name:").setText(newPath);
        basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_OK);
        bot.button(SarosConstant.BUTTON_OK).click();
        windowObject.waitUntilShellClosed("Rename Resource");
    }

    public void renamePkg(String newName, String... texts)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = helperObject.changeToRegex(texts);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Refactor",
            "Rename...");
        windowObject.activateShellWithText("Rename Package");
        bot.textWithLabel("New name:").setText(newName);
        basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_OK);
        bot.button(SarosConstant.BUTTON_OK).click();
        windowObject.waitUntilShellClosed("Rename Package");
    }

    public void switchToTag() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        windowObject.waitUntilShellActive("Switch");
        bot.button("Select...").click();
        windowObject.confirmWindowWithTree("Repository Browser",
            SarosConstant.BUTTON_OK, "tags", "eclipsecon2009");
        bot.button(SarosConstant.BUTTON_OK).click();
        windowObject.waitUntilShellClosed("SVN Switch");
    }

    public void importProjectFromSVN(String path) throws RemoteException {
        workbenchObject.activateEclipseShell();
        menuObject.clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
            SarosConstant.MENU_TITLE_IMPORT);
        exportedWindowObject.confirmWindowWithTreeWithFilterText(
            SarosConstant.SHELL_TITLE_IMPORT, "SVN",
            "Checkout Projects from SVN", SarosConstant.BUTTON_NEXT);
        if (bot.table().containsItem(path)) {
            exportedWindowObject.confirmWindowWithTable("Checkout from SVN",
                BotConfiguration.SVN_URL, SarosConstant.BUTTON_NEXT);
        } else {
            bot.radio("Create a new repository location").click();
            bot.button(SarosConstant.BUTTON_NEXT).click();
            bot.comboBoxWithLabel("Url:").setText(path);
            bot.button(SarosConstant.BUTTON_NEXT).click();
            windowObject.waitUntilShellActive("Checkout from SVN");
        }
        windowObject.confirmWindowWithTree("Checkout from SVN",
            SarosConstant.BUTTON_FINISH, path, "trunk", "examples");
        windowObject.waitUntilShellActive("SVN Checkout");
        SWTBotShell shell2 = bot.shell("SVN Checkout");
        windowObject.waitUntilShellCloses(shell2);
    }

    public void shareProject(String projectName) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperObject.changeToRegex(nodes);

        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT);
    }

    public void shareprojectWithVCSSupport(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperObject.changeToRegex(nodes);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS);
    }

    public void shareProjectPartically(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperObject.changeToRegex(nodes);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Saros",
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY);
    }

    public void addToSession(String projectName) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperObject.changeToRegex(nodes);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
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
        windowObject.waitUntilShellActive(SarosConstant.SHELL_TITLE_INVITATION);
        tableObject.selectCheckBoxsInTable(inviteeJIDS);
        basicObject.waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        bot.button(SarosConstant.BUTTON_FINISH).click();
    }

}
