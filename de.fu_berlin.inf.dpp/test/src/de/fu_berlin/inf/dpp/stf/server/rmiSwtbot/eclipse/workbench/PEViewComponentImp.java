package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.program.Program;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.widgets.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

public class PEViewComponentImp extends EclipseComponent implements
    PEViewComponent {

    /* View infos */
    protected final static String VIEWNAME = "Package Explorer";
    protected final static String VIEWID = "org.eclipse.jdt.ui.PackageExplorer";

    /*
     * title of shells which are pop up by performing the actions on the package
     * explorer view.
     */
    private final static String SHELL_DELETE_RESOURCE = "Delete Resources";
    private final static String SHELL_EDITOR_SELECTION = "Editor Selection";
    private final static String SHELL_MOVE = "Move";
    private final static String SHELL_REVERT = "Revert";
    private final static String SHELL_SHARE_PROJECT = "Share Project";
    private final static String SHELL_SAROS_RUNNING_VCS_OPERATION = "Saros running VCS operation";
    private final static String SHELL_RENAME_PACKAGE = "Rename Package";
    private final static String SHELL_RENAME_RESOURCE = "Rename Resource";
    private final static String SHELL_RENAME_COMPiIATION_UNIT = "Rename Compilation Unit";
    private static final String SHELL_SWITCH = "Switch";
    private static final String SHELL_SVN_SWITCH = "SVN Switch";
    private static final String SHELL_NEW_JAVA_PROJECT = "New Java Project";
    private static final String LABEL_PROJECT_NAME = "Project name:";
    private static final String SHELL_NEW_PROJECT = "New Project";
    private final static String SHELL_CONFIRM_DISCONNECT_FROM_SVN = "Confirm Disconnect from SVN";
    private final static String SHELL_NEW_FOLDER = "New Folder";
    private final static String SHELL_NEW_FILE = "New File";
    private final static String SHELL_NEW_JAVA_PACKAGE = "New Java Package";
    private final static String SHELL_NEW_JAVA_CLASS = "New Java Class";
    private static final String SHELL_IMPORT = "Import";

    /* Label of pop up windows */
    private final static String LABEL_CREATE_A_NEW_REPOSITORY_LOCATION = "Create a new repository location";
    private final static String LABEL_URL = "Url:";
    private final static String LABEL_NEW_NAME = "New name:";
    private final static String LABEL_TO_URL = "To URL:";
    private static final String LABEL_SWITCH_TOHEAD_REVISION = "Switch to HEAD revision";
    private static final String LABEL_REVISION = "Revision:";
    private static final String LABEL_FOLDER_NAME = "Folder name:";
    private static final String LABEL_FILE_NAME = "File name:";

    /* Context menu of a selected tree item on the package explorer view */
    private final static String DELETE = "Delete";
    private final static String REFACTOR = "Refactor";
    private static final String NEW = "New";

    /* Context menu of a selected file on the package explorer view */
    private final static String OPEN = "Open";
    private final static String OPEN_WITH = "Open With";
    private final static String TEAM = "Team";

    /* All the sub menus of the context menu "Open with" */
    // private final static String TEXT_EDITOR = "Text Editor";
    // private final static String SYSTEM_EDITOR = "System Editor";
    // private final static String DEFAULT_EDITOR = "Default Editor";
    private final static String OTHER = "Other...";

    /* All the sub menus of the context menu "Team" */
    private final static String REVERT = "Revert...";
    private final static String DISCONNECT = "Disconnect...";
    private final static String SHARE_PROJECT = "Share Project...";
    private final static String SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION = "Switch to another Branch/Tag/Revision...";

    /* All the sub menus of the context menu "Refactor" */
    private final static String RENAME = "Rename...";
    private final static String MOVE = "Move...";

    /* All the sub menus of the context menu "New" */
    private static final String PROJECT = "Project...";
    private static final String FOLDER = "Folder";
    private static final String FILE = "File";
    private static final String CLASS = "Class";
    private static final String PACKAGE = "Package";
    private static final String JAVA_PROJECT = "Java Project";

    /* categories and nodes of the shell "New Project" */
    private static final String CATEGORY_GENERAL = "General";
    private static final String NODE_PROJECT = "Project";

    /* table iems of the shell "Share project" of the conext menu "Team" */
    private final static String REPOSITORY_TYPE_SVN = "SVN";

    /***********************************************************************
     * 
     * exported functions
     * 
     ***********************************************************************/

    /**********************************************
     * 
     * open/close/activate the package explorer view
     * 
     **********************************************/
    public void openPEView() throws RemoteException {
        if (!isPEViewOpen())
            viewPart.openViewById(VIEWID);
    }

    public boolean isPEViewOpen() throws RemoteException {
        return viewPart.isViewOpen(VIEWNAME);
    }

    public void closePEView() throws RemoteException {
        viewPart.closeViewByTitle(VIEWNAME);
    }

    public void setFocusOnPEView() throws RemoteException {
        viewPart.setFocusOnViewByTitle(VIEWNAME);
    }

    public boolean isPEViewActive() throws RemoteException {
        return viewPart.isViewActive(VIEWNAME);
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "New"
     * 
     **********************************************/
    public void newProject(String projectName) throws RemoteException {
        if (!isProjectExist(projectName)) {
            workbenchC.activateEclipseShell();
            menuPart.clickMenuWithTexts(FILE, NEW, PROJECT);
            confirmWizardNewProject(projectName);
        }
    }

    public void newJavaProject(String projectName) throws RemoteException {
        if (!isProjectExist(projectName)) {
            workbenchC.activateEclipseShell();
            menuPart.clickMenuWithTexts(FILE, NEW, JAVA_PROJECT);
            confirmWindowNewJavaProject(projectName);
        }
    }

    public boolean isProjectExist(String projectName) throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        return project.exists();
    }

    public void newFolder(String newFolderName, String... parentNodes)
        throws RemoteException {
        precondition();
        String[] folderNodes = new String[parentNodes.length];
        for (int i = 0; i < parentNodes.length; i++) {
            folderNodes[i] = parentNodes[i];
        }
        folderNodes[folderNodes.length - 1] = newFolderName;
        if (!isFolderExist(folderNodes)) {
            try {
                viewPart.selectTreeItemWithLabelsInView(VIEWNAME, parentNodes);
                menuPart.clickMenuWithTexts(FILE, NEW, FOLDER);
                confirmWindowNewFolder(newFolderName);
            } catch (WidgetNotFoundException e) {
                final String cause = "Error creating new folder";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
        }
    }

    public boolean isFolderExist(String... folderNodes) throws RemoteException {
        IPath path = new Path(getPath(folderNodes));
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            return false;
        return true;
    }

    public void waitUntilFolderExist(String... folderNodes)
        throws RemoteException {
        String fullPath = getPath(folderNodes);
        waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void newPackage(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches("[\\w\\.]*\\w+")) {
            if (!isPkgExist(projectName, pkg))
                try {
                    workbenchC.activateEclipseShell();
                    menuPart.clickMenuWithTexts(FILE, NEW, PACKAGE);
                    confirmWindowNewJavaPackage(projectName, pkg);
                } catch (WidgetNotFoundException e) {
                    final String cause = "error creating new package";
                    log.error(cause, e);
                    throw new RemoteException(cause, e);
                }
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public boolean isPkgExist(String projectName, String pkg)
        throws RemoteException {
        IPath path = new Path(getPkgPath(projectName, pkg));
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource != null)
            return true;
        return false;
    }

    public void waitUntilPkgExist(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches("[\\w\\.]*\\w+")) {
            waitUntil(SarosConditions.isResourceExist(getPkgPath(projectName,
                pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void waitUntilPkgNotExist(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches("[\\w\\.]*\\w+")) {
            waitUntil(SarosConditions.isResourceNotExist(getPkgPath(
                projectName, pkg)));
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void newFile(String... fileNodes) throws RemoteException {
        if (!isFileExist(getPath(fileNodes)))
            try {
                precondition();
                String[] parentNodes = new String[fileNodes.length - 1];
                String newFileName = "";
                for (int i = 0; i < fileNodes.length; i++) {
                    if (i == fileNodes.length - 1)
                        newFileName = fileNodes[i];
                    else
                        parentNodes[i] = fileNodes[i];
                }
                viewPart.selectTreeItemWithLabelsInView(VIEWNAME, parentNodes);
                menuPart.clickMenuWithTexts(FILE, NEW, FILE);
                confirmWindowNewFile(newFileName);
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new file.";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    public boolean isFileExist(String filePath) throws RemoteException {
        IPath path = new Path(filePath);
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);
        return file.exists();
    }

    public boolean isFileExist(String... nodes) throws RemoteException {
        return isFileExist(getPath(nodes));
    }

    public boolean isClassExist(String projectName, String pkg, String className)
        throws RemoteException {
        return isFileExist(getClassPath(projectName, pkg, className));
    }

    public boolean isFileExistWithGUI(String... nodes) throws RemoteException {
        workbenchC.activateEclipseShell();
        precondition();
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        return treePart.isTreeItemWithMatchTextExist(tree, nodes);
    }

    public void waitUntilFileExist(String... fileNodes) throws RemoteException {
        String fullPath = getPath(fileNodes);
        waitUntil(SarosConditions.isResourceExist(fullPath));
    }

    public void newClass(String projectName, String pkg, String className)
        throws RemoteException {
        if (!isFileExist(getClassPath(projectName, pkg, className))) {
            try {
                workbenchC.activateEclipseShell();
                menuPart.clickMenuWithTexts(FILE, NEW, CLASS);
                confirmWindowNewJavaClass(projectName, pkg, className);
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new Java Class";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
        }
    }

    public void waitUntilClassExist(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        waitUntil(SarosConditions.isResourceExist(path));
    }

    public void waitUntilClassNotExist(String projectName, String pkg,
        String className) throws RemoteException {
        String path = getClassPath(projectName, pkg, className);
        waitUntil(SarosConditions.isResourceNotExist(path));
    }

    public void newClassImplementsRunnable(String projectName, String pkg,
        String className) throws RemoteException {
        if (!isFileExist(getClassPath(projectName, pkg, className))) {
            precondition();
            menuPart.clickMenuWithTexts(FILE, NEW, CLASS);
        }
        SWTBotShell shell = bot.shell(SHELL_NEW_JAVA_CLASS);
        shell.activate();
        bot.textWithLabel("Source folder:").setText(projectName + "/src");
        bot.textWithLabel("Package:").setText(pkg);
        bot.textWithLabel("Name:").setText(className);
        bot.button("Add...").click();
        windowPart.waitUntilShellActive("Implemented Interfaces Selection");
        bot.shell("Implemented Interfaces Selection").activate();
        SWTBotText text = bot.textWithLabel("Choose interfaces:");
        bot.sleep(2000);
        text.setText("java.lang.Runnable");
        tablePart.waitUntilTableHasRows(1);
        bot.button(OK).click();
        bot.shell(SHELL_NEW_JAVA_CLASS).activate();
        bot.checkBox("Inherited abstract methods").click();
        bot.button(FINISH).click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    public void newJavaProjectWithClass(String projectName, String pkg,
        String className) throws RemoteException {
        newJavaProject(projectName);
        newClass(projectName, pkg, className);
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Open"
     * 
     **********************************************/

    public void openFile(String... fileNodes) throws RemoteException {
        precondition();
        viewPart.clickContextMenuOfTreeItemInView(VIEWNAME, OPEN, fileNodes);
    }

    public void openClass(String projectName, String pkg, String className)
        throws RemoteException {
        String[] classNodes = getClassNodes(projectName, pkg, className);
        openFile(helperPart.changeToRegex(classNodes));
    }

    public void openClassWith(String whichEditor, String projectName,
        String pkg, String className) throws RemoteException {
        openFileWith(whichEditor, getClassNodes(projectName, pkg, className));
    }

    public void openFileWith(String whichEditor, String... fileNodes)
        throws RemoteException {
        precondition();
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        tree.expandNode(fileNodes).select();
        ContextMenuHelper.clickContextMenu(tree, OPEN_WITH, OTHER);
        windowPart.waitUntilShellActive(SHELL_EDITOR_SELECTION);
        SWTBotTable table = bot.table();
        table.select(whichEditor);
        basicPart.waitUntilButtonIsEnabled(OK);
        windowPart.confirmWindow(SHELL_EDITOR_SELECTION, OK);
    }

    public void openClassWithSystemEditor(String projectName, String pkg,
        String className) throws RemoteException {
        IPath path = new Path(getClassPath(projectName, pkg, className));
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        Program.launch(resource.getLocation().toString());
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Delete"
     * 
     **********************************************/

    public void deleteProject(String projectName) throws RemoteException {
        IPath path = new Path(projectName);
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource == null) {
            log.debug("File " + projectName + " not found for deletion");
            return;
        }
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete file " + projectName, e);
            }
        }
    }

    public void deleteAllProjectsWithGUI() throws RemoteException {
        precondition();
        SWTBotTreeItem[] allTreeItems = viewPart.getView(VIEWNAME).bot().tree()
            .getAllItems();
        if (allTreeItems != null) {
            for (SWTBotTreeItem item : allTreeItems) {
                item.contextMenu(DELETE).click();
                windowPart.confirmWindowWithCheckBox(SHELL_DELETE_RESOURCE, OK,
                    true);
                windowPart.waitUntilShellClosed(SHELL_DELETE_RESOURCE);
            }
        }
    }

    public void deleteProjectWithGUI(String projectName) throws RemoteException {
        precondition();
        viewPart
            .clickContextMenuOfTreeItemInView(VIEWNAME, DELETE, projectName);
        windowPart.confirmWindowWithCheckBox(SHELL_DELETE_RESOURCE, OK, true);
        windowPart.waitUntilShellClosed(SHELL_DELETE_RESOURCE);
    }

    public void deleteFolder(String... folderNodes) throws RemoteException {
        String folderpath = getPath(folderNodes);
        IPath path = new Path(getPath(folderNodes));
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete folder " + folderpath, e);
            }
        }
    }

    public void deletePkg(String projectName, String pkg)
        throws RemoteException {
        if (pkg.matches("[\\w\\.]*\\w+")) {
            IPath path = new Path(getPkgPath(projectName, pkg));
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
                .getRoot();
            IResource resource = root.findMember(path);
            if (resource.isAccessible()) {
                try {
                    FileUtil.delete(resource);
                    root.refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (CoreException e) {
                    log.debug("Couldn't delete file " + projectName, e);
                }
            }
        } else {
            throw new RuntimeException(
                "The passed parameter \"pkg\" isn't valid, the package name should corresponds to the pattern [\\w\\.]*\\w+ e.g. PKG1.PKG2.PKG3");
        }
    }

    public void deleteFile(String... nodes) throws RemoteException {
        precondition();
        viewPart.clickContextMenuOfTreeItemInView(VIEWNAME, DELETE, nodes);
        windowPart.confirmDeleteWindow(OK);
    }

    public void deleteClass(String projectName, String pkg, String className)
        throws RemoteException {
        IPath path = new Path(getClassPath(projectName, pkg, className));
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);

            } catch (CoreException e) {
                log.debug("Couldn't delete file " + className + ".java", e);
            }
        }
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Refactor"
     * 
     **********************************************/

    public void moveClassTo(String sourceProject, String sourcePkg,
        String className, String targetProject, String targetPkg)
        throws RemoteException {
        precondition();
        String[] nodes = getClassNodes(sourceProject, sourcePkg, className);
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            helperPart.changeToRegex(nodes), REFACTOR, MOVE);
        windowPart.waitUntilShellActive(SHELL_MOVE);
        windowPart.confirmWindowWithTree(SHELL_MOVE, OK, targetProject, SRC,
            targetPkg);
        windowPart.waitUntilShellClosed(SHELL_MOVE);
    }

    public void rename(String shellTitle, String confirmLabel, String newName,
        String[] nodes) throws RemoteException {
        precondition();
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            helperPart.changeToRegex(nodes), REFACTOR, RENAME);
        windowPart.activateShellWithText(shellTitle);
        bot.textWithLabel(LABEL_NEW_NAME).setText(newName);
        basicPart.waitUntilButtonIsEnabled(confirmLabel);
        bot.button(confirmLabel).click();
        windowPart.waitUntilShellClosed(shellTitle);
    }

    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException {
        String[] nodes = getClassNodes(projectName, pkg, className);
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            helperPart.changeToRegex(nodes), REFACTOR, RENAME);
        String shellTitle = SHELL_RENAME_COMPiIATION_UNIT;
        windowPart.activateShellWithText(shellTitle);
        bot.textWithLabel(LABEL_NEW_NAME).setText(newName);
        basicPart.waitUntilButtonIsEnabled(FINISH);
        bot.button(FINISH).click();
        /*
         * TODO Sometimes the window doesn't close when clicking on Finish, but
         * stays open with the warning 'class contains a main method blabla'. In
         * this case just click Finish again.
         */
        bot.sleep(50);
        if (windowPart.isShellOpen(SHELL_RENAME_COMPiIATION_UNIT)) {
            bot.button(FINISH).click();
        }
        windowPart.waitUntilShellClosed(shellTitle);
    }

    public void renameFile(String newName, String... nodes)
        throws RemoteException {
        rename(SHELL_RENAME_RESOURCE, OK, newName, nodes);
    }

    public void renameFolder(String newName, String... nodes)
        throws RemoteException {
        rename(SHELL_RENAME_RESOURCE, OK, newName, nodes);
    }

    public void renameJavaProject(String newName, String... nodes)
        throws RemoteException {
        rename("Rename Java Project", OK, newName, nodes);
    }

    public void renamePkg(String newName, String projectName, String pkg)
        throws RemoteException {
        String[] pkgNodes = getPkgNodes(projectName, pkg);
        rename(SHELL_RENAME_PACKAGE, OK, newName, pkgNodes);
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "SVN"
     * 
     **********************************************/
    public void shareProjectWithSVN(String projectName, String repositoryURL)
        throws RemoteException {
        String[] matchTexts = { projectName + ".*" };
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            matchTexts, TEAM, SHARE_PROJECT);
        windowPart.confirmWindowWithTable(SHELL_SHARE_PROJECT,
            REPOSITORY_TYPE_SVN, NEXT);
        log.debug("SVN share project text: " + bot.text());
        if (bot.table().containsItem(repositoryURL)) {
            windowPart.confirmWindowWithTable(SHELL_SHARE_PROJECT,
                repositoryURL, NEXT);
        } else {
            bot.radio(LABEL_CREATE_A_NEW_REPOSITORY_LOCATION).click();
            bot.button(NEXT).click();
            bot.comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
        }
        basicPart.waitUntilButtonIsEnabled(FINISH);
        bot.button(FINISH).click();
        windowPart.waitUntilShellClosed(SHELL_SHARE_PROJECT);
    }

    public void shareProjectWithSVNWhichIsConfiguredWithSVNInfos(
        String projectName, String repositoryURL) throws RemoteException {
        String[] matchTexts = { projectName + ".*" };
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            matchTexts, TEAM, SHARE_PROJECT);
        windowPart.confirmWindowWithTable(SHELL_SHARE_PROJECT,
            REPOSITORY_TYPE_SVN, NEXT);
        log.debug("SVN share project text: " + bot.text());
        basicPart.waitUntilButtonIsEnabled(FINISH);
        bot.button(FINISH).click();
        windowPart.waitUntilShellClosed(SHELL_SHARE_PROJECT);
    }

    public void shareProjectWithSVNUsingSpecifiedFolderName(String projectName,
        String repositoryURL, String specifiedFolderName)
        throws RemoteException {
        precondition();
        String[] matchTexts = { projectName + ".*" };
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            matchTexts, TEAM, SHARE_PROJECT);

        windowPart.confirmWindowWithTable(SHELL_SHARE_PROJECT,
            REPOSITORY_TYPE_SVN, NEXT);

        SWTBotTable table = null;
        final SWTBotShell shareProjectShell = bot.shell(SHELL_SHARE_PROJECT);
        try {
            table = shareProjectShell.bot().table();
        } catch (WidgetNotFoundException e) {
            //
        }

        if (table == null || !table.containsItem(repositoryURL)) {
            // close window
            shareProjectShell.close();
            // in svn repos view: enter url
            viewPart
                .openViewById("org.tigris.subversion.subclipse.ui.repository.RepositoriesView");
            viewPart.setFocusOnViewByTitle("SVN Repositories");
            final boolean viewWasOpen = viewPart.isViewOpen("SVN Repositories");
            final SWTBotView repoView = viewPart.getView("SVN Repositories");
            repoView.toolbarButton("Add SVN Repository").click();
            if (!windowPart.activateShellWithText("Add SVN Repository")) {
                windowPart.waitUntilShellActive("Add SVN Repository");
            }
            bot.comboBoxWithLabel(LABEL_URL).setText(repositoryURL);
            bot.button(FINISH).click();
            windowPart.waitUntilShellClosed("Add SVN Repository");
            if (!viewWasOpen)
                repoView.close();
            // recur...
            shareProjectWithSVNUsingSpecifiedFolderName(projectName,
                repositoryURL, specifiedFolderName);
            return;
        }

        windowPart.confirmWindowWithTable(SHELL_SHARE_PROJECT, repositoryURL,
            NEXT);

        bot.radio("Use specified folder name:").click();
        bot.text().setText(specifiedFolderName);
        bot.button(FINISH).click();
        windowPart.waitUntilShellActive("Remote Project Exists");
        windowPart.confirmWindow("Remote Project Exists", YES);
        bot.sleep(500);
        if (windowPart.isShellOpen("Confirm Open Perspective"))
            windowPart.confirmWindow("Confirm Open Perspective", NO);
        else
            windowPart.waitUntilShellClosed(SHELL_SHARE_PROJECT);
    }

    public void importProjectFromSVN(String repositoryURL)
        throws RemoteException {
        precondition();
        menuPart.clickMenuWithTexts("File", "Import...");
        windowPart.confirmWindowWithTreeWithFilterText(SHELL_IMPORT,
            REPOSITORY_TYPE_SVN, "Checkout Projects from SVN", NEXT);
        if (bot.table().containsItem(repositoryURL)) {
            windowPart.confirmWindowWithTable("Checkout from SVN",
                repositoryURL, NEXT);
        } else {
            bot.radio("Create a new repository location").click();
            bot.button(NEXT).click();
            bot.comboBoxWithLabel("Url:").setText(repositoryURL);
            bot.button(NEXT).click();
            windowPart.waitUntilShellActive("Checkout from SVN");
        }
        windowPart.confirmWindowWithTreeWithWaitingExpand("Checkout from SVN",
            FINISH, repositoryURL, "trunk", "examples");
        windowPart.waitUntilShellActive("SVN Checkout");

        SWTBotShell shell2 = bot.shell("SVN Checkout");
        windowPart.waitUntilShellCloses(shell2);
    }

    public void disConnect(String projectName) throws RemoteException {
        String[] matchTexts = { projectName + ".*" };
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            matchTexts, TEAM, DISCONNECT);
        windowPart.confirmWindow(SHELL_CONFIRM_DISCONNECT_FROM_SVN, YES);
    }

    public void revertProject(String projectName) throws RemoteException {
        precondition();
        String[] matchTexts = { projectName + ".*" };
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            matchTexts, TEAM, REVERT);
        windowPart.confirmWindow(SHELL_REVERT, OK);
        windowPart.waitUntilShellClosed(SHELL_REVERT);
    }

    public void updateProject(String projectName, String versionID)
        throws RemoteException {
        String[] nodes = { projectName + ".*" };
        switchToAnotherRevision(nodes, versionID);
    }

    public void updateClass(String projectName, String pkg, String className,
        String revision) throws RemoteException {
        String[] nodes = getClassNodes(projectName, pkg, className);
        nodes = helperPart.changeToRegex(nodes);
        switchToAnotherRevision(nodes, revision);
    }

    public void switchProjectWithGui(String projectName, String url)
        throws RemoteException {
        precondition();
        String[] matchTexts = { projectName + ".*" };
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            matchTexts, TEAM, SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION);
        windowPart.waitUntilShellActive(SHELL_SWITCH);
        bot.comboBoxWithLabel(LABEL_TO_URL).setText(url);
        bot.button(OK).click();
        windowPart.waitUntilShellClosed(SHELL_SVN_SWITCH);
    }

    public void switchProject(String projectName, String url)
        throws RemoteException {
        switchResource(projectName, url, "HEAD");
    }

    public void switchResource(String fullPath, String url)
        throws RemoteException {
        switchResource(fullPath, url, "HEAD");
    }

    public void switchResource(String fullPath, String url, String revision)
        throws RemoteException {
        precondition();
        final IPath path = new Path(fullPath);
        final IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            throw new RemoteException("Resource \"" + path + "\" not found.");

        final IProject project = resource.getProject();
        VCSAdapter vcs = VCSAdapter.getAdapter(project);
        if (vcs == null) {
            throw new RemoteException("No VCSAdapter found for \""
                + project.getName() + "\".");
        }

        vcs.switch_(resource, url, revision, new NullProgressMonitor());
    }

    public void waitUntilWindowSarosRunningVCSOperationClosed()
        throws RemoteException {
        windowPart.waitUntilShellClosed(SHELL_SAROS_RUNNING_VCS_OPERATION);
    }

    public boolean isProjectManagedBySVN(String projectName)
        throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        final VCSAdapter vcs = VCSAdapter.getAdapter(project);
        if (vcs == null)
            return false;
        return true;
    }

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException {
        waitUntil(SarosConditions.isInSVN(projectName));
    }

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException {
        waitUntil(SarosConditions.isNotInSVN(projectName));
    }

    public String getRevision(String fullPath) throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            throw new RemoteException("Resource \"" + fullPath
                + "\" not found.");
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        VCSResourceInfo info = vcs.getResourceInfo(resource);
        String result = info != null ? info.revision : null;
        return result;
    }

    public void waitUntilRevisionIsSame(String fullPath, String revision)
        throws RemoteException {
        waitUntil(SarosConditions.isRevisionSame(fullPath, revision));
    }

    public void waitUntilUrlIsSame(String fullPath, String url)
        throws RemoteException {
        waitUntil(SarosConditions.isUrlSame(fullPath, url));
    }

    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            throw new RemoteException("Resource not found at \"" + fullPath
                + "\"");
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        final VCSResourceInfo info = vcs.getResourceInfo(resource);
        return info.url;
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    protected void precondition() throws RemoteException {
        openPEView();
        setFocusOnPEView();
    }

    private void confirmWindowNewJavaProject(String projectName) {
        SWTBotShell shell = bot.shell(SHELL_NEW_JAVA_PROJECT);
        shell.activate();
        bot.textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
        bot.button(FINISH).click();
        windowPart.waitUntilShellClosed(SHELL_NEW_JAVA_PROJECT);
    }

    private void confirmWindowNewFolder(String newFolderName) {
        SWTBotShell shell = bot.shell(SHELL_NEW_FOLDER);
        shell.activate();
        bot.textWithLabel(LABEL_FOLDER_NAME).setText(newFolderName);
        bot.button(FINISH).click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    private void confirmWindowNewFile(String newFileName) {
        SWTBotShell shell = bot.shell(SHELL_NEW_FILE);
        shell.activate();
        bot.textWithLabel(LABEL_FILE_NAME).setText(newFileName);
        basicPart.waitUntilButtonIsEnabled(FINISH);
        bot.button(FINISH).click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    private void confirmWindowNewJavaPackage(String projectName, String pkg) {
        SWTBotShell shell = bot.shell(SHELL_NEW_JAVA_PACKAGE);
        shell.activate();
        bot.textWithLabel("Source folder:").setText((projectName + "/src"));
        bot.textWithLabel("Name:").setText(pkg);
        bot.button(FINISH).click();
        windowPart.waitUntilShellClosed(SHELL_NEW_JAVA_PACKAGE);
    }

    private void confirmWindowNewJavaClass(String projectName, String pkg,
        String className) {
        SWTBotShell shell = bot.shell(SHELL_NEW_JAVA_CLASS);
        shell.activate();
        bot.textWithLabel("Source folder:").setText(projectName + "/src");
        bot.textWithLabel("Package:").setText(pkg);
        bot.textWithLabel("Name:").setText(className);
        bot.button(FINISH).click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    private void confirmWizardNewProject(String projectName) {
        windowPart.confirmWindowWithTree(SHELL_NEW_PROJECT, NEXT,
            CATEGORY_GENERAL, NODE_PROJECT);
        bot.textWithLabel(LABEL_PROJECT_NAME).setText(projectName);
        bot.button(FINISH).click();
        windowPart.waitUntilShellClosed(SHELL_NEW_PROJECT);
        bot.sleep(50);
    }

    private void switchToAnotherRevision(String[] matchTexts, String versionID)
        throws RemoteException {
        precondition();
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            matchTexts, TEAM, SWITCH_TO_ANOTHER_BRANCH_TAG_REVISION);
        windowPart.waitUntilShellActive(SHELL_SWITCH);
        if (bot.checkBox(LABEL_SWITCH_TOHEAD_REVISION).isChecked())
            bot.checkBox(LABEL_SWITCH_TOHEAD_REVISION).click();
        bot.textWithLabel(LABEL_REVISION).setText(versionID);
        bot.button(OK).click();
        if (windowPart.isShellOpen(SHELL_SVN_SWITCH))
            windowPart.waitUntilShellClosed(SHELL_SVN_SWITCH);
    }

    public void copyProject(String target, String source)
        throws RemoteException {

        if (isProjectExist(target)) {
            throw new RemoteException("Can't copy project from " + source
                + " to " + target + " , the target already exists.");
        }
        precondition();
        String[] matchTexts = helperPart.changeToRegex(source);
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            matchTexts, "Copy");
        viewPart.clickSubmenusOfContextMenuOfTreeItemInView(VIEWNAME,
            matchTexts, "Paste");
        windowPart.activateShellWithText("Copy Project");
        bot.textWithLabel("Project name:").setText(target);
        bot.button(OK).click();
        windowPart.waitUntilShellClosed("Copy Project");
        bot.sleep(1000);
    }

}
