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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.stf.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

/**
 * RmiSWTWorkbenchBot delegates to {@link SWTWorkbenchBot} to implement an
 * java.rmi interface for {@link SWTWorkbenchBot}.
 */
public class RmiSWTWorkbenchBot implements IRmiSWTWorkbenchBot {
    private static final transient Logger log = Logger
        .getLogger(RmiSWTWorkbenchBot.class);

    private static final boolean SCREENSHOTS = true;

    protected static transient SarosSWTWorkbenchBot delegate;

    /** The RMI registry used, is not exported */
    protected static transient Registry registry;

    private static transient RmiSWTWorkbenchBot self;

    protected transient String myName;

    /** RMI exported remote usable SWTWorkbenchBot replacement */
    public IRmiSWTWorkbenchBot stub;

    public static String WHICHOS = System.getProperty("os.name");

    public int sleepTime = 750;

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

    /*********************** high-level RMI exported Methods *******************/

    /**
     * Throws WidgetException if given project does not exist in Package
     * Explorer.
     */

    public void sleep(long millis) throws RemoteException {
        delegate.sleep(millis);
    }

    public void captureScreenshot(String filename) throws RemoteException {
        if (SCREENSHOTS)
            delegate.captureScreenshot(filename);
    }

    /**************** import project *********************/
    public void importProjectFromSVN(String path) throws RemoteException {
        clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
            SarosConstant.MENU_TITLE_IMPORT);
        confirmWindowWithTreeWithFilterText(SarosConstant.SHELL_TITLE_IMPORT,
            "SVN", "Checkout Projects from SVN", SarosConstant.BUTTON_NEXT);
        confirmWindowWithTable("Checkout from SVN", BotConfiguration.SVN_URL,
            SarosConstant.BUTTON_NEXT);
        confirmWindowWithTree("Checkout from SVN", SarosConstant.BUTTON_FINISH,
            path, "trunk", "examples");
        waitUntilShellActive("SVN Checkout");
        SWTBotShell shell2 = delegate.shell("SVN Checkout");
        waitUntilShellCloses(shell2);
    }

    /****************** delete widget ********************/

    public void deleteResource(String projectName) throws RemoteException {
        selectTreeWithLabelsInView(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
            projectName);
        clickMenuWithTexts(SarosConstant.MENU_TITLE_EDIT,
            SarosConstant.MENU_TITLE_DELETE);
        confirmWindowWithCheckBox(SarosConstant.SHELL_TITLE_DELETE_RESOURCE,
            SarosConstant.BUTTON_OK, true);
        // waitUntilShellCloses(delegate
        // .shell(SarosConstant.SHELL_TITLE_DELETE_RESOURCE));
    }

    public void deleteFile(String... labels) throws RemoteException {
        selectTreeWithLabelsInView(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
            labels);
        clickMenuWithTexts(SarosConstant.MENU_TITLE_EDIT,
            SarosConstant.MENU_TITLE_DELETE);
        confirmWindowWithCheckBox(SarosConstant.SHELL_TITLE_CONFIRM_DELETE,
            SarosConstant.BUTTON_OK, true);
        // waitUntilShellCloses(delegate
        // .shell(SarosConstant.SHELL_TITLE_DELETE_RESOURCE));
    }

    /************* new widget ******************/

    public void newJavaProject() throws RemoteException {
        Double rand = Math.random();
        newJavaProject("Foo-" + rand.toString().substring(3, 10));
    }

    public void newJavaProject(String project) throws RemoteException {
        try {
            clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
                SarosConstant.MENU_TITLE_NEW,
                SarosConstant.MENU_TITLE_JAVA_PROJECT);
        } catch (WidgetNotFoundException e) {
            clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
                SarosConstant.MENU_TITLE_NEW, SarosConstant.MENU_TITLE_PROJECT);
            confirmWindowWithTreeWithFilterText(
                SarosConstant.SHELL_TITLE_NEW_PROJECT,
                SarosConstant.CATEGORY_JAVA, SarosConstant.NODE_JAVA_PROJECT,
                SarosConstant.BUTTON_NEXT);
        }

        try {
            // delegate.sleep(sleepTime);
            setTextWithLabel("Project name:", project);
            // Integer i = 0;
            // while (!isButtonEnabled(SarosConstant.BUTTON_FINISH)) {
            // i++;
            // setTextWithLabel("Project name:", project + i.toString());
            // }
            delegate.button("Finish").click();
            if (isShellActive("Open Associated Perspective?")) {
                clickButton(SarosConstant.BUTTON_YES);
            }
            // delegate.sleep(sleepTime);
        } catch (WidgetNotFoundException e) {
            log.error("error creating new Java Project", e);
        }
    }

    public void newJavaClass(String projectName, String pkg, String className)
        throws RemoteException {
        try {
            clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
                SarosConstant.MENU_TITLE_NEW, SarosConstant.MENU_TITLE_CLASS);
            waitUntilShellActive(SarosConstant.SHELL_TITLE_NEW_JAVA_CLASS);
            setTextWithLabel("Source folder:", projectName + "/src");
            // delegate.sleep(sleepTime);
            setTextWithLabel("Package:", pkg);
            // delegate.sleep(sleepTime);
            setTextWithLabel("Name:", className);
            // delegate.sleep(sleepTime);
            // implementsInterface("java.lang.Runnable");
            // delegate.checkBox("Inherited abstract methods").click();
            clickCheckBox("Inherited abstract methods");
            waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
            clickButton(SarosConstant.BUTTON_FINISH);
            // waitUntilShellCloses(shell);
            // openJavaFileWithEditor(projectName, pkg, className + ".java");
            // delegate.sleep(sleepTime);
            // editor.navigateTo(2, 0);
            // editor.quickfix("Add unimplemented methods");
            // editor.save();
            // delegate.sleep(750);
        } catch (WidgetNotFoundException e) {
            log.error("error creating new Java Class", e);
        }
    }

    /******************** open Widget *************************/

    public void openViewWithName(String viewTitle, String category,
        String nodeName) throws RemoteException {
        if (!isViewOpen(viewTitle)) {
            clickMenuWithTexts(SarosConstant.MENU_TITLE_WINDOW,
                SarosConstant.MENU_TITLE_SHOW_VIEW,
                SarosConstant.MENU_TITLE_OTHER);
            confirmWindowWithTreeWithFilterText(
                SarosConstant.MENU_TITLE_SHOW_VIEW, category, nodeName,
                SarosConstant.BUTTON_OK);
        }
    }

    public void openPackageExplorerView() throws RemoteException {
        openViewWithName(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, "Java",
            "Package Explorer");
    }

    public void openProblemsView() throws RemoteException {
        openViewWithName("Problems", "General", "Problems");
    }

    public void openProjectExplorerView() throws RemoteException {
        openViewWithName("Project Explorer", "General", "Project Explorer");
    }

    public void openJavaPerspective() throws RemoteException {
        openPerspectiveWithName(SarosConstant.PERSPECTIVE_TITLE_JAVA);
    }

    public void openDebugPerspective() throws RemoteException {
        openPerspectiveWithName(SarosConstant.PERSPECTIVE_TITLE_DEBUG);
    }

    protected void openPerspectiveWithName(String nodeName)
        throws RemoteException {
        if (!isPerspectiveActive(nodeName)) {
            clickMenuWithTexts(SarosConstant.MENU_TITLE_WINDOW,
                SarosConstant.MENU_TITLE_OPEN_PERSPECTIVE,
                SarosConstant.MENU_TITLE_OTHER);
            confirmWindowWithTable(SarosConstant.MENU_TITLE_OPEN_PERSPECTIVE,
                nodeName, SarosConstant.BUTTON_OK);
        }
    }

    protected void openFileWithEditorInView(String viewName, String... names)
        throws RemoteException {
        clickContextMenuOfTreeInView(viewName, SarosConstant.CONTEXT_MENU_OPEN,
            names);
    }

    public void openJavaFileWithEditor(String projectName, String packageName,
        String className) throws RemoteException {
        if (!isEditorOpen(className)) {
            openFileWithEditorInView(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
                projectName, "src", packageName, className + ".java");
            delegate.sleep(sleepTime);
        }

    }

    /******************** confirm window *************************/

    public void confirmWindow(String title, String buttonText)
        throws RemoteException {
        // waitUntilShellActive(title);
        activateShellWithText(title);
        clickButton(buttonText);
        delegate.sleep(sleepTime);
    }

    public void confirmWindowWithCheckBox(String title, String buttonText,
        boolean isChecked) throws RemoteException {
        activateShellWithText(title);
        if (isChecked)
            clickCheckBox();
        clickButton(buttonText);
        delegate.sleep(sleepTime);
    }

    public void confirmWindowWithCheckBox(String shellName, String buttonText,
        String... itemNames) throws RemoteException {
        // waitUntilShellActive(shellName);
        for (String itemName : itemNames) {
            selectCheckBoxWithText(itemName);
        }
        clickButton(buttonText);
        // waitUntilShellCloses(shellName);
    }

    public void confirmWindowWithTable(String shellName, String itemName,
        String buttonName) throws RemoteException {
        // waitUntilShellActive(shellName);
        try {
            delegate.table().select(itemName);
            waitUntilButtonEnabled(buttonName);
            clickButton(buttonName);
            // waitUntilShellCloses(shellName);
        } catch (WidgetNotFoundException e) {
            log.error("tableItem" + itemName + "can not be fund!");
        }
    }

    public void confirmWindowWithTree(String shellName, String buttonName,
        String... itemNames) throws RemoteException {
        // waitUntilShellActive(shellName);
        SWTBotTree tree = delegate.tree();
        selectTreeWithLabels(tree, itemNames);
        waitUntilButtonEnabled(buttonName);
        clickButton(buttonName);
        // waitUntilShellCloses(shellName);
    }

    public void confirmWindowWithTreeWithFilterText(String shellName,
        String NameOfParentTree, String treeName, String buttonName)
        throws RemoteException {
        // waitUntilShellActive(shellName);
        setTextWithoutLabel(SarosConstant.TEXT_FIELD_TYPE_FILTER_TEXT, treeName);
        waitUntilTreeExisted(delegate.tree(), NameOfParentTree);
        SWTBotTreeItem treeItem = delegate.tree(0)
            .getTreeItem(NameOfParentTree);
        waitUntilTreeItemExisted(treeItem, treeName);
        treeItem.getNode(treeName).select();
        waitUntilButtonEnabled(buttonName);
        clickButton(buttonName);
        // waitUntilShellCloses(shellName);
    }

    /******************** is... **************************/

    public boolean isJavaProjectExist(String projectName)
        throws RemoteException {
        openPackageExplorerView();
        activatePackageExplorerView();
        SWTBotTree tree = getViewWithText(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER).bot().tree();
        return isTreeItemOfTreeExisted(tree, projectName);
    }

    public boolean isJavaClassExist(String projectName, String pkg,
        String className) throws RemoteException {
        openPackageExplorerView();
        activatePackageExplorerView();
        return isTreeItemExist(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
            projectName, "src", pkg, className + ".java");

    }

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
        return delegate.activeView().getTitle().equals(title);
        // try {
        // return delegate.viewByTitle(title).isActive();
        // } catch (WidgetNotFoundException e) {
        // log.info("view" + title + "can not be fund!");
        // return false;
        // }
    }

    public boolean isPerspectiveOpen(String title) throws RemoteException {
        try {
            return delegate.perspectiveByLabel(title) != null;
        } catch (WidgetNotFoundException e) {
            log.warn("perspective '" + title + "' doesn't exist!");
            return false;
        }
    }

    public boolean isPerspectiveActive(String title) throws RemoteException {
        try {
            return delegate.perspectiveByLabel(title).isActive();
        } catch (WidgetNotFoundException e) {
            log.warn("perspective '" + title + "' doesn't exist!");
            return false;
        }
    }

    public boolean isEditorOpen(String name) throws RemoteException {
        try {
            return delegate.editorByTitle(name) != null;
        } catch (WidgetNotFoundException e) {
            log.warn("Editor '" + name + "' doesn't exist!");
            return false;
        }
    }

    public boolean isJavaEditorActive(String className) throws RemoteException {
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
        SWTBotTreeItem item = selectTreeWithLabelsInView(viewName, labels);
        try {
            item.contextMenu(contextName);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isTreeItemOfTreeExisted(SWTBotTree tree, String label)
        throws RemoteException {
        // return getAllItemsOftreeInView().contains(label);
        try {

            tree.getTreeItem(label);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean isTreeItemExist(String viewTitle, String... paths)
        throws RemoteException {

        SWTBotTree tree = getViewWithText(viewTitle).bot().tree();
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
        activateViewWithTitle(viewName);
        SWTBotTable table = getViewWithText(viewName).bot().table();
        return isTableItemExist(table, itemName);
    }

    public boolean isTableItemInWindowExist(String title, String label)
        throws RemoteException {
        activateShellWithText(title);
        SWTBotShell shell = getShellWithText(title);
        SWTBotTable table = shell.bot().table();
        return isTableItemExist(table, label);
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

    /************* get widget **************************/

    public SWTBotTreeItem getTreeWithLabels(SWTBotTree tree, String... labels) {
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

    public SWTBotView getViewWithText(String title) throws RemoteException {
        SWTBotView view = delegate.viewByTitle(title);
        view.show();
        return view;
    }

    public SWTBotShell getShellWithText(String text) throws RemoteException {
        return delegate.shell(text);
    }

    public SWTBotShell getCurrentActiveShell() {
        return delegate.activeShell();
    }

    public List<String> getViewTitles() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotView view : delegate.views())
            list.add(view.getTitle());
        return list;
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

    public String getJavaTextOnLine(String projectName, String packageName,
        String className, int line) throws RemoteException {
        openJavaFileWithEditor(projectName, packageName, className);
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
        openJavaFileWithEditor(projectName, packageName, className);
        activateJavaEditor(className);
        return getLineBackground(getJavaEditor(className), line);
    }

    protected RGB getLineBackground(SWTBotEclipseEditor editor, int line) {
        return editor.getLineBackground(line);
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

    public SWTBotEclipseEditor getJavaEditor(String className)
        throws RemoteException {
        return getTextEditor(className + ".java");
    }

    public String getTextOfJavaEditor(String projectName, String packageName,
        String className) throws RemoteException {
        openJavaFileWithEditor(projectName, packageName, className);
        activateJavaEditor(className);
        return getTextEditor(className + ".java").getText();
    }

    public SWTBotToolbarButton getToolbarButtonWithTooltipInView(
        String viewName, String buttonTooltip) throws RemoteException {
        for (SWTBotToolbarButton toolbarButton : delegate.viewByTitle(viewName)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(buttonTooltip)) {
                return toolbarButton;
            }
        }
        return null;
    }

    /***************** select tree ******************/

    public SWTBotTree selectTreeWithLabel(String label) throws RemoteException {
        return delegate.treeWithLabel(label);
    }

    public void selectLineInJavaEditor(int line, String fileName)
        throws RemoteException {
        selectLineInEditor(line, fileName + ".java");
    }

    protected void selectLineInEditor(int line, String fileName) {
        getTextEditor(fileName).selectLine(line);
    }

    public SWTBotTreeItem selectTreeWithLabels(SWTBotTree tree,
        String... labels) throws RemoteException {
        try {
            return tree.expandNode(labels).select();
        } catch (WidgetNotFoundException e) {
            log.error("treeitem not found");
            return null;
        }
        // SWTBotTreeItem selectedTreeItem = null;
        // for (String label : labels) {
        // try {
        // if (selectedTreeItem == null) {
        // waitUntilTreeExisted(tree, label);
        // selectedTreeItem = tree.expandNode(label);
        // log.info("treeItem name: " + selectedTreeItem.getText());
        // } else {
        // waitUntilTreeItemExisted(selectedTreeItem, label);
        // selectedTreeItem = selectedTreeItem.expandNode(label);
        // log.info("treeItem name: " + selectedTreeItem.getText());
        // }
        // } catch (WidgetNotFoundException e) {
        // log.error("treeitem \"" + label + "\" not found");
        // }
        // }
        // if (selectedTreeItem != null) {
        // selectedTreeItem.select();
        // return selectedTreeItem;
        // }
        // return null;
    }

    /**
     * @param viewTitle
     *            the title of the specified view
     * @param labels
     *            all labels on the widget
     * @return a {@link SWTBotTreeItem} with the specified <code>label</code>.
     */

    public SWTBotTreeItem selectTreeWithLabelsInView(String viewName,
        String... labels) throws RemoteException {
        // SWTBotView view =
        // getViewByTitle(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
        //
        // Composite composite = (Composite) view.getWidget();
        // Tree swtTree = delegate.widget(WidgetMatcherFactory
        // .widgetOfType(Tree.class), composite);
        //
        // SWTBotTree tree = new SWTBotTree(swtTree);
        SWTBotView view = getViewWithText(viewName);
        SWTBotTree tree = view.bot().tree();
        return selectTreeWithLabels(tree, labels);
    }

    public SWTBotTableItem selectTableItemWithLabel(SWTBotTable table,
        String label) throws RemoteException {
        try {
            waitUntilTableItemExisted(table, label);
            return table.getTableItem(label);

        } catch (WidgetNotFoundException e) {
            log.warn("table item " + label + " not found.", e);
        }
        return null;
    }

    public SWTBotTableItem selectTableItemWithLabelInView(String viewName,
        String label) throws RemoteException {
        try {

            SWTBotView view = getViewWithText(viewName);
            SWTBotTable table = view.bot().table();
            return selectTableItemWithLabel(table, label).select();
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

    public void activateJavaEditor(String className) throws RemoteException {
        activateEditor(className + ".java");
    }

    public SWTBotShell activateShellWithText(String text)
        throws RemoteException {
        SWTBotShell shell;
        try {
            shell = delegate.shell(text);
            if (!shell.isActive())
                return shell.activate();
        } catch (Exception e) {
            throw new RuntimeException("Could not find shell with title "
                + text);
        }
        return null;
    }

    public SWTBotShell activateShellWithMatchText(String matchText)
        throws RemoteException {
        try {
            SWTBotShell[] shells = delegate.shells();
            for (SWTBotShell shell : shells) {
                if (shell.getText().matches(matchText)) {
                    if (!shell.isActive()) {
                        shell.activate();
                        return shell;
                    }
                }
            }
        } catch (WidgetNotFoundException e) {
            log.error("Don't fund gematched Shell with the Text\"" + matchText
                + "\"!");
        }
        return null;
    }

    /**
     * Should be only called if View is open
     */
    public void activateViewWithTitle(String title) throws RemoteException {
        try {
            if (!isViewActive(title)) {
                delegate.viewByTitle(title).setFocus();
                // waitUntil(SarosConditions.isViewActive(delegate, title));
            }
        } catch (WidgetNotFoundException e) {
            log.warn("Widget not found '" + title + "'", e);
        }
    }

    public void activatePackageExplorerView() throws RemoteException {
        activateViewWithTitle(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
    }

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

    protected void activateEditor(String name) {
        try {
            delegate.cTabItem(name).activate();
        } catch (WidgetNotFoundException e) {
            log.warn("tableItem not found '", e);
        }
        // waitUntilEditorActive(name);
    }

    /************* click ********************/

    public void clickButton() throws RemoteException {
        delegate.button().click();
    }

    public void clickButton(int num) throws RemoteException {
        delegate.button(num).click();
    }

    public void clickButton(String name) {
        delegate.button(name).click();
    }

    public void clickButton(String mnemonicText, int index)
        throws RemoteException {
        delegate.button(mnemonicText, index).click();
    }

    public void clickMenuWithText(String text) throws RemoteException {
        SWTBotMenu menu = delegate.menu(text);
        menu.click();
    }

    public void clickMenuWithTexts(String... texts) throws RemoteException {
        SWTBotMenu selectedmenu = null;
        for (String text : texts) {
            try {
                if (selectedmenu == null)
                    selectedmenu = delegate.menu(text);
                else
                    selectedmenu = selectedmenu.menu(text);
            } catch (WidgetNotFoundException e) {
                log.error("menu \"" + text + "\" not found!");
            }
        }
        if (selectedmenu != null)
            selectedmenu.click();
    }

    public void clickContextMenuOfTreeInView(String viewName, String context,
        String... itemNames) throws RemoteException {
        // SWTBotView view = getViewByTitle(viewName);
        // SWTBotTree tree = view.bot().tree();
        //
        // SWTBotTreeItem selectedTreeItem = null;
        // for (String itemName : itemNames) {
        // if (selectedTreeItem == null) {
        // selectedTreeItem = tree.expandNode(itemName);
        // } else
        // selectedTreeItem = selectedTreeItem.expandNode(itemName);
        // log.info("treeName: " + selectedTreeItem.getText());
        // }
        // if (selectedTreeItem != null) {
        // selectedTreeItem.select();
        // delegate.sleep(sleepTime);
        // selectedTreeItem.contextMenu(contextName).click();
        // delegate.sleep(sleepTime);
        // }

        SWTBotTreeItem treeItem = selectTreeWithLabelsInView(viewName,
            itemNames);
        treeItem.contextMenu(context).click();
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

    public SWTBotToolbarButton clickToolbarButtonWithTooltipInView(
        String viewName, String buttonTooltip) throws RemoteException {
        // return
        // delegate.viewByTitle(title).toolbarButton(buttonTooltip).click();
        for (SWTBotToolbarButton toolbarButton : delegate.viewByTitle(viewName)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(buttonTooltip)) {
                return toolbarButton.click();
            }
        }
        // SWTBotToolbarButton button =
        // getToolbarButtonWithTooltipInViewWithTitle(
        // title, buttonTooltip);
        // if (button != null) {
        // // waitUntilButtonEnabled(button.getText());
        //
        // button.click();
        // delegate.sleep(sleepTime);
        // return button;
        // }
        return null;

        // throw new RemoteException("Button with tooltip '" + buttonTooltip
        // + "' was not found on view with title '" + title + "'");
    }

    public void clickToolbarPushButtonWithTooltipInView(String viewName,
        String tooltip) throws RemoteException {
        delegate.viewByTitle(viewName).toolbarPushButton(tooltip).click();
    }

    public void clickCheckBox(String title) throws RemoteException {
        delegate.checkBox(title).click();
        delegate.sleep(sleepTime);
    }

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

    /**************** set... *******************/
    public void setTextWithLabel(String label, String text)
        throws RemoteException {
        delegate.textWithLabel(label).setText(text);
    }

    public void setTextWithoutLabel(String match, String replace)
        throws RemoteException {
        delegate.text(match).setText(replace);
    }

    public void setTextinEditor(String contents, String fileName)
        throws RemoteException {
        SWTBotEclipseEditor e = getTextEditor(fileName);
        e.setText(contents);
        e.save();
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

    public void waitUntilFileEqualWithFile(String projectName,
        String packageName, String className, String file)
        throws RemoteException {
        waitUntil(SarosConditions.isFilesEqual(this, projectName, packageName,
            className, file));
    }

    public void waitUntilShellCloses(SWTBotShell shell) throws RemoteException {
        waitUntil(shellCloses(shell));
    }

    public void waitUntilShellCloses(String shellText) throws RemoteException {
        waitUntil(SarosConditions.isShellClosed(delegate, shellText));
    }

    public void waitUntilJavaEditorActive(String className)
        throws RemoteException {
        waitUntilEditorActive(className + ".java");
    }

    public void waitUntilEditorActive(String name) throws RemoteException {
        waitUntil(SarosConditions.isEditorActive(delegate, name));
    }

    public void waitUntilTableHasRows(int row) throws RemoteException {
        waitUntil(tableHasRows(delegate.table(), row));
    }

    public void waitUntilTableItemExisted(SWTBotTable table,
        String tableItemName) throws RemoteException {
        waitUntil(SarosConditions.existTableItem(table, tableItemName));
    }

    public void waitUntilTreeItemExisted(SWTBotTreeItem treeItem,
        String nodeName) {
        waitUntil(SarosConditions.existTreeItem(treeItem, nodeName));
    }

    public void waitUntilTreeExisted(SWTBotTree tree, String nodeName)
        throws RemoteException {
        waitUntil(SarosConditions.existTree(tree, nodeName));
    }

    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException {
        waitUntil(Conditions.widgetIsEnabled(delegate.button(mnemonicText)));
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
        waitUntil(Conditions.widgetIsEnabled(delegate
            .buttonWithTooltip(tooltipText)));
    }

    public void waitUntilContextMenuOfTableItemEnabled(
        SWTBotTableItem tableItem, String context) throws RemoteException {
        waitUntil(SarosConditions.ExistContextMenuOfTableItem(tableItem,
            context));
    }

    public void waitUntilShellIsActive(String shellText) {
        waitUntil(Conditions.shellIsActive(shellText));
    }

    public void waitUntilShellActive(String title) throws RemoteException {
        waitUntil(SarosConditions.ShellActive(delegate, title));
    }

    protected void waitUntil(ICondition condition) throws TimeoutException {
        delegate.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }

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
        openJavaFileWithEditor(projectName, packageName, className);
        activateJavaEditor(className);
        selectLineInJavaEditor(line, className);
        clickMenuWithTexts("Run", "Toggle Breakpoint");

    }

    public void debugJavaFile(String projectName, String packageName,
        String className) throws RemoteException {
        openJavaFileWithEditor(projectName, packageName, className);
        activateJavaEditor(className);
        clickMenuWithTexts("Debug");
        if (isShellActive("Confirm Perspective Switch"))
            confirmWindow("Confirm Perspective Switch",
                SarosConstant.BUTTON_YES);
        openPerspectiveWithName("Debug");
    }
}
