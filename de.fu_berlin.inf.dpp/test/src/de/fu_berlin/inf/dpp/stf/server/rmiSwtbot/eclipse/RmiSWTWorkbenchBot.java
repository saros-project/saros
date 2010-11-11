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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.BasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.EditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages.HelperObject;
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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IEclipseBasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IEclipseEditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IEclipseMainMenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IEclipseWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IPackageExplorerViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.IProgressViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages.PackageExplorerViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.ISarosMainMenuObject;

/**
 * RmiSWTWorkbenchBot delegates to {@link SWTWorkbenchBot} to implement an
 * java.rmi interface for {@link SWTWorkbenchBot}.
 */
public class RmiSWTWorkbenchBot implements IRmiSWTWorkbenchBot {
    private static final transient Logger log = Logger
        .getLogger(RmiSWTWorkbenchBot.class);

    public static transient SarosSWTBot delegate;

    private static transient RmiSWTWorkbenchBot self;

    /** The RMI registry used, is not exported */
    protected static transient Registry registry;

    /** RMI exported remote usable SWTWorkbenchBot replacement */
    public IRmiSWTWorkbenchBot stub;

    protected transient String myName;

    public int sleepTime = 750;

    public IEclipseWindowObject eclipseWindowObject;
    public IEclipseState eclipseState;
    public IEclipseEditorObject eclipseEditorObject;
    public IPackageExplorerViewObject packageExplorerViewObject;
    public ISarosMainMenuObject mainMenuObject;
    public IProgressViewObject progressViewObject;
    public IEclipseBasicObject eclipseBasicObject;

    public TableObject tableObject;
    public ToolbarObject tBarObject;
    public TreeObject treeObject;
    public ViewObject viewObject;
    public PerspectiveObject persObject;
    public EditorObject editorObject;
    public HelperObject mainObject;
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

    /**
     * Export give eclipse basic object object by given name on our local RMI
     * Registry.
     */
    public void exportEclipseBasicObject(
        IEclipseBasicObject eclipseBasicObject, String exportName) {
        try {
            this.eclipseBasicObject = (IEclipseBasicObject) UnicastRemoteObject
                .exportObject(eclipseBasicObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.eclipseBasicObject);
        } catch (RemoteException e) {
            log.error("Could not export eclipse basic object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind eclipse basic object, because it is bound already.",
                e);
        }
    }

    /**
     * Export give progress view object by given name on our local RMI Registry.
     */
    public void exportProgressViewObject(
        IProgressViewObject progressViewObject, String exportName) {
        try {
            this.progressViewObject = (IProgressViewObject) UnicastRemoteObject
                .exportObject(progressViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.progressViewObject);
        } catch (RemoteException e) {
            log.error("Could not export progress view object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind progress view object, because it is bound already.",
                e);
        }
    }

    /**
     * Export give main menu object by given name on our local RMI Registry.
     */
    public void exportMainMenuObject(ISarosMainMenuObject sarosMainMenuObject,
        String exportName) {
        try {
            this.mainMenuObject = (ISarosMainMenuObject) UnicastRemoteObject
                .exportObject(sarosMainMenuObject, 0);
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
