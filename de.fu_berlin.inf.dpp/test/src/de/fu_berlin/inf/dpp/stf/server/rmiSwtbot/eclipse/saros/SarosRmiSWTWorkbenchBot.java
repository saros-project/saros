package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;
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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ISarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.PopUpWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.PopUpWindowObjectImp;
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
 * exports {@link ISarosState} via RMI. You should not use this within tests.
 * Have a look at {@link Musician} if you want to write tests.
 */
public class SarosRmiSWTWorkbenchBot extends RmiSWTWorkbenchBot implements
    ISarosRmiSWTWorkbenchBot {
    private static final transient Logger log = Logger
        .getLogger(SarosRmiSWTWorkbenchBot.class);

    public static final transient String TEMPDIR = System
        .getProperty("java.io.tmpdir");

    private static transient SarosRmiSWTWorkbenchBot self;

    /** RMI exported Saros object */
    public ISarosState stateObject;

    public RosterViewObject rosterViewObject;

    public PopUpWindowObject popupWindowObject;

    public SessionViewObject sessonViewObject;

    public RemoteScreenViewObject remoteScreenV;

    public ChatViewObject chatV;

    public WorkbenchObject workbench;

    public RosterViewObject getRosterViewObject() throws RemoteException {
        return rosterViewObject;
    }

    public PopUpWindowObject getPopupWindowObject() throws RemoteException {
        return popupWindowObject;
    }

    public SessionViewObject getSessionViewObject() throws RemoteException {
        return sessonViewObject;
    }

    /**
     * SarosRmiSWTWorkbenchBot is a singleton
     */
    public static SarosRmiSWTWorkbenchBot getInstance() {
        if (delegate != null && self != null)
            return self;

        SarosSWTBot swtwbb = new SarosSWTBot();
        self = new SarosRmiSWTWorkbenchBot(swtwbb);
        return self;
    }

    /**
     * RmiSWTWorkbenchBot is a singleton, but inheritance is possible
     */
    protected SarosRmiSWTWorkbenchBot(SarosSWTBot bot) {
        super(bot);
        tableObject = new TableObject(this);
        tBarObject = new ToolbarObject(this);
        treeObject = new TreeObject(this);
        viewObject = new ViewObject(this);
        persObject = new PerspectiveObject(this);
        editorObject = new EditorObject(this);
        mainObject = new HelperObject(this);
        menuObject = new MenuObject(this);
        windowObject = new WindowObject(this);
        basicObject = new BasicObject(this);

    }

    /**
     * Export given state object by given name on our local RMI Registry.
     */
    public void exportState(SarosState state, String exportName) {
        try {
            this.stateObject = (ISarosState) UnicastRemoteObject.exportObject(
                state, 0);
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
            this.rosterViewObject = (RosterViewObject) UnicastRemoteObject
                .exportObject(rosterView, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.rosterViewObject);
        } catch (RemoteException e) {
            log.error("Could not export rosterview object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind rosterview object, because it is bound already.",
                e);
        }
    }

    /**
     * Export given pop up window object by given name on our local RMI
     * Registry.
     */
    public void exportPopUpWindow(PopUpWindowObjectImp popUpWindowObject,
        String exportName) {
        try {
            this.popupWindowObject = (PopUpWindowObject) UnicastRemoteObject
                .exportObject(popUpWindowObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.popupWindowObject);
        } catch (RemoteException e) {
            log.error("Could not export popup window object.", e);
        } catch (AlreadyBoundException e) {
            log.error(
                "Could not bind popup window object, because it is bound already.",
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
            this.sessonViewObject = (SessionViewObject) UnicastRemoteObject
                .exportObject(sharedSessonViewObject, 0);
            addShutdownHook(exportName);
            registry.bind(exportName, this.sessonViewObject);
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
