package de.fu_berlin.inf.dpp.stf.swtbot;

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
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * RmiSWTWorkbenchBot delegates to {@link SWTWorkbenchBot} to implement an
 * java.rmi interface for {@link SWTWorkbenchBot}.
 */
public class RmiSWTWorkbenchBot implements IRmiSWTWorkbenchBot {
    private static final transient Logger log = Logger
        .getLogger(RmiSWTWorkbenchBot.class);

    private static final boolean SCREENSHOTS = false;

    protected static transient SWTWorkbenchBot delegate;

    /** The RMI registry used, is not exported */
    protected static transient Registry registry;

    private static transient RmiSWTWorkbenchBot self;

    protected transient String myName;

    /** RMI exported remote usable SWTWorkbenchBot replacement */
    public IRmiSWTWorkbenchBot stub;

    /** RmiSWTWorkbenchBot is a singleton */
    public static RmiSWTWorkbenchBot getInstance() {
        if (delegate != null && self != null)
            return self;

        self = new RmiSWTWorkbenchBot();
        return self;
    }

    protected RmiSWTWorkbenchBot() {
        this(new SWTWorkbenchBot());
    }

    /** RmiSWTWorkbenchBot is a singleton, but inheritance is possible */
    protected RmiSWTWorkbenchBot(SWTWorkbenchBot bot) {
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

    public void confirmWindow(String title, String buttonText)
        throws RemoteException {
        activateShellByText(title);
        clickButton(buttonText);
    }

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
        delegate.sleep(750);
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

    public void newJavaClass(String projectName, String pkg, String className) {
        try {
            delegate.menu("File").menu("New").menu("Class").click();
            delegate.sleep(750);

            delegate.shell("New Java Class").activate();
            delegate.sleep(750);
            delegate.textWithLabel("Source folder:").setText(
                projectName + "/src");
            delegate.sleep(750);
            delegate.textWithLabel("Package:").setText(pkg);
            delegate.sleep(750);
            delegate.textWithLabel("Name:").setText(className);
            delegate.sleep(750);
            delegate.button("Finish").click();
            delegate.sleep(5000);
        } catch (WidgetNotFoundException e) {
            log.error("error creating new Java Class", e);
        }
    }

    public void newJavaProject() {
        Double rand = Math.random();
        newJavaProject("Foo-" + rand.toString().substring(3, 10));
    }

    public void newJavaProject(String project) {
        SWTBotMenu menu;
        try {
            menu = delegate.menu("File").menu("New").menu("Java Project");
            delegate.sleep(750);
            menu.click();
        } catch (WidgetNotFoundException e) {
            SWTBotMenu sub = delegate.menu("File").menu("New");
            sub.menu("Project...").click();
            // delegate.activeShell();
            SWTBotTree tree = delegate.tree(); // .select("Java");
            SWTBotTreeItem item = tree.expandNode("Java");
            item.select("Java Project");
            delegate.button("Next >").click();
        }

        try {
            delegate.sleep(750);
            delegate.textWithLabel("Project name:").setText(project);
            Integer i = 0;
            while (!delegate.button("Finish").isEnabled()) {
                i++;
                delegate.textWithLabel("Project name:").setText(
                    project + i.toString());
            }
            delegate.sleep(750);
            delegate.button("Finish").click();

            try {
                delegate.sleep(750);
                delegate.button("Yes").click();
            } catch (WidgetNotFoundException e) {
                // ignore
            }

            delegate.sleep(2000);
        } catch (WidgetNotFoundException e) {
            log.error("error creating new Java Project", e);
        }
    }

    public void openJavaPackageExplorerView() throws RemoteException {
        openViewByName("Java", "Package Explorer");
    }

    public void openProblemsView() throws RemoteException {
        openViewByName("General", "Problems");
    }

    public void openProjectExplorerView() throws RemoteException {
        openViewByName("General", "Project Explorer");
    }

    public void openViewByName(String category, String nodeName) {
        delegate.sleep(750);
        delegate.menu("Window").menu("Show View").menu("Other...").click();
        delegate.sleep(750);
        SWTBotText text = delegate.text("type filter text");
        text.setText(nodeName);
        delegate.sleep(750);
        SWTBotTreeItem treeitem = delegate.tree(0).getTreeItem(category);
        delegate.sleep(750);
        treeitem.getNode(nodeName).select();
        delegate.sleep(750);
        if (delegate.button(1).isEnabled())
            delegate.button(1).click(); // OK
        else
            throw new RuntimeException("OK button was not enabled");
        delegate.sleep(750);
    }

    /**
     * Throws WidgetException if given project does not exist in Package
     * Explorer.
     */
    public void removeProject(String projectName) {
        SWTBotView view = delegate.viewByTitle("Package Explorer");
        delegate.sleep(250);
        SWTBotTree tree = view.bot().tree().select(projectName);
        SWTBotTreeItem item = tree.getTreeItem(projectName).select();
        SWTBotMenu menu = item.contextMenu("Delete");
        delegate.sleep(250);
        menu.click();
        // delegate.shell("Delete Resources").activate();
        delegate.checkBox().select();
        delegate.sleep(250);
        delegate.button("OK").click();
    }

    /*********************** low-level RMI exported Methods *******************/

    public void activeShell() {
        delegate.activeShell();
    }

    public void activateShellByText(String text) throws RemoteException {
        SWTBotShell shell = delegate.shell(text);
        if (!shell.isActive())
            shell.activate();
    }

    public void clickButton() throws RemoteException {
        delegate.button().click();
    }

    public void clickButton(String mnemonicText, int index)
        throws RemoteException {
        delegate.button(mnemonicText, index).click();
    }

    public void clickButton(int num) throws RemoteException {
        delegate.button(num).click();
    }

    public void clickButton(String name) {
        delegate.button(name).click();
    }

    public void clickMenuByName(String name) throws RemoteException {
        SWTBotMenu menu = delegate.menu(name);
        menu.click();
    }

    public void clickMenuByName(List<String> names) throws RemoteException {
        SWTBotMenu parentMenu = delegate.menu(names.remove(0));
        for (String name : names) {
            parentMenu = parentMenu.menu(name);
        }
        parentMenu.click();
    }

    public void clickToolbarButtonByTextOnViewByTitle(String title,
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

    public void clickToolbarButtonByTooltipOnViewByTitle(String title,
        String buttonTooltip) throws RemoteException {
        SWTBotView view = delegate.viewByTitle(title);
        if (view != null) {
            for (SWTBotToolbarButton button : view.getToolbarButtons()) {
                if (button.getToolTipText().matches(buttonTooltip)) {
                    button.click();
                    return;
                }
            }
        }

        throw new RemoteException("Button with tooltip '" + buttonTooltip
            + "' was not found on view with title '" + title + "'");
    }

    public void closeViewByTitle(String title) {
        delegate.viewByTitle(title).close();
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

    public boolean isShellOpenByTitle(String title) {
        try {
            SWTBotShell shell = delegate.shell(title);
            return shell.isActive();
        } catch (WidgetNotFoundException e) {
            log.info("No shell with title " + title + " was found.");
        }
        return false;
    }

    public boolean isViewOpen(String title) {
        try {
            return delegate.viewByTitle(title) != null;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public void selectTreeByLabel(String label) throws RemoteException {
        delegate.treeWithLabel(label);
    }

    /**
     * Should be only called if View is open
     */
    public void setFocusOnViewByTitle(String title) throws RemoteException {
        try {
            delegate.viewByTitle(title).setFocus();
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

    public void waitOnShellByTitle(String title) {
        try {
            while (!isShellOpenByTitle(title)) {
                Thread.sleep(250);
            }
        } catch (InterruptedException e) {
            log.warn("Code not designed to be interruptible.", e);
        }
    }

    public void captureScreenshot(String filename) throws RemoteException {
        if (SCREENSHOTS)
            delegate.captureScreenshot(filename);
    }

    /*********************** test RMI exported Methods *******************/

    public String test() {
        return "TEST";
    }
}
