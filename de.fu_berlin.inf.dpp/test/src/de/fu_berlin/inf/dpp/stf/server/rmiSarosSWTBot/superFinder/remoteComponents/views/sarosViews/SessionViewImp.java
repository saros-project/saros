package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.SuperRemoteBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.SarosContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.ViewsImp;

/**
 * This implementation of {@link SessionView}
 * 
 * @author lchen
 */
public class SessionViewImp extends ViewsImp implements SessionView {

    private static transient SessionViewImp self;

    private RemoteBotView view;

    private RemoteBotTable table;

    /**
     * {@link SessionViewImp} is a singleton, but inheritance is possible.
     */
    public static SessionViewImp getInstance() {
        if (self != null)
            return self;
        self = new SessionViewImp();
        return self;
    }

    public SessionView setView(RemoteBotView view) throws RemoteException {
        this.view = view;
        if (this.view.bot().existsTable())
            this.table = this.view.bot().table();
        return this;
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

    public SarosContextMenuWrapper selectParticipant(final JID participantJID)
        throws RemoteException {
        if (!isInSession())
            throw new RuntimeException("You are not in a session!");
        String participantLabel = getParticipantLabel(participantJID);
        sarosContextMenu.setTableItem(table.getTableItem(participantLabel));
        sarosContextMenu.setParticipantJID(participantJID);
        sarosContextMenu.setSessionView(this);
        return sarosContextMenu;
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

        clickToolbarButtonWithTooltip(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS);
        for (JID invitee : getInvitees())
            selectParticipant(invitee).waitUntilHasReadOnlyAccess();
        bot().sleep(300);

    }

    public void leaveSession() throws RemoteException {
        if (!isHost()) {
            clickToolbarButtonWithTooltip(TB_LEAVE_SESSION);
            bot().waitUntilShellIsOpen(SHELL_CONFIRM_LEAVING_SESSION);
            bot().shell(SHELL_CONFIRM_LEAVING_SESSION).activate();
            bot().shell(SHELL_CONFIRM_LEAVING_SESSION).confirm(YES);
        } else {
            clickToolbarButtonWithTooltip(TB_STOP_SESSION);
            bot().waitUntilShellIsOpen(SHELL_CONFIRM_CLOSING_SESSION);
            bot().shell(SHELL_CONFIRM_CLOSING_SESSION).activate();
            bot().shell(SHELL_CONFIRM_CLOSING_SESSION).confirm(YES);
        }
        waitUntilIsNotInSession();
    }

    /**
     * TODO: With {@link RemoteBotView#toolbarButtonWithRegex(String)} to perform
     * this action you will get WidgetNotFoundException.
     */
    public void addBuddyToSession(String... jidOfInvitees)
        throws RemoteException {
        view.toolbarButton(TB_ADD_BUDDY_TO_SESSION).click();
        sarosBot().confirmShellAddBuddyToSession(jidOfInvitees);
    }

    /**
     * Note: {@link STF#TB_INCONSISTENCY_DETECTED} is not complete toolbarName,
     * so we need to use {@link RemoteBotView#toolbarButtonWithRegex(String)} to
     * perform this action.
     */
    public void inconsistencyDetected() throws RemoteException {
        view.toolbarButtonWithRegex(TB_INCONSISTENCY_DETECTED + ".*").click();
        bot().waitUntilShellIsClosed(SHELL_PROGRESS_INFORMATION);
    }

    /**********************************************
     * 
     * States
     * 
     **********************************************/
    public boolean existsParticipant(JID participantJID) throws RemoteException {
        String participantLabel = getParticipantLabel(participantJID);
        for (int i = 0; i < table.rowCount(); i++) {
            if (table.getTableItem(i).getText().equals(participantLabel))
                return true;
        }
        return false;
    }

    public String getParticipantLabel(JID participantJID)
        throws RemoteException {
        String contactLabel;
        if (localJID.equals(participantJID)) {
            if (hasWriteAccessNoGUI())
                contactLabel = OWN_PARTICIPANT_NAME;
            else
                contactLabel = OWN_PARTICIPANT_NAME + " " + PERMISSION_NAME;
        } else if (sarosBot().views().buddiesView().hasNickName(participantJID)) {
            if (hasWriteAccessByNoGUI(participantJID))
                contactLabel = sarosBot().views().buddiesView()
                    .getNickName(participantJID)
                    + " (" + participantJID.getBase() + ")";
            else
                contactLabel = sarosBot().views().buddiesView()
                    .getNickName(participantJID)
                    + " ("
                    + participantJID.getBase()
                    + ")"
                    + " "
                    + PERMISSION_NAME;
        } else {
            if (hasWriteAccessByNoGUI(participantJID))
                contactLabel = participantJID.getBase();
            else
                contactLabel = participantJID.getBase() + " " + PERMISSION_NAME;
        }
        return contactLabel;
    }

    public boolean isInSession() throws RemoteException {
        if (view.existsToolbarButton(TB_STOP_SESSION))
            return view.toolbarButton(TB_STOP_SESSION).isEnabled();
        else if (view.existsToolbarButton(TB_LEAVE_SESSION))
            return view.toolbarButton(TB_LEAVE_SESSION).isEnabled();
        return false;
    }

    public boolean existsLabelInSessionView() throws RemoteException {
        return view.bot().existsLabel();
    }

    public boolean isHost() throws RemoteException {
        if (!isInSession())
            return false;
        String ownLabelsInSessionView = getParticipantLabel(localJID);
        String talbeItem = table.getTableItem(0).getText();
        if (ownLabelsInSessionView.equals(talbeItem))
            return true;
        return false;
    }

    public boolean isFollowing() throws RemoteException {
        JID followedBuddy = getFollowedBuddy();
        if (followedBuddy == null)
            return false;
        return selectParticipant(followedBuddy).isFollowingThisBuddy();
    }

    public String getFirstLabelTextInSessionview() throws RemoteException {
        if (existsLabelInSessionView())
            return view.bot().label().getText();
        throw new RuntimeException("There are no label in the session view.");
    }

    public List<String> getAllParticipants() throws RemoteException {
        List<String> allParticipantsName = new ArrayList<String>();
        for (int i = 0; i < table.rowCount(); i++) {
            allParticipantsName.add(table.getTableItem(i).getText());
        }
        return allParticipantsName;
    }

    public JID getJID() throws RemoteException {
        return localJID;
    }

    public JID getFollowedBuddy() throws RemoteException {
        if (editorManager.getFollowedUser() != null)
            return editorManager.getFollowedUser().getJID();
        else
            return null;
    }

    /**********************************************
     * 
     * Wait until
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

    public void waitUntilIsInviteeInSession(SuperRemoteBot sarosBot)
        throws RemoteException {
        sarosBot.views().sessionView().waitUntilIsInSession();
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

    public void waitUntilIsInviteeNotInSession(SuperRemoteBot sarosBot)
        throws RemoteException {
        sarosBot.views().sessionView().waitUntilIsNotInSession();
    }

    public void waitUntilAllPeersLeaveSession(
        final List<JID> jidsOfAllParticipants) throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                for (JID jid : jidsOfAllParticipants) {
                    if (existsParticipant(jid))
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

        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return view.toolbarButtonWithRegex(
                    TB_INCONSISTENCY_DETECTED + ".*").isEnabled();
            }

            public String getFailureMessage() {
                return "The toolbar button " + TB_INCONSISTENCY_DETECTED
                    + " isn't enabled.";
            }
        });
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/
    private void clickToolbarButtonWithTooltip(String tooltipText)
        throws RemoteException {
        view.toolbarButtonWithRegex(tooltipText + ".*").click();
    }

    private void selectParticipant(JID jidOfSelectedUser, String message)
        throws RemoteException {
        if (localJID.equals(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        selectParticipant(jidOfSelectedUser);
    }

    private List<JID> getInvitees() {
        List<JID> invitees = new ArrayList<JID>();
        ISarosSession sarosSession = sessionManager.getSarosSession();
        for (User user : sarosSession.getParticipants()) {
            if (!user.isHost())
                invitees.add(user.getJID());
        }
        return invitees;
    }

    private boolean hasWriteAccessNoGUI() {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;
        return sarosSession.hasWriteAccess();
    }

    private boolean hasWriteAccessByNoGUI(JID jid) {
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
}
