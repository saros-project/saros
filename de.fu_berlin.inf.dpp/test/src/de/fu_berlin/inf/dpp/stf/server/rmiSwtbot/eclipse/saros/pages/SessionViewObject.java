package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ISarosState;

/**
 * This implementation of {@link ISessionViewObject}
 * 
 * @author Lin
 */
public class SessionViewObject implements ISessionViewObject {

    private transient static final Logger log = Logger
        .getLogger(SessionViewObject.class);

    private SarosRmiSWTWorkbenchBot rmiBot;

    public static SessionViewObject classVariable;

    private String viewName = SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION;
    private String viewID = SarosConstant.ID_SESSION_VIEW;
    private String progressShellName = SarosConstant.SHELL_TITLE_PROGRESS_INFORMATION;
    private String roleName = SarosConstant.ROLENAME;
    private String followThisUser = SarosConstant.CONTEXT_MENU_FOLLOW_THIS_USER;

    public SessionViewObject() {
        // Default constructor needed for RMI
    }

    /**
     * constructs a SessionViewObject, which would only be created in class
     * {@links StartupSaros} and then exported by
     * {@link SarosRmiSWTWorkbenchBot} on our local RMI Registry.
     * 
     * @param rmiBot
     *            controls Saros from the GUI perspective and manage all
     *            exported rmi-objects.
     */
    public SessionViewObject(SarosRmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
    }

    /**************************************************************
     * 
     * exported function
     * 
     **************************************************************/
    public boolean isInSession() throws RemoteException {
        precondition();
        SWTBotToolbarButton toolbarButton = rmiBot.viewObject
            .getToolbarButtonWithTooltipInView(viewName,
                SarosConstant.TOOL_TIP_TEXT_LEAVE_THE_SESSION);
        return toolbarButton.isEnabled();
    }

    public void openSessionView() throws RemoteException {
        if (!isSessionViewOpen())
            rmiBot.viewObject.openViewById(viewID);
    }

    public boolean isSessionViewOpen() throws RemoteException {
        return rmiBot.viewObject.isViewOpen(viewName);
    }

    public void setFocusOnSessionView() throws RemoteException {
        rmiBot.viewObject.setFocusOnViewByTitle(viewName);
        rmiBot.viewObject.waitUntilViewActive(viewName);
    }

    public boolean isSessionViewActive() throws RemoteException {
        return rmiBot.viewObject.isViewActive(viewName);
    }

    public void closeSessionView() throws RemoteException {
        if (isSessionViewOpen())
            rmiBot.viewObject.closeViewById(viewID);
    }

    public boolean isContactInSessionView(String contactName)
        throws RemoteException {
        precondition();
        SWTBotTable table = rmiBot.tableObject.getTable();
        for (int i = 0; i < table.rowCount(); i++) {
            if (table.getTableItem(i).getText().equals(contactName))
                return true;
        }
        return false;
    }

    public void giveDriverRole(ISarosState stateOfInvitee)
        throws RemoteException {
        JID InviteeJID = stateOfInvitee.getJID();
        if (stateOfInvitee.isDriver(InviteeJID)) {
            throw new RuntimeException(
                "User \""
                    + InviteeJID.getBase()
                    + "\" is already a driver! Please pass a correct Musician Object to the method.");
        }
        precondition();
        rmiBot.tableObject.clickContextMenuOfTable(InviteeJID.getBase(),
            SarosConstant.CONTEXT_MENU_GIVE_DRIVER_ROLE);
        rmiBot.waitUntilShellClosed(progressShellName);
    }

    public void giveExclusiveDriverRole(String inviteeBaseJID)
        throws RemoteException {
        precondition();
        rmiBot.tableObject.clickContextMenuOfTable(inviteeBaseJID,
            SarosConstant.CONTEXT_MENU_GIVE_EXCLUSIVE_DRIVER_ROLE);
        rmiBot.waitUntilShellClosed(progressShellName);
    }

    public void removeDriverRole(String inviteeBaseJID) throws RemoteException {
        precondition();
        rmiBot.tableObject.clickContextMenuOfTable(inviteeBaseJID + roleName,
            SarosConstant.CONTEXT_MENU_REMOVE_DRIVER_ROLE);
        rmiBot.waitUntilShellClosed(progressShellName);
    }

    public void followThisUser(ISarosState stateOfFollowedUser)
        throws RemoteException {
        precondition();
        JID JIDOfFollowedUser = stateOfFollowedUser.getJID();
        if (rmiBot.stateObject.isInFollowMode()
            && rmiBot.stateObject.isSameUser(JIDOfFollowedUser)) {
            log.debug(JIDOfFollowedUser.getBase()
                + " is already followed by you.");
            return;
        }
        log.debug("JID of the followed User: " + JIDOfFollowedUser.getBase());
        if (rmiBot.stateObject.isSameUser(JIDOfFollowedUser)) {
            throw new RuntimeException(
                "Hi guy, you can't follow youself, it makes no sense! Please pass a correct parameter to the method.");
        }
        if (stateOfFollowedUser.isDriver()) {
            rmiBot.tableObject.clickContextMenuOfTable(
                JIDOfFollowedUser.getBase() + roleName, followThisUser);
        } else
            rmiBot.tableObject.clickContextMenuOfTable(
                JIDOfFollowedUser.getBase() + "", followThisUser);
    }

    public boolean isInFollowMode() throws RemoteException {
        precondition();
        List<String> allContactsName = getAllContactsInSessionView();
        for (String contactName : allContactsName) {
            // SWTBotTable table = rmiBot.tableObject.getTable();
            if (!rmiBot.tableObject.existContextOfTableItem(contactName,
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER))
                continue;
            if (isStopFollowingThisUserEnabled(contactName))
                return true;
        }
        return false;
    }

    public void stopFollowing() throws RemoteException {
        JID followedUserJID = rmiBot.stateObject.getFollowedUserJID();
        if (followedUserJID == null) {
            log.debug(" You are not in follow mode, so you don't need to perform thhe function.");
            return;
        }
        log.debug(" JID of the followed user: " + followedUserJID.getBase());
        precondition();
        if (rmiBot.stateObject.isDriver(followedUserJID))
            rmiBot.tableObject.clickContextMenuOfTable(
                followedUserJID.getBase() + roleName,
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
        else
            rmiBot.tableObject.clickContextMenuOfTable(
                followedUserJID.getBase(),
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
    }

    public void stopFollowingThisUser(ISarosState stateOfFollowedUser)
        throws RemoteException {
        precondition();
        JID followedUserJID = stateOfFollowedUser.getJID();
        if (!rmiBot.stateObject.isInFollowMode()) {
            log.debug(" You are not in follow mode, so you don't need to perform thhe function.");
            return;
        }
        if (rmiBot.stateObject.isSameUser(followedUserJID)) {
            throw new RuntimeException(
                "Hi guy, you can't stop following youself, it makes no sense! Please pass a correct parameter to the method.");
        }
        if (stateOfFollowedUser.isDriver(followedUserJID))
            rmiBot.tableObject.clickContextMenuOfTable(
                followedUserJID.getBase() + roleName,
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
        else
            rmiBot.tableObject.clickContextMenuOfTable(
                followedUserJID.getBase(),
                SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
    }

    public boolean isStopFollowingThisUserVisible(String contactName)
        throws RemoteException {
        return rmiBot.tableObject.isContextMenuOfTableVisible(contactName,
            SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
    }

    public boolean isStopFollowingThisUserEnabled(String contactName)
        throws RemoteException {
        return rmiBot.tableObject.isContextMenuOfTableEnabled(contactName,
            SarosConstant.CONTEXT_MENU_STOP_FOLLOWING_THIS_USER);
    }

    public void shareYourScreenWithSelectedUser(ISarosState respondentState)
        throws RemoteException {
        JID respondentJID = respondentState.getJID();
        if (rmiBot.stateObject.isSameUser(respondentJID)) {
            throw new RuntimeException(
                "Hi guy, you can't share screen with youself, it makes no sense! Please pass a correct parameter to the method.");
        }
        precondition();
        if (respondentState.isDriver(respondentJID)) {
            rmiBot.tableObject.selectTableItemWithLabel(respondentJID.getBase()
                + roleName);
        } else {
            rmiBot.tableObject
                .selectTableItemWithLabel(respondentJID.getBase());
        }
        clickToolbarButtonWithTooltip(SarosConstant.TOOL_TIP_TEXT_SHARE_SCREEN_WITH_USER);
    }

    public void stopSessionWithUser(String name) throws RemoteException {
        precondition();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_STOP_SESSION_WITH_USER + " " + name);
    }

    public void sendAFileToSelectedUser(String inviteeJID)
        throws RemoteException {
        precondition();
        rmiBot.viewObject.selectTableItemWithLabelInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION, inviteeJID);
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_SEND_FILE_TO_SELECTED_USER);
    }

    public void openInvitationInterface() throws RemoteException {
        precondition();
        rmiBot.viewObject.clickToolbarPushButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_OPEN_INVITATION_INTERFACE);
    }

    public void startAVoIPSession() throws RemoteException {
        precondition();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_START_VOIP_SESSION);
    }

    public void noInconsistencies() throws RemoteException {
        precondition();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_NO_INCONSISTENCIES);
    }

    public void removeAllRriverRoles() throws RemoteException {
        precondition();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_REMOVE_ALL_DRIVER_ROLES);
    }

    public void enableDisableFollowMode() throws RemoteException {
        precondition();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_ENABLE_DISABLE_FOLLOW_MODE);
    }

    public void leaveTheSession() throws RemoteException {
        precondition();
        rmiBot.viewObject.clickToolbarButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_LEAVE_THE_SESSION);
    }

    public void jumpToPositionOfSelectedUser(String participantJID, String sufix)
        throws RemoteException {
        precondition();
        rmiBot.viewObject.clickContextMenuOfTableInView(viewName,
            participantJID + sufix,
            SarosConstant.CONTEXT_MENU_JUMP_TO_POSITION_SELECTED_USER);
    }

    /**************************************************************
     * 
     * Inner function
     * 
     **************************************************************/

    /**
     * 
     * Define the precondition which should be guaranteed when you want to
     * perform actions within the session view.
     * 
     * @throws RemoteException
     */
    private void precondition() throws RemoteException {
        openSessionView();
        setFocusOnSessionView();
    }

    /**
     * 
     * It get all contact names listed in the session view and would be used by
     * {@link SessionViewObject#isInFollowMode}.
     * 
     * @return list, which contain all the contact names.
     * @throws RemoteException
     */
    private List<String> getAllContactsInSessionView() throws RemoteException {
        precondition();
        List<String> allContactsName = new ArrayList<String>();
        SWTBotTable table = rmiBot.tableObject.getTable();
        for (int i = 0; i < table.rowCount(); i++) {
            allContactsName.add(table.getTableItem(i).getText());
        }
        return allContactsName;
    }

    private void clickToolbarButtonWithTooltip(String tooltip) {
        rmiBot.viewObject
            .clickToolbarButtonWithTooltipInView(viewName, tooltip);
    }
}
