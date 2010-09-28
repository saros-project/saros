package de.fu_berlin.inf.dpp.stf.swtbot;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.tableHasRows;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.stf.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.TableObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.ToolbarObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.TreeObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.WaitUntilObject;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

/**
 * RmiSWTWorkbenchBot delegates to {@link SWTWorkbenchBot} to implement an
 * java.rmi interface for {@link SWTWorkbenchBot}.
 */
public class RmiSWTWorkbenchBot implements IRmiSWTWorkbenchBot {
    private static final transient Logger log = Logger
        .getLogger(RmiSWTWorkbenchBot.class);

    private static final boolean SCREENSHOTS = true;

    public static transient SarosSWTWorkbenchBot delegate;

    private static transient RmiSWTWorkbenchBot self;

    /** The RMI registry used, is not exported */
    protected static transient Registry registry;

    /** RMI exported remote usable SWTWorkbenchBot replacement */
    public IRmiSWTWorkbenchBot stub;

    protected transient String myName;

    public int sleepTime = 750;

    public WaitUntilObject wUntilObject = new WaitUntilObject(this);
    public TableObject tableObject = new TableObject(this);
    public ToolbarObject tBarObject = new ToolbarObject(this);
    public TreeObject treeObject = new TreeObject(this);

    /** RmiSWTWorkbenchBot is a singleton */
    public static RmiSWTWorkbenchBot getInstance() {
        if (delegate != null && self != null)
            return self;

        self = new RmiSWTWorkbenchBot();
        return self;
    }

    protected RmiSWTWorkbenchBot() {
        this(new SarosSWTWorkbenchBot());
    }

    /** RmiSWTWorkbenchBot is a singleton, but inheritance is possible */
    protected RmiSWTWorkbenchBot(SarosSWTWorkbenchBot bot) {
        super();
        assert bot != null : "delegated SWTWorkbenchBot is null";
        delegate = bot;
    }

    /*************** RMI Methods ******************/

    /**
     * Add a shutdown hook to unbind exported Object from registry.
     */
    protected void addShutdownHook(final String name) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    if (registry != null && name != null)
                        registry.unbind(name);
                } catch (RemoteException e) {
                    log.warn("Failed to unbind: " + name, e);
                } catch (NotBoundException e) {
                    log.warn("Failed to unbind: " + name, e);
                }
            }
        });
    }

    public void init(String exportName, int port) throws RemoteException {
        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            registry = LocateRegistry.getRegistry(port);
            myName = exportName;

        }
        stub = (IRmiSWTWorkbenchBot) UnicastRemoteObject.exportObject(this, 0);

        addShutdownHook(exportName);
        try {
            registry.bind(exportName, stub);
        } catch (AlreadyBoundException e) {
            log.debug("Object with name " + exportName + " was already bound.",
                e);
        } catch (Exception e) {
            log.debug("bind failed: ", e);
        }
    }

    public void listRmiObjects() {
        try {
            for (String s : registry.list())
                log.debug("registered Object: " + s);
        } catch (AccessException e) {
            log.error("Failed on access", e);
        } catch (RemoteException e) {
            log.error("Failed", e);
        }

    }

    /*******************************************************************************
     * 
     * Package Explorer View Page
     * 
     *******************************************************************************/
    public void activatePackageExplorerView() throws RemoteException {
        activateViewWithTitle(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
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
        SWTBotTree tree = treeObject
            .getTreeInView(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
        tree.select(projectName);
        clickMenuWithTexts("Edit", "Delete");
        confirmWindowWithCheckBox(SarosConstant.SHELL_TITLE_DELETE_RESOURCE,
            SarosConstant.BUTTON_OK, true);
        waitUntilShellCloses(SarosConstant.SHELL_TITLE_DELETE_RESOURCE);
    }

    /**
     * Delete the project using FileUntil.delete(resource). This delete-method
     * costs less time than the previous version.
     * 
     * @param projectName
     *            name of the project, which you want to delete.
     */
    public void deleteProject(String projectName) throws RemoteException {
        IPath path = new Path(projectName);
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete file " + projectName, e);
            }
        }
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
        delegate.tree().expandNode(nodes);
        treeObject.selectTreeWithLabelsInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, nodes);
        clickMenuWithTexts(SarosConstant.MENU_TITLE_EDIT,
            SarosConstant.MENU_TITLE_DELETE);
        waitUntilShellActive(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
        confirmWindow(SarosConstant.SHELL_TITLE_CONFIRM_DELETE,
            SarosConstant.BUTTON_OK);

    }

    /**
     * Delete a package of the specified java project using
     * FileUntil.delete(resource).
     * 
     * @param projectName
     *            name of the project, which package you want to delete.
     * @param pkg
     *            name of the package, which you want to delete.
     */
    public void deletePkg(String projectName, String pkg)
        throws RemoteException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/"));
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        if (resource.isAccessible()) {
            try {
                FileUtil.delete(resource);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete file " + projectName, e);
            }
        }
    }

    /**
     * Delete a class of the specified java project using
     * FileUntil.delete(resource).
     * 
     * @param projectName
     *            name of the project, which class you want to delete.
     * @param pkg
     *            name of the package, which class you want to delete.
     * @param className
     *            name of the class, which you want to delete.
     */
    public void deleteClass(String projectName, String pkg, String className)
        throws RemoteException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
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
        if (!isClassOpen(className)) {
            openFileInView(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
                projectName, "src", packageName, className + ".java");
            delegate.sleep(sleepTime);
        }
    }

    /**
     * This method is only a helper method for "openClass". Later you can define
     * e.g. openXml, openText, openTHML using it. Make sure, the path is
     * completely defined, e.g. in openClass you should pass parameter "nodes"
     * such as Foo_Saros, src, my.pkg, MyClass.java to the method.
     * 
     * @param viewName
     *            e.g. Package Explorer view or Resource Explorer view
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    protected void openFileInView(String viewName, String... nodes)
        throws RemoteException {
        treeObject.clickContextMenuOfTreeInView(viewName,
            SarosConstant.CONTEXT_MENU_OPEN, nodes);
    }

    public void moveClassTo(String projectName, String pkg, String className,
        String targetProject, String targetPkg) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = changeToRegex(projectName, "src", pkg, className);
        log.info("matchTexts: " + matchTexts);
        clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Refactor",
            "Move...");
        waitUntilShellActive("Move");
        confirmWindowWithTree("Move", SarosConstant.BUTTON_OK, targetProject,
            "src", targetPkg);

    }

    public void disConnectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Disconnect...");
        confirmWindow("Confirm Disconnect from SVN", SarosConstant.BUTTON_YES);
    }

    public void connectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Share Project...");
        confirmWindowWithTable("Share Project", "SVN",
            SarosConstant.BUTTON_NEXT);
        clickButton(SarosConstant.BUTTON_FINISH);
    }

    public void switchToOtherRevision() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        waitUntilShellActive("Switch");
        clickCheckBox("Switch to HEAD revision");
        setTextWithLabel("Revision:", "115");
        clickButton(SarosConstant.BUTTON_OK);
        waitUntilShellCloses("SVN Switch");
    }

    public void switchToOtherRevision(String CLS_PATH) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();

        String[] matchTexts = CLS_PATH.split("/");
        for (int i = 0; i < matchTexts.length; i++) {
            matchTexts[i] = matchTexts[i] + ".*";
        }
        // String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        waitUntilShellActive("Switch");
        clickCheckBox("Switch to HEAD revision");
        setTextWithLabel("Revision:", "116");
        clickButton(SarosConstant.BUTTON_OK);
        waitUntilShellCloses("SVN Switch");
    }

    public void revert() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Revert...");
        confirmWindow("Revert", SarosConstant.BUTTON_OK);
        waitUntilShellCloses("Revert");
    }

    public void renameFile(String newName, String... texts)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = changeToRegex(texts);
        clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Refactor",
            "Rename...");
        activateShellWithText("Rename Compilation Unit");
        setTextWithLabel("New name:", newName);
        waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        clickButton(SarosConstant.BUTTON_FINISH);
    }

    public void renamePkg(String newName, String... texts)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = changeToRegex(texts);
        clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Refactor",
            "Rename...");
        activateShellWithText("Rename Package");
        setTextWithLabel("New name:", newName);
        waitUntilButtonEnabled(SarosConstant.BUTTON_OK);
        clickButton(SarosConstant.BUTTON_OK);
        waitUntilShellCloses("Rename Package");
    }

    public void switchToTag() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        waitUntilShellActive("Switch");
        clickButton("Select...");
        confirmWindowWithTree("Repository Browser", SarosConstant.BUTTON_OK,
            "tags", "eclipsecon2009");
        clickButton(SarosConstant.BUTTON_OK);
        waitUntilShellCloses("SVN Switch");
    }

    public void importProjectFromSVN(String path) throws RemoteException {
        activateEclipseShell();
        clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
            SarosConstant.MENU_TITLE_IMPORT);
        confirmWindowWithTreeWithFilterText(SarosConstant.SHELL_TITLE_IMPORT,
            "SVN", "Checkout Projects from SVN", SarosConstant.BUTTON_NEXT);
        if (delegate.table().containsItem(path)) {
            confirmWindowWithTable("Checkout from SVN",
                BotConfiguration.SVN_URL, SarosConstant.BUTTON_NEXT);
        } else {
            clickRadio("Create a new repository location");
            clickButton(SarosConstant.BUTTON_NEXT);
            delegate.comboBoxWithLabel("Url:").setText(path);
            clickButton(SarosConstant.BUTTON_NEXT);
            waitUntilShellActive("Checkout from SVN");
        }
        confirmWindowWithTree("Checkout from SVN", SarosConstant.BUTTON_FINISH,
            path, "trunk", "examples");
        waitUntilShellActive("SVN Checkout");
        SWTBotShell shell2 = delegate.shell("SVN Checkout");
        waitUntilShellCloses(shell2);
    }

    /*******************************************************************************
     * 
     * main menu page
     * 
     *******************************************************************************/

    /**
     * Create a java project and a class in the project. The combination with
     * function newJavaProject and newClass is used very often, so i put them
     * together to simplify the junit-tests.
     * 
     * Attention: after creating a project delegate need to sleep a moment until
     * he is allowed to create class. so if you want to create a project with a
     * class, please use this mothde, otherwise you should get
     * WidgetNotfoundException.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class, e.g. MyClass
     */
    public void newJavaProjectWithClass(String projectName, String pkg,
        String className) throws RemoteException {
        newJavaProject(projectName);
        delegate.sleep(50);
        newClass(projectName, pkg, className);
    }

    /**
     * Create a java project. The name of the method is defined the same as the
     * menu names. The name "newJavaProject" then means: hello guys, please
     * click main menus File -> New -> JavaProject.
     * 
     * 1. if the java project already exist, return.
     * 
     * 2. activate the saros-instance-window(alice / bob / carl). If the
     * workbench isn't active, delegate can't find the main menus.
     * 
     * 3. click main menus File -> New -> JavaProject.
     * 
     * 4. confirm the pop-up window "New Java Project"
     * 
     * 5. delegate wait so long until the pop-up window is closed.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * 
     */
    public void newJavaProject(String projectName) throws RemoteException {
        if (!isJavaProjectExist(projectName)) {
            activateEclipseShell();
            delegate.menu("File").menu("New").menu("Java Project").click();
            SWTBotShell shell = delegate.shell("New Java Project");
            shell.activate();
            delegate.textWithLabel("Project name:").setText(projectName);
            delegate.button("Finish").click();
            delegate.waitUntil(Conditions.shellCloses(shell));
            delegate.sleep(50);
            // // TODO version without timeout
            // final String baseName = projectName + "_base";
            // if (!isJavaProjectExist(projectName)) {
            // activateEclipseShell();
            // try {
            // // New Java Project
            // clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
            // SarosConstant.MENU_TITLE_NEW,
            // SarosConstant.MENU_TITLE_JAVA_PROJECT);
            // } catch (WidgetNotFoundException e) {
            // // New Project...
            // clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
            // SarosConstant.MENU_TITLE_NEW,
            // SarosConstant.MENU_TITLE_PROJECT);
            // // Java Project
            // confirmWindowWithTreeWithFilterText(
            // SarosConstant.SHELL_TITLE_NEW_PROJECT,
            // SarosConstant.CATEGORY_JAVA,
            // SarosConstant.NODE_JAVA_PROJECT,
            // SarosConstant.BUTTON_NEXT);
            // }
            //
            // waitUntilShellActive("New Java Project");
            // final SWTBotShell newProjectDialog = delegate.activeShell();
            //
            // setTextWithLabel("Project name:", projectName);
            // clickButton(SarosConstant.BUTTON_FINISH);
            // waitUntilShellCloses(newProjectDialog);
            //
            // if (isShellActive("Open Associated Perspective?")) {
            // clickButton(SarosConstant.BUTTON_YES);
            // waitUntilShellCloses("Open Associated Perspective?");
            // }
            // }
            // // final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
            // // .getRoot();
            // // IProject project = root.getProject(baseName);
            // // try {
            // // project.copy(new Path(projectName), true, null);
            // // root.refreshLocal(IResource.DEPTH_INFINITE, null);
            // // } catch (CoreException e) {
            // // log.debug("Couldn't copy project " + baseName, e);
            // // }
            // delegate.sleep(100);
        }
    }

    /**
     * Create a new package. The name of the method is defined the same as the
     * menu names. The name "newPackage" then means: hello guys, please click
     * main menus File -> New -> Package.
     * 
     * 1. if the package already exist, return.
     * 
     * 2. activate the saros-instance-window(alice / bob / carl). If the
     * workbench isn't active, delegate can't find the main menus.
     * 
     * 3. click main menus File -> New -> Package.
     * 
     * 4. confirm the pop-up window "New Java Package"
     * 
     * 5. delegate wait so long until the pop-up window is closed.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * 
     */
    public void newPackage(String projectName, String pkg)
        throws RemoteException {
        if (!isPkgExist(projectName, pkg))
            try {
                activateEclipseShell();
                clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
                    SarosConstant.MENU_TITLE_NEW, "Package");
                activateShellWithMatchText("New Java Package");
                setTextWithLabel("Source folder:", projectName + "/src");
                setTextWithLabel("Name:", pkg);
                clickButton(SarosConstant.BUTTON_FINISH);
                waitUntilShellCloses("New java Package");
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new package";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    /**
     * Create a new package. The name of the method is defined the same as the
     * menu names. The name "newClass" then means: hello guys, please click main
     * menus File -> New -> Class.
     * 
     * 1. if the package already exist, return.
     * 
     * 2. activate the saros-instance-window(alice / bob / carl). If the
     * workbench isn't active, delegate can't find the main menus.
     * 
     * 3. click main menus File -> New -> Class.
     * 
     * 4. confirm the pop-up window "New Java Class"
     * 
     * 5. delegate wait so long until the pop-up window is closed.
     * 
     * @param projectName
     *            name of the project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg.
     * @param className
     *            name of the class, e.g. myClass.
     * 
     */
    public void newClass(String projectName, String pkg, String className)
        throws RemoteException {
        if (!isClassExist(projectName, pkg, className))
            try {
                activateEclipseShell();
                delegate.menu("File").menu("New").menu("Class").click();
                SWTBotShell shell = delegate.shell("New Java Class");
                shell.activate();
                delegate.textWithLabel("Source folder:").setText(
                    projectName + "/src");
                delegate.textWithLabel("Package:").setText(pkg);
                delegate.textWithLabel("Name:").setText(className);
                delegate.button("Finish").click();
                delegate.waitUntil(Conditions.shellCloses(shell));
                // activateEclipseShell();
                // clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
                // SarosConstant.MENU_TITLE_NEW,
                // SarosConstant.MENU_TITLE_CLASS);
                // waitUntilShellActive(SarosConstant.SHELL_TITLE_NEW_JAVA_CLASS);
                // activateShellWithMatchText(SarosConstant.SHELL_TITLE_NEW_JAVA_CLASS);
                // // FIXME WidgetNotFoundException in TestEditDuringInvitation
                // final SWTBotShell newClassDialog = delegate.activeShell();
                // setTextWithLabel("Source folder:", projectName + "/src");
                // setTextWithLabel("Package:", pkg);
                // setTextWithLabel("Name:", className);
                // // implementsInterface("java.lang.Runnable");
                // // delegate.checkBox("Inherited abstract methods").click();
                // clickCheckBox("Inherited abstract methods");
                // waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
                // clickButton(SarosConstant.BUTTON_FINISH);
                // waitUntilShellCloses(newClassDialog);
                // // openJavaFileWithEditor(projectName, pkg, className +
                // // ".java");
                // // delegate.sleep(sleepTime);
                // // editor.navigateTo(2, 0);
                // // editor.quickfix("Add unimplemented methods");
                // // editor.save();
                // // delegate.sleep(750);
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new Java Class";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    /**
     * Open the view "Package Explorer". The name of the method is defined the
     * same as the menu names. The name "showViewPackageExplorer" then means:
     * hello guy, please click main menus Window -> Show view ->
     * PackageExplorer.
     * 
     */
    public void showViewPackageExplorer() throws RemoteException {
        openViewWithName(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, "Java",
            "Package Explorer");
    }

    /**
     * Open the view "Problems". The name of the method is defined the same as
     * the menu names. The name "showViewProblem" then means: hello guy, please
     * click main menus Window -> Show view -> Problems.
     * 
     */
    public void showViewProblems() throws RemoteException {
        openViewWithName("Problems", "General", "Problems");
    }

    /**
     * Open the view "Project Explorer". The name of the method is defined the
     * same as the menu names. The name "showViewProblem" then means: hello guy,
     * please click main menus Window -> Show view -> Project Explorer.
     * 
     */
    public void showViewProjectExplorer() throws RemoteException {
        openViewWithName("Project Explorer", "General", "Project Explorer");
    }

    /**
     * Open the perspective "Java". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveJava" then means: hello guy,
     * please click main menus Window -> Open perspective -> Java.
     * 
     */
    public void openPerspectiveJava() throws RemoteException {
        openPerspectiveWithName(SarosConstant.PERSPECTIVE_TITLE_JAVA);
    }

    /**
     * Open the perspective "Debug". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveDebug" then means: hello guy,
     * please click main menus Window -> Open perspective -> Debug.
     * 
     */
    public void openPerspectiveDebug() throws RemoteException {
        openPerspectiveWithName(SarosConstant.PERSPECTIVE_TITLE_DEBUG);
    }

    /*******************************************************************************
     * 
     * window page
     * 
     *******************************************************************************/
    public boolean activateShellWithText(String title) throws RemoteException {
        SWTBotShell[] shells = delegate.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().equals(title)) {
                log.debug("shell found");
                if (!shell.isActive()) {
                    shell.activate();
                }
                return true;
            }
        }
        log.error("No shell found matching \"" + title + "\"!");
        return false;
    }

    public boolean activateShellWithMatchText(String matchText)
        throws RemoteException {
        SWTBotShell[] shells = delegate.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().matches(matchText)) {
                log.debug("shell found matching \"" + matchText + "\"");
                if (!shell.isActive()) {
                    shell.activate();
                }
                return shell.isActive();
            }
        }
        final String message = "No shell found matching \"" + matchText + "\"!";
        log.error(message);
        throw new RemoteException(message);
    }

    /**
     * confirm a pop-up window.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @throws RemoteException
     */
    public void confirmWindow(String title, String buttonText)
        throws RemoteException {
        // waitUntilShellActive(title);
        if (activateShellWithText(title)) {
            clickButton(buttonText);
            delegate.sleep(sleepTime);
        }
    }

    /**
     * confirm a pop-up window with a checkbox.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param isChecked
     *            if the checkbox selected or not.
     * @throws RemoteException
     */
    public void confirmWindowWithCheckBox(String title, String buttonText,
        boolean isChecked) throws RemoteException {
        activateShellWithText(title);
        if (isChecked)
            clickCheckBox();
        clickButton(buttonText);
        delegate.sleep(sleepTime);
    }

    /**
     * confirm a pop-up window with more than one checkbox.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param itemNames
     *            the labels of the checkboxs, which you want to select.
     * @throws RemoteException
     */
    public void confirmWindowWithCheckBox(String title, String buttonText,
        String... itemNames) throws RemoteException {
        waitUntilShellActive(title);
        for (String itemName : itemNames) {
            selectCheckBoxWithText(itemName);
        }
        waitUntilButtonEnabled(buttonText);
        clickButton(buttonText);
        // waitUntilShellCloses(shellName);
    }

    /**
     * confirm a pop-up window with a table. You should first select a table
     * item and then confirm with button.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param itemName
     *            the name of the table item, which you want to select.
     * @throws RemoteException
     */
    public void confirmWindowWithTable(String title, String itemName,
        String buttonText) throws RemoteException {
        // waitUntilShellActive(shellName);
        try {
            delegate.table().select(itemName);
            waitUntilButtonEnabled(buttonText);
            clickButton(buttonText);
            // waitUntilShellCloses(shellName);
        } catch (WidgetNotFoundException e) {
            log.error("tableItem" + itemName + "can not be fund!");
        }
    }

    /**
     * confirm a pop-up window with a tree. You should first select a tree node
     * and then confirm with button.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void confirmWindowWithTree(String title, String buttonText,
        String... nodes) throws RemoteException {
        // waitUntilShellActive(shellName);
        SWTBotTree tree = delegate.tree();
        log.info("allItems " + tree.getAllItems().length);
        selectTreeWithLabels(tree, nodes);
        waitUntilButtonEnabled(buttonText);
        clickButton(buttonText);
        // waitUntilShellCloses(shellName);
    }

    /**
     * confirm a pop-up window with a tree using filter text. You should first
     * input a filter text in the text field and then select a tree node,
     * confirm with button.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param teeNode
     *            tree node, which you want to select.
     * @param rootOfTreeNode
     *            root of the tree node.
     * @throws RemoteException
     */
    public void confirmWindowWithTreeWithFilterText(String title,
        String rootOfTreeNode, String teeNode, String buttonText)
        throws RemoteException {
        // waitUntilShellActive(shellName);
        setTextWithoutLabel(SarosConstant.TEXT_FIELD_TYPE_FILTER_TEXT, teeNode);
        waitUntilTreeExisted(delegate.tree(), rootOfTreeNode);
        SWTBotTreeItem treeItem = delegate.tree(0).getTreeItem(rootOfTreeNode);
        waitUntilTreeNodeExisted(treeItem, teeNode);
        treeItem.getNode(teeNode).select();
        waitUntilButtonEnabled(buttonText);
        clickButton(buttonText);
        // waitUntilShellCloses(shellName);
    }

    /*******************************************************************************
     * 
     * exported helper methods
     * 
     *******************************************************************************/

    public void sleep(long millis) throws RemoteException {
        delegate.sleep(millis);
    }

    public void captureScreenshot(String filename) throws RemoteException {
        if (SCREENSHOTS)
            delegate.captureScreenshot(filename);
    }

    public boolean isMenusOfContextMenuOfTreeItemInViewExist(String viewTitle,
        String[] matchTexts, String... contexts) throws RemoteException {
        try {
            SWTBotTree tree = treeObject
                .getViewWithText(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER)
                .bot().tree();
            // you should first select the project,whose context you want to
            // click.
            SWTBotTreeItem treeItem = treeObject.getTreeItemWithMatchText(tree,
                contexts);
            treeItem.select();
            ContextMenuHelper.clickContextMenu(tree, contexts);
        } catch (WidgetNotFoundException e) {
            return false;
        }
        return true;
    }

    public boolean isInSVN() throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(BotConfiguration.PROJECTNAME_SVN);
        final VCSAdapter vcs = VCSAdapter.getAdapter(project);
        if (vcs == null)
            return false;
        return true;
    }

    public boolean isJavaProjectExist(String projectName)
        throws RemoteException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(projectName);
        return project.exists();
    }

    public boolean isResourceExist(String CLS_PATH) throws RemoteException {
        IPath path = new Path(CLS_PATH);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            return false;
        return true;
    }

    public boolean isPkgExist(String projectName, String pkg)
        throws RemoteException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/"));

        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        if (resource == null)
            return false;
        return true;
    }

    public boolean isClassExist(String projectName, String pkg, String className)
        throws RemoteException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);
        return file.exists();
    }

    public boolean isClassExistGUI(String... matchTexts) throws RemoteException {
        activateEclipseShell();
        showViewPackageExplorer();
        activatePackageExplorerView();
        SWTBotTree tree = treeObject
            .getTreeInView(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
        return treeObject.isTreeItemWithMatchTextExist(tree, matchTexts);
    }

    // // FIXME If the file doesn't exist, this method hits the
    // // SWTBotPreferences.TIMEOUT (5000ms) while waiting on a tree node.
    // public boolean isJavaClassExistInGui(String projectName, String pkg,
    // String className) throws RemoteException {
    // showViewPackageExplorer();
    // activatePackageExplorerView();
    // return isTreeItemExist(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
    // projectName, "src", pkg, className + ".java");
    // }

    public boolean isTextWithLabelEqualWithText(String label, String text)
        throws RemoteException {
        return delegate.textWithLabel(label).getText().equals(text);
    }

    public boolean isButtonEnabled(int num) throws RemoteException {
        return delegate.button(num).isEnabled();
    }

    public boolean isButtonEnabled(String name) throws RemoteException {
        return delegate.button(name).isEnabled();
    }

    public boolean isViewOpen(String title) throws RemoteException {
        return getViewTitles().contains(title);
        // try {
        // return delegate.viewByTitle(title) != null;
        // } catch (WidgetNotFoundException e) {
        // log.info("view " + title + "can not be fund!");
        // return false;
        // }
    }

    public boolean isViewActive(String title) throws RemoteException {
        if (!isViewOpen(title))
            return false;
        return delegate.activeView().getTitle().equals(title);
        // SWTBotView activeView;
        // try {
        // activeView = delegate.activeView();
        // } catch (WidgetNotFoundException e) {
        // // no active view
        // return false;
        // }
        // return activeView.getTitle().equals(title);
    }

    public boolean isPerspectiveOpen(String title) throws RemoteException {
        return getPerspectiveTitles().contains(title);
        // try {
        // return delegate.perspectiveByLabel(title) != null;
        // } catch (WidgetNotFoundException e) {
        // log.warn("perspective '" + title + "' doesn't exist!");
        // return false;
        // }
    }

    public boolean isPerspectiveActive(String title) throws RemoteException {
        if (!isPerspectiveOpen(title))
            return false;
        return delegate.activePerspective().getLabel().equals(title);
        // try {
        // return delegate.perspectiveByLabel(title).isActive();
        // } catch (WidgetNotFoundException e) {
        // log.warn("perspective '" + title + "' doesn't exist!");
        // return false;
        // }
    }

    public boolean isClassOpen(String className) throws RemoteException {
        return isEditorOpen(className + ".java");
    }

    public boolean isEditorOpen(String name) throws RemoteException {
        return getEditorTitles().contains(name);

        // try {
        // return delegate.editorByTitle(name) != null;
        // } catch (WidgetNotFoundException e) {
        // log.warn("Editor '" + name + "' doesn't exist!");
        // return false;
        // }
    }

    public boolean isJavaEditorActive(String className) throws RemoteException {
        if (!isClassOpen(className))
            return false;
        return isEditorActive(className + ".java");
    }

    protected boolean isEditorActive(String name) {
        return delegate.activeEditor().getTitle().equals(name);
    }

    public boolean isShellOpen(String title) throws RemoteException {
        SWTBotShell[] shells = delegate.shells();
        for (SWTBotShell shell : shells)
            if (shell.getText().equals(title))
                return true;
        return false;
    }

    public boolean isShellActive(String title) throws RemoteException {
        if (!isShellOpen(title))
            return false;
        SWTBotShell activeShell = delegate.activeShell();
        String shellTitle = activeShell.getText();
        return shellTitle.equals(title);
    }

    public boolean isContextMenuOfTableItemInViewExist(String viewName,
        String itemName, String contextName) throws RemoteException {
        activateViewWithTitle(viewName);
        SWTBotTableItem item = selectTableItemWithLabelInView(viewName,
            itemName);
        try {
            item.contextMenu(contextName);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isContextMenuOfTreeItemInViewExist(String viewName,
        String contextName, String... labels) throws RemoteException {
        activateViewWithTitle(viewName);
        SWTBotTreeItem item = treeObject.selectTreeWithLabelsInView(viewName,
            labels);
        try {
            item.contextMenu(contextName);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isTreeItemOfTreeExisted(SWTBotTree tree, String label)
        throws RemoteException {
        return getAllItemsOftreeInView().contains(label);
        // try {
        // tree.getTreeItem(label);
        // return true;
        // } catch (WidgetNotFoundException e) {
        // return false;
        // }

    }

    public boolean isTreeItemExist(String viewTitle, String... paths)
        throws RemoteException {

        SWTBotTree tree = treeObject.getViewWithText(viewTitle).bot().tree();
        try {
            tree.expandNode(paths);
            return true;
        } catch (WidgetNotFoundException e) {
            log.error("treeitem  not found");
            return false;
        }

        // SWTBotTreeItem selectedTreeItem = null;
        // for (String label : paths) {
        // try {
        // if (selectedTreeItem == null) {
        // // waitUntilTreeExisted(tree, label);
        // selectedTreeItem = tree.getTreeItem(label);
        // log.info("treeItemName: " + selectedTreeItem.getText());
        // } else {
        // // waitUntilTreeItemExisted(selectedTreeItem, label);
        // selectedTreeItem = selectedTreeItem.
        // log.info("treeItemName: " + selectedTreeItem.getText());
        // }
        // } catch (WidgetNotFoundException e) {
        // log.error("treeitem \"" + label + "\" not found");
        // return false;
        // }
        // }
        // return selectedTreeItem != null;
    }

    // return getAllItemsOfTreeItemInView(paths).contains(itemName);
    // }

    // public boolean isTreeItemInViewExist(String viewName, String... labels)
    // throws RemoteException {
    // SWTBotTree tree = getViewWithText(viewName).bot().tree();
    // return isTreeItemExist(tree, labels);
    // }
    //
    // public boolean isTreeItemInWindowExist(String title, String... labels)
    // throws RemoteException {
    // activateShellWithText(title);
    // SWTBotShell shell = getShellWithText(title);
    // SWTBotTree tree = shell.bot().tree();
    // return isTreeItemExist(tree, labels);
    // }

    // public boolean isTreeItemExist(SWTBotTree tree, String... labels)
    // throws RemoteException {
    // SWTBotTreeItem selectedTreeItem = null;
    // for (String label : labels) {
    // try {
    // if (selectedTreeItem == null) {
    // // waitUntilTreeExisted(tree, label);
    // selectedTreeItem = tree.getTreeItem(label);
    // } else {
    // // waitUntilTreeItemExisted(selectedTreeItem, label);
    // selectedTreeItem = selectedTreeItem.getNode(label);
    // }
    // } catch (WidgetNotFoundException e) {
    // log.error("treeitem \"" + label + "\" not found");
    // return false;
    // }
    // }
    // if (selectedTreeItem != null) {
    // return true;
    // } else
    // return false;
    // }

    public boolean istableItemInViewExist(String viewName, String itemName)
        throws RemoteException {
        return isTableItemExist(getTableInView(viewName), itemName);
    }

    public boolean isTableItemInWindowExist(String title, String label)
        throws RemoteException {
        activateShellWithText(title);
        return isTableItemExist(getTableInShell(title), label);
    }

    public boolean isTableItemExist(SWTBotTable table, String itemName)
        throws RemoteException {
        try {
            // waitUntilTableItemExsited(table, itemName);
            table.getTableItem(itemName);
            return true;
        } catch (WidgetNotFoundException e) {
            log.warn("table item " + itemName + " not found.", e);
        }
        return false;
    }

    // private void implementsInterface(String interfaceClass)
    // throws WidgetNotFoundException {
    // delegate.button("Add...").click();
    // delegate.sleep(750);
    // delegate.shell("Implemented Interfaces Selection").activate();
    // delegate.sleep(750);
    // delegate.textWithLabel("Choose interfaces:").setText(interfaceClass);
    // delegate.sleep(750);
    // delegate.waitUntil(Conditions.tableHasRows(delegate.table(), 1));
    // delegate.button("OK").click();
    // delegate.sleep(750);
    // delegate.shell("New Java Class").activate();
    // }

    // public SWTBotMenu menu(String name) {
    // try {
    // return delegate.menu(name);
    // } catch (WidgetNotFoundException e) {
    // throw new RuntimeException("Widget " + name + " not found");
    // }
    // }

    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        final VCSResourceInfo info = vcs.getResourceInfo(resource);
        return info.url;

        // IProject project = ResourcesPlugin.getWorkspace().getRoot()
        // .getProject("examples");
        // final Path path = new Path(
        // "examples/src/org.eclipsecon.swtbot.example.MyFirstTest01.java");
        // IResource resource = project.findMember(path);
        // final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        // if (vcs == null)
        // return null;
        // final VCSResourceInfo info = vcs.getResourceInfo(resource);
        // return info.url;
    }

    public String getRevision(String fullPath) throws RemoteException {
        IPath path = new Path(fullPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);
        final VCSAdapter vcs = VCSAdapter.getAdapter(resource.getProject());
        if (vcs == null)
            return null;
        final VCSResourceInfo info = vcs.getResourceInfo(resource);
        return info.revision;
    }

    public SWTBotShell getShellWithText(String text) throws RemoteException {
        return delegate.shell(text);
    }

    public String getCurrentActiveShell() {
        final SWTBotShell activeShell = delegate.activeShell();
        return activeShell == null ? null : activeShell.getText();
    }

    public List<String> getViewTitles() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotView view : delegate.views())
            list.add(view.getTitle());
        return list;
    }

    public String getJavaTextOnLine(String projectName, String packageName,
        String className, int line) throws RemoteException {
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        return getTextOnLine(getJavaEditor(className), line);
    }

    protected String getTextOnLine(SWTBotEclipseEditor editor, int line) {
        return editor.getTextOnLine(line);
    }

    public int getJavaCursorLinePosition(String projectName,
        String packageName, String className) throws RemoteException {
        // openJavaFileWithEditor(projectName, packageName, className);
        activateJavaEditor(className);
        return getCursorLinePosition(getJavaEditor(className));
    }

    protected int getCursorLinePosition(SWTBotEclipseEditor editor) {
        log.info("cursorPosition: " + editor.cursorPosition().line);
        return editor.cursorPosition().line;
    }

    public RGB getJavaLineBackground(String projectName, String packageName,
        String className, int line) throws RemoteException {
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        return getLineBackground(getJavaEditor(className), line);
    }

    protected RGB getLineBackground(SWTBotEclipseEditor editor, int line) {
        return editor.getLineBackground(line);
    }

    public SWTBotEclipseEditor getJavaEditor(String className)
        throws RemoteException {
        return getTextEditor(className + ".java");
    }

    // public boolean isTwoClassSame(String projectName1, String pkg1,
    // String className1, String projectName2, String pkg2, String className2)
    // throws RemoteException, IOException {
    // InputStream input1 = getContentOfClass(projectName1, pkg1, className1);
    // InputStream input2 = getContentOfClass(projectName2, pkg2, className2);
    // boolean error = false;
    // try {
    // byte[] buffer1 = new byte[1024];
    // byte[] buffer2 = new byte[1024];
    // try {
    // int numRead1 = 0;
    // int numRead2 = 0;
    // while (true) {
    // numRead1 = input1.read(buffer1);
    // numRead2 = input2.read(buffer2);
    // if (numRead1 > -1) {
    // if (numRead2 != numRead1)
    // return false;
    // // Otherwise same number of bytes read
    // if (!Arrays.equals(buffer1, buffer2))
    // return false;
    // // Otherwise same bytes read, so continue ...
    // } else {
    // // Nothing more in stream 1 ...
    // return numRead2 < 0;
    // }
    // }
    // } finally {
    // input1.close();
    // }
    // } catch (IOException e) {
    // error = true; // this error should be thrown, even if there is an
    // // error closing stream 2
    // throw e;
    // } catch (RuntimeException e) {
    // error = true; // this error should be thrown, even if there is an
    // // error closing stream 2
    // throw e;
    // } finally {
    // try {
    // input2.close();
    // } catch (IOException e) {
    // if (!error)
    // throw e;
    // }
    // }
    // }
    //
    // public InputStream getContentOfClass(String projectName, String pkg,
    // String className) throws RemoteException {
    // IPath path = new Path(projectName + "/src/"
    // + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
    // log.info("Checking existence of file \"" + path + "\"");
    // final IFile file = ResourcesPlugin.getWorkspace().getRoot()
    // .getFile(path);
    // try {
    // return file.getContents();
    // } catch (CoreException e) {
    // log.debug("", e);
    // }
    // return null;
    // }

    /*******************************************************************************
     * 
     * Editor page
     * 
     *******************************************************************************/
    protected void activateEditor(String name) {
        try {
            delegate.cTabItem(name).activate();
        } catch (WidgetNotFoundException e) {
            log.warn("tableItem not found '", e);
        }
        // waitUntilEditorActive(name);
    }

    public void activateJavaEditor(String className) throws RemoteException {
        activateEditor(className + ".java");
    }

    public String getTextOfJavaEditor(String projectName, String packageName,
        String className) throws RemoteException {
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        return getTextEditor(className + ".java").getText();
    }

    public void selectLineInJavaEditor(int line, String fileName)
        throws RemoteException {
        selectLineInEditor(line, fileName + ".java");
    }

    public SWTBotTableItem selectTableItemWithLabelInView(String viewName,
        String label) throws RemoteException {
        try {
            SWTBotView view = treeObject.getViewWithText(viewName);
            SWTBotTable table = view.bot().table();
            return tableObject.selectTableItemWithLabel(table, label).select();
        } catch (WidgetNotFoundException e) {
            log.warn(" table item " + label + " on View " + viewName
                + " not found.", e);
        }
        return null;
    }

    public void selectCheckBoxWithText(String text) throws RemoteException {
        for (int i = 0; i < delegate.table().rowCount(); i++) {
            if (delegate.table().getTableItem(i).getText(0).equals(text)) {
                delegate.table().getTableItem(i).check();
                log.debug("found invitee: " + text);
                delegate.sleep(sleepTime);
                return;
            }
        }
    }

    public void selectCheckBoxWithList(List<String> invitees)
        throws RemoteException {
        for (int i = 0; i < delegate.table().rowCount(); i++) {
            String next = delegate.table().getTableItem(i).getText(0);
            if (invitees.contains(next)) {
                delegate.table().getTableItem(i).check();
            }
        }
    }

    /*********************** activate widget *******************/

    public void activateRosterView() throws RemoteException {
        activateViewWithTitle(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public void activateRemoteScreenView() throws RemoteException {
        activateViewWithTitle(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    public void activateSharedSessionView() throws RemoteException {
        activateViewWithTitle(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
    }

    public void activateChatView() throws RemoteException {
        activateViewWithTitle(SarosConstant.VIEW_TITLE_CHAT_VIEW);
    }

    /************* click ********************/

    public void clickMenusOfContextMenuOfTreeItemInView(String viewTitle,
        String[] matchTexts, String... contexts) throws RemoteException {

        try {
            SWTBotTree tree = treeObject
                .getViewWithText(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER)
                .bot().tree();
            // you should first select the project,whose context you want to
            // click.
            SWTBotTreeItem treeItem = treeObject.getTreeItemWithMatchText(tree,
                matchTexts);
            treeItem.select();
            ContextMenuHelper.clickContextMenu(tree, contexts);
        } catch (WidgetNotFoundException e) {
            log.error("context menu can't be found.", e);
        }
    }

    public void clickButton() throws RemoteException {
        delegate.button().click();
    }

    public void clickButton(int num) throws RemoteException {
        delegate.button(num).click();
    }

    public void clickButton(String name) {
        try {
            delegate.button(name).click();
        } catch (WidgetNotFoundException e) {
            if (e.getCause().equals("Could not find widget."))
                throw new WidgetNotFoundException("Could not find button "
                    + name + ".");
            throw e;
        }
    }

    public void clickMenuWithText(String text) throws RemoteException {
        SWTBotMenu menu = delegate.menu(text);
        menu.click();
    }

    public void clickMenuWithTexts(String... texts) throws RemoteException {
        SWTBotMenu selectedmenu = null;
        for (String text : texts) {
            try {
                if (selectedmenu == null) {
                    selectedmenu = delegate.menu(text);
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

    // public void clickMenuByName(List<String> names) throws RemoteException {
    // SWTBotMenu parentMenu = delegate.menu(names.remove(0));
    // for (String name : names) {
    // parentMenu = parentMenu.menu(name);
    // }
    // parentMenu.click();
    // }

    // public void clickToolbarButtonWithTextInViewWithTitle(String title,
    // String buttonText) throws RemoteException {
    // SWTBotView view = delegate.viewByTitle(title);
    // if (view != null) {
    // for (SWTBotToolbarButton button : view.getToolbarButtons()) {
    // if (button.getText().matches(buttonText)) {
    // button.click();
    // return;
    // }
    // }
    // }
    // throw new RemoteException("Button with text '" + buttonText
    // + "' was not found on view with title '" + title + "'");
    // }

    public void clickRadio(String title) throws RemoteException {
        delegate.radio(title).click();
        delegate.sleep(sleepTime);
    }

    public void clickCheckBox() throws RemoteException {
        delegate.checkBox().click();
        delegate.sleep(sleepTime);
    }

    public void clickContextMenuOfTableInView(String viewName, String itemName,
        String contextName) throws RemoteException {
        try {
            SWTBotTableItem item = selectTableItemWithLabelInView(viewName,
                itemName);
            item.contextMenu(contextName).click();
        } catch (WidgetNotFoundException e) {
            log.warn("contextmenu " + contextName + " of table item "
                + itemName + " on View " + viewName + " not found.", e);
        }
    }

    /****************** close widget *******************/

    public void closeViewWithText(String title) throws RemoteException {
        if (isViewOpen(title)) {
            delegate.viewByTitle(title).close();
        }
    }

    public void closeEditorWithText(String text) throws RemoteException {
        if (isEditorOpen(text)) {
            delegate.editorByTitle(text).close();
        }
    }

    // public void setTextInJavaEditor(String contents, String projectName,
    // String packageName, String className) throws RemoteException {
    // SWTBotEclipseEditor e = getTextEditor(className);
    // e.setText(contents);
    // delegate.sleep(sleepTime);
    // // Keyboard keyboard = KeyboardFactory.getDefaultKeyboard(e.getWidget(),
    // // null);
    // //
    // // e.navigateTo(7, 0);
    // // e.setFocus();
    // // keyboard.typeText("HelloWorldssdfffffffffffffffffffffffffffffffff");
    // // delegate.sleep(2000);
    // // // e.autoCompleteProposal("sys", "sysout - print to standard out");
    // // //
    // // // e.navigateTo(3, 0);
    // // // e.autoCompleteProposal("main", "main - main method");
    // // //
    // // // e.typeText("new Thread (new HelloWorld ());");
    // // if (true)
    // // return;
    // // e.notifyKeyboardEvent(SWT.CTRL, '2');
    // // e.notifyKeyboardEvent(SWT.NONE, 'L');
    // // e.notifyKeyboardEvent(SWT.NONE, '\n');
    // //
    // // e.typeText("\n");
    // // e.typeText("thread.start();\n");
    // // e.typeText("thread.join();");
    // // e.quickfix("Add throws declaration");
    // // e.notifyKeyboardEvent(SWT.NONE, (char) 27);
    // // e.notifyKeyboardEvent(SWT.NONE, '\n');
    // //
    // // e.notifyKeyboardEvent(SWT.CTRL, 's');
    // //
    // // e.notifyKeyboardEvent(SWT.ALT | SWT.SHIFT, 'x');
    // // e.notifyKeyboardEvent(SWT.NONE, 'j');
    //
    // }

    /********************** waitUntil ********************/

    public void waitUntilPkgExist(String projectName, String pkg)
        throws RemoteException {
        String path = projectName + "/src/" + pkg.replaceAll("\\.", "/");
        wUntilObject.waitUntil(SarosConditions.isResourceExist(path));
    }

    public void waitUntilPkgNotExist(String projectName, String pkg)
        throws RemoteException {
        String path = projectName + "/src/" + pkg.replaceAll("\\.", "/");
        wUntilObject.waitUntil(SarosConditions.isResourceNotExist(path));
    }

    public void waitUntilClassExist(String projectName, String pkg,
        String className) throws RemoteException {
        String path = projectName + "/src/" + pkg.replaceAll("\\.", "/") + "/"
            + className + ".java";
        wUntilObject.waitUntil(SarosConditions.isResourceExist(path));
    }

    public void waitUntilClassNotExist(String projectName, String pkg,
        String className) throws RemoteException {
        String path = projectName + "/src/" + pkg.replaceAll("\\.", "/") + "/"
            + className + ".java";
        wUntilObject.waitUntil(SarosConditions.isResourceNotExist(path));
    }

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isNotInSVN(projectName));
    }

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isInSVN(projectName));
    }

    public void waitUntilFileEqualWithFile(String projectName,
        String packageName, String className, String file)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isFilesEqual(this, projectName,
            packageName, className, file));
    }

    public void waitUntilShellCloses(SWTBotShell shell) throws RemoteException {
        wUntilObject.waitUntil(shellCloses(shell));
        delegate.sleep(10);
    }

    public void waitUntilShellCloses(String shellText) throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isShellClosed(delegate,
            shellText));
        delegate.sleep(10);
    }

    public void waitUntilJavaEditorActive(String className)
        throws RemoteException {
        waitUntilEditorActive(className + ".java");
    }

    public void waitUntilEditorActive(String name) {
        wUntilObject.waitUntil(SarosConditions.isEditorActive(delegate, name));
    }

    public void waitUntilTableHasRows(int row) throws RemoteException {
        wUntilObject.waitUntil(tableHasRows(delegate.table(), row));
    }

    public void waitUntilTableItemExisted(SWTBotTable table,
        String tableItemName) throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.existTableItem(table,
            tableItemName));
    }

    public void waitUntilTreeNodeExisted(SWTBotTreeItem treeItem,
        String nodeName) {
        wUntilObject.waitUntil(SarosConditions
            .existTreeItem(treeItem, nodeName));
    }

    public void waitUntilTreeExisted(SWTBotTree tree, String nodeName)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.existTree(tree, nodeName));
    }

    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException {
        wUntilObject.waitUntil(Conditions.widgetIsEnabled(delegate
            .button(mnemonicText)));
        // try {
        // while (!delegate.button(mnemonicText).isEnabled()) {
        // delegate.sleep(100);
        // }
        // } catch (Exception e) {
        // // next window opened
        // }
    }

    public void waitUnitButtonWithTooltipTextEnabled(String tooltipText)
        throws RemoteException {
        wUntilObject.waitUntil(Conditions.widgetIsEnabled(delegate
            .buttonWithTooltip(tooltipText)));
    }

    public void waitUntilContextMenuOfTableItemEnabled(
        SWTBotTableItem tableItem, String context) throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.ExistContextMenuOfTableItem(
            tableItem, context));
    }

    public void waitUntilShellActive(String title) throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.ShellActive(delegate, title));
        if (!isShellActive(title))
            throw new RemoteException("Couldn't activate shell \"" + title
                + "\"");
    }

    // protected void waitUntil(ICondition condition) throws TimeoutException {
    // delegate.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    // }

    // public void activeJavaEditor(String className) throws RemoteException {
    // activeEditor(className, ".java");
    // }

    // public void showWindowUntilClosed(String title) throws RemoteException {
    // try {
    // while (true) {
    // delegate.shell(title);
    // delegate.sleep(100);
    // }
    // } catch (Exception e) {
    // // window closed
    // }
    // }

    /************************* component **************************/
    public void setBreakPoint(int line, String projectName, String packageName,
        String className) throws RemoteException {
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        selectLineInJavaEditor(line, className);
        clickMenuWithTexts("Run", "Toggle Breakpoint");

    }

    public void debugJavaFile(String projectName, String packageName,
        String className) throws RemoteException {
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        clickMenuWithTexts("Debug");
        if (isShellActive("Confirm Perspective Switch"))
            confirmWindow("Confirm Perspective Switch",
                SarosConstant.BUTTON_YES);
        openPerspectiveWithName("Debug");
    }

    /*******************************************************************************
     * 
     * main page
     * 
     *******************************************************************************/
    public boolean activateEclipseShell() throws RemoteException {
        return activateShellWithMatchText(".+? - .+");
    }

    public SWTBotShell getEclipseShell() throws RemoteException {
        SWTBotShell[] shells = delegate.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().matches(".+? - .+")) {
                log.debug("shell found matching \"" + ".+? - .+" + "\"");

                return shell;
            }
        }
        final String message = "No shell found matching \"" + ".+? - .+"
            + "\"!";
        log.error(message);
        throw new RemoteException(message);
    }

    public void resetWorkbench() throws RemoteException {
        List<? extends SWTBotEditor> editors = delegate.editors();
        for (SWTBotEditor editor : editors) {
            editor.close();
        }
        for (int i = 0; i < delegate.shells().length; i++) {
            SWTBotShell shell = delegate.shells()[i];
            if (!shell.getText().matches(".+? - .+")) {
                shell.close();
            }
        }

        // page.resetPerspective();
        // String defaultPerspectiveId = workbench.getPerspectiveRegistry()
        // .getDefaultPerspective();
        // workbench.showPerspective(defaultPerspectiveId, workbenchWindow);
        // page.resetPerspective();

    }

    /*******************************************************************************
     * 
     * un-exported Helper methods
     * 
     *******************************************************************************/

    protected SWTBotTreeItem selectTreeWithLabels(SWTBotTree tree,
        String... labels) throws RemoteException {
        SWTBotTreeItem selectedTreeItem = null;
        for (String label : labels) {
            try {
                if (selectedTreeItem == null) {
                    waitUntilTreeExisted(tree, label);
                    selectedTreeItem = tree.expandNode(label);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                } else {
                    waitUntilTreeNodeExisted(selectedTreeItem, label);
                    selectedTreeItem = selectedTreeItem.expandNode(label);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                }
            } catch (WidgetNotFoundException e) {
                log.error("treeitem \"" + label + "\" not found");
            }
        }
        if (selectedTreeItem != null) {
            log.info("treeItem name: " + selectedTreeItem.getText());
            selectedTreeItem.select();
            selectedTreeItem.click();
            return selectedTreeItem;
        }
        return null;
    }

    protected void selectLineInEditor(int line, String fileName) {
        getTextEditor(fileName).selectLine(line);
    }

    protected SWTBotTree selectTreeWithLabel(String label) {
        return delegate.treeWithLabel(label);
    }

    protected SWTBotToolbarButton getToolbarButtonWithTooltipInView(
        String viewName, String buttonTooltip) {
        for (SWTBotToolbarButton toolbarButton : delegate.viewByTitle(viewName)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(buttonTooltip)) {
                return toolbarButton;
            }
        }
        return null;
    }

    protected SWTBotEclipseEditor getTextEditor(String fileName) {
        SWTBotEditor editor;
        editor = delegate.editorByTitle(fileName);
        SWTBotEclipseEditor e = editor.toTextEditor();
        // try {
        // editor = delegate.editorByTitle(className + ".java");
        // } catch (WidgetNotFoundException e) {
        // openFile(projectName, packageName, className + ".java");
        // editor = delegate.editorByTitle(className + ".java");
        // }
        // SWTBotEclipseEditor e = editor.toTextEditor();
        // delegate.cTabItem(className + ".java").activate();
        return e;
    }

    protected List<String> getAllItemsOftreeInView() {
        SWTBotTree tree = delegate.tree();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < tree.getAllItems().length; i++) {
            list.add(tree.getAllItems()[i].getText());
            log.info("existed TreeItem in Tree:  " + list.get(i));
        }
        return list;
    }

    protected List<String> getAllItemsOfTreeItemInView(String... paths) {
        SWTBotTree tree = delegate.tree();
        SWTBotTreeItem item = getTreeWithLabels(tree, paths);
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < item.getItems().length; i++) {
            list.add(item.getItems()[i].getText());
            log.info("existed item In TreeItem:  " + list.get(i));
        }
        return list;
    }

    protected List<String> getEditorTitles() {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotEditor editor : delegate.editors())
            list.add(editor.getTitle());
        return list;
    }

    protected List<String> getPerspectiveTitles() {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotPerspective perspective : delegate.perspectives())
            list.add(perspective.getLabel());
        return list;
    }

    protected SWTBotTreeItem getTreeWithLabels(SWTBotTree tree,
        String... labels) {
        // SWTBotTreeItem selectedTreeItem = null;
        // for (String label : labels) {
        // try {
        // if (selectedTreeItem == null) {
        // selectedTreeItem = tree.getTreeItem(label);
        // log.info("treeItem name: " + selectedTreeItem.getText());
        // } else {
        // selectedTreeItem = selectedTreeItem.getNode(label);
        // log.info("treeItem name: " + selectedTreeItem.getText());
        // }
        // } catch (WidgetNotFoundException e) {
        // log.error("treeitem \"" + label + "\" not found");
        // }
        // }
        // return selectedTreeItem;
        try {
            return tree.expandNode(labels);
        } catch (WidgetNotFoundException e) {
            log.warn("table item not found.", e);
            return null;
        }

    }

    protected SWTBotTable getTableInView(String viewName) {
        return treeObject.getViewWithText(viewName).bot().table();
    }

    protected SWTBotTable getTableInShell(String shellName)
        throws RemoteException {
        SWTBotShell shell = getShellWithText(shellName);
        return shell.bot().table();
    }

    protected List<String> getAllProjects() {
        SWTBotTree tree = treeObject
            .getViewWithText(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER).bot()
            .tree();
        List<String> projectNames = new ArrayList<String>();
        for (int i = 0; i < tree.getAllItems().length; i++) {
            projectNames.add(tree.getAllItems()[i].getText());
        }
        return projectNames;
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
     * @param nodeName
     *            example: "Java"
     */
    protected void openPerspectiveWithName(String nodeName)
        throws RemoteException {
        if (!isPerspectiveActive(nodeName)) {
            activateEclipseShell();
            clickMenuWithTexts(SarosConstant.MENU_TITLE_WINDOW,
                SarosConstant.MENU_TITLE_OPEN_PERSPECTIVE,
                SarosConstant.MENU_TITLE_OTHER);
            confirmWindowWithTable(SarosConstant.MENU_TITLE_OPEN_PERSPECTIVE,
                nodeName, SarosConstant.BUTTON_OK);
        }
    }

    /**
     * Open a view using Window->Show View->Other... The method is defined as
     * helper method for other showView* methods and should not be exported
     * using rmi.
     * 
     * 1. if the view already exist, return.
     * 
     * 2. activate the saros-instance-window(alice / bob / carl). If the
     * workbench isn't active, delegate can't find the main menus.
     * 
     * 3. click main menus Window -> Show View -> Other....
     * 
     * 4. confirm the pop-up window "Show View"
     * 
     * @param category
     *            example: "General"
     * @param nodeName
     *            example: "Console"
     */
    protected void openViewWithName(String viewTitle, String category,
        String nodeName) throws RemoteException {
        if (!isViewOpen(viewTitle)) {
            activateEclipseShell();
            clickMenuWithTexts(SarosConstant.MENU_TITLE_WINDOW,
                SarosConstant.MENU_TITLE_SHOW_VIEW,
                SarosConstant.MENU_TITLE_OTHER);
            confirmWindowWithTreeWithFilterText(
                SarosConstant.MENU_TITLE_SHOW_VIEW, category, nodeName,
                SarosConstant.BUTTON_OK);
        }
    }

    /**
     * Should be only called if View is open
     */
    protected void activateViewWithTitle(String title) throws RemoteException {
        try {
            if (!isViewActive(title)) {
                delegate.viewByTitle(title).setFocus();
                // waitUntil(SarosConditions.isViewActive(delegate, title));
            }
        } catch (WidgetNotFoundException e) {
            log.warn("Widget not found '" + title + "'", e);
        }
    }

    protected String[] changeToRegex(String... texts) {
        String[] matchTexts = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            matchTexts[i] = texts[i] + ".*";
        }
        return matchTexts;
    }

    protected void setTextWithLabel(String label, String text) {
        delegate.textWithLabel(label).setText(text);
    }

    protected void setTextWithoutLabel(String match, String replace) {
        delegate.text(match).setText(replace);
    }

    protected void setTextinEditor(String contents, String fileName) {
        SWTBotEclipseEditor e = getTextEditor(fileName);
        e.setText(contents);
        e.save();
    }

    protected void clickCheckBox(String title) {
        delegate.checkBox(title).click();

    }
}
