package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.BasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.EditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.MainObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.MenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.PerspectiveObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.TableObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.ToolbarObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.TreeObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.ViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.WindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI.EclipseState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI.IEclipseState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.EclipseEditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.EclipseWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IEclipseEditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IEclipseMainMenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IEclipseWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IPackageExplorerViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.PackageExplorerViewObject;

/**
 * RmiSWTWorkbenchBot delegates to {@link SWTWorkbenchBot} to implement an
 * java.rmi interface for {@link SWTWorkbenchBot}.
 */
public class RmiSWTWorkbenchBot implements IRmiSWTWorkbenchBot {
    private static final transient Logger log = Logger
        .getLogger(RmiSWTWorkbenchBot.class);

    private static final boolean SCREENSHOTS = true;

    public static transient SarosSWTBot delegate;

    private static transient RmiSWTWorkbenchBot self;

    /** The RMI registry used, is not exported */
    protected static transient Registry registry;

    /** RMI exported remote usable SWTWorkbenchBot replacement */
    public IRmiSWTWorkbenchBot stub;

    protected transient String myName;

    public int sleepTime = 750;

    // public WaitUntilObject wUntilObject;
    public TableObject tableObject;
    public ToolbarObject tBarObject;
    public TreeObject treeObject;
    public ViewObject viewObject;
    public PerspectiveObject persObject;
    public EditorObject editorObject;
    public MainObject mainObject;
    public MenuObject menuObject;
    public WindowObject windowObject;
    public BasicObject basicObject;

    /** RmiSWTWorkbenchBot is a singleton */
    public static RmiSWTWorkbenchBot getInstance() {
        if (delegate != null && self != null)
            return self;

        self = new RmiSWTWorkbenchBot();
        return self;
    }

    protected RmiSWTWorkbenchBot() {
        this(new SarosSWTBot());
    }

    /** RmiSWTWorkbenchBot is a singleton, but inheritance is possible */
    protected RmiSWTWorkbenchBot(SarosSWTBot bot) {
        super();
        assert bot != null : "delegated SWTWorkbenchBot is null";
        delegate = bot;
        // wUntilObject = new WaitUntilObject(this);
        tableObject = new TableObject(this);
        tBarObject = new ToolbarObject(this);
        treeObject = new TreeObject(this);
        viewObject = new ViewObject(this);
        persObject = new PerspectiveObject(this);
        editorObject = new EditorObject(this);
        mainObject = new MainObject(this);
        menuObject = new MenuObject(this);
        windowObject = new WindowObject(this);
        basicObject = new BasicObject(this);
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

    public IEclipseWindowObject eclipseWindowObject;
    public IEclipseState eclipseState;
    public IEclipseEditorObject eclipseEditorObject;
    public IPackageExplorerViewObject packageExplorerViewObject;
    public IEclipseMainMenuObject mainMenuObject;

    /**
     * Export give main menu object by given name on our local RMI Registry.
     */
    public void exportMainMenuObject(
        IEclipseMainMenuObject eclipseMainMenuObject, String exportName) {
        try {
            this.mainMenuObject = (IEclipseMainMenuObject) UnicastRemoteObject
                .exportObject(eclipseMainMenuObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.mainMenuObject);
        } catch (RemoteException e) {
            log.error("Could not export main menu object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind main menu object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given eclipse window object by given name on our local RMI
     * Registry.
     */
    public void exportEclipseWindowObject(
        EclipseWindowObject eclipseWindowObject, String exportName) {
        try {
            this.eclipseWindowObject = (IEclipseWindowObject) UnicastRemoteObject
                .exportObject(eclipseWindowObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.eclipseWindowObject);
        } catch (RemoteException e) {
            log.error("Could not export eclipse window object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind eclipse window object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given eclipse editor object by given name on our local RMI
     * Registry.
     */
    public void exportEclipseEditorObject(
        EclipseEditorObject eclipseEditorObject, String exportName) {
        try {
            this.eclipseEditorObject = (IEclipseEditorObject) UnicastRemoteObject
                .exportObject(eclipseEditorObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.eclipseEditorObject);
        } catch (RemoteException e) {
            log.error("Could not export eclipse editor object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind eclipse editor object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given eclipse state object by given name on our local RMI
     * Registry.
     */
    public void exportEclipseState(EclipseState eclipseState, String exportName) {
        try {
            this.eclipseState = (IEclipseState) UnicastRemoteObject
                .exportObject(eclipseState, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.eclipseState);
        } catch (RemoteException e) {
            log.error("Could not export eclipse state object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind eclipse state object, because it is bound already.",
                e);
        }
    }

    /**
     * Export package Explorer view object by given name on our local RMI
     * Registry.
     */
    public void exportPackageExplorerViewObject(
        PackageExplorerViewObject packageExplorerViewObject, String exportName) {
        try {
            this.packageExplorerViewObject = (IPackageExplorerViewObject) UnicastRemoteObject
                .exportObject(packageExplorerViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.packageExplorerViewObject);
        } catch (RemoteException e) {
            log.error("Could not export package explorer view object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind package explorer view object, because it is bound already.",
                e);
        }
    }

    /*******************************************************************************
     * 
     * Progress View Page
     * 
     *******************************************************************************/
    public void openProgressView() throws RemoteException {
        viewObject.openViewById("org.eclipse.ui.views.ProgressView");
    }

    public void activateProgressView() throws RemoteException {
        viewObject.setFocusOnViewByTitle(SarosConstant.VIEW_TITLE_PROGRESS);
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
        mainMenuObject.openPerspectiveJava();
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

    public void clickButton(String mnemonicText) throws RemoteException {
        delegate.button(mnemonicText).click();
    }

    public String getSecondLabelOfProblemOccurredWindow()
        throws RemoteException {
        SWTBotShell activeShell = delegate.activeShell();
        return activeShell.bot().label(2).getText();
    }

    public IEclipseWindowObject getEclipseWindowObject() throws RemoteException {
        return eclipseWindowObject;
    }

    public IEclipseState getEclipseState() throws RemoteException {
        return eclipseState;
    }

    public IEclipseEditorObject getEclipseEditorObject() throws RemoteException {
        return eclipseEditorObject;
    }

}
