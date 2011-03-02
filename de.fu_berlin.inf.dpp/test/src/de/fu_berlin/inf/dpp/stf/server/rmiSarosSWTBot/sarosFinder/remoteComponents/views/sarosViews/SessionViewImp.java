package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.SarosComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosContextMenuWrapperImp;

/**
 * This implementation of {@link SessionView}
 * 
 * @author lchen
 */
public class SessionViewImp extends SarosComponentImp implements SessionView {

    private static transient SessionViewImp self;
    private STFBotView view;
    private STFBotTable table;
    private static SarosContextMenuWrapperImp contextMenu;

    /**
     * {@link SessionViewImp} is a singleton, but inheritance is possible.
     */
    public static SessionViewImp getInstance() {
        if (self != null)
            return self;
        self = new SessionViewImp();
        contextMenu = SarosContextMenuWrapperImp.getInstance();
        return self;
    }

    public SessionView setView(STFBotView view) throws RemoteException {
        this.view = view;
        this.table = view.bot().table();
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

    public SarosContextMenuWrapper selectBuddy(final JID participantJID)
        throws RemoteException {
        String participantLabel = sarosBot().state().getParticipantLabel(
            participantJID);
        contextMenu.setTableItem(table.getTableItem(participantLabel));
        contextMenu.setParticipantJID(participantJID);
        return contextMenu;
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
        if (!sarosBot().state().isHost()) {
            throw new RuntimeException("Only host can perform this action.");
        }
        if (isToolbarButtonEnabled(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS)) {
            clickToolbarButtonWithTooltip(TB_RESTRICT_INVITEES_TO_READ_ONLY_ACCESS);
            for (JID invitee : getInvitees())
                sarosBot().condition().waitUntilHasReadOnlyAccessBy(invitee);
            bot().sleep(300);
        }
    }

    public void leaveTheSession() throws RemoteException {

        if (!sarosBot().state().isHost()) {
            clickToolbarButtonWithTooltip(TB_LEAVE_THE_SESSION);
            bot().waitUntilShellIsOpen(SHELL_CONFIRM_LEAVING_SESSION);
            bot().shell(SHELL_CONFIRM_LEAVING_SESSION).activate();
            bot().shell(SHELL_CONFIRM_LEAVING_SESSION).confirm(YES);
        } else {
            clickToolbarButtonWithTooltip(TB_LEAVE_THE_SESSION);
            bot().waitUntilShellIsOpen(SHELL_CONFIRM_CLOSING_SESSION);
            bot().shell(SHELL_CONFIRM_CLOSING_SESSION).activate();
            bot().shell(SHELL_CONFIRM_CLOSING_SESSION).confirm(YES);
        }
        sarosBot().condition().waitUntilIsNotInSession();
    }

    public void openInvitationInterface(String... jidOfInvitees)
        throws RemoteException {
        clickToolbarButtonWithTooltip(TB_OPEN_INVITATION_INTERFACE);
        sarosBot().confirmShellInvitation(jidOfInvitees);
    }

    public void inconsistencyDetected() throws RemoteException {
        clickToolbarButtonWithTooltip(TB_INCONSISTENCY_DETECTED);
        bot().waitsUntilShellIsClosed(SHELL_PROGRESS_INFORMATION);
    }

    private void selectParticipant(JID jidOfSelectedUser, String message)
        throws RemoteException {
        if (localJID.equals(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        selectBuddy(jidOfSelectedUser);
    }

    private boolean isToolbarButtonEnabled(String tooltip)
        throws RemoteException {
        return view.toolbarButtonWithRegex(tooltip + ".*").isEnabled();
    }

    private void clickToolbarButtonWithTooltip(String tooltipText)
        throws RemoteException {
        view.toolbarButtonWithRegex(tooltipText + ".*").click();
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
