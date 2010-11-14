package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.stf.client.Musician;
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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ExStateObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExChatViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExChatViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExRemoteScreenViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExRemoteScreenViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExRosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExRosterViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExMainMenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExSarosMainMenuObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExSarosPopUpWindowObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExSessionViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExSessionViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExWorkbenchObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExWorkbenchObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExBasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseBasicObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExEditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseEditorObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExPackageExplorerViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PackageExplorerViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExProgressViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewObjectImp;

/**
 * SarosRmiSWTWorkbenchBot controls Eclipse Saros from the GUI perspective. It
 * exports {@link ExStateObject} via RMI. You should not use this within
 * tests. Have a look at {@link Musician} if you want to write tests.
 * 
 */
public class STFController {

    private static final transient Logger log = Logger
        .getLogger(STFController.class);

    public static final transient String TEMPDIR = System
        .getProperty("java.io.tmpdir");

    private static transient STFController stfController;

    public static transient SarosSWTBot sarosSWTBot;

    public int sleepTime = 750;

    /** The RMI registry used, is not exported */
    private static transient Registry registry;

    /**
     * {@link STFController} is a singleton, but inheritance is possible.
     */
    public static STFController getInstance() {
        if (sarosSWTBot != null && stfController != null)
            return stfController;
        SarosSWTBot swtwbb = new SarosSWTBot();
        stfController = new STFController(swtwbb);
        return stfController;
    }

    /**
     * Initiate {@link STFController} and all the no exported objects.
     */
    protected STFController(SarosSWTBot bot) {
        super();
        assert bot != null : "SarosSWTBot is null";
        sarosSWTBot = bot;
        EclipseObject.bot = sarosSWTBot;
        EclipseObject.sleepTime = sleepTime;
        initNoExportedObects();
    }

    private void initNoExportedObects() {
        EclipseObject.tableO = new TableObject();
        EclipseObject.toolbarO = new ToolbarObject();
        EclipseObject.treeO = new TreeObject();
        EclipseObject.viewO = new ViewObject();
        EclipseObject.perspectiveO = new PerspectiveObject();
        EclipseObject.editorO = new EditorObject();
        EclipseObject.helperO = new HelperObject();
        EclipseObject.menuO = new MenuObject();
        EclipseObject.windowO = new WindowObject();
        EclipseObject.basicO = new BasicObject();
    }

    /*
     * sometimes when connecting to a server i'm getting error:
     * java.rmi.NoSuchObjectException:no Such object in table. This happens when
     * the remote object the stub refers to has been DGC'd and GC's locally. My
     * solution is keeping a static references "classVariable" to the object in
     * the object in the server JVM.
     */
    public void initExportedObjects(int port, Saros saros,
        SessionManager sessionManager, DataTransferManager dataTransferManager,
        EditorManager editorManager) throws RemoteException {
        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            registry = LocateRegistry.getRegistry(port);
        }

        EclipseObject.exBasicO = (ExBasicObject) exportObject(
            EclipseBasicObjectImp.getInstance(), "basicObject");

        EclipseObject.exPackageExplorerVO = (ExPackageExplorerViewObject) exportObject(
            PackageExplorerViewObjectImp.getInstance(), "packageExplorerView");

        EclipseObject.exProgressVO = (ExProgressViewObject) exportObject(
            ProgressViewObjectImp.getInstance(), "progressView");

        EclipseObject.exMainMenuO = (ExMainMenuObject) exportObject(
            ExSarosMainMenuObjectImp.getInstance(), "sarosMainMenu");

        EclipseObject.exEditorO = (ExEditorObject) exportObject(
            EclipseEditorObjectImp.getInstance(), "eclipseEditor");

        EclipseObject.exRosterVO = (ExRosterViewObject) exportObject(
            ExRosterViewObjectImp.getInstance(), "rosterView");

        EclipseObject.exWindowO = (ExWindowObject) exportObject(
            ExSarosPopUpWindowObjectImp.getInstance(), "popUpWindow");

        EclipseObject.exSessonVO = (ExSessionViewObject) exportObject(
            ExSessionViewObjectImp.getInstance(), "sessionView");

        EclipseObject.exRemoteScreenVO = (ExRemoteScreenViewObject) exportObject(
            ExRemoteScreenViewObjectImp.getInstance(), "remoteScreenView");

        EclipseObject.exChatVO = (ExChatViewObject) exportObject(
            ExChatViewObjectImp.getInstance(), "chatView");

        EclipseObject.exWorkbenchO = (ExWorkbenchObject) exportObject(
            ExWorkbenchObjectImp.getInstance(), "workbench");

        EclipseObject.exStateO = (ExStateObject) exportObject(
            SarosStateObjectImp.getInstance(saros, sessionManager,
                dataTransferManager, editorManager), "state");
    }

    /**
     * Add a shutdown hook to unbind exported Object from registry.
     */
    private void addShutdownHook(final String name) {
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

    /**
     * Export object by given name on our local RMI Registry.
     */
    private Remote exportObject(Remote exportedObject, String exportName) {
        try {
            Remote remoteObject = UnicastRemoteObject.exportObject(
                exportedObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, remoteObject);
            return remoteObject;
        } catch (RemoteException e) {
            log.error("Could not export the object " + exportName, e);
        } catch (AlreadyBoundException e) {
            log.error("Could not bind the object " + exportName
                + ", because it is bound already.", e);
        }
        return null;
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
}
