package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.BasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.EditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.HelperObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.MenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.PerspectiveObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.TableObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ToolbarObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.TreeObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.WindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosMainMenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosPopUpWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosPopUpWindowObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseBasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseEditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseEditorObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PackageExplorerViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PackageExplorerViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewObject;

/**
 * RmiSWTWorkbenchBot delegates to {@link SWTWorkbenchBot} to implement an
 * java.rmi interface for {@link SWTWorkbenchBot}.
 */
public class EclipseControler {
    private static final transient Logger log = Logger
        .getLogger(EclipseControler.class);

    public static transient SarosSWTBot sarosSWTBot;

    private static transient EclipseControler eclipseControler;

    public int sleepTime = 750;

    /** The RMI registry used, is not exported */
    protected static transient Registry registry;

    public SarosPopUpWindowObject windowObject;
    public EclipseEditorObject editorObject;
    public PackageExplorerViewObject packageExplorerVObject;
    public SarosMainMenuObject menuObject;
    public ProgressViewObject progressVObject;
    public EclipseBasicObject basicObject;

    public TableObject table;
    public ToolbarObject toolbar;
    public TreeObject tree;
    public ViewObject view;
    public PerspectiveObject perspective;
    public EditorObject editor;
    public HelperObject helper;
    public MenuObject menu;
    public WindowObject window;
    public BasicObject basic;

    /** RmiSWTWorkbenchBot is a singleton */
    public static EclipseControler getInstance() {
        if (sarosSWTBot != null && eclipseControler != null)
            return eclipseControler;

        eclipseControler = new EclipseControler();
        return eclipseControler;
    }

    protected EclipseControler() {
        this(new SarosSWTBot());
    }

    /** RmiSWTWorkbenchBot is a singleton, but inheritance is possible */
    protected EclipseControler(SarosSWTBot bot) {
        super();
        assert bot != null : "SarosSWTBot is null";
        sarosSWTBot = bot;

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
    public void exportEclipseBasicObject(EclipseBasicObject eclipseBasicObject,
        String exportName) {
        try {
            this.basicObject = (EclipseBasicObject) UnicastRemoteObject
                .exportObject(eclipseBasicObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.basicObject);
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
    public void exportProgressViewObject(ProgressViewObject progressViewObject,
        String exportName) {
        try {
            this.progressVObject = (ProgressViewObject) UnicastRemoteObject
                .exportObject(progressViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.progressVObject);
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
    public void exportMainMenuObject(SarosMainMenuObject sarosMainMenuObject,
        String exportName) {
        try {
            this.menuObject = (SarosMainMenuObject) UnicastRemoteObject
                .exportObject(sarosMainMenuObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.menuObject);
        } catch (RemoteException e) {
            log.error("Could not export main menu object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind main menu object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given eclipse editor object by given name on our local RMI
     * Registry.
     */
    public void exportEclipseEditorObject(
        EclipseEditorObjectImp eclipseEditorObject, String exportName) {
        try {
            this.editorObject = (EclipseEditorObject) UnicastRemoteObject
                .exportObject(eclipseEditorObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.editorObject);
        } catch (RemoteException e) {
            log.error("Could not export eclipse editor object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind eclipse editor object, because it is bound already.",
                e);
        }
    }

    /**
     * Export package Explorer view object by given name on our local RMI
     * Registry.
     */
    public void exportPackageExplorerViewObject(
        PackageExplorerViewObjectImp packageExplorerViewObject,
        String exportName) {
        try {
            this.packageExplorerVObject = (PackageExplorerViewObject) UnicastRemoteObject
                .exportObject(packageExplorerViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.packageExplorerVObject);
        } catch (RemoteException e) {
            log.error("Could not export package explorer view object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind package explorer view object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given pop up window object by given name on our local RMI
     * Registry.
     */
    public void exportPopUpWindow(SarosPopUpWindowObjectImp popUpWindowObject,
        String exportName) {
        try {
            this.windowObject = (SarosPopUpWindowObject) UnicastRemoteObject
                .exportObject(popUpWindowObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.windowObject);
        } catch (RemoteException e) {
            log.error("Could not export popup window object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind popup window object, because it is bound already.",
                e);
        }
    }
}
