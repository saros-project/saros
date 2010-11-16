package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.widgets.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class PEViewComponentImp extends EclipseComponent implements
    PEViewComponent {

    private static transient PEViewComponentImp self;

    /*
     * View infos
     */
    private final static String VIEWNAME = SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER;
    // private final static String VIEWID = SarosConstant.ID_

    /*
     * title of shells which are pop up by performing the actions on the package
     * explorer view.
     */
    private final static String INVITATIONCANCELLED = "Invitation Cancelled";
    private final static String SESSIONINVITATION = "Session Invitation";
    private final static String PROBLEMOCCURRED = "Problem Occurred";
    private final static String SAROS_RUNNING_VCS_OPERATION = "Saros running VCS operation";
    private final static String DELETE_RESOURCE = SarosConstant.SHELL_TITLE_DELETE_RESOURCE;
    private final static String CONFIRM_DELETE = SarosConstant.SHELL_TITLE_CONFIRM_DELETE;
    private final static String EDITOR_SELECTION = "Editor Selection";
    private final static String MOVE_TITLE = "Move";
    private final static String CONFIRM_DISCONNECT_FROM_SVN = "Confirm Disconnect from SVN";

    /*
     * Tool tip text of toolbar buttons on the package explorer view
     */

    // Context menu of the tree on the view
    private final static String DELETE = "Delete";
    private final static String OPEN_WITH = "Open With";
    private final static String OTHER = "Other...";
    private final static String REFACTOR = "Refactor";
    private final static String MOVE = "Move...";
    private final static String TEAM = "Team";
    private final static String DISCONNECT = "Disconnect...";
    private final static String SHARE_PROJECT = "Share Project...";
    private final static String SWITCH_TO_ANOTHER_BRANCH = "Switch to another Branch/Tag/Revision...";

    private final static String SRC = "src";
    private final static String SUFIX_JAVA = ".java";

    /**
     * {@link BasicComponentImp} is a singleton, but inheritance is possible.
     */
    public static PEViewComponentImp getInstance() {
        if (self != null)
            return self;
        self = new PEViewComponentImp();
        return self;
    }

    public void closePackageExplorerView() throws RemoteException {
        viewO.closeViewByTitle(VIEWNAME);
    }

    public void setFocusOnPackageExplorerView() throws RemoteException {
        viewO.setFocusOnViewByTitle(VIEWNAME);
    }

    public void deleteProjectGui(String projectName) throws RemoteException {
        precondition();
        // SWTBotTree tree = viewO.getTreeInView(VIEWNAME);
        // tree.select(projectName);
        // menuO.clickMenuWithTexts("Edit", DELETE);
        viewO.clickContextMenuOfTreeInView(VIEWNAME, DELETE, projectName);
        windowO.confirmWindowWithCheckBox(DELETE_RESOURCE, OK, true);
        windowO.waitUntilShellClosed(DELETE_RESOURCE);
    }

    public void deleteFileGui(String... nodes) throws RemoteException {
        precondition();
        viewO.clickContextMenuOfTreeInView(VIEWNAME, DELETE, nodes);
        windowO.waitUntilShellActive(CONFIRM_DELETE);
        windowO.confirmWindow(CONFIRM_DELETE, OK);
    }

    public boolean isClassExistGUI(String... matchTexts) throws RemoteException {
        exWorkbenchO.activateEclipseShell();
        precondition();
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
            viewO.openFileInView(VIEWNAME, projectName, SRC, packageName,
                className + SUFIX_JAVA);
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
        tree.expandNode(projectName, SRC, packageName, className + SUFIX_JAVA)
            .select();
        ContextMenuHelper.clickContextMenu(tree, OPEN_WITH, OTHER);
        windowO.waitUntilShellActive(EDITOR_SELECTION);
        SWTBotTable table = bot.table();
        table.select(whichEditor);
        basicO.waitUntilButtonIsEnabled(OK);
        windowO.confirmWindow(EDITOR_SELECTION, OK);
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
        setFocusOnPackageExplorerView();
        String[] matchTexts = helperO.changeToRegex(projectName, SRC, pkg,
            className);
        log.info("matchTexts: " + matchTexts);
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            REFACTOR, MOVE);
        windowO.waitUntilShellActive(MOVE_TITLE);
        windowO.confirmWindowWithTree(MOVE_TITLE, OK, targetProject, SRC,
            targetPkg);
    }

    public void disConnectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            TEAM, DISCONNECT);
        windowO.confirmWindow(CONFIRM_DISCONNECT_FROM_SVN, YES);
    }

    public void connectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            TEAM, SHARE_PROJECT);
        windowO.confirmWindowWithTable("Share Project", "SVN", NEXT);
        bot.button(FINISH).click();
    }

    public void switchToOtherRevision() throws RemoteException {
        precondition();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            TEAM, SWITCH_TO_ANOTHER_BRANCH);
        windowO.waitUntilShellActive("Switch");
        bot.checkBox("Switch to HEAD revision").click();
        bot.textWithLabel("Revision:").setText("115");
        bot.button(OK).click();
        windowO.waitUntilShellClosed("SVN Switch");
    }

    public void switchToOtherRevision(String CLS_PATH) throws RemoteException {
        precondition();
        String[] matchTexts = CLS_PATH.split("/");
        for (int i = 0; i < matchTexts.length; i++) {
            matchTexts[i] = matchTexts[i] + ".*";
        }
        // String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            TEAM, SWITCH_TO_ANOTHER_BRANCH);
        windowO.waitUntilShellActive("Switch");
        bot.checkBox("Switch to HEAD revision").click();
        bot.textWithLabel("Revision:").setText("116");
        bot.button(OK).click();
        windowO.waitUntilShellClosed("SVN Switch");
    }

    public void revert() throws RemoteException {
        showViewPackageExplorer();
        setFocusOnPackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            TEAM, "Revert...");
        windowO.confirmWindow("Revert", OK);
        windowO.waitUntilShellClosed("Revert");
    }

    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException {
        renameFile(newName, projectName, SRC, pkg, className);
    }

    public void renameFile(String newName, String... texts)
        throws RemoteException {
        precondition();
        String[] matchTexts = helperO.changeToRegex(texts);
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            REFACTOR, "Rename...");
        windowO.activateShellWithText("Rename Compilation Unit");
        bot.textWithLabel("New name:").setText(newName);
        basicO.waitUntilButtonIsEnabled(FINISH);
        bot.button(FINISH).click();
        windowO.waitUntilShellClosed("Rename Compilation Unit");
    }

    public void renameFolder(String projectName, String oldPath, String newPath)
        throws RemoteException {
        precondition();
        String[] nodes = { projectName, oldPath };
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, nodes,
            REFACTOR, "Rename...");
        windowO.waitUntilShellActive("Rename Resource");
        bot.textWithLabel("New name:").setText(newPath);
        basicO.waitUntilButtonIsEnabled(OK);
        bot.button(OK).click();
        windowO.waitUntilShellClosed("Rename Resource");
    }

    public void renamePkg(String newName, String... texts)
        throws RemoteException {
        precondition();
        String[] matchTexts = helperO.changeToRegex(texts);
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            REFACTOR, "Rename...");
        windowO.activateShellWithText("Rename Package");
        bot.textWithLabel("New name:").setText(newName);
        basicO.waitUntilButtonIsEnabled(OK);
        bot.button(OK).click();
        windowO.waitUntilShellClosed("Rename Package");
    }

    public void switchToTag() throws RemoteException {
        precondition();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            TEAM, SWITCH_TO_ANOTHER_BRANCH);
        windowO.waitUntilShellActive("Switch");
        bot.button("Select...").click();
        windowO.confirmWindowWithTree("Repository Browser", OK, "tags",
            "eclipsecon2009");
        bot.button(OK).click();
        windowO.waitUntilShellClosed("SVN Switch");
    }

    public void importProjectFromSVN(String path) throws RemoteException {
        exWorkbenchO.activateEclipseShell();
        menuO.clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
            SarosConstant.MENU_TITLE_IMPORT);
        windowO.confirmWindowWithTreeWithFilterText(
            SarosConstant.SHELL_TITLE_IMPORT, "SVN",
            "Checkout Projects from SVN", NEXT);
        if (bot.table().containsItem(path)) {
            windowO.confirmWindowWithTable("Checkout from SVN",
                BotConfiguration.SVN_URL, NEXT);
        } else {
            bot.radio("Create a new repository location").click();
            bot.button(NEXT).click();
            bot.comboBoxWithLabel("Url:").setText(path);
            bot.button(NEXT).click();
            windowO.waitUntilShellActive("Checkout from SVN");
        }
        windowO.confirmWindowWithTree("Checkout from SVN", FINISH, path,
            "trunk", "examples");
        windowO.waitUntilShellActive("SVN Checkout");
        SWTBotShell shell2 = bot.shell("SVN Checkout");
        windowO.waitUntilShellCloses(shell2);
    }

    public void shareProject(String projectName) throws RemoteException {
        showViewPackageExplorer();
        setFocusOnPackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperO.changeToRegex(nodes);

        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            "Saros", SarosConstant.CONTEXT_MENU_SHARE_PROJECT);
    }

    public void shareprojectWithVCSSupport(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        setFocusOnPackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperO.changeToRegex(nodes);
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            "Saros", SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS);
    }

    public void shareProjectPartically(String projectName)
        throws RemoteException {
        showViewPackageExplorer();
        setFocusOnPackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperO.changeToRegex(nodes);
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            "Saros", SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY);
    }

    public void addToSession(String projectName) throws RemoteException {
        showViewPackageExplorer();
        setFocusOnPackageExplorerView();
        String[] nodes = { projectName };
        String[] matchTexts = helperO.changeToRegex(nodes);
        viewO.clickMenusOfContextMenuOfTreeItemInView(VIEWNAME, matchTexts,
            "Saros", SarosConstant.CONTEXT_MENU_ADD_TO_SESSION);
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

    public boolean isWindowInvitationCancelledActive() throws RemoteException {
        return windowO.isShellActive(INVITATIONCANCELLED);

    }

    public void closeWindowInvitaitonCancelled() throws RemoteException {
        windowO.closeShell(INVITATIONCANCELLED);

    }

    public void waitUntilIsWindowInvitationCnacelledActive()
        throws RemoteException {
        windowO.waitUntilShellActive(INVITATIONCANCELLED);

    }

    public boolean isWIndowSessionInvitationActive() throws RemoteException {
        return windowO.isShellActive(SESSIONINVITATION);
    }

    public void closeWIndowSessionInvitation() throws RemoteException {
        windowO.closeShell(SESSIONINVITATION);
    }

    public void waitUntilWIndowSessionInvitationActive() throws RemoteException {
        windowO.waitUntilShellActive(SESSIONINVITATION);

    }

    public void confirmInvitationWindow(String... invitees)
        throws RemoteException {
        windowO.activateShellWithText(SarosConstant.SHELL_TITLE_INVITATION);
        windowO.confirmWindowWithCheckBox(SarosConstant.SHELL_TITLE_INVITATION,
            FINISH, invitees);
    }

    public void confirmSessionInvitationWizard(String inviter,
        String projectname) throws RemoteException {
        windowO
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingNewproject(projectname);
    }

    public void confirmSessionInvitationWizardUsingExistProject(String inviter,
        String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingExistProject(projectName);
    }

    public void confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
        String inviter, String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(projectName);
    }

    public void confirmSessionInvitationWizardUsingExistProjectWithCopy(
        String inviter, String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        confirmSessionInvitationWindowStep1();
        confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(projectName);
    }

    /**
     * First step: invitee acknowledge session to given inviter
     * 
     * This method captures two screenshots as side effect.
     */
    public void confirmSessionInvitationWindowStep1() throws RemoteException {
        // if (!isTextWithLabelEqualWithText(SarosConstant.TEXT_LABEL_INVITER,
        // inviter))
        // log.warn("inviter does not match: " + inviter);
        // rmiBot.captureScreenshot(rmiBot.TEMPDIR +
        // "/acknowledge_project1.png");
        basicO.waitUntilButtonIsEnabled(NEXT);
        bot.button(NEXT).click();
        // rmiBot.captureScreenshot(rmiBot.TEMPDIR +
        // "/acknowledge_project2.png");
        basicO.waitUntilButtonIsEnabled(FINISH);
    }

    /**
     * Second step: invitee acknowledge a new project
     * 
     * This method captures two screenshots as side effect.
     */
    public void confirmSessionInvitationWindowStep2UsingNewproject(
        String projectName) throws RemoteException {
        bot.radio(SarosConstant.RADIO_LABEL_CREATE_NEW_PROJECT).click();
        // rmiBot.captureScreenshot(rmiBot.TEMPDIR +
        // "/acknowledge_project3.png");
        bot.button(FINISH).click();
        // rmiBot.captureScreenshot(rmiBot.TEMPDIR +
        // "/acknowledge_project4.png");
        windowO.waitUntilShellCloses(bot.shell(SESSIONINVITATION));
    }

    public void confirmSessionInvitationWindowStep2UsingExistProject(
        String projectName) throws RemoteException {
        bot.radio("Use existing project").click();
        bot.button("Browse").click();
        windowO.confirmWindowWithTree("Folder Selection", OK, projectName);
        bot.button(FINISH).click();

        windowO.confirmWindow("Warning: Local changes will be deleted", YES);

        /*
         * if there are some files locally, which are not saved yet, you will
         * get a popup window with the title "Save Resource" after you comfirm
         * the window "Warning: Local changes will be deleted" with YES.
         */
        if (windowO.isShellActive("Save Resource")) {
            windowO.confirmWindow("Save Resource", YES);
            /*
             * it take some more time for the session invitation if you don't
             * save your files locally. So rmiBot need to wait until the
             * invitation is finished.
             */
            windowO.waitUntilShellCloses(bot
                .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
        }

        /*
         * after the release 10.10.28 the time of sharing project has become so
         * fast, that the pop up window "Session Invitation" is immediately
         * disappeared after you confirm the window ""Warning: Local changes
         * will be deleted".
         * 
         * So i have to check first,whether the window "Session Invitation" is
         * still open at all before i run the waitUntilShellCloses(it guarantees
         * that rmiBot wait until the invitation is finished). Otherwise you may
         * get the WidgetNotfoundException.
         */
        if (windowO.isShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION)) {
            windowO.waitUntilShellCloses(bot
                .shell(SarosConstant.SHELL_TITLE_SESSION_INVITATION));
        }

    }

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(
        String projectName) throws RemoteException {
        bot.radio("Use existing project").click();
        bot.button("Browse").click();
        windowO.confirmWindowWithTree("Folder Selection", OK, projectName);
        bot.button(FINISH).click();
        windowO.confirmWindow("Warning: Local changes will be deleted", NO);
    }

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(
        String projectName) throws RemoteException {
        bot.radio("Use existing project").click();
        bot.button("Browse").click();
        windowO.confirmWindowWithTree("Folder Selection", OK, projectName);
        bot.checkBox("Create copy for working distributed. New project name:")
            .click();
        bot.button(FINISH).click();
        windowO.waitUntilShellCloses(bot.shell(SESSIONINVITATION));
    }

    public void confirmInvitationCancelledWindow() throws RemoteException {
        SWTBotShell shell = bot.shell(INVITATIONCANCELLED);
        shell.activate().setFocus();
        SWTBotButton button = shell.bot().button();
        button.click();
    }

    public void cancelInivtationInSessionInvitationWindow()
        throws RemoteException {
        SWTBotShell shell = bot.activeShell();
        shell.bot().toolbarButton().click();
    }

    public void confirmSessionUsingNewOrExistProject(JID inviterJID,
        String projectName, int typeOfSharingProject) throws RemoteException {
        windowO.waitUntilShellActive(SESSIONINVITATION);
        switch (typeOfSharingProject) {
        case SarosConstant.CREATE_NEW_PROJECT:
            confirmSessionInvitationWizard(inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT:
            confirmSessionInvitationWizardUsingExistProject(
                inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE:
            confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
                inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_COPY:
            confirmSessionInvitationWizardUsingExistProjectWithCopy(
                inviterJID.getBase(), projectName);
            break;
        default:
            break;
        }
    }

    public void waitUntilIsWindowProblemOccurredActive() throws RemoteException {
        windowO.isShellActive(PROBLEMOCCURRED);
    }

    public void waitUntilSarosRunningVCSOperationClosed()
        throws RemoteException {
        windowO.waitUntilShellClosed(SAROS_RUNNING_VCS_OPERATION);
    }

    public String getSecondLabelOfProblemOccurredWindow()
        throws RemoteException {
        return bot.shell(PROBLEMOCCURRED).bot().label(2).getText();
    }

    public void confirmProblemOccurredWindow(String plainJID)
        throws RemoteException {
        windowO.waitUntilShellActive(PROBLEMOCCURRED);
        bot.text().getText().matches("*." + plainJID + ".*");
        basicO.waitUntilButtonIsEnabled(OK);
        bot.button(OK).click();
    }

    @Override
    protected void precondition() throws RemoteException {
        showViewPackageExplorer();
        setFocusOnPackageExplorerView();
    }

}
