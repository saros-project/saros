package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;

/**
 * This implementation of {@link SessionViewComponent}
 * 
 * @author Lin
 */
public class SessionViewComponentImp extends EclipseComponent implements
    SessionViewComponent {

    private static transient SessionViewComponentImp self;

    /*
     * View infos
     */
    private final static String VIEWNAME = "Shared Project Session";
    private final static String VIEWID = "de.fu_berlin.inf.dpp.ui.SessionView";

    /*
     * title of shells which are pop up by performing the actions on the view.
     */
    private final static String SHELL_CONFIRM_CLOSING_SESSION = "Confirm Closing Session";
    private final static String SHELL_INCOMING_SCREENSHARING_SESSION = "Incoming screensharing session";
    private final static String SHELL_INVITATION = "Invitation";
    private final static String SHELL_ERROR_IN_SAROS_PLUGIN = "Error in Saros-Plugin";
    private final static String CLOSING_THE_SESSION = "Closing the Session";
    protected final static String CONFIRM_LEAVING_SESSION = "Confirm Leaving Session";

    /*
     * Tool tip text of all the toolbar buttons on the view
     */
    private final static String TB_SHARE_SCREEN_WITH_USER = "Share your screen with selected user";
    private final static String TB_STOP_SESSION_WITH_USER = "Stop session with user";
    private final static String TB_SEND_A_FILE_TO_SELECTED_USER = "Send a file to selected user";
    private final static String TB_START_VOIP_SESSION = "Start a VoIP Session...";
    private final static String TB_INCONSISTEN_CYDETECTED = "Inconsistency Detected in.*";
    private final static String TB_OPEN_INVITATION_INTERFACE = "Open invitation interface";
    private final static String TB_REMOVE_ALL_DRIVER_ROLES = "Remove all driver roles";
    private final static String TB_ENABLE_DISABLE_FOLLOW_MODE = "Enable/Disable follow mode";
    private final static String TB_LEAVE_THE_SESSION = "Leave the session";

    // Context menu's name of the table on the view
    private final static String CM_GIVE_EXCLUSIVE_DRIVER_ROLE = "Give exclusive driver role";
    private final static String CM_GIVE_DRIVER_ROLE = "Give driver role";
    private final static String CM_REMOVE_DRIVER_ROLE = "Remove driver role";
    private final static String CM_FOLLOW_THIS_USER = "Follow this user";
    private final static String CM_STOP_FOLLOWING_THIS_USER = "Stop following this user";
    private final static String CM_JUMP_TO_POSITION_SELECTED_USER = "Jump to position of selected user";
    private final static String CM_CHANGE_COLOR = "Change Color";

    /**
     * {@link SessionViewComponentImp} is a singleton, but inheritance is
     * possible.
     */
    public static SessionViewComponentImp getInstance() {
        if (self != null)
            return self;
        self = new SessionViewComponentImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * open/close/activate the view
     * 
     **********************************************/
    public void openSessionView() throws RemoteException {
        if (!isSessionViewOpen())
            viewPart.openViewById(VIEWID);
    }

    public boolean isSessionViewOpen() throws RemoteException {
        return viewPart.isViewOpen(VIEWNAME);
    }

    public void closeSessionView() throws RemoteException {
        if (isSessionViewOpen())
            viewPart.closeViewById(VIEWID);
    }

    public void setFocusOnSessionView() throws RemoteException {
        viewPart.setFocusOnViewByTitle(VIEWNAME);
        viewPart.waitUntilViewActive(VIEWNAME);
    }

    public boolean isSessionViewActive() throws RemoteException {
        return viewPart.isViewActive(VIEWNAME);
    }

    /**********************************************
     * 
     * is Session open/close?
     * 
     **********************************************/
    public boolean isInSession() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_LEAVE_THE_SESSION)
            && state.isInSession();
    }

    public void waitUntilSessionOpen() throws RemoteException {
        waitUntil(SarosConditions.isInSession(state));
    }

    public void waitUntilSessionOpenBy(SarosState stateOfPeer)
        throws RemoteException {
        waitUntil(SarosConditions.isInSession(stateOfPeer));
    }

    public void waitUntilSessionClosed() throws RemoteException {
        waitUntil(SarosConditions.isSessionClosed(state));
    }

    public void waitUntilSessionClosedBy(SarosState stateOfPeer)
        throws RemoteException {
        waitUntil(SarosConditions.isSessionClosed(stateOfPeer));
    }

    public boolean isContactInSessionView(String contactName)
        throws RemoteException {
        precondition();
        SWTBotTable table = tablePart.getTable();
        for (int i = 0; i < table.rowCount(); i++) {
            if (table.getTableItem(i).getText().equals(contactName))
                return true;
        }
        return false;
    }

    /**********************************************
     * 
     * context menu of a contact on the view: give/remove driver role
     * 
     **********************************************/
    public void giveDriverRole(SarosState stateOfInvitee)
        throws RemoteException {
        JID InviteeJID = stateOfInvitee.getJID();
        if (stateOfInvitee.isDriver(InviteeJID)) {
            throw new RuntimeException(
                "User \""
                    + InviteeJID.getBase()
                    + "\" is already a driver! Please pass a correct Musician Object to the method.");
        }
        precondition();
        if (stateOfInvitee.isHost(InviteeJID))
            tablePart.clickContextMenuOfTable("You", CM_GIVE_DRIVER_ROLE);
        else
            tablePart.clickContextMenuOfTable(InviteeJID.getBase(),
                CM_GIVE_DRIVER_ROLE);
        windowPart.waitUntilShellActive(PROGRESSINFORMATION);
        windowPart.waitUntilShellClosed(PROGRESSINFORMATION);
        bot.sleep(sleepTime);
    }

    public void giveExclusiveDriverRole(SarosState stateOfInvitee)
        throws RemoteException {
        JID InviteeJID = stateOfInvitee.getJID();
        if (stateOfInvitee.isDriver(InviteeJID)) {
            throw new RuntimeException(
                "User \""
                    + InviteeJID.getBase()
                    + "\" is already a driver! Please pass a correct Musician Object to the method.");
        }
        precondition();
        if (stateOfInvitee.isHost(InviteeJID))
            tablePart.clickContextMenuOfTable("You",
                CM_GIVE_EXCLUSIVE_DRIVER_ROLE);
        else
            tablePart.clickContextMenuOfTable(InviteeJID.getBase(),
                CM_GIVE_EXCLUSIVE_DRIVER_ROLE);
        windowPart.waitUntilShellActive(PROGRESSINFORMATION);
        windowPart.waitUntilShellClosed(PROGRESSINFORMATION);
        bot.sleep(sleepTime);
    }

    public void removeDriverRole(SarosState stateOfInvitee)
        throws RemoteException {
        JID InviteeJID = stateOfInvitee.getJID();
        if (!stateOfInvitee.isDriver(InviteeJID)) {
            throw new RuntimeException(
                "User \""
                    + InviteeJID.getBase()
                    + "\" is  no driver! Please pass a correct Musician Object to the method.");
        }
        precondition();
        tablePart.clickContextMenuOfTable(InviteeJID.getBase() + ROLENAME,
            CM_REMOVE_DRIVER_ROLE);
        windowPart.waitUntilShellOpen(PROGRESSINFORMATION);
        windowPart.waitUntilShellClosed(PROGRESSINFORMATION);
    }

    /**********************************************
     * 
     * context menu of a contact on the view: follow/stop following user
     * 
     **********************************************/
    public void followThisUser(SarosState stateOfFollowedUser)
        throws RemoteException {
        precondition();
        JID JIDOfFollowedUser = stateOfFollowedUser.getJID();
        if (state.isInFollowMode()) {
            log.debug(JIDOfFollowedUser.getBase()
                + " is already followed by you.");
            return;
        }
        clickContextMenuOfSelectedUser(
            stateOfFollowedUser,
            CM_FOLLOW_THIS_USER,
            "Hi guy, you can't follow youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    public boolean isInFollowMode() throws RemoteException {
        precondition();
        List<String> allContactsName = getAllContactsInSessionView();
        for (String contactName : allContactsName) {
            // SWTBotTable table = tableObject.getTable();
            if (!tablePart.existsContextOfTableItem(contactName,
                CM_STOP_FOLLOWING_THIS_USER))
                continue;
            if (isStopFollowingThisUserEnabled(contactName))
                return true;
        }
        return false;
    }

    public void stopFollowing() throws RemoteException {
        JID followedUserJID = state.getFollowedUserJID();
        if (followedUserJID == null) {
            log.debug(" You are not in follow mode, so you don't need to perform thhe function.");
            return;
        }
        log.debug(" JID of the followed user: " + followedUserJID.getBase());
        precondition();
        if (state.isDriver(followedUserJID))
            tablePart.clickContextMenuOfTable(followedUserJID.getBase()
                + ROLENAME, CM_STOP_FOLLOWING_THIS_USER);
        else
            tablePart.clickContextMenuOfTable(followedUserJID.getBase(),
                CM_STOP_FOLLOWING_THIS_USER);
    }

    public void stopFollowingThisUser(SarosState stateOfFollowedUser)
        throws RemoteException {
        if (!state.isInFollowMode()) {
            log.debug(" You are not in follow mode, so you don't need to perform thhe function.");
            return;
        }
        clickContextMenuOfSelectedUser(
            stateOfFollowedUser,
            CM_STOP_FOLLOWING_THIS_USER,
            "Hi guy, you can't stop following youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    public boolean isStopFollowingThisUserVisible(String contactName)
        throws RemoteException {
        return tablePart.isContextMenuOfTableVisible(contactName,
            CM_STOP_FOLLOWING_THIS_USER);
    }

    public boolean isStopFollowingThisUserEnabled(String contactName)
        throws RemoteException {
        return tablePart.isContextMenuOfTableEnabled(contactName,
            CM_STOP_FOLLOWING_THIS_USER);
    }

    public void waitUntilIsFollowingUser(String baseJIDOfFollowedUser)
        throws RemoteException {
        waitUntil(SarosConditions.isFollowingUser(state, baseJIDOfFollowedUser));
    }

    /**********************************************
     * 
     * context menu of a contact on the view: jump to position of selected user
     * 
     **********************************************/
    public void jumpToPositionOfSelectedUser(SarosState stateOfselectedUser)
        throws RemoteException {
        clickContextMenuOfSelectedUser(
            stateOfselectedUser,
            CM_JUMP_TO_POSITION_SELECTED_USER,
            "Hi guy, you can't jump to the position of youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    /**********************************************
     * 
     * toolbar button on the view: share your screen with selected user
     * 
     **********************************************/
    public void shareYourScreenWithSelectedUser(SarosState stateOfselectedUser)
        throws RemoteException {
        selectUser(
            stateOfselectedUser,
            "Hi guy, you can't share screen with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_SHARE_SCREEN_WITH_USER);
    }

    public void confirmIncomingScreensharingSesionWindow()
        throws RemoteException {
        windowPart.waitUntilShellActive(SHELL_INCOMING_SCREENSHARING_SESSION);
        windowPart.confirmWindow(SHELL_INCOMING_SCREENSHARING_SESSION, YES);
    }

    /**********************************************
     * 
     * toolbar button on the view: stop session with user
     * 
     **********************************************/
    public void stopSessionWithUser(SarosState stateOfselectedUser)
        throws RemoteException {
        selectUser(
            stateOfselectedUser,
            "Hi guy, you can't stop screen session with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_STOP_SESSION_WITH_USER);
    }

    /**********************************************
     * 
     * toolbar button on the view: send a file to selected user
     * 
     **********************************************/
    public void sendAFileToSelectedUser(SarosState stateOfselectedUser)
        throws RemoteException {
        selectUser(
            stateOfselectedUser,
            "Hi guy, you can't send a file to youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_SEND_A_FILE_TO_SELECTED_USER);
    }

    /**********************************************
     * 
     * toolbar button on the view: start a VoIP session
     * 
     **********************************************/
    public void startAVoIPSession(SarosState stateOfselectedUser)
        throws RemoteException {
        selectUser(
            stateOfselectedUser,
            "Hi guy, you can't start a VoIP session with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_START_VOIP_SESSION);
        if (windowPart.isShellActive(SHELL_ERROR_IN_SAROS_PLUGIN)) {
            confirmErrorInSarosPluginWindow();
        }
    }

    public void confirmErrorInSarosPluginWindow() throws RemoteException {
        windowPart.confirmWindow(SHELL_ERROR_IN_SAROS_PLUGIN, OK);
    }

    /**********************************************
     * 
     * toolbar button on the view: inconsistence detected
     * 
     **********************************************/
    public void inconsistencyDetected() throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_INCONSISTEN_CYDETECTED);
        windowPart.waitUntilShellCloses(PROGRESSINFORMATION);
    }

    public boolean isInconsistencyDetectedEnabled() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_INCONSISTEN_CYDETECTED);
    }

    /**********************************************
     * 
     * toolbar button on the view: remove all river role
     * 
     **********************************************/
    public void removeAllRriverRoles() throws RemoteException {
        precondition();
        if (isRemoveAllRiverEnabled())
            clickToolbarButtonWithTooltip(TB_REMOVE_ALL_DRIVER_ROLES);
    }

    public boolean isRemoveAllRiverEnabled() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_REMOVE_ALL_DRIVER_ROLES);
    }

    /**********************************************
     * 
     * toolbar button on the view: enable/disable follow mode
     * 
     **********************************************/
    public void enableDisableFollowMode() throws RemoteException {
        precondition();
        if (isEnableDisableFollowModeEnabled())
            clickToolbarButtonWithTooltip(TB_ENABLE_DISABLE_FOLLOW_MODE);
    }

    public boolean isEnableDisableFollowModeEnabled() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_ENABLE_DISABLE_FOLLOW_MODE);
    }

    /**********************************************
     * 
     * toolbar button on the view: leave the session
     * 
     **********************************************/
    private void leaveTheSession() {
        clickToolbarButtonWithTooltip(TB_LEAVE_THE_SESSION);
    }

    public void waitUntilAllPeersLeaveSession(List<JID> jidsOfAllParticipants)
        throws RemoteException {
        waitUntil(SarosConditions.existsNoParticipants(state,
            jidsOfAllParticipants));
    }

    public void leaveTheSessionByPeer() throws RemoteException {
        precondition();
        leaveTheSession();
        windowPart.confirmWindow(CONFIRM_LEAVING_SESSION, YES);
        waitUntilSessionClosed();
    }

    public void leaveTheSessionByHost() throws RemoteException {
        precondition();
        leaveTheSession();
        // Util.runSafeAsync(log, new Runnable() {
        // public void run() {
        // try {
        // exWindowO.confirmWindow("Confirm Closing Session",
        // SarosConstant.BUTTON_YES);
        // } catch (RemoteException e) {
        // // no popup
        // }
        // }
        // });
        if (windowPart.isShellActive(SHELL_CONFIRM_CLOSING_SESSION))
            windowPart.confirmWindow(SHELL_CONFIRM_CLOSING_SESSION, YES);
        waitUntilSessionClosed();
    }

    public void confirmClosingTheSessionWindow() throws RemoteException {
        windowPart.waitUntilShellActive(CLOSING_THE_SESSION);
        windowPart.confirmWindow(CLOSING_THE_SESSION, OK);
        windowPart.waitUntilShellCloses(CLOSING_THE_SESSION);
    }

    /**********************************************
     * 
     * toolbar button on the view: open invitation interface
     * 
     **********************************************/
    public void openInvitationInterface(String jidOfInvitee)
        throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_OPEN_INVITATION_INTERFACE);
        comfirmInvitationWindow(jidOfInvitee);
    }

    public void comfirmInvitationWindow(String jidOfinvitee)
        throws RemoteException {
        windowPart.waitUntilShellActive(SHELL_INVITATION);
        windowPart.confirmWindowWithCheckBox(SHELL_INVITATION, FINISH,
            jidOfinvitee);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    /**
     * 
     * Define the precondition which should be guaranteed when you want to
     * perform actions within the session view.
     * 
     * @throws RemoteException
     */
    @Override
    protected void precondition() throws RemoteException {
        openSessionView();
        setFocusOnSessionView();
    }

    /**
     * 
     * It get all contact names listed in the session view and would be used by
     * {@link SessionViewComponentImp#isInFollowMode}.
     * 
     * @return list, which contain all the contact names.
     * @throws RemoteException
     */
    private List<String> getAllContactsInSessionView() throws RemoteException {
        precondition();
        List<String> allContactsName = new ArrayList<String>();
        SWTBotTable table = tablePart.getTable();
        for (int i = 0; i < table.rowCount(); i++) {
            allContactsName.add(table.getTableItem(i).getText());
        }
        return allContactsName;
    }

    private void clickContextMenuOfSelectedUser(SarosState stateOfselectedUser,
        String context, String message) throws RemoteException {
        JID jidOfSelectedUser = stateOfselectedUser.getJID();
        if (state.isSameUser(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        precondition();
        if (stateOfselectedUser.isDriver(jidOfSelectedUser))
            tablePart.clickContextMenuOfTable(jidOfSelectedUser.getBase()
                + ROLENAME, context);
        else
            tablePart.clickContextMenuOfTable(jidOfSelectedUser.getBase(),
                context);
    }

    private void selectUser(SarosState stateOfselectedUser, String message)
        throws RemoteException {
        JID jidOfSelectedUser = stateOfselectedUser.getJID();
        if (state.isSameUser(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        precondition();
        if (stateOfselectedUser.isDriver(jidOfSelectedUser)) {
            viewPart.selectTableItemWithLabelInView(VIEWNAME,
                jidOfSelectedUser.getBase() + ROLENAME);
        } else {
            viewPart.selectTableItemWithLabelInView(VIEWNAME,
                jidOfSelectedUser.getBase());
        }
    }

    private boolean isToolbarButtonEnabled(String tooltip) {
        return viewPart.isToolbarInViewEnabled(VIEWNAME, tooltip);
    }

    private void clickToolbarButtonWithTooltip(String tooltipText) {
        viewPart.clickToolbarButtonWithTooltipInView(VIEWNAME, tooltipText);
    }

    private List<SWTBotToolbarButton> getToolbarButtons() {
        return viewPart.getToolbarButtonsOnView(VIEWNAME);
    }
}
