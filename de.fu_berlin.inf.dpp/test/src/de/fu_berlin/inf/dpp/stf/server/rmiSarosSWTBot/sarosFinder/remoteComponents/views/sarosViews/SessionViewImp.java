package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.SarosComponentImp;

/**
 * This implementation of {@link SessionView}
 * 
 * @author lchen
 */
public class SessionViewImp extends SarosComponentImp implements SessionView {

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
     * actions
     * 
     **********************************************/

    public void grantWriteAccess(final JID participantJID)
        throws RemoteException {
        if (!isHost()) {
            throw new RuntimeException(
                "Only host has access to grant write access.");
        }

        if (hasWriteAccessBy(participantJID)) {
            throw new RuntimeException("User \"" + participantJID.getBase()
                + "\" already has write access!.");
        }
        precondition();
        String participantLabel = getParticipantLabel(participantJID);
        bot().view(VIEW_SAROS_SESSION).bot_().table()
            .getTableItem(participantLabel).contextMenu(CM_GRANT_WRITE_ACCESS)
            .click();
        waitUntilHasWriteAccessBy(participantJID);
        bot().sleep(300);
    }

    public void restrictToReadOnlyAccess(final JID participantJID)
        throws RemoteException {
        if (!isHost()) {
            throw new RuntimeException(
                "Only host has access to grant write access.");
        }

        if (hasReadOnlyAccessBy(participantJID)) {
            throw new RuntimeException("User \"" + participantJID.getBase()
                + "\" already has read-only access!");
        }
        precondition();
        String contactLabel = getParticipantLabel(participantJID);
        bot().view(VIEW_SAROS_SESSION).bot_().table()
            .getTableItem(contactLabel)
            .contextMenu(CM_RESTRICT_TO_READ_ONLY_ACCESS).click();

        waitUntilHasReadOnlyAccessBy(participantJID);
    }

    public void followThisBuddy(JID jidOfFollowedUser) throws RemoteException {
        precondition();
        if (isFollowingBuddy(jidOfFollowedUser)) {
            log.debug(jidOfFollowedUser.getBase()
                + " is already followed by you.");
            return;
        }
        clickContextMenuOfSelectedBuddy(
            jidOfFollowedUser,
            CM_FOLLOW_THIS_BUDDY,
            "Hi guy, you can't follow youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    public void stopFollowing() throws RemoteException {
        final JID followedUserJID = getFollowedBuddyJIDNoGUI();
        if (followedUserJID == null) {
            log.debug(" You are not in follow mode, so you don't need to perform thhe function.");
            return;
        }
        log.debug(" JID of the followed user: " + followedUserJID.getBase());
        precondition();
        String contactLabel = getParticipantLabel(followedUserJID);
        bot().view(VIEW_SAROS_SESSION).bot_().table()
            .getTableItem(contactLabel)
            .contextMenu(CM_STOP_FOLLOWING_THIS_BUDDY).click();

        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isFollowingBuddy(followedUserJID);
            }

            public String getFailureMessage() {
                return followedUserJID.getBase() + " is still followed.";
            }
        });
    }

    public void stopFollowingThisBuddy(JID jidOfFollowedBuddy)
        throws RemoteException {
        if (!isInFollowModeNoGUI()) {
            log.debug(" You are not in follow mode, so you don't need to perform thhe function.");
            return;
        }
        precondition();
        clickContextMenuOfSelectedBuddy(
            jidOfFollowedBuddy,
            CM_STOP_FOLLOWING_THIS_BUDDY,
            "Hi guy, you can't stop following youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    public void jumpToPositionOfSelectedBuddy(JID jidOfselectedUser)
        throws RemoteException {
        clickContextMenuOfSelectedBuddy(
            jidOfselectedUser,
            CM_JUMP_TO_POSITION_SELECTED_BUDDY,
            "Hi guy, you can't jump to the position of youself, it makes no sense! Please pass a correct parameter to the method.");
    }

    public void shareYourScreenWithSelectedBuddy(JID jidOfPeer)
        throws RemoteException {
        selectParticipant(
            jidOfPeer,
            "Hi guy, you can't share screen with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_SHARE_SCREEN_WITH_BUDDY);
    }

    public void stopSessionWithBuddy(JID jidOfPeer) throws RemoteException {
        selectParticipant(
            jidOfPeer,
            "Hi guy, you can't stop screen session with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_STOP_SESSION_WITH_BUDDY);
    }

    public void sendAFileToSelectedBuddy(JID jidOfPeer) throws RemoteException {
        selectParticipant(
            jidOfPeer,
            "Hi guy, you can't send a file to youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_SEND_A_FILE_TO_SELECTED_BUDDY);
    }

    public void startAVoIPSessionWithSelectedBuddy(JID jidOfPeer)
        throws RemoteException {
        selectParticipant(
            jidOfPeer,
            "Hi guy, you can't start a VoIP session with youself, it makes no sense! Please pass a correct parameter to the method.");
        clickToolbarButtonWithTooltip(TB_START_VOIP_SESSION);
        if (bot().shell(SHELL_ERROR_IN_SAROS_PLUGIN).isActive()) {
            bot().shell(SHELL_ERROR_IN_SAROS_PLUGIN).confirm(OK);
        }
    }

    public void restrictInviteesToReadOnlyAccess() throws RemoteException {
        if (!isHost()) {
            throw new RuntimeException("Only host can perform this action.");
        }
        precondition();
        if (isToolbarButtonEnabled(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS)) {
            clickToolbarButtonWithTooltip(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS);
            for (JID invitee : getInvitees())
                waitUntilHasReadOnlyAccessBy(invitee);
        }
    }

    public void leaveTheSessionByPeer() throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_LEAVE_THE_SESSION);
        bot().waitUntilShellIsOpen(SHELL_CONFIRM_LEAVING_SESSION);
        bot().shell(SHELL_CONFIRM_LEAVING_SESSION).activate();
        bot().shell(SHELL_CONFIRM_LEAVING_SESSION).confirm(YES);
        waitUntilIsNotInSession();
    }

    public void leaveTheSessionByHost() throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_LEAVE_THE_SESSION);
        bot().waitUntilShellIsOpen(SHELL_CONFIRM_CLOSING_SESSION);
        bot().shell(SHELL_CONFIRM_CLOSING_SESSION).activate();
        bot().shell(SHELL_CONFIRM_CLOSING_SESSION).confirm(YES);
        waitUntilIsNotInSession();
    }

    public void confirmShellClosingTheSession() throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_CLOSING_THE_SESSION);
        bot().shell(SHELL_CLOSING_THE_SESSION).activate();
        bot().shell(SHELL_CLOSING_THE_SESSION).confirm(OK);
        bot().waitsUntilShellIsClosed(SHELL_CLOSING_THE_SESSION);
    }

    public void openInvitationInterface(String... jidOfInvitees)
        throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_OPEN_INVITATION_INTERFACE);
        confirmShellInvitation(jidOfInvitees);
    }

    public void inconsistencyDetected() throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_INCONSISTENCY_DETECTED);
        bot().waitsUntilShellIsClosed(SHELL_PROGRESS_INFORMATION);
    }

    /**********************************************
     * 
     * State
     * 
     **********************************************/

    public boolean isInSession() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_LEAVE_THE_SESSION);
    }

    public boolean existsParticipant(JID participantJID) throws RemoteException {
        precondition();
        String participantLabel = getParticipantLabel(participantJID);

        STFBotTable table = bot().view(VIEW_SAROS_SESSION).bot_().table();

        for (int i = 0; i < table.rowCount(); i++) {
            if (table.getTableItem(i).getText().equals(participantLabel))
                return true;
        }
        return false;
    }

    public boolean existsLabelInSessionView() throws RemoteException {
        precondition();
        return bot().view(VIEW_SAROS_SESSION).bot_().existsLabel();
    }

    public boolean hasWriteAccess() throws RemoteException {
        precondition();
        return !getParticipantLabel(localJID).contains(PERMISSION_NAME);
    }

    public boolean hasWriteAccessBy(JID... jids) throws RemoteException {
        precondition();
        boolean result = true;
        for (JID jid : jids) {
            result &= !bot().view(VIEW_SAROS_SESSION).bot_().table()
                .getTableItem(getParticipantLabel(jid))
                .contextMenu(CM_GRANT_WRITE_ACCESS).isEnabled()
                && !getParticipantLabel(jid).contains(PERMISSION_NAME);
        }
        return result;
    }

    public boolean hasReadOnlyAccess() throws RemoteException {
        precondition();
        return getParticipantLabel(localJID).contains(PERMISSION_NAME);
    }

    public boolean hasReadOnlyAccessBy(JID... jids) throws RemoteException {
        precondition();
        boolean result = true;
        for (JID jid : jids) {
            boolean isEnabled = bot().view(VIEW_SAROS_SESSION).bot_().table()
                .getTableItem(getParticipantLabel(jid))
                .contextMenu(CM_RESTRICT_TO_READ_ONLY_ACCESS).isEnabled();
            result &= !isEnabled
                && getParticipantLabel(jid).contains(PERMISSION_NAME);
        }
        return result;
    }

    public boolean isHost() throws RemoteException {
        precondition();
        String ownLabelsInSessionView = getParticipantLabel(localJID);

        String talbeItem = bot().view(VIEW_SAROS_SESSION).bot_().table()
            .getTableItem(0).getText();
        if (ownLabelsInSessionView.equals(talbeItem))
            return true;
        return false;
    }

    public boolean isHost(JID jidOfParticipant) throws RemoteException {
        precondition();
        String participantLabelsInSessionView = getParticipantLabel(jidOfParticipant);
        String talbeItem = bot().view(VIEW_SAROS_SESSION).bot_().table()
            .getTableItem(0).getText();
        if (participantLabelsInSessionView.equals(talbeItem))
            return true;
        return false;
    }

    public boolean isParticipant() throws RemoteException {
        precondition();
        return existsParticipant(getJID());
    }

    public boolean isParticipant(JID jid) throws RemoteException {
        precondition();
        return existsParticipant(jid);
    }

    public boolean areParticipants(List<JID> jidOfParticipants)
        throws RemoteException {
        precondition();
        boolean result = true;
        for (JID jid : jidOfParticipants) {
            result &= existsParticipant(jid);
        }
        return result;
    }

    public boolean isFollowing() throws RemoteException {
        JID followedBuddy = getFollowedBuddyJIDNoGUI();
        if (followedBuddy == null)
            return false;
        return isFollowingBuddyNoGUI(followedBuddy.getBase());

    }

    public boolean isFollowingBuddy(JID buddyJID) throws RemoteException {
        // STFBotTableItem tableItem = bot().view(VIEW_SAROS_SESSION).bot_()
        // .table().getTableItem(getParticipantLabel(buddyJID));
        // return tableItem.existsContextMenu(CM_STOP_FOLLOWING_THIS_BUDDY);
        return isFollowingBuddyNoGUI(buddyJID.getBase());
    }

    public String getFirstLabelTextInSessionview() throws RemoteException {
        if (existsLabelInSessionView())
            return bot().view(VIEW_SAROS_SESSION).bot_().label().getText();
        return null;
    }

    public String getParticipantLabel(JID participantJID)
        throws RemoteException {
        String contactLabel;
        if (localJID.equals(participantJID)) {
            if (hasWriteAccessNoGUI())
                contactLabel = OWN_PARTICIPANT_NAME;
            else
                contactLabel = OWN_PARTICIPANT_NAME + " " + PERMISSION_NAME;
        } else if (rosterV.hasBuddyNickNameNoGUI(participantJID)) {
            if (hasWriteAccessByNoGUI(participantJID))
                contactLabel = rosterV.getBuddyNickNameNoGUI(participantJID)
                    + " (" + participantJID.getBase() + ")";
            else
                contactLabel = rosterV.getBuddyNickNameNoGUI(participantJID)
                    + " (" + participantJID.getBase() + ")" + " "
                    + PERMISSION_NAME;
        } else {
            if (hasWriteAccessByNoGUI(participantJID))
                contactLabel = participantJID.getBase();
            else
                contactLabel = participantJID.getBase() + " " + PERMISSION_NAME;
        }
        return contactLabel;
    }

    public List<String> getAllParticipantsInSessionView()
        throws RemoteException {
        precondition();
        List<String> allParticipantsName = new ArrayList<String>();
        STFBotTable table = bot().view(VIEW_SAROS_SESSION).bot_().table();
        for (int i = 0; i < table.rowCount(); i++) {
            allParticipantsName.add(table.getTableItem(i).getText());
        }
        return allParticipantsName;
    }

    public void setJID(JID jid) throws RemoteException {
        localJID = jid;
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/

    public void waitUntilIsInSession() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isInSession();
            }

            public String getFailureMessage() {
                return "can't open the session.";
            }
        });
    }

    public void waitUntilIsInviteeInSession(
        final SessionView sessionViewOfInvitee) throws RemoteException {
        sessionViewOfInvitee.waitUntilIsInSession();
    }

    public void waitUntilIsNotInSession() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isInSession();
            }

            public String getFailureMessage() {
                return "can't close the session.";
            }
        });
    }

    public void waitUntilIsInviteeNotInSession(SessionView sessionViewOfInvitee)
        throws RemoteException {
        sessionViewOfInvitee.waitUntilIsNotInSession();
    }

    public void waitUntilHasWriteAccess() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return hasWriteAccess();
            }

            public String getFailureMessage() {
                return "can't grant " + localJID.getBase()
                    + " the write access.";
            }
        });
    }

    public void waitUntilHasWriteAccessBy(final JID jid) throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return hasWriteAccessBy(jid);
            }

            public String getFailureMessage() {
                return "can't grant " + jid.getBase() + " the write accesss.";
            }
        });
    }

    public void waitUntilHasReadOnlyAccess() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !hasWriteAccess();
            }

            public String getFailureMessage() {
                return "can't restrict " + localJID.getBase()
                    + " to read-only access";
            }
        });
    }

    public void waitUntilHasReadOnlyAccessBy(final JID jid)
        throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !hasWriteAccessBy(jid);
            }

            public String getFailureMessage() {
                return "can't restrict " + jid.getBase()
                    + " to read-only access.";
            }
        });
    }

    public void waitUntilIsFollowingBuddy(final JID followedBuddyJID)
        throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isFollowingBuddy(followedBuddyJID);
            }

            public String getFailureMessage() {
                return localJID.getBase() + " is not folloing the user "
                    + followedBuddyJID.getName();
            }
        });

    }

    public void waitUntilAllPeersLeaveSession(
        final List<JID> jidsOfAllParticipants) throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                for (JID jid : jidsOfAllParticipants) {
                    if (isParticipantNoGUI(jid))
                        return false;
                }
                return true;
            }

            public String getFailureMessage() {
                return "There are someone, who still not leave the session.";
            }
        });
    }

    public void waitUntilIsInconsistencyDetected() throws RemoteException {
        precondition();
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isToolbarButtonEnabled(TB_INCONSISTENCY_DETECTED);
            }

            public String getFailureMessage() {
                return "The toolbar button " + TB_INCONSISTENCY_DETECTED
                    + " isn't enabled.";
            }
        });
    }

    /**************************************************************
     * 
     * NO GUI
     * 
     **************************************************************/
    public boolean hasWriteAccessNoGUI() throws RemoteException {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        return sarosSession.hasWriteAccess();
    }

    public boolean hasWriteAccessByNoGUI(JID jid) throws RemoteException {
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

    public boolean haveWriteAccessByNoGUI(List<JID> jids) {
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

    public boolean isInFollowModeNoGUI() throws RemoteException {
        return editorManager.isFollowing();
    }

    public boolean isInSessionNoGUI() {
        log.debug("isInSession() == " + sessionManager.getSarosSession() != null);
        return sessionManager.getSarosSession() != null;
    }

    public boolean isHostNoGUI() throws RemoteException {
        return isHostNoGUI(getJID());
    }

    public boolean isHostNoGUI(JID jid) throws RemoteException {
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

    public boolean hasReadOnlyAccessNoGUI() throws RemoteException {
        return hasReadOnlyAccessNoGUI(getJID());
    }

    public boolean hasReadOnlyAccessNoGUI(JID jid) throws RemoteException {
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

    public boolean haveReadOnlyAccessNoGUI(List<JID> jids)
        throws RemoteException {
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

    public boolean isParticipantNoGUI() throws RemoteException {
        return isParticipantNoGUI(getJID());
    }

    public boolean isParticipantNoGUI(JID jid) throws RemoteException {
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

    public boolean areParticipantsNoGUI(List<JID> jids) throws RemoteException {
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

    public boolean isFollowingBuddyNoGUI(String baseJID) throws RemoteException {
        if (getFollowedBuddyJIDNoGUI() == null)
            return false;
        else
            return getFollowedBuddyJIDNoGUI().getBase().equals(baseJID);
    }

    public JID getJID() throws RemoteException {
        return localJID;
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    protected void precondition() throws RemoteException {
        bot().openViewById(VIEW_SAROS_SESSION_ID);
        bot().view(VIEW_SAROS_SESSION).setFocus();
    }

    private void clickContextMenuOfSelectedBuddy(JID jidOfSelectedUser,
        String context, String message) throws RemoteException {
        if (localJID.equals(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        precondition();
        String contactLabel = getParticipantLabel(jidOfSelectedUser);
        bot().captureScreenshot(
            bot().getPathToScreenShot()
                + "/serverside_vor_jump_to_position.png");

        bot().view(VIEW_SAROS_SESSION).bot_().table()
            .getTableItem(contactLabel).contextMenu(context).click();
    }

    private void selectParticipant(JID jidOfSelectedUser, String message)
        throws RemoteException {
        if (localJID.equals(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        precondition();
        String contactLabel = getParticipantLabel(jidOfSelectedUser);
        bot().view(VIEW_SAROS_SESSION).bot_().table()
            .getTableItem(contactLabel).select();
    }

    private boolean isToolbarButtonEnabled(String tooltip)
        throws RemoteException {
        return bot().view(VIEW_SAROS_SESSION)
            .toolbarButtonWithRegex(tooltip + ".*").isEnabled();
    }

    private void clickToolbarButtonWithTooltip(String tooltipText)
        throws RemoteException {
        bot().view(VIEW_SAROS_SESSION)
            .toolbarButtonWithRegex(tooltipText + ".*").click();
    }

    private List<String> getToolbarButtons() throws RemoteException {
        return bot().view(VIEW_SAROS_SESSION).getToolTipTextOfToolbarButtons();
    }

    /**
     * @return the JID of the followed user or null if currently no user is
     *         followed.
     * 
     */
    public JID getFollowedBuddyJIDNoGUI() {
        if (editorManager.getFollowedUser() != null)
            return editorManager.getFollowedUser().getJID();
        else
            return null;
    }

    public List<JID> getInvitees() {
        List<JID> invitees = new ArrayList<JID>();
        ISarosSession sarosSession = sessionManager.getSarosSession();

        for (User user : sarosSession.getParticipants()) {
            if (!user.isHost())
                invitees.add(user.getJID());
        }
        return invitees;
    }
}
