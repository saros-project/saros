package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

/**
 * This implementation of {@link SessionView}
 * 
 * @author Lin
 */
public class SessionViewImp extends EclipsePart implements SessionView {

    private static transient SessionViewImp self;

    /**
     * {@link SessionViewImp} is a singleton, but inheritance is possible.
     */
    public static SessionViewImp getInstance() {
        if (self != null)
            return self;
        self = new SessionViewImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * is Session open/close?
     * 
     **********************************************/
    public boolean isInSession() {
        log.debug("isInSession() == " + sessionManager.getSarosSession() != null);
        return sessionManager.getSarosSession() != null;
    }

    public boolean isInSessionGUI() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_LEAVE_THE_SESSION);
    }

    public void waitUntilIsInSession() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isInSession();
            }

            public String getFailureMessage() {
                return "can't open the session.";
            }
        });
    }

    public void waitUntilInviteeIsInSession(final SessionView sessionV)
        throws RemoteException {
        sessionV.waitUntilIsInSession();
    }

    public void waitUntilSessionClosed() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isInSession();
            }

            public String getFailureMessage() {
                return "can't close the session.";
            }
        });
    }

    public void waitUntilSessionClosedBy(SessionView sessionV)
        throws RemoteException {
        sessionV.waitUntilSessionClosed();
    }

    /**********************************************
     * 
     * informations in the session view's field
     * 
     **********************************************/
    public boolean existsParticipant(JID contactJID) throws RemoteException {
        precondition();
        String participantLabel = getParticipantLabel(contactJID);
        SWTBotTable table = tableW.getTableInView(VIEW_SAROS_SESSION);
        for (int i = 0; i < table.rowCount(); i++) {
            if (table.getTableItem(i).getText().equals(participantLabel))
                return true;
        }
        return false;
    }

    public void selectParticipant(JID contactJID) throws RemoteException {
        precondition();
        if (existsParticipant(contactJID)) {
            String contactLabel = getParticipantLabel(contactJID);
            SWTBotTable table = tableW.getTableInView(VIEW_SAROS_SESSION);
            table.getTableItem(contactLabel).select();
        }
    }

    public String getParticipantLabel(JID contactJID) throws RemoteException {
        String contactLabel;
        if (localJID.equals(contactJID)) {
            if (hasWriteAccess())
                contactLabel = OWN_PARTICIPANT_NAME;
            else
                contactLabel = OWN_PARTICIPANT_NAME + PERMISSION_NAME;
        } else if (rosterV.hasBuddyNickName(contactJID)) {
            if (hasWriteAccess(contactJID))
                contactLabel = rosterV.getBuddyNickName(contactJID) + " ("
                    + contactJID.getBase() + ")";
            else
                contactLabel = rosterV.getBuddyNickName(contactJID) + " ("
                    + contactJID.getBase() + ")" + PERMISSION_NAME;
        } else {
            if (hasWriteAccess(contactJID))
                contactLabel = contactJID.getBase();
            else
                contactLabel = contactJID.getBase() + PERMISSION_NAME;
        }
        return contactLabel;
    }

    public boolean existsLabelTextInSessionView() throws RemoteException {
        precondition();
        return labelW.existsLabelInView(VIEW_SAROS_SESSION);
    }

    public String getFirstLabelTextInSessionview() throws RemoteException {
        if (existsLabelTextInSessionView())
            return viewW.getView(VIEW_SAROS_SESSION).bot().label().getText();
        return null;
    }

    /**********************************************
     * 
     * context menu of a contact on the view: give/Restrict To Read-Only Access
     * 
     **********************************************/

    public void grantWriteAccessGUI(final SessionView sessionV)
        throws RemoteException {
        final JID jidOfPeer = sessionV.getJID();
        if (hasWriteAccess(jidOfPeer)) {
            throw new RuntimeException(
                "User \""
                    + jidOfPeer.getBase()
                    + "\" already has write access! Please pass a correct Object to the method.");
        }
        precondition();
        String participantLabel = getParticipantLabel(jidOfPeer);
        tableW.clickContextMenuOfTableItemInView(VIEW_SAROS_SESSION,
            participantLabel, CM_GRANT_WRITE_ACCESS);
        sessionV.waitUntilHasWriteAccess();
    }

    public void restrictToReadOnlyAccess(final SessionView sessionV)
        throws RemoteException {
        // TODO add the correct implementation
    }

    public void restrictToReadOnlyAccessGUI(final SessionView sessionV)
        throws RemoteException {
        final JID jidOfPeer = sessionV.getJID();
        if (!sessionV.hasWriteAccess()) {
            throw new RuntimeException(
                "User \""
                    + jidOfPeer.getBase()
                    + "\" has read-only access! Please pass a correct Musician Object to the method.");
        }
        precondition();
        String contactLabel = getParticipantLabel(jidOfPeer);
        tableW.clickContextMenuOfTableItemInView(VIEW_SAROS_SESSION,
            contactLabel, CM_RESTRICT_TO_READ_ONLY_ACCESS);
        sessionV.waitUntilHasReadOnlyAccess();
    }

    public void waitUntilHasWriteAccess() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return hasWriteAccess();
            }

            public String getFailureMessage() {
                return localJID.getBase() + " has read-only access.";
            }
        });
    }

    public void waitUntilHasWriteAccess(final JID jid) throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return hasWriteAccess(jid);
            }

            public String getFailureMessage() {
                return jid.getBase() + " has read-only access.";
            }
        });
    }

    public void waitUntilHasReadOnlyAccess() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !hasWriteAccess();
            }

            public String getFailureMessage() {
                return localJID.getBase() + " has read-only access";
            }
        });
    }

    public boolean hasWriteAccess() throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        return sarosSession.hasWriteAccess();
    }

    public boolean hasWriteAccess(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        log.debug("isDriver(" + jid.toString() + ") == "
            + sarosSession.getUsersWithWriteAccess().contains(user));
        return sarosSession.getUsersWithWriteAccess().contains(user);
    }

    public boolean haveWriteAccess(List<JID> jids) {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                User user = sarosSession.getUser(jid);
                result &= sarosSession.getUsersWithWriteAccess().contains(user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isHost() throws RemoteException {
        return isHost(getJID());
    }

    public boolean isHost(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        final boolean result = user == sarosSession.getHost();
        log.debug("isHost(" + jid.toString() + ") == " + result);
        return result;
    }

    public boolean hasReadOnlyAccess() throws RemoteException {
        return hasReadOnlyAccess(getJID());
    }

    public boolean hasReadOnlyAccess(JID jid) throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        User user = sarosSession.getUser(jid);
        if (user == null)
            return false;
        log.debug("hasReadOnlyAccess(" + jid.toString() + ") == "
            + sarosSession.getUsersWithReadOnlyAccess().contains(user));
        return sarosSession.getUsersWithReadOnlyAccess().contains(user);
    }

    public boolean haveReadOnlyAccess(List<JID> jids) {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                User user = sarosSession.getUser(jid);
                result &= sarosSession.getUsersWithReadOnlyAccess().contains(
                    user);
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    public boolean isParticipant() throws RemoteException {
        return isParticipant(getJID());
    }

    public boolean isParticipant(JID jid) throws RemoteException {
        try {
            ISarosSession sarosSession = sessionManager.getSarosSession();
            if (sarosSession == null)
                return false;
            User user = sarosSession.getUser(jid);
            if (user == null)
                return false;
            log.debug("isParticipant(" + jid.toString() + ") == "
                + sarosSession.getParticipants().contains(user));
            return sarosSession.getParticipants().contains(user);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areParticipants(List<JID> jids) throws RemoteException {
        boolean result = true;
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        for (JID jid : jids) {
            try {
                result &= sarosSession.getParticipants().contains(
                    sarosSession.getUser(jid));
            } catch (Exception e) {
                return false;
            }
        }
        return result;
    }

    /**********************************************
     * 
     * context menu of a contact on the view: follow/stop following user
     * 
     **********************************************/
    public void followThisBuddy(JID jidOfFollowedUser) throws RemoteException {
        // TODO add the implementation.
    }

    public void followThisBuddyGUI(JID jidOfFollowedUser)
        throws RemoteException {
        precondition();
        if (isInFollowMode()) {
            log.debug(jidOfFollowedUser.getBase()
                + " is already followed by you.");
            return;
        }
        clickContextMenuOfSelectedUser(
            jidOfFollowedUser,
            CM_FOLLOW_THIS_BUDDY,
            "Hi guy, you can't follow youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    public boolean isInFollowMode() throws RemoteException {
        return editorManager.isFollowing();
    }

    public boolean isInFollowModeGUI() throws RemoteException {
        try {
            precondition();
            SWTBotTable table = tableW.getTableInView(VIEW_SAROS_SESSION);
            for (int i = 0; i < table.rowCount(); i++) {
                try {
                    return table.getTableItem(i)
                        .contextMenu(CM_STOP_FOLLOWING_THIS_BUDDY).isEnabled();
                } catch (WidgetNotFoundException e) {
                    continue;
                }
            }
        } catch (WidgetNotFoundException e) {
            return false;
        }
        return false;
    }

    public void waitUntilIsFollowingBuddy(final String baseJIDOfFollowedUser)
        throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isFollowingBuddy(baseJIDOfFollowedUser);
            }

            public String getFailureMessage() {
                return localJID.getBase() + " is not folloing the user "
                    + baseJIDOfFollowedUser;
            }
        });

    }

    public boolean isFollowingBuddy(String baseJID) throws RemoteException {
        if (getFollowedBuddyJID() == null)
            return false;
        else
            return getFollowedBuddyJID().getBase().equals(baseJID);
    }

    public JID getFollowedBuddyJID() throws RemoteException {
        if (editorManager.getFollowedUser() != null)
            return editorManager.getFollowedUser().getJID();
        else
            return null;
    }

    public void stopFollowing() throws RemoteException {
        // TODO add the implementation
    }

    public void stopFollowingGUI() throws RemoteException {
        final JID followedUserJID = getFollowedBuddyJID();
        if (followedUserJID == null) {
            log.debug(" You are not in follow mode, so you don't need to perform thhe function.");
            return;
        }
        log.debug(" JID of the followed user: " + followedUserJID.getBase());
        precondition();
        String contactLabel = getParticipantLabel(followedUserJID);
        tableW.clickContextMenuOfTableItemInView(VIEW_SAROS_SESSION,
            contactLabel, CM_STOP_FOLLOWING_THIS_BUDDY);
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isInFollowMode();
            }

            public String getFailureMessage() {
                return followedUserJID.getBase() + " is still followed.";
            }
        });
    }

    public void stopFollowingThisBuddyGUI(JID jidOfFollowedUser)
        throws RemoteException {
        if (!isInFollowMode()) {
            log.debug(" You are not in follow mode, so you don't need to perform thhe function.");
            return;
        }
        precondition();
        clickContextMenuOfSelectedUser(
            jidOfFollowedUser,
            CM_STOP_FOLLOWING_THIS_BUDDY,
            "Hi guy, you can't stop following youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    public boolean isCMStopFollowingThisBuddyVisible(String contactName)
        throws RemoteException {
        precondition();
        return tableW.isContextMenuOfTableItemVisibleInView(VIEW_SAROS_SESSION,
            contactName, CM_STOP_FOLLOWING_THIS_BUDDY);
    }

    public boolean isCMStopFollowingThisBuddyEnabled(String contactName)
        throws RemoteException {
        precondition();
        return tableW.isContextMenuOfTableItemEnabledInView(VIEW_SAROS_SESSION,
            contactName, CM_STOP_FOLLOWING_THIS_BUDDY);
    }

    /**********************************************
     * 
     * context menu of a contact on the view: jump to position of selected user
     * 
     **********************************************/
    public void jumpToPositionOfSelectedBuddyGUI(JID jidOfselectedUser)
        throws RemoteException {
        clickContextMenuOfSelectedUser(
            jidOfselectedUser,
            CM_JUMP_TO_POSITION_SELECTED_BUDDY,
            "Hi guy, you can't jump to the position of youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    /**********************************************
     * 
     * toolbar button on the view: share your screen with selected user
     * 
     **********************************************/
    public void shareYourScreenWithSelectedBuddyGUI(JID jidOfPeer)
        throws RemoteException {
        selectUser(
            jidOfPeer,
            "Hi guy, you can't share screen with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_SHARE_SCREEN_WITH_BUDDY);
    }

    public void stopSessionWithUserGUI(JID jidOfPeer) throws RemoteException {
        selectUser(
            jidOfPeer,
            "Hi guy, you can't stop screen session with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_STOP_SESSION_WITH_USER);
    }

    public void confirmIncomingScreensharingSesionWindow()
        throws RemoteException {
        shellC.waitUntilShellActive(SHELL_INCOMING_SCREENSHARING_SESSION);
        shellC.confirmShell(SHELL_INCOMING_SCREENSHARING_SESSION, YES);
    }

    public void confirmWindowScreensharingAErrorOccured()
        throws RemoteException {
        shellC.waitUntilShellActive(SHELL_SCREENSHARING_ERROR_OCCURED);
        shellC.confirmShell(SHELL_SCREENSHARING_ERROR_OCCURED, OK);
    }

    /**********************************************
     * 
     * toolbar button on the view: send a file to selected user
     * 
     **********************************************/
    public void sendAFileToSelectedUserGUI(JID jidOfPeer)
        throws RemoteException {
        selectUser(
            jidOfPeer,
            "Hi guy, you can't send a file to youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_SEND_A_FILE_TO_SELECTED_BUDDY);
    }

    /**********************************************
     * 
     * toolbar button on the view: start a VoIP session
     * 
     **********************************************/
    public void startAVoIPSessionGUI(JID jidOfPeer) throws RemoteException {
        selectUser(
            jidOfPeer,
            "Hi guy, you can't start a VoIP session with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_START_VOIP_SESSION);
        if (shellC.isShellActive(SHELL_ERROR_IN_SAROS_PLUGIN)) {
            confirmErrorInSarosPluginWindow();
        }
    }

    public void confirmErrorInSarosPluginWindow() throws RemoteException {
        shellC.confirmShell(SHELL_ERROR_IN_SAROS_PLUGIN, OK);
    }

    /**********************************************
     * 
     * toolbar button on the view: inconsistence detected
     * 
     **********************************************/
    public void inconsistencyDetectedGUI() throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_INCONSISTENCY_DETECTED);
        shellC.waitUntilShellClosed(SHELL_PROGRESS_INFORMATION);
    }

    public boolean isInconsistencyDetectedEnabled() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_INCONSISTENCY_DETECTED);
    }

    public void waitUntilInconsistencyDetected() throws RemoteException {
        precondition();
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isToolbarButtonEnabled(TB_INCONSISTENCY_DETECTED);
            }

            public String getFailureMessage() {
                return "The toolbar button " + TB_INCONSISTENCY_DETECTED
                    + " isn't enabled.";
            }
        });
    }

    /**********************************************
     * 
     * toolbar button on the view: remove all river role
     * 
     **********************************************/
    public void restrictInviteesToReadOnlyAccessGUI() throws RemoteException {
        precondition();
        if (isRestrictInviteesToReadOnlyAccessEnabled()) {
            clickToolbarButtonWithTooltip(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS);
            shellC.waitUntilShellClosed(SHELL_PROGRESS_INFORMATION);
        }
    }

    public boolean isRestrictInviteesToReadOnlyAccessEnabled()
        throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS);
    }

    /**********************************************
     * 
     * toolbar button on the view: enable/disable follow mode
     * 
     **********************************************/
    public void enableDisableFollowModeGUI() throws RemoteException {
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
    public void clickTBleaveTheSession() throws RemoteException {
        clickToolbarButtonWithTooltip(TB_LEAVE_THE_SESSION);
    }

    public void waitUntilAllPeersLeaveSession(
        final List<JID> jidsOfAllParticipants) throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                for (JID jid : jidsOfAllParticipants) {
                    if (isParticipant(jid))
                        return false;
                }
                return true;
            }

            public String getFailureMessage() {
                return "There are someone, who still not leave the session.";
            }
        });
    }

    public void leaveTheSessionByPeer() throws RemoteException {
        precondition();
        clickTBleaveTheSession();
        if (!shellC.activateShellWithText(SHELL_CONFIRM_LEAVING_SESSION))
            shellC.waitUntilShellActive(SHELL_CONFIRM_LEAVING_SESSION);
        shellC.confirmShell(SHELL_CONFIRM_LEAVING_SESSION, YES);
        waitUntilSessionClosed();
    }

    public void leaveTheSessionByHost() throws RemoteException {
        precondition();
        clickTBleaveTheSession();
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
        if (!shellC.activateShellWithText(SHELL_CONFIRM_CLOSING_SESSION))
            shellC.waitUntilShellActive(SHELL_CONFIRM_CLOSING_SESSION);
        shellC.confirmShell(SHELL_CONFIRM_CLOSING_SESSION, YES);
        waitUntilSessionClosed();
    }

    public void confirmClosingTheSessionWindow() throws RemoteException {
        shellC.waitUntilShellOpen(SHELL_CLOSING_THE_SESSION);
        shellC.activateShellWithText(SHELL_CLOSING_THE_SESSION);
        shellC.confirmShell(SHELL_CLOSING_THE_SESSION, OK);
        shellC.waitUntilShellClosed(SHELL_CLOSING_THE_SESSION);
    }

    /**********************************************
     * 
     * toolbar button on the view: open invitation interface
     * 
     **********************************************/
    public void openInvitationInterface(String... jidOfInvitees)
        throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_OPEN_INVITATION_INTERFACE);
        sarosC.confirmShellInvitation(jidOfInvitees);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    public void setJID(JID jid) throws RemoteException {
        this.localJID = jid;
    }

    public JID getJID() throws RemoteException {
        return localJID;
    }

    /**
     * 
     * Define the precondition which should be guaranteed when you want to
     * perform actions within the session view.
     * 
     * @throws RemoteException
     */
    protected void precondition() throws RemoteException {
        viewW.openViewById(VIEW_SAROS_SESSION_ID);
        viewW.setFocusOnViewByTitle(VIEW_SAROS_SESSION);
    }

    /**
     * 
     * It get all contact names listed in the session view and would be used by
     * {@link SessionViewImp#isInFollowMode}.
     * 
     * @return list, which contain all the contact names.
     * @throws RemoteException
     */
    private List<String> getAllContactsInSessionView() throws RemoteException {
        precondition();
        List<String> allContactsName = new ArrayList<String>();
        SWTBotTable table = tableW.getTableInView(VIEW_SAROS_SESSION);
        for (int i = 0; i < table.rowCount(); i++) {
            allContactsName.add(table.getTableItem(i).getText());
        }
        return allContactsName;
    }

    private void clickContextMenuOfSelectedUser(JID jidOfSelectedUser,
        String context, String message) throws RemoteException {
        if (localJID.equals(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        workbenchC.activateWorkbench();
        precondition();
        String contactLabel = getParticipantLabel(jidOfSelectedUser);
        workbenchC.captureScreenshot(workbenchC.getPathToScreenShot()
            + "/serverside_vor_jump_to_position.png");
        tableW.clickContextMenuOfTableItemInView(VIEW_SAROS_SESSION,
            contactLabel, context);

    }

    private void selectUser(JID jidOfSelectedUser, String message)
        throws RemoteException {
        if (localJID.equals(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        precondition();
        String contactLabel = getParticipantLabel(jidOfSelectedUser);
        tableW.getTableItemInView(VIEW_SAROS_SESSION, contactLabel);
    }

    private boolean isToolbarButtonEnabled(String tooltip)
        throws RemoteException {
        return toolbarButtonW.isToolbarButtonInViewEnabled(VIEW_SAROS_SESSION,
            tooltip);
    }

    private void clickToolbarButtonWithTooltip(String tooltipText)
        throws RemoteException {
        toolbarButtonW.clickToolbarButtonWithRegexTooltipInView(
            VIEW_SAROS_SESSION, tooltipText);
    }

    private List<SWTBotToolbarButton> getToolbarButtons()
        throws RemoteException {
        return toolbarButtonW.getAllToolbarButtonsInView(VIEW_SAROS_SESSION);
    }

}
