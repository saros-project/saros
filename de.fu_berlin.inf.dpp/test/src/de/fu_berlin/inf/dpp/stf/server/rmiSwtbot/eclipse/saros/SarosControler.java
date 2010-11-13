package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseControler;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;
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
 */
public class SarosControler extends EclipseControler {

    private static final transient Logger log = Logger
        .getLogger(SarosControler.class);

    public static final transient String TEMPDIR = System
        .getProperty("java.io.tmpdir");

    private static transient SarosControler sarosControler;

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
}
