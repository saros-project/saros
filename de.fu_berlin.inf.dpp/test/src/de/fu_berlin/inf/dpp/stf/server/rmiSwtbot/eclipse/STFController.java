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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.BasicPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.EditorPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.HelperPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.MenuPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.PerspectivePart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.TablePart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ToolbarPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.TreePart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.ViewPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.WindowPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosMainMenuComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RSViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RSViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosMainMenuComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosWorkbenchComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosWorkbenchComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.BasicComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.BasicComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponenttImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PEViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PEViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewComponentImp;

/**
 * SarosRmiSWTWorkbenchBot controls Eclipse Saros from the GUI perspective. It
 * exports {@link SarosState} via RMI. You should not use this within tests.
 * Have a look at {@link Musician} if you want to write tests.
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
        EclipseComponent.bot = sarosSWTBot;
        EclipseComponent.sleepTime = sleepTime;
        initNoExportedObects();
    }

    private void initNoExportedObects() {
        EclipseComponent.tableO = new TablePart();
        EclipseComponent.toolbarO = new ToolbarPart();
        EclipseComponent.treeO = new TreePart();
        EclipseComponent.viewO = new ViewPart();
        EclipseComponent.perspectiveO = new PerspectivePart();
        EclipseComponent.editorO = new EditorPart();
        EclipseComponent.helperO = new HelperPart();
        EclipseComponent.menuO = new MenuPart();
        EclipseComponent.windowO = new WindowPart();
        EclipseComponent.basicO = new BasicPart();
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

        EclipseComponent.exBasicO = (BasicComponent) exportObject(
            BasicComponentImp.getInstance(), "basicObject");

        EclipseComponent.exPackageExplorerVO = (PEViewComponent) exportObject(
            PEViewComponentImp.getInstance(), "packageExplorerView");

        EclipseComponent.exProgressVO = (ProgressViewComponent) exportObject(
            ProgressViewComponentImp.getInstance(), "progressView");

        EclipseComponent.exMainMenuO = (SarosMainMenuComponent) exportObject(
            SarosMainMenuComponentImp.getInstance(), "sarosMainMenu");

        EclipseComponent.exEditorO = (EditorComponent) exportObject(
            EditorComponenttImp.getInstance(), "eclipseEditor");

        EclipseComponent.exRosterVO = (RosterViewComponent) exportObject(
            RosterViewComponentImp.getInstance(), "rosterView");

        // EclipseObject.exWindowO = (ExWindowObject) exportObject(
        // ExSarosPopUpWindowObjectImp.getInstance(), "popUpWindow");

        EclipseComponent.exSessonVO = (SessionViewComponent) exportObject(
            SessionViewComponentImp.getInstance(), "sessionView");

        EclipseComponent.exRemoteScreenVO = (RSViewComponent) exportObject(
            RSViewComponentImp.getInstance(), "remoteScreenView");

        EclipseComponent.exChatVO = (ChatViewComponent) exportObject(
            ChatViewComponentImp.getInstance(), "chatView");

        EclipseComponent.exWorkbenchO = (SarosWorkbenchComponent) exportObject(
            SarosWorkbenchComponentImp.getInstance(), "workbench");

        EclipseComponent.exStateO = (SarosState) exportObject(
            SarosStateImp.getInstance(saros, sessionManager,
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
