package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.ChatViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.IChatViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.IRemoteScreenViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.IRosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.ISarosWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.ISessionViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.PopUpWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.RemoteScreenViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.RosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.SessionViewObject;
import de.fu_berlin.inf.dpp.util.Util;

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

    public IRosterViewObject rosterViewObject;

    public ISarosWindowObject popupWindowObject;

    public ISessionViewObject sessonViewObject;

    public IRemoteScreenViewObject remoteScreenV;

    public IChatViewObject chatV;

    public IRosterViewObject getRosterViewObject() throws RemoteException {
        return rosterViewObject;
    }

    public ISarosWindowObject getPopupWindowObject() throws RemoteException {
        return popupWindowObject;
    }

    public ISessionViewObject getSessionViewObject() throws RemoteException {
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
    public void exportRosterView(RosterViewObject rosterView, String exportName) {
        try {
            this.rosterViewObject = (IRosterViewObject) UnicastRemoteObject
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
    public void exportPopUpWindow(PopUpWindowObject popUpWindowObject,
        String exportName) {
        try {
            this.popupWindowObject = (ISarosWindowObject) UnicastRemoteObject
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
    public void exportSessionView(SessionViewObject sharedSessonViewObject,
        String exportName) {
        try {
            this.sessonViewObject = (ISessionViewObject) UnicastRemoteObject
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
        RemoteScreenViewObject remoteScreenViewObject, String exportName) {
        try {
            this.remoteScreenV = (IRemoteScreenViewObject) UnicastRemoteObject
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
    public void exportChatView(ChatViewObject chatViewObject, String exportName) {
        try {
            this.chatV = (IChatViewObject) UnicastRemoteObject.exportObject(
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

    /*******************************************************************************
     * 
     * Chat view page
     * 
     *******************************************************************************/

    /*******************************************************************************
     * 
     * frequently used components
     * 
     *******************************************************************************/

    public void leaveSessionByPeer() throws RemoteException {
        // Need to check for isDriver before leaving.
        sessonViewObject.leaveTheSession();
        eclipseWindowObject.confirmWindow(
            SarosConstant.SHELL_TITLE_CONFIRM_LEAVING_SESSION,
            SarosConstant.BUTTON_YES);
        sessonViewObject.waitUntilSessionCloses();
    }

    public void leaveSessionByHost() throws RemoteException {
        sessonViewObject.leaveTheSession();
        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                try {
                    eclipseWindowObject.confirmWindow(
                        "Confirm Closing Session", SarosConstant.BUTTON_YES);
                } catch (RemoteException e) {
                    // no popup
                }
            }
        });
        if (eclipseWindowObject.isShellActive("Confirm Closing Session"))
            eclipseWindowObject.confirmWindow("Confirm Closing Session",
                SarosConstant.BUTTON_YES);
        sessonViewObject.waitUntilSessionCloses();
    }

    public void confirmSessionUsingNewOrExistProject(
        ISarosRmiSWTWorkbenchBot inviteeBot, JID inviterJID,
        String projectName, int typeOfSharingProject) throws RemoteException {
        inviteeBot.getEclipseWindowObject().waitUntilShellActive(
            SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        switch (typeOfSharingProject) {
        case SarosConstant.CREATE_NEW_PROJECT:
            inviteeBot.getPopupWindowObject().confirmSessionInvitationWizard(
                inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT:
            inviteeBot.getPopupWindowObject()
                .confirmSessionInvitationWizardUsingExistProject(
                    inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE:
            inviteeBot
                .getPopupWindowObject()
                .confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
                    inviterJID.getBase(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_COPY:
            inviteeBot.getPopupWindowObject()
                .confirmSessionInvitationWizardUsingExistProjectWithCopy(
                    inviterJID.getBase(), projectName);
            break;
        default:
            break;
        }
    }

    public void xmppConnect(JID jid, String password) throws RemoteException {
        log.trace("connectedByXMPP");
        boolean connectedByXMPP = rosterViewObject.isConnectedByXMPP();
        if (!connectedByXMPP) {
            log.trace("clickTBConnectInRosterView");
            rosterViewObject.clickTBConnectInRosterView();
            eclipseBasicObject.sleep(100);// wait a bit to check if shell pops
                                          // up
            log.trace("isShellActive");
            boolean shellActive = eclipseWindowObject
                .isShellActive(SarosConstant.SAROS_CONFI_SHELL_TITLE);
            if (shellActive) {
                log.trace("confirmSarosConfigurationWindow");
                popupWindowObject.confirmSarosConfigurationWizard(
                    jid.getDomain(), jid.getName(), password);
            }
            rosterViewObject.waitUntilConnected();
        }
    }

    public void openSarosViews() throws RemoteException {
        rosterViewObject.openRosterView();
        sessonViewObject.openSessionView();
        chatV.openChatView();
        remoteScreenV.openRemoteScreenView();
    }

    /*******************************************************************************
     * 
     * saros main page
     * 
     *******************************************************************************/

    public void resetSaros() throws RemoteException {
        rosterViewObject.xmppDisconnect();
        eclipseState.deleteAllProjects();
    }

}
