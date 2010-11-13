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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RemoteScreenViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RemoteScreenViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosMainMenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosMainMenuObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosPopUpWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosPopUpWindowObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.WorkbenchObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.WorkbenchObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseBasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseBasicObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseEditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseEditorObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PackageExplorerViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PackageExplorerViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewObjectImp;

/**
 * SarosRmiSWTWorkbenchBot controls Eclipse Saros from the GUI perspective. It
 * exports {@link SarosStateObject} via RMI. You should not use this within
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
        EclipseObject.tableObject = new TableObject();
        EclipseObject.toolbarObject = new ToolbarObject();
        EclipseObject.treeObject = new TreeObject();
        EclipseObject.viewObject = new ViewObject();
        EclipseObject.persObject = new PerspectiveObject();
        EclipseObject.editorObject = new EditorObject();
        EclipseObject.helperObject = new HelperObject();
        EclipseObject.menuObject = new MenuObject();
        EclipseObject.windowObject = new WindowObject();
        EclipseObject.basicObject = new BasicObject();
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

        EclipseObject.exportedBasicObject = (EclipseBasicObject) exportObject(
            EclipseBasicObjectImp.getInstance(), "basicObject");

        EclipseObject.packageExplorerVObject = (PackageExplorerViewObject) exportObject(
            PackageExplorerViewObjectImp.getInstance(), "packageExplorerView");

        EclipseObject.progressVObject = (ProgressViewObject) exportObject(
            ProgressViewObjectImp.getInstance(), "progressView");

        EclipseObject.exportedMenuObject = (SarosMainMenuObject) exportObject(
            SarosMainMenuObjectImp.getInstance(), "sarosMainMenu");

        EclipseObject.exportedEditorObject = (EclipseEditorObject) exportObject(
            EclipseEditorObjectImp.getInstance(), "eclipseEditor");

        EclipseObject.rosterVObject = (RosterViewObject) exportObject(
            RosterViewObjectImp.getInstance(), "rosterView");

        EclipseObject.exportedWindowObject = (SarosPopUpWindowObject) exportObject(
            SarosPopUpWindowObjectImp.getInstance(), "popUpWindow");

        EclipseObject.sessonVObject = (SessionViewObject) exportObject(
            SessionViewObjectImp.getInstance(), "sessionView");

        EclipseObject.remoteScreenVObject = (RemoteScreenViewObject) exportObject(
            RemoteScreenViewObjectImp.getInstance(), "remoteScreenView");

        EclipseObject.chatVObject = (ChatViewObject) exportObject(
            ChatViewObjectImp.getInstance(), "chatView");

        EclipseObject.workbenchObject = (WorkbenchObject) exportObject(
            WorkbenchObjectImp.getInstance(), "workbench");

        EclipseObject.stateObject = (SarosStateObject) exportObject(
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
