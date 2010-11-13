package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseControler;
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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosMainMenuObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosPopUpWindowObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.WorkbenchObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.WorkbenchObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseBasicObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseEditorObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PackageExplorerViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewObjectImp;

/**
 * SarosRmiSWTWorkbenchBot controls Eclipse Saros from the GUI perspective. It
 * exports {@link SarosStateObject} via RMI. You should not use this within
 * tests. Have a look at {@link Musician} if you want to write tests.
 */
public class SarosControler extends EclipseControler {

    private static final transient Logger log = Logger
        .getLogger(SarosControler.class);

    public static final transient String TEMPDIR = System
        .getProperty("java.io.tmpdir");

    private static transient SarosControler sarosControler;

    /** RMI exported Saros object */
    public SarosStateObject stateObject;
    public RosterViewObject rosterVObject;
    public SessionViewObject sessonVObject;
    public RemoteScreenViewObject remoteScreenVObject;
    public ChatViewObject chatVObject;
    public WorkbenchObject workbenchObject;

    /**
     * {@link SarosControler} is a singleton, but inheritance is possible.
     */
    public static SarosControler getInstance() {
        if (sarosSWTBot != null && sarosControler != null)
            return sarosControler;
        SarosSWTBot swtwbb = new SarosSWTBot();
        sarosControler = new SarosControler(swtwbb);
        return sarosControler;
    }

    /**
     * Initiate {@link SarosControler} and all the no exported objects.
     */
    protected SarosControler(SarosSWTBot bot) {
        super(bot);
        initNoExportedObects();
    }

    private void initNoExportedObects() {
        table = new TableObject(this);
        toolbar = new ToolbarObject(this);
        tree = new TreeObject(this);
        view = new ViewObject(this);
        perspective = new PerspectiveObject(this);
        editor = new EditorObject(this);
        helper = new HelperObject(this);
        menu = new MenuObject(this);
        window = new WindowObject(this);
        basic = new BasicObject(this);
    }

    /*
     * sometimes when connecting to a server i'm getting error:
     * java.rmi.NoSuchObjectException:no Such object in table. This happens when
     * the remote object the stub refers to has been DGC'd and GC's locally. My
     * solution is keeping a static references "classVariable" to the object in
     * the object in the server JVM.
     */
    public void init(int port, Saros saros, SessionManager sessionManager,
        DataTransferManager dataTransferManager, EditorManager editorManager)
        throws RemoteException {
        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            registry = LocateRegistry.getRegistry(port);
        }

        exportEclipseBasicObject(EclipseBasicObjectImp.getInstance(this),
            "basicObject");
        exportPackageExplorerViewObject(
            PackageExplorerViewObjectImp.getInstance(this),
            "packageExplorerView");
        exportProgressViewObject(ProgressViewObjectImp.getInstance(this),
            "progressView");
        exportMainMenuObject(SarosMainMenuObjectImp.getInstance(this),
            "sarosMainMenu");
        exportEclipseEditorObject(EclipseEditorObjectImp.getInstance(this),
            "eclipseEditor");
        exportRosterView(RosterViewObjectImp.getInstance(this), "rosterView");
        exportPopUpWindow(SarosPopUpWindowObjectImp.getInstance(this),
            "popUpWindow");
        exportSessionView(SessionViewObjectImp.getInstance(this), "sessionView");
        exportRemoteScreenView(RemoteScreenViewObjectImp.getInstance(this),
            "remoteScreenView");
        exportChatView(ChatViewObjectImp.getInstance(this), "chatView");
        exportWorkbench(WorkbenchObjectImp.getInstance(this), "workbench");
        exportState(SarosStateObjectImp.getInstance(this, saros,
            sessionManager, dataTransferManager, editorManager), "state");

    }

    /**
     * Export given saros state object by given name on our local RMI Registry.
     */
    public void exportState(SarosStateObjectImp state, String exportName) {
        try {
            this.stateObject = (SarosStateObject) UnicastRemoteObject
                .exportObject(state, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.stateObject);
        } catch (RemoteException e) {
            log.error("Could not export stat object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind stat object, because it is bound already.", e);
        }
    }

    /**
     * Export given roster view object by given name on our local RMI Registry.
     */
    public void exportRosterView(RosterViewObjectImp rosterView,
        String exportName) {
        try {
            this.rosterVObject = (RosterViewObject) UnicastRemoteObject
                .exportObject(rosterView, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.rosterVObject);
        } catch (RemoteException e) {
            log.error("Could not export rosterview object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind rosterview object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given shared session view object by given name on our local RMI
     * Registry.
     */
    public void exportSessionView(SessionViewObjectImp sharedSessonViewObject,
        String exportName) {
        try {
            this.sessonVObject = (SessionViewObject) UnicastRemoteObject
                .exportObject(sharedSessonViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.sessonVObject);
        } catch (RemoteException e) {
            log.error("Could not export shared session view object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind shared session view object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given remote screen view object by given name on our local RMI
     * Registry.
     */
    public void exportRemoteScreenView(
        RemoteScreenViewObjectImp remoteScreenViewObject, String exportName) {
        try {
            this.remoteScreenVObject = (RemoteScreenViewObject) UnicastRemoteObject
                .exportObject(remoteScreenViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.remoteScreenVObject);
        } catch (RemoteException e) {
            log.error("Could not export remote screen view object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind remote screen view object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given chat view object by given name on our local RMI Registry.
     */
    public void exportChatView(ChatViewObjectImp chatViewObject,
        String exportName) {
        try {
            this.chatVObject = (ChatViewObject) UnicastRemoteObject
                .exportObject(chatViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.chatVObject);
        } catch (RemoteException e) {
            log.error("Could not export chat view object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind chat view object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given workbench object by given name on our local RMI Registry.
     */
    public void exportWorkbench(WorkbenchObjectImp workbenchObject,
        String exportName) {
        try {
            this.workbenchObject = (WorkbenchObject) UnicastRemoteObject
                .exportObject(workbenchObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.workbenchObject);
        } catch (RemoteException e) {
            log.error("Could not export workbench object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind workbench object, because it is bound already.",
                e);
        }
    }

}
