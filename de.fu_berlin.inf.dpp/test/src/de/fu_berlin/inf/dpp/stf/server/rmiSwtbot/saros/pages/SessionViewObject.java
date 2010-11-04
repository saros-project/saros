package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.pages;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.SarosRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.noGUI.ISarosState;

public class SessionViewObject implements ISessionViewObject {

    private transient static final Logger log = Logger
        .getLogger(SessionViewObject.class);

    private static transient SarosRmiSWTWorkbenchBot rmiBot;
    public static SessionViewObject classVariable;

    public SessionViewObject() {
        // Default constructor needed for RMI
    }

    public SessionViewObject(SarosRmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
    }

    public void activateSharedSessionView() throws RemoteException {
        rmiBot.viewObject
            .activateViewWithTitle(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
    }

    public void giveDriverRole(String inviteeJID) throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, inviteeJID,
            SarosConstant.CONTEXT_MENU_GIVE_DRIVER_ROLE);
        rmiBot.waitUntilShellCloses("Progress Information");
    }

    public void giveExclusiveDriverRole(String inviteePlainJID)
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, inviteePlainJID,
            SarosConstant.CONTEXT_MENU_GIVE_EXCLUSIVE_DRIVER_ROLE);
        rmiBot.waitUntilShellCloses("Progress Information");
    }

    public boolean isSharedSessionViewOpen() throws RemoteException {
        return rmiBot.viewObject
            .isViewOpen(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
    }

    /**
     * "Shared Project Session" View must be open
     */
    public boolean isInSession() throws RemoteException {
        rmiBot.viewObject.activateViewWithTitle("Shared Project Session");
        return rmiBot.delegate.viewByTitle("Shared Project Session")
            .toolbarButton("Leave the session").isEnabled();
    }

    public boolean isContactInSessionView(String Contact)
        throws RemoteException {
        activateSharedSessionView();
        SWTBotTable table = rmiBot.viewObject
            .getTableInView(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
        for (int i = 0; i < table.rowCount(); i++) {
            if (table.getTableItem(i).getText().matches(".*" + Contact + ".*"))
                return true;
        }
        return false;
    }

    public boolean isFollowing() throws RemoteException {
        return rmiBot.stateObject.isFollowing();
    }

    public void openSharedSessionView() throws RemoteException {
        rmiBot.viewObject.showViewById("de.fu_berlin.inf.dpp.ui.SessionView");
    }

    public void closeSessionView() throws RemoteException {
        rmiBot.viewObject.hideViewById("de.fu_berlin.inf.dpp.ui.SessionView");
    }

    public void clickTBShareYourScreenWithSelectedUserInSPSView()
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_SHARE_SCREEN_WITH_USER);
    }

    public void clickTBStopSessionWithUserInSPSView(String name)
        throws RemoteException {
        // selectTableItemWithLabelInViewWithTitle(
        // SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION, name);
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_STOP_SESSION_WITH_USER + " " + name);
    }

    public void clickTBSendAFileToSelectedUserInSPSView(String inviteeJID)
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.selectTableItemWithLabelInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION, inviteeJID);
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_SEND_FILE_TO_SELECTED_USER);
    }

    public void clickTBOpenInvitationInterfaceInSPSView()
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickToolbarPushButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_OPEN_INVITATION_INTERFACE);
    }

    public void clickTBStartAVoIPSessionInSPSView() throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_START_VOIP_SESSION);
    }

    public void clickTBNoInconsistenciesInSPSView() throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_NO_INCONSISTENCIES);
    }

    public void clickTBRemoveAllRriverRolesInSPSView() throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_REMOVE_ALL_DRIVER_ROLES);
    }

    public void clickTBEnableDisableFollowModeInSPSView()
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_ENABLE_DISABLE_FOLLOW_MODE);
    }

    public void clickTBLeaveTheSessionInSPSView() throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_LEAVE_THE_SESSION);
    }

    public void clickCMJumpToPositionOfSelectedUserInSPSView(
        String participantJID, String sufix) throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, participantJID + sufix,
            SarosConstant.CONTEXT_MENU_JUMP_TO_POSITION_SELECTED_USER);
    }

    public void clickCMStopFollowingThisUserInSPSView(ISarosState state, JID jid)
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        if (state.isDriver(jid))
            rmiBot.viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW,
                jid.getBase() + " (Driver)",
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
        else
            rmiBot.viewObject.clickContextMenuOfTableInView(
                BotConfiguration.NAME_SESSION_VIEW, jid.getBase() + "",
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);

    }

    public void clickCMgiveExclusiveDriverRoleInSPSView(String inviteeJID)
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, inviteeJID,
            SarosConstant.CONTEXT_MENU_REMOVE_DRIVER_ROLE);
    }

    public void clickCMRemoveDriverRoleInSPSView(String inviteeJID)
        throws RemoteException {
        openSharedSessionView();
        activateSharedSessionView();
        rmiBot.viewObject.clickContextMenuOfTableInView(
            BotConfiguration.NAME_SESSION_VIEW, inviteeJID,
            SarosConstant.CONTEXT_MENU_REMOVE_DRIVER_ROLE);
    }
}
