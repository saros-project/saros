package de.fu_berlin.inf.dpp.stf.swtbot;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.tableHasRows;

import java.io.IOException;
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
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import de.fu_berlin.inf.dpp.stf.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.EditorObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.MainObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.MenuObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.PerspectiveObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.TableObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.ToolbarObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.TreeObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.ViewObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.WaitUntilObject;
import de.fu_berlin.inf.dpp.stf.swtbot.helpers.WindowObject;
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
    public ViewObject viewObject = new ViewObject(this);
    public PerspectiveObject persObject = new PerspectiveObject(this);
    public EditorObject editorObject = new EditorObject(this);
    public MainObject mainObject = new MainObject(this);
    public MenuObject menuObject = new MenuObject(this);
    public WindowObject windowObject = new WindowObject(this);

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
     * Progress View Page
     * 
     *******************************************************************************/
    public void openProgressView() throws RemoteException {
        viewObject.showViewById("org.eclipse.ui.views.ProgressView");
    }

    public void activateProgressView() throws RemoteException {
        viewObject.activateViewWithTitle(SarosConstant.VIEW_TITLE_PROGRESS);
    }

    public boolean existPorgress() throws RemoteException {
        openProgressView();
        activateProgressView();
        SWTBotView view = delegate.viewByTitle("Progress");
        view.setFocus();
        view.toolbarButton("Remove All Finished Operations").click();
        SWTBot bot = view.bot();
        try {
            SWTBotToolbarButton b = bot.toolbarButton();
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }

        // if (b == null)
        // return false;
        // else
        // return true;
        // if (bot.text().getText().matches("No operations to display.*"))
        // return false;
        // return true;
    }

    /*******************************************************************************
     * 
     * Package Explorer View Page
     * 
     *******************************************************************************/

    public void closePackageExplorerView() throws RemoteException {
        viewObject.closeViewWithText(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
    }

    public void closeWelcomeView() throws RemoteException {
        viewObject.closeViewWithText(SarosConstant.VIEW_TITLE_WELCOME);
    }

    public void activatePackageExplorerView() throws RemoteException {
        viewObject
            .activateViewWithTitle(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
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
        SWTBotTree tree = viewObject
            .getTreeInView(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
        tree.select(projectName);
        menuObject.clickMenuWithTexts("Edit", "Delete");
        confirmWindowWithCheckBox(SarosConstant.SHELL_TITLE_DELETE_RESOURCE,
            SarosConstant.BUTTON_OK, true);
        waitUntilShellCloses(SarosConstant.SHELL_TITLE_DELETE_RESOURCE);
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
        viewObject.clickContextMenuOfTreeInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, "Delete", nodes);
        waitUntilShellActive(SarosConstant.SHELL_TITLE_CONFIRM_DELETE);
        confirmWindow(SarosConstant.SHELL_TITLE_CONFIRM_DELETE,
            SarosConstant.BUTTON_OK);
    }

    public boolean isClassExistGUI(String... matchTexts) throws RemoteException {
        activateEclipseShell();
        showViewPackageExplorer();
        activatePackageExplorerView();
        SWTBotTree tree = viewObject
            .getTreeInView(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
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
        if (!isClassOpen(className)) {
            viewObject.openFileInView(
                SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, projectName, "src",
                packageName, className + ".java");
            delegate.sleep(sleepTime);
        }
    }

    public void openClassWithSystemEditor(String projectName, String pkg,
        String className) throws RemoteException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(path);
        Program.launch(resource.getLocation().toString());
    }

    public void openClassWith(String whichEditor, String projectName,
        String packageName, String className) throws RemoteException {
        SWTBotTree tree = viewObject
            .getTreeInView(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
        tree.expandNode(projectName, "src", packageName, className + ".java")
            .select();
        ContextMenuHelper.clickContextMenu(tree, "Open With", "Other...");
        waitUntilShellActive("Editor Selection");
        SWTBotTable table = delegate.table();
        table.select(whichEditor);
        waitUntilButtonEnabled(SarosConstant.BUTTON_OK);
        confirmWindow("Editor Selection", SarosConstant.BUTTON_OK);
    }

    public void moveClassTo(String projectName, String pkg, String className,
        String targetProject, String targetPkg) throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = mainObject.changeToRegex(projectName, "src", pkg,
            className);
        log.info("matchTexts: " + matchTexts);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Refactor",
            "Move...");
        waitUntilShellActive("Move");
        confirmWindowWithTree("Move", SarosConstant.BUTTON_OK, targetProject,
            "src", targetPkg);

    }

    public void disConnectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Disconnect...");
        confirmWindow("Confirm Disconnect from SVN", SarosConstant.BUTTON_YES);
    }

    public void connectSVN() throws RemoteException {
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Share Project...");
        confirmWindowWithTable("Share Project", "SVN",
            SarosConstant.BUTTON_NEXT);
        delegate.button(SarosConstant.BUTTON_FINISH).click();
    }

    public void switchToOtherRevision() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        waitUntilShellActive("Switch");
        delegate.checkBox("Switch to HEAD revision").click();
        delegate.textWithLabel("Revision:").setText("115");
        delegate.button(SarosConstant.BUTTON_OK).click();
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
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        waitUntilShellActive("Switch");
        delegate.checkBox("Switch to HEAD revision").click();
        delegate.textWithLabel("Revision:").setText("116");
        delegate.button(SarosConstant.BUTTON_OK).click();
        waitUntilShellCloses("SVN Switch");
    }

    public void revert() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Revert...");
        confirmWindow("Revert", SarosConstant.BUTTON_OK);
        waitUntilShellCloses("Revert");
    }

    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException {
        renameFile(newName, projectName, "src", pkg, className);
    }

    public void renameFile(String newName, String... texts)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = mainObject.changeToRegex(texts);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Refactor",
            "Rename...");
        windowObject.activateShellWithText("Rename Compilation Unit");
        delegate.textWithLabel("New name:").setText(newName);
        waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
        delegate.button(SarosConstant.BUTTON_FINISH).click();
        waitUntilShellCloses("Rename Compilation Unit");
    }

    public void renameFolder(String projectName, String oldPath, String newPath)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] nodes = { projectName, oldPath };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, nodes, "Refactor",
            "Rename...");
        waitUntilShellActive("Rename Resource");
        delegate.textWithLabel("New name:").setText(newPath);
        waitUntilButtonEnabled(SarosConstant.BUTTON_OK);
        delegate.button(SarosConstant.BUTTON_OK).click();
        waitUntilShellCloses("Rename Resource");
    }

    public void renamePkg(String newName, String... texts)
        throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = mainObject.changeToRegex(texts);
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Refactor",
            "Rename...");
        windowObject.activateShellWithText("Rename Package");
        delegate.textWithLabel("New name:").setText(newName);
        waitUntilButtonEnabled(SarosConstant.BUTTON_OK);
        delegate.button(SarosConstant.BUTTON_OK).click();
        waitUntilShellCloses("Rename Package");
    }

    public void switchToTag() throws RemoteException {
        showViewPackageExplorer();
        activatePackageExplorerView();
        String[] matchTexts = { BotConfiguration.PROJECTNAME_SVN + ".*" };
        viewObject.clickMenusOfContextMenuOfTreeItemInView(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, matchTexts, "Team",
            "Switch to another Branch/Tag/Revision...");
        waitUntilShellActive("Switch");
        delegate.button("Select...").click();
        confirmWindowWithTree("Repository Browser", SarosConstant.BUTTON_OK,
            "tags", "eclipsecon2009");
        delegate.button(SarosConstant.BUTTON_OK).click();
        waitUntilShellCloses("SVN Switch");
    }

    public void importProjectFromSVN(String path) throws RemoteException {
        activateEclipseShell();
        menuObject.clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
            SarosConstant.MENU_TITLE_IMPORT);
        confirmWindowWithTreeWithFilterText(SarosConstant.SHELL_TITLE_IMPORT,
            "SVN", "Checkout Projects from SVN", SarosConstant.BUTTON_NEXT);
        if (delegate.table().containsItem(path)) {
            confirmWindowWithTable("Checkout from SVN",
                BotConfiguration.SVN_URL, SarosConstant.BUTTON_NEXT);
        } else {
            delegate.radio("Create a new repository location").click();
            delegate.button(SarosConstant.BUTTON_NEXT).click();
            delegate.comboBoxWithLabel("Url:").setText(path);
            delegate.button(SarosConstant.BUTTON_NEXT).click();
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
     * @throws RemoteException
     * 
     *******************************************************************************/

    public void preference() throws RemoteException {
        activateEclipseShell();
        menuObject.clickMenuWithTexts("Window", "Preferences");
    }

    public void newTextFileLineDelimiter(String OS) throws RemoteException {
        preference();
        SWTBotTree tree = delegate.tree();
        tree.expandNode("General").select("Workspace");

        if (OS.equals("Default")) {
            delegate.radioInGroup("Default", "New text file line delimiter")
                .click();
        } else {
            delegate.radioInGroup("Other:", "New text file line delimiter")
                .click();
            delegate.comboBoxInGroup("New text file line delimiter")
                .setSelection(OS);
        }
        delegate.button("Apply").click();
        delegate.button("OK").click();
        waitUntilShellCloses("Preferences");
    }

    public String getTextFileLineDelimiter() throws RemoteException {
        preference();
        SWTBotTree tree = delegate.tree();
        tree.expandNode("General").select("Workspace");
        if (delegate.radioInGroup("Default", "New text file line delimiter")
            .isSelected()) {
            closeShell("Preferences");
            return "Default";
        } else if (delegate.radioInGroup("Other:",
            "New text file line delimiter").isSelected()) {
            SWTBotCombo combo = delegate
                .comboBoxInGroup("New text file line delimiter");
            String itemName = combo.items()[combo.selectionIndex()];
            closeShell("Preferences");
            return itemName;
        }
        closeShell("Preferences");
        return "";
    }

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
                menuObject.clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
                    SarosConstant.MENU_TITLE_NEW, "Package");
                activateShellWithMatchText("New Java Package");
                delegate.textWithLabel("Source folder:").setText(
                    (projectName + "/src"));
                delegate.textWithLabel("Name:").setText(pkg);
                delegate.button(SarosConstant.BUTTON_FINISH).click();
                waitUntilShellCloses("New java Package");
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new package";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    /**
     * Create a new folder. Via File -> New -> Folder.
     * 
     * 1. If the folder already exists, return.
     * 
     * 2. Activate saros instance window. If workbench isn't active, delegate
     * can't find main menus.
     * 
     * 3. Click menu: File -> New -> Folder.
     * 
     * 4. Confirm pop-up window "New Folder".
     * 
     * 
     */
    public void newFolder(String projectName, String folderName)
        throws RemoteException {
        if (!isFolderExist(projectName, folderName))
            try {
                activateEclipseShell();
                delegate.menu("File").menu("New").menu("Folder").click();
                SWTBotShell shell = delegate.shell("New Folder");
                shell.activate();
                delegate.textWithLabel("Enter or select the parent folder:")
                    .setText(projectName);
                delegate.textWithLabel("Folder name:").setText(folderName);
                delegate.button("Finish").click();
                delegate.waitUntil(Conditions.shellCloses(shell));
            } catch (WidgetNotFoundException e) {
                final String cause = "Error creating new folder";
                log.error(cause, e);
                throw new RemoteException(cause, e);
            }
    }

    public void newFile(String fileName, String... folders)
        throws RemoteException {
        if (!isFileExist(fileName, folders))
            try {
                activateEclipseShell();
                delegate.menu("File").menu("New").menu("File").click();
                SWTBotShell shell = delegate.shell("New File");
                shell.activate();
                if (folders.length > 0)
                    delegate.tree().expandNode(folders).select();
                delegate.textWithLabel("File name:").setText(fileName);
                delegate.button("Finish").click();
                delegate.waitUntil(Conditions.shellCloses(shell));
            } catch (WidgetNotFoundException e) {
                final String cause = "error creating new file.";
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

    public void newClassImplementsRunnable(String projectName, String pkg,
        String className) throws RemoteException {
        activateEclipseShell();
        delegate.menu("File").menu("New").menu("Class").click();
        SWTBotShell shell = delegate.shell("New Java Class");
        shell.activate();
        delegate.textWithLabel("Source folder:").setText(projectName + "/src");
        delegate.textWithLabel("Package:").setText(pkg);
        delegate.textWithLabel("Name:").setText(className);

        delegate.button("Add...").click();
        waitUntilShellActive("Implemented Interfaces Selection");
        delegate.shell("Implemented Interfaces Selection").activate();
        SWTBotText text = delegate.textWithLabel("Choose interfaces:");
        delegate.sleep(2000);
        text.setText("java.lang.Runnable");
        waitUntilTableHasRows(1);

        delegate.button("OK").click();
        delegate.shell("New Java Class").activate();

        delegate.checkBox("Inherited abstract methods").click();
        delegate.button("Finish").click();
        delegate.waitUntil(Conditions.shellCloses(shell));
    }

    /**
     * Open the view "Package Explorer". The name of the method is defined the
     * same as the menu names. The name "showViewPackageExplorer" then means:
     * hello guy, please click main menus Window -> Show view ->
     * PackageExplorer.
     * 
     */
    public void showViewPackageExplorer() throws RemoteException {
        viewObject.openViewWithName(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
            "Java", "Package Explorer");
    }

    /**
     * Open the view "Problems". The name of the method is defined the same as
     * the menu names. The name "showViewProblem" then means: hello guy, please
     * click main menus Window -> Show view -> Problems.
     * 
     */
    public void showViewProblems() throws RemoteException {
        viewObject.openViewWithName("Problems", "General", "Problems");
    }

    /**
     * Open the view "Project Explorer". The name of the method is defined the
     * same as the menu names. The name "showViewProblem" then means: hello guy,
     * please click main menus Window -> Show view -> Project Explorer.
     * 
     */
    public void showViewProjectExplorer() throws RemoteException {
        viewObject.openViewWithName("Project Explorer", "General",
            "Project Explorer");
    }

    /**
     * Open the perspective "Java". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveJava" then means: hello guy,
     * please click main menus Window -> Open perspective -> Java.
     * 
     */
    public void openPerspectiveJava() throws RemoteException {
        persObject.openPerspectiveWithId(SarosConstant.ID_JAVA_PERSPECTIVE);
    }

    /**
     * test, if the java perspective is active.
     */
    public boolean isJavaPerspectiveActive() throws RemoteException {
        return persObject
            .isPerspectiveActive(SarosConstant.ID_JAVA_PERSPECTIVE);
    }

    /**
     * Open the perspective "Debug". The name of the method is defined the same
     * as the menu names. The name "openPerspectiveDebug" then means: hello guy,
     * please click main menus Window -> Open perspective -> Debug.
     * 
     */
    public void openPerspectiveDebug() throws RemoteException {
        persObject.openPerspectiveWithId(SarosConstant.ID_DEBUG_PERSPECTIVE);
    }

    /**
     * test, if the debug perspective is active.
     */
    public boolean isDebugPerspectiveActive() throws RemoteException {
        return persObject
            .isPerspectiveActive(SarosConstant.ID_DEBUG_PERSPECTIVE);
    }

    /*******************************************************************************
     * 
     * popup window page
     * 
     *******************************************************************************/
    public void closeShell(String title) throws RemoteException {
        delegate.shell(title).close();
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
        if (windowObject.activateShellWithText(title)) {
            delegate.button(buttonText).click();
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
        windowObject.activateShellWithText(title);
        if (isChecked)
            delegate.checkBox().click();
        delegate.button(buttonText).click();
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
            tableObject.selectCheckBoxInTable(itemName);
        }
        waitUntilButtonEnabled(buttonText);
        delegate.button(buttonText).click();
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
            delegate.button(buttonText).click();
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
        treeObject.selectTreeWithLabelsWithWaitungExpand(tree, nodes);
        waitUntilButtonEnabled(buttonText);
        delegate.button(buttonText).click();
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
        delegate.text(SarosConstant.TEXT_FIELD_TYPE_FILTER_TEXT).setText(
            teeNode);
        waitUntilTreeExisted(delegate.tree(), rootOfTreeNode);
        SWTBotTreeItem treeItem = delegate.tree(0).getTreeItem(rootOfTreeNode);
        waitUntilTreeNodeExisted(treeItem, teeNode);
        treeItem.getNode(teeNode).select();
        waitUntilButtonEnabled(buttonText);
        delegate.button(buttonText).click();
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

    /*******************************************************************************
     * 
     * no GUI methods
     * 
     *******************************************************************************/

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

    public void deleteAllProjects() throws RemoteException {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = root.getProjects();
        for (int i = 0; i < projects.length; i++) {
            try {
                FileUtil.delete(projects[i]);
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                log.debug("Couldn't delete files ", e);
            }
        }
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

    public boolean isFileExist(String fileName, String... folders)
        throws RemoteException {
        String filepath = "";
        for (String folder : folders) {
            filepath += folder + "/";
        }
        filepath += fileName;
        log.info("Checking existence of file \"" + filepath + "\"");
        IPath path = new Path(filepath);
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);
        return file.exists();
    }

    /**
     * get the content of the class file, which is saved.
     */
    public String getClassContent(String projectName, String pkg,
        String className) throws RemoteException, IOException, CoreException {
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        log.info("Checking full path: \"" + file.getFullPath().toOSString()
            + "\"");
        return mainObject.ConvertStreamToString(file.getContents());
    }

    public boolean isFolderExist(String projectName, String folderPath)
        throws RemoteException {
        IPath path = new Path(projectName + "/" + folderPath);
        IResource resource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(path);

        if (resource == null)
            return false;

        return true;
    }

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

    /*******************************************************************************
     * 
     * Editor page
     * 
     *******************************************************************************/

    public boolean isClassOpen(String className) throws RemoteException {
        return editorObject.isEditorOpen(className + ".java");
    }

    public boolean isJavaEditorActive(String className) throws RemoteException {
        if (!isClassOpen(className))
            return false;
        return editorObject.isEditorActive(className + ".java");
    }

    public String getJavaTextOnLine(String projectName, String packageName,
        String className, int line) throws RemoteException {
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        return getJavaEditor(className).getTextOnLine(line);
    }

    public int getJavaCursorLinePosition(String projectName,
        String packageName, String className) throws RemoteException {
        // openJavaFileWithEditor(projectName, packageName, className);
        activateJavaEditor(className);
        SWTBotEclipseEditor editor = getJavaEditor(className);
        log.info("cursorPosition: " + editor.cursorPosition().line);
        return editor.cursorPosition().line;
    }

    public RGB getJavaLineBackground(String projectName, String packageName,
        String className, int line) throws RemoteException {
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        return getJavaEditor(className).getLineBackground(line);
    }

    public SWTBotEclipseEditor getJavaEditor(String className)
        throws RemoteException {
        return editorObject.getTextEditor(className + ".java");
    }

    public void activateJavaEditor(String className) throws RemoteException {
        editorObject.activateEditor(className + ".java");
    }

    /**
     * get content of a class file, which may be not saved.
     */
    public String getTextOfJavaEditor(String projectName, String packageName,
        String className) throws RemoteException {
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        return editorObject.getTextEditor(className + ".java").getText();
    }

    public void selectLineInJavaEditor(int line, String fileName)
        throws RemoteException {
        editorObject.selectLineInEditor(line, fileName + ".java");
    }

    public void setBreakPoint(int line, String projectName, String packageName,
        String className) throws RemoteException {
        openClass(projectName, packageName, className);
        activateJavaEditor(className);
        selectLineInJavaEditor(line, className);
        menuObject.clickMenuWithTexts("Run", "Toggle Breakpoint");

    }

    public void closeJavaEditorWithSave(String className)
        throws RemoteException {
        activateJavaEditor(className);
        getJavaEditor(className).save();
        getJavaEditor(className).close();
        // Display.getDefault().syncExec(new Runnable() {
        // public void run() {
        // final IWorkbench wb = PlatformUI.getWorkbench();
        // final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        // IWorkbenchPage page = win.getActivePage();
        // if (page != null) {
        // page.closeEditor(page.getActiveEditor(), true);
        // Shell activateShell = Display.getCurrent().getActiveShell();
        // activateShell.close();
        //
        // }
        // }
        // });
    }

    public void closejavaEditorWithoutSave(String className)
        throws RemoteException {
        activateJavaEditor(className);
        getJavaEditor(className).close();
        confirmWindow("Save Resource", SarosConstant.BUTTON_YES);
    }

    /**
     * Returns whether the contents of this class file have changed since the
     * last save operation.
     * <p>
     * <b>Note:</b> if the class file isn't open, it will be opened first using
     * the defined editor (parameter: idOfEditor).
     * </p>
     * 
     * @return <code>true</code> if the contents have been modified and need
     *         saving, and <code>false</code> if they have not changed since the
     *         last save
     */
    public boolean isClassDirty(String projectName, String pkg,
        String className, final String idOfEditor) throws RemoteException {
        final List<Boolean> results = new ArrayList<Boolean>();
        IPath path = new Path(projectName + "/src/"
            + pkg.replaceAll("\\.", "/") + "/" + className + ".java");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

                IWorkbenchPage page = win.getActivePage();
                if (page != null) {
                    IEditorInput editorInput = new FileEditorInput(file);
                    try {
                        page.openEditor(editorInput, idOfEditor);
                    } catch (PartInitException e) {
                        log.debug("", e);
                    }
                    results.add(page.findEditor(editorInput).isDirty());
                }
            }
        });
        return results.get(0);
    }

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

    public void waitUntilFolderExist(String projectName, String path)
        throws RemoteException {
        String wholePath = projectName + "/" + path;
        wUntilObject.waitUntil(SarosConditions.isResourceExist(wholePath));
    }

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isNotInSVN(projectName));
    }

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isInSVN(projectName));
    }

    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a minute to see the changes . So it will
     * be a good idea that you give bob some time before you compare the two
     * files from Alice and Bob.
     * 
     * <p>
     * <b>Note:</b> the mothod is different from
     * {@link #waitUntilEditorContentSame(String, String, String, String)}. this
     * method compare only the contents of the class files which is saved.
     * </p>
     * * *
     */
    public void waitUntilClassContentsSame(String projectName, String pkg,
        String className, String otherClassContent) throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isClassContentsSame(this,
            projectName, pkg, className, otherClassContent));
    }

    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a minute to see the changes . So it will
     * be a good idea that you give bob some time before you compare the two
     * files from Alice and Bob.
     * 
     * <p>
     * <b>Note:</b> the mothod is different from
     * {@link #waitUntilClassContentsSame(String, String, String, String)}. this
     * method compare only the contents of the class files which may be not
     * saved.
     * </p>
     * * *
     */
    public void waitUntilEditorContentSame(String projectName, String pkg,
        String className, String otherClassContent) throws RemoteException {
        wUntilObject.waitUntil(SarosConditions.isEditorContentsSame(this,
            projectName, pkg, className, otherClassContent));
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

    /*******************************************************************************
     * 
     * main page
     * 
     *******************************************************************************/
    public void activateEclipseShell() throws RemoteException {
        getEclipseShell().activate().setFocus();
        // return activateShellWithMatchText(".+? - .+");
        // Display.getDefault().syncExec(new Runnable() {
        // public void run() {
        // final IWorkbench wb = PlatformUI.getWorkbench();
        // final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        // win.getShell().setActive();
        // }
        // });

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
        openPerspectiveJava();
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                IWorkbenchPage page = win.getActivePage();
                if (page != null) {
                    page.closeAllEditors(false);
                }
                Shell activateShell = Display.getCurrent().getActiveShell();
                if (activateShell != null && activateShell != win.getShell()) {
                    activateShell.close();
                }
            }
        });
    }

}
