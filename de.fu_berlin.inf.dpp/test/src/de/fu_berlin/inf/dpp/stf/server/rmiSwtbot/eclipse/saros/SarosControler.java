package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseControler;
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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RemoteScreenViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RemoteScreenViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.WorkbenchObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.WorkbenchObjectImp;

/**
 * SarosRmiSWTWorkbenchBot controls Eclipse Saros from the GUI perspective. It
 * exports {@link SarosState} via RMI. You should not use this within tests.
 * Have a look at {@link Musician} if you want to write tests.
 */
public class SarosControler extends EclipseControler {
    private static final transient Logger log = Logger
        .getLogger(SarosControler.class);

    public static final transient String TEMPDIR = System
        .getProperty("java.io.tmpdir");

    private static transient SarosControler self;

    /** RMI exported Saros object */
    public SarosState state;

    public RosterViewObject rosterV;

    public SessionViewObject sessonV;

    public RemoteScreenViewObject remoteScreenV;

    public ChatViewObject chatV;

    public WorkbenchObject workbench;

    /**
     * SarosRmiSWTWorkbenchBot is a singleton
     */
    public static SarosControler getInstance() {
        if (delegate != null && self != null)
            return self;

        SarosSWTBot swtwbb = new SarosSWTBot();
        self = new SarosControler(swtwbb);
        return self;
    }

    /**
     * RmiSWTWorkbenchBot is a singleton, but inheritance is possible
     */
    protected SarosControler(SarosSWTBot bot) {
        super(bot);
        tableObject = new TableObject(this);
        tBarObject = new ToolbarObject(this);
        treeObject = new TreeObject(this);
        viewObject = new ViewObject(this);
        persObject = new PerspectiveObject(this);
        editorObject = new EditorObject(this);
        helperObject = new HelperObject(this);
        menuObject = new MenuObject(this);
        windowObject = new WindowObject(this);
        basicObject = new BasicObject(this);

    }

    /**
     * Export given state object by given name on our local RMI Registry.
     */
    public void exportState(SarosStateImp state, String exportName) {
        try {
            this.state = (SarosState) UnicastRemoteObject
                .exportObject(state, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.state);
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
            this.rosterV = (RosterViewObject) UnicastRemoteObject.exportObject(
                rosterView, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.rosterV);
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
            this.sessonV = (SessionViewObject) UnicastRemoteObject
                .exportObject(sharedSessonViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.sessonV);
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
            this.remoteScreenV = (RemoteScreenViewObject) UnicastRemoteObject
                .exportObject(remoteScreenViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.remoteScreenV);
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
            this.chatV = (ChatViewObject) UnicastRemoteObject.exportObject(
                chatViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.chatV);
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
            this.workbench = (WorkbenchObject) UnicastRemoteObject
                .exportObject(workbenchObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.workbench);
        } catch (RemoteException e) {
            log.error("Could not export workbench object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind workbench object, because it is bound already.",
                e);
        }
    }

}
