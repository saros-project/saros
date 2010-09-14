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
    public void removeProject(String projectName) throws RemoteException {

        selectTreeWithLabelsInViewWithTitle(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, projectName);
        clickMenuWithTexts(SarosConstant.MENU_TITLE_EDIT,
            SarosConstant.MENU_TITLE_DELETE);
        confirmWindowWithCheckBox(SarosConstant.SHELL_TITLE_DELETE_RESOURCE,
            SarosConstant.BUTTON_OK, true);
        // waitUntilShellCloses(delegate
        // .shell(SarosConstant.SHELL_TITLE_DELETE_RESOURCE));
    }

    public void newJavaClass(String projectName, String pkg, String className)
        throws RemoteException {
        try {
            clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
                SarosConstant.MENU_TITLE_NEW, SarosConstant.MENU_TITLE_CLASS);
            delegate.sleep(sleepTime);
            SWTBotShell shell = activateShellWithText(SarosConstant.SHELL_TITLE_NEW_JAVA_CLASS);
            setTextWithLabel("Source folder:", projectName + "/src");
            delegate.sleep(sleepTime);
            setTextWithLabel("Package:", pkg);
            delegate.sleep(sleepTime);
            setTextWithLabel("Name:", className);
            delegate.sleep(sleepTime);
            // implementsInterface("java.lang.Runnable");
            // delegate.checkBox("Inherited abstract methods").click();
            clickCheckBox("Inherited abstract methods");
            waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
            clickButton(SarosConstant.BUTTON_FINISH);
            waitUntilShellCloses(shell);
            getEclipseEditor(projectName, pkg, className);
            delegate.sleep(sleepTime);
            // editor.navigateTo(2, 0);
            // editor.quickfix("Add unimplemented methods");
            // editor.save();
            // delegate.sleep(750);
        } catch (WidgetNotFoundException e) {
            log.error("error creating new Java Class", e);
        }
    }

    /*
     * open view
     */
    public void openJavaPackageExplorerView() throws RemoteException {
        openViewByName("Java", "Package Explorer");
    }

    public void openProblemsView() throws RemoteException {
        openViewByName("General", "Problems");
    }

    public void openProjectExplorerView() throws RemoteException {
        openViewByName("General", "Project Explorer");
    }

    public void openViewByName(String category, String nodeName)
        throws RemoteException {
        clickMenuWithTexts(SarosConstant.MENU_TITLE_WINDOW,
            SarosConstant.MENU_TITLE_SHOW_VIEW, SarosConstant.MENU_TITLE_OTHER);
        delegate.sleep(sleepTime);

        confirmWindowWithTreeWithFilterText(SarosConstant.MENU_TITLE_SHOW_VIEW,
            category, nodeName, SarosConstant.BUTTON_OK);
    }

    /*
     * confirmWindow
     */
    public void confirmWindow(String title, String buttonText)
        throws RemoteException {
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

    /*
     * isProjectIn*
     */
    public boolean isProjectInWorkspacePackageExplorer(String projectName)
        throws RemoteException {
        return isProjectInWorkspace("Package Explorer", projectName);
    }

    public boolean isProjectInWorkspaceProjectExplorer(String projectName)
        throws RemoteException {
        return isProjectInWorkspace("Project Explorer", projectName);
    }

    public boolean isProjectInWorkspace(String viewName, String projectName)
        throws RemoteException {
        setFocusOnViewByTitle(viewName);
        SWTBotView view = delegate.viewByTitle(viewName);
        delegate.sleep(sleepTime);
        SWTBotTree tree = view.bot().tree().select(projectName);

        // no projects
        if (!tree.hasItems())
            return false;
        try {
            tree.getTreeItem(projectName);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    private void implementsInterface(String interfaceClass)
        throws WidgetNotFoundException {
        delegate.button("Add...").click();
        delegate.sleep(sleepTime);
        delegate.shell("Implemented Interfaces Selection").activate();
        delegate.sleep(sleepTime);
        delegate.textWithLabel("Choose interfaces:").setText(interfaceClass);
        delegate.sleep(sleepTime);
        delegate.waitUntil(Conditions.tableHasRows(delegate.table(), 1));
        delegate.button("OK").click();
        delegate.sleep(sleepTime);
        delegate.shell("New Java Class").activate();
    }

    // public SWTBotMenu menu(String name) {
    // try {
    // return delegate.menu(name);
    // } catch (WidgetNotFoundException e) {
    // throw new RuntimeException("Widget " + name + " not found");
    // }
    // }

    public void newJavaProject() throws RemoteException {
        Double rand = Math.random();
        newJavaProject("Foo-" + rand.toString().substring(3, 10));
    }

    public void newJavaProject(String project) throws RemoteException {
        SWTBotMenu menu;
        try {
            // menu = menu("File").menu("New").menu("Java Project");
            // menu.click();
            clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
                SarosConstant.MENU_TITLE_NEW,
                SarosConstant.MENU_TITLE_JAVA_PROJECT);
        } catch (WidgetNotFoundException e) {
            // SWTBotMenu sub = delegate.menu("File").menu("New");
            // sub.menu("Project...").click();
            clickMenuWithTexts(SarosConstant.MENU_TITLE_FILE,
                SarosConstant.MENU_TITLE_NEW, "Project...");
            // SWTBotTree tree = delegate.tree(); // .select("Java");
            confirmWindowWithTreeWithFilterText("New project", "Java",
                "Java Project", SarosConstant.BUTTON_NEXT);
            // SWTBotTreeItem item = tree.expandNode("Java");
            // item.select("Java Project");
            // delegate.button("Next >").click();
        }

        try {
            delegate.sleep(sleepTime);
            // delegate.textWithLabel("Project name:").setText(project);
            setTextWithLabel("Project name:", project);
            Integer i = 0;
            while (!isButtonEnabled(SarosConstant.BUTTON_FINISH)) {
                i++;
                // delegate.textWithLabel("Project name:").setText(
                // project + i.toString());
                setTextWithLabel("Project name:", project + i.toString());
            }
            // delegate.sleep(sleepTime);
            waitUntilButtonEnabled(SarosConstant.BUTTON_FINISH);
            delegate.button("Finish").click();

            if (isShellActive("Open Associated Perspective?")) {
                // delegate.button("Yes").click();
                clickButton(SarosConstant.BUTTON_YES);
            }

            delegate.sleep(sleepTime);
        } catch (WidgetNotFoundException e) {
            log.error("error creating new Java Project", e);
        }
    }

    public SWTBotView getViewByTitle(String title) throws RemoteException {
        SWTBotView view = delegate.viewByTitle(title);
        view.show();
        return view;
    }

    /*
     * select tree
     */
    public void selectTreeWithLabel(String label) throws RemoteException {
        delegate.treeWithLabel(label);
    }

    /**
     * @param viewTitle
     *            the title of the specified view
     * @param labels
     *            all labels on the widget
     * @return a {@link SWTBotTreeItem} with the specified <code>label</code>.
     */

    public SWTBotTreeItem selectTreeWithLabelsInViewWithTitle(String viewTitle,
        String... labels) throws RemoteException {
        // SWTBotView view =
        // getViewByTitle(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
        //
        // Composite composite = (Composite) view.getWidget();
        // Tree swtTree = delegate.widget(WidgetMatcherFactory
        // .widgetOfType(Tree.class), composite);
        //
        // SWTBotTree tree = new SWTBotTree(swtTree);
        SWTBotView view = getViewByTitle(viewTitle);
        SWTBotTree tree = view.bot().tree();
        return selectTreeWithLabels(tree, labels);
    }

    public SWTBotTreeItem selectTreeWithLabels(SWTBotTree tree,
        String... labels) {
        SWTBotTreeItem selectedTreeItem = null;
        for (String label : labels) {
            try {
                if (selectedTreeItem == null) {
                    waitUntilTreeExisted(tree, label);
                    selectedTreeItem = tree.expandNode(label);
                } else {
                    waitUntilTreeItemExisted(selectedTreeItem, label);
                    selectedTreeItem = selectedTreeItem.expandNode(label);
                }
            } catch (WidgetNotFoundException e) {
                log.error("treeitem \"" + label + "\" not found");
            }
        }
        if (selectedTreeItem != null) {
            selectedTreeItem.select();
            return selectedTreeItem;
        }
        return null;
    }

    /*********************** low-level RMI exported Methods *******************/

    /**
     * i think, the method delegate.activeShell() return currently active shell.
     * if it does not exist such a shell, other deacitve shell wouldn't be
     * active.
     */
    public SWTBotShell getCurrentActiveShell() {
        return delegate.activeShell();
    }

    public SWTBotShell activateShellWithText(String text)
        throws RemoteException {
        SWTBotShell shell;
        try {
            shell = delegate.shell(text);
            if (!shell.isActive())
                shell.activate();
        } catch (Exception e) {
            throw new RuntimeException("Could not find shell with title "
                + text);
        }
        return shell;
    }

    /*
     * click
     */
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

    public void clickContextMenuOfTreeInViewWithTitle(String viewName,
        String context, String... itemNames) throws RemoteException {
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

        SWTBotTreeItem treeItem = selectTreeWithLabelsInViewWithTitle(viewName,
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

    public void clickToolbarButtonWithTextInViewWithTitle(String title,
        String buttonText) throws RemoteException {
        SWTBotView view = delegate.viewByTitle(title);
        if (view != null) {
            for (SWTBotToolbarButton button : view.getToolbarButtons()) {
                if (button.getText().matches(buttonText)) {
                    button.click();
                    return;
                }
            }
        }
        throw new RemoteException("Button with text '" + buttonText
            + "' was not found on view with title '" + title + "'");
    }

    public SWTBotToolbarButton clickToolbarButtonWithTooltipInViewWithTitle(
        String title, String buttonTooltip) throws RemoteException {

        SWTBotToolbarButton button = getToolbarButtonWithTooltipInViewWithTitle(
            title, buttonTooltip);
        if (button != null) {
            button.click();
            delegate.sleep(sleepTime);
            return button;
        }
        return null;

        // throw new RemoteException("Button with tooltip '" + buttonTooltip
        // + "' was not found on view with title '" + title + "'");
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

    public void clickContextMenuOfTableInViewWithTitle(String viewName,
        String itemName, String contextName) throws RemoteException {
        setFocusOnViewByTitle(viewName);
        try {
            SWTBotTable table = delegate.viewByTitle(viewName).bot().table();
            if (table != null) {
                SWTBotTableItem item = table.getTableItem(itemName);
                delegate.sleep(sleepTime);
                SWTBotMenu menu = item.contextMenu(contextName);
                menu.click();
                delegate.sleep(sleepTime);
            }
        } catch (WidgetNotFoundException e) {
            log.warn("contextmenu " + contextName + " of table item "
                + itemName + " on View " + viewName + " not found.", e);
        }
    }

    public SWTBotTableItem selectTableItemWithLabel(SWTBotTable table,
        String label) {
        try {
            return table.getTableItem(label);

        } catch (WidgetNotFoundException e) {
            log.warn("table item " + label + " not found.", e);
        }
        return null;
    }

    public void closeViewByTitle(String title) throws RemoteException {
        SWTBotView viewByTitle = delegate.viewByTitle(title);
        viewByTitle.close();
        delegate.sleep(sleepTime);
    }

    public List<String> getViewTitles() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotView view : delegate.views())
            list.add(view.getTitle());

        return list;
    }

    public boolean isButtonEnabled(int num) throws RemoteException {
        return delegate.button(num).isEnabled();
    }

    public boolean isButtonEnabled(String name) throws RemoteException {
        return delegate.button(name).isEnabled();
    }

    public boolean isShellActive(String title) {
        try {
            SWTBotShell activeShell = delegate.activeShell();
            if (title.equals(activeShell.getText()))
                return true;
        } catch (WidgetNotFoundException e) {
            log.info("No shell with title " + title + " was found.");
        }
        return false;
    }

    public boolean isViewOpen(String title) throws RemoteException {
        List<SWTBotView> views = delegate.views();
        for (SWTBotView swtBotView : views) {
            if (title.equals(swtBotView.getTitle()))
                return true;
        }
        log.info("View " + title + " not found.");
        return false;
    }

    /**
     * Should be only called if View is open
     */
    public void setFocusOnViewByTitle(String title) throws RemoteException {
        try {
            delegate.viewByTitle(title).setFocus();
            delegate.sleep(sleepTime);
        } catch (WidgetNotFoundException e) {
            log.warn("Widget not found '" + title + "'", e);
        }
    }

    public void setTextWithLabel(String label, String text)
        throws RemoteException {
        delegate.textWithLabel(label).setText(text);
    }

    public void setTextWithText(String match, String replace)
        throws RemoteException {
        delegate.text(match).setText(replace);
    }

    public void sleep(long millis) throws RemoteException {
        delegate.sleep(millis);
    }

    // public void waitOnShellByTitle(String title) {
    // try {
    // while (!isShellOpenByTitle(title)) {
    // Thread.sleep(250);
    // }
    // } catch (InterruptedException e) {
    // log.warn("Code not designed to be interruptible.", e);
    // }
    // }

    public void captureScreenshot(String filename) throws RemoteException {
        if (SCREENSHOTS)
            delegate.captureScreenshot(filename);
    }

    /**
     * Lin
     */
    public void activeMusician() {
        SWTBotShell[] shells = delegate.shells();
        for (SWTBotShell shell : shells) {
            log.debug(shell.getText());
        }
        if (shells != null) {
            if (!shells[0].isActive()) {
                shells[0].activate();
                delegate.sleep(sleepTime);
            }

        } else {
            if (shells == null) {
                log.warn("There are no shell!"
                    + "At the beginning with a test we need to at first run one or more saros instance with the test run-configuration. "
                    + "Please look at the test, start the accordingly saros-instances and try again.");
            } else
                log.warn("There are more than one shell! Before testing you need only to start the needed saros-instances. rest thing would be done by the test."
                    + "At the beginning with the test we should active the shell (saros instance, which would be getestet). otherweise some of Test may be"
                    + " not successfully performed, because 'delegate' can not find the suitable topmenu. deactive application in OS Mac hide also his topmenu.");
        }
    }

    /*
     * waitUntil
     */

    public void waitUntilShellCloses(SWTBotShell shell) throws RemoteException {
        waitUntil(shellCloses(shell));
    }

    public void waitUntilShellActive(String title) throws RemoteException {
        waitUntil(SarosConditions.ShellActive(delegate, title));
    }

    public void waitUntilTableHasRows(int row) throws RemoteException {
        waitUntil(tableHasRows(delegate.table(), row));
    }

    public void waitUntilConnect() {
        waitUntil(SarosConditions.isConnect(delegate));
    }

    public void waitUntilTreeItemExisted(SWTBotTreeItem treeItem,
        String nodeName) {
        waitUntil(SarosConditions.existTreeItem(treeItem, nodeName));
    }

    public void waitUntilTreeExisted(SWTBotTree tree, String nodeName) {
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

    public void waitUntilShellIsActive(String shellText) {
        waitUntil(Conditions.shellIsActive(shellText));
    }

    private void waitUntil(ICondition condition) throws TimeoutException {
        delegate.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }

    public boolean isPerspectiveOpen(String title) throws RemoteException {
        try {
            return delegate.perspectiveByLabel(title).isActive();
        } catch (WidgetNotFoundException e) {
            log.warn("perspective '" + title + "' doesn't exist!");
            return false;
        }

    }

    /*
     * confirm
     */

    public void confirmWindowWithTable(String shellName, String itemName,
        String buttonName) throws RemoteException {
        activateShellWithText(shellName);
        delegate.table().select(itemName);
        waitUntilButtonEnabled(buttonName);
        clickButton(buttonName);
        delegate.sleep(sleepTime);
    }

    public void confirmWindowWithTreeWithFilterText(String shellName,
        String NameOfParentTree, String treeName, String buttonName)
        throws RemoteException {
        activateShellWithText(shellName);
        setTextWithText(SarosConstant.TEXT_FIELD_TYPE_FILTER_TEXT, treeName);
        waitUntilTreeExisted(delegate.tree(), NameOfParentTree);
        SWTBotTreeItem treeItem = delegate.tree(0)
            .getTreeItem(NameOfParentTree);
        waitUntilTreeItemExisted(treeItem, treeName);
        treeItem.getNode(treeName).select();
        waitUntilButtonEnabled(buttonName);
        clickButton(buttonName);
        delegate.sleep(sleepTime);
    }

    public void confirmWindowWithTree(String shellName, String buttonName,
        String... itemNames) throws RemoteException {
        activateShellWithText(shellName);
        SWTBotTree tree = delegate.tree();
        selectTreeWithLabels(tree, itemNames);
        waitUntilButtonEnabled(buttonName);
        clickButton(buttonName);
    }

    public void openPerspectiveByName(String nodeName) throws RemoteException {
        clickMenuWithTexts(SarosConstant.MENU_TITLE_WINDOW,
            SarosConstant.MENU_TITLE_OPEN_PERSPECTIVE,
            SarosConstant.MENU_TITLE_OTHER);
        delegate.sleep(sleepTime);
        confirmWindowWithTable(SarosConstant.MENU_TITLE_OPEN_PERSPECTIVE,
            nodeName, SarosConstant.BUTTON_OK);
    }

    public String getTextOfClass(String projectName, String packageName,
        String className) throws RemoteException {
        return getEclipseEditor(projectName, packageName, className).getText();
    }

    public SWTBotEclipseEditor getEclipseEditor(String projectName,
        String packageName, String className) throws RemoteException {
        SWTBotEditor editor;
        try {
            editor = delegate.editorByTitle(className + ".java");
        } catch (WidgetNotFoundException e) {
            openFile(projectName, packageName, className + ".java");
            editor = delegate.editorByTitle(className + ".java");
        }
        SWTBotEclipseEditor e = editor.toTextEditor();
        delegate.cTabItem(className + ".java").activate();
        return e;
    }

    public void setTextInClass(String contents, String projectName,
        String packageName, String className) throws RemoteException {
        SWTBotEclipseEditor e = getEclipseEditor(projectName, packageName,
            className);

        // Keyboard keyboard = KeyboardFactory.getDefaultKeyboard(e.getWidget(),
        // null);
        //
        // e.navigateTo(7, 0);
        // e.setFocus();
        // keyboard.typeText("HelloWorldssdfffffffffffffffffffffffffffffffff");
        // delegate.sleep(2000);
        // // e.autoCompleteProposal("sys", "sysout - print to standard out");
        // //
        // // e.navigateTo(3, 0);
        // // e.autoCompleteProposal("main", "main - main method");
        // //
        // // e.typeText("new Thread (new HelloWorld ());");
        // if (true)
        // return;
        // e.notifyKeyboardEvent(SWT.CTRL, '2');
        // e.notifyKeyboardEvent(SWT.NONE, 'L');
        // e.notifyKeyboardEvent(SWT.NONE, '\n');
        //
        // e.typeText("\n");
        // e.typeText("thread.start();\n");
        // e.typeText("thread.join();");
        // e.quickfix("Add throws declaration");
        // e.notifyKeyboardEvent(SWT.NONE, (char) 27);
        // e.notifyKeyboardEvent(SWT.NONE, '\n');
        //
        // e.notifyKeyboardEvent(SWT.CTRL, 's');
        //
        // e.notifyKeyboardEvent(SWT.ALT | SWT.SHIFT, 'x');
        // e.notifyKeyboardEvent(SWT.NONE, 'j');

        e.setText(contents);
        e.save();
        delegate.sleep(sleepTime);
    }

    public void openFile(String projectName, String packageName,
        String className) throws RemoteException {
        clickContextMenuOfTreeInViewWithTitle(
            SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER,
            SarosConstant.CONTEXT_MENU_OPEN, projectName, "src", packageName,
            className);

        // SWTBotTree tree =
        // delegate.viewByTitle("Package Explorer").bot().tree();
        // SWTBotTreeItem item = tree.expandNode(projectName).expandNode("src")
        // .expandNode(packageName).expandNode(className + ".java");
        // log.debug("editorName: " + item.getText());
        // item.select().contextMenu("Open").click();
        delegate.sleep(sleepTime);

    }

    public boolean isEditorActive(String className) {
        try {
            SWTBotEditor editor = delegate.editorByTitle(className + ".java");
            return editor.isActive();
            // return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public void activeJavaEditor(String className) throws RemoteException {
        activeEditor(className, ".java");
    }

    private void activeEditor(String name, String extr) {
        delegate.cTabItem(name + extr).activate();
        delegate.sleep(sleepTime);
    }

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

    public boolean equalFieldTextWithText(String label, String text)
        throws RemoteException {
        return delegate.textWithLabel(label).getText().equals(text);
    }

    public boolean existContextMenuOfTableItemOnView(String viewName,
        String itemName, String contextName) throws RemoteException {
        setFocusOnViewByTitle(viewName);
        try {
            SWTBotTable table = delegate.viewByTitle(viewName).bot().table();
            if (table != null) {
                SWTBotTableItem item = table.getTableItem(itemName);
                delegate.sleep(sleepTime);
                SWTBotMenu menu = item.contextMenu(contextName);
                if (menu != null)
                    return true;
            }
        } catch (WidgetNotFoundException e) {
            log.warn("contextmenu " + contextName + " of table item "
                + itemName + " on View " + viewName + " not found.", e);
            return false;
        }
        return false;
    }

    public SWTBotToolbarButton getToolbarButtonWithTooltipInViewWithTitle(
        String title, String buttonTooltip) throws RemoteException {
        for (SWTBotToolbarButton toolbarButton : delegate.viewByTitle(title)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(buttonTooltip)) {
                return toolbarButton;
            }
        }
        return null;
    }

    public void getProjectFromSVN(String path) throws RemoteException {
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

    public SWTBotShell getShellWithText(String text) throws RemoteException {
        return delegate.shell(text);
    }

}
