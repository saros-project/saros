package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.hamcrest.Matcher;
import org.jivesoftware.smack.Roster;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarDropDownButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.ISuperBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInBuddiesArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.IContextMenusInSessionArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl.ContextMenusInBuddiesArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.sarosview.impl.ContextMenusInSessionArea;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.IChatroom;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.ISarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

/**
 * This implementation of {@link ISarosView}
 * 
 * @author lchen
 */
public final class SarosView extends StfRemoteObject implements ISarosView {

    private static final Logger log = Logger.getLogger(SarosView.class);

    private static final SarosView INSTANCE = new SarosView();

    private IRemoteBotView view;
    private IRemoteBotTree tree;

    public static SarosView getInstance() {
        return INSTANCE;
    }

    public ISarosView setView(IRemoteBotView view) throws RemoteException {
        setViewWithTree(view);
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * ToolbarButtons
     * 
     **********************************************/

    public void connectWith(JID jid, String password) throws RemoteException {
        log.trace("connectedByXMPP");

        log.trace("click the toolbar button 'Connect' in the buddies view");
        if (!SuperBot.getInstance().menuBar().saros().preferences()
            .existsAccount(jid)) {
            SuperBot.getInstance().menuBar().saros().preferences()
                .addAccount(jid, password);
        } else {
            if (!SuperBot.getInstance().menuBar().saros().preferences()
                .isAccountActive(jid)) {
                SuperBot.getInstance().menuBar().saros().preferences()
                    .activateAccount(jid);
            }
        }
        if (!isConnected()) {
            clickToolbarButtonWithTooltip(TB_CONNECT);
            waitUntilIsConnected();
        } else {
            clickToolbarButtonWithTooltip(TB_DISCONNECT);
            waitUntilIsDisconnected();

            clickToolbarButtonWithTooltip(TB_CONNECT);
            waitUntilIsConnected();
        }

    }

    public void connectWithActiveAccount() throws RemoteException {
        if (isDisconnected()) {
            if (!SuperBot.getInstance().menuBar().saros().preferences()
                .existsAccount()) {
                throw new RuntimeException(
                    "unable to connect with the active account, it does not exists");
            }
            clickToolbarButtonWithTooltip(TB_CONNECT);
            waitUntilIsConnected();
        }
    }

    public void disconnect() throws RemoteException {
        if (isConnected()) {
            clickToolbarButtonWithTooltip(TB_DISCONNECT);
            waitUntilIsDisconnected();
        }
    }

    /*
     * FIXME: there are some problems by clicking the toolbarDropDownButton.
     */
    @SuppressWarnings("unused")
    private void selectConnectAccount(String baseJID) throws RemoteException {
        IRemoteBotToolbarDropDownButton b = view
            .toolbarDropDownButton(TB_CONNECT);
        @SuppressWarnings("static-access")
        Matcher<MenuItem> withRegex = WidgetMatcherFactory.withRegex(baseJID
            + ".*");
        b.menuItem(withRegex).click();
        try {
            b.pressShortcut(KeyStroke.getInstance("ESC"));
        } catch (ParseException e) {
            log.debug("", e);
        }
    }

    public void addNewBuddy(JID jid) throws RemoteException {
        if (!hasBuddy(jid)) {
            clickToolbarButtonWithTooltip(TB_ADD_A_NEW_BUDDY);
            SuperBot.getInstance().confirmShellAddBuddy(jid);
        }
    }

    public void shareYourScreenWithSelectedBuddy(JID jidOfPeer)
        throws RemoteException {
        selectParticipant(jidOfPeer, "you cannot share a screen with youself");
        clickToolbarButtonWithTooltip(TB_SHARE_SCREEN_WITH_BUDDY);
    }

    public void stopSessionWithBuddy(JID jidOfPeer) throws RemoteException {
        selectParticipant(jidOfPeer,
            "you cannot stop a screen session with youself");
        clickToolbarButtonWithTooltip(TB_STOP_SESSION_WITH_BUDDY);
    }

    public void sendFileToSelectedBuddy(JID jidOfPeer) throws RemoteException {
        selectParticipant(jidOfPeer, "you cannot send a file to youself");
        clickToolbarButtonWithTooltip(TB_SEND_A_FILE_TO_SELECTED_BUDDY);
    }

    public void startAVoIPSessionWithSelectedBuddy(JID jidOfPeer)
        throws RemoteException {
        selectParticipant(jidOfPeer,
            "you cannot start a VoIP session with youself");
        clickToolbarButtonWithTooltip(TB_START_VOIP_SESSION);
        if (RemoteWorkbenchBot.getInstance().shell(SHELL_ERROR_IN_SAROS_PLUGIN)
            .isActive()) {
            RemoteWorkbenchBot.getInstance().shell(SHELL_ERROR_IN_SAROS_PLUGIN)
                .confirm(OK);
        }
    }

    public void leaveSession() throws RemoteException {
        if (isInSession()) {
            if (!isHost()) {
                clickToolbarButtonWithTooltip(TB_LEAVE_SESSION);
                RemoteWorkbenchBot.getInstance().waitUntilShellIsOpen(
                    SHELL_CONFIRM_LEAVING_SESSION);
                RemoteWorkbenchBot.getInstance()
                    .shell(SHELL_CONFIRM_LEAVING_SESSION).activate();
                RemoteWorkbenchBot.getInstance()
                    .shell(SHELL_CONFIRM_LEAVING_SESSION).confirm(YES);
            } else {
                clickToolbarButtonWithTooltip(TB_STOP_SESSION);
                RemoteWorkbenchBot.getInstance().waitUntilShellIsOpen(
                    SHELL_CONFIRM_CLOSING_SESSION);
                RemoteWorkbenchBot.getInstance()
                    .shell(SHELL_CONFIRM_CLOSING_SESSION).activate();
                RemoteWorkbenchBot.getInstance()
                    .shell(SHELL_CONFIRM_CLOSING_SESSION).confirm(YES);
            }
            waitUntilIsNotInSession();
        }
    }

    // /**
    // * TODO: With {@link IRemoteBotView#toolbarButtonWithRegex(String)} to
    // * perform this action you will get WidgetNotFoundException.
    // */
    //
    // public void addBuddyToSession(String... jidOfInvitees)
    // throws RemoteException {
    // view.toolbarButton(TB_ADD_BUDDY_TO_SESSION).click();
    // superBot().confirmShellAddBuddyToSession(jidOfInvitees);
    // }

    /**
     * Note: {@link StfRemoteObject#TB_INCONSISTENCY_DETECTED} is not complete
     * toolbarName, so we need to use
     * {@link IRemoteBotView#toolbarButtonWithRegex(String)} to perform this
     * action.
     */
    public void inconsistencyDetected() throws RemoteException {
        view.toolbarButtonWithRegex(TB_INCONSISTENCY_DETECTED + ".*").click();
        RemoteWorkbenchBot.getInstance().waitUntilShellIsClosed(
            SHELL_PROGRESS_INFORMATION);
    }

    /**********************************************
     * 
     * Content of Saros View
     * 
     **********************************************/

    public IContextMenusInBuddiesArea selectBuddies() throws RemoteException {
        initBuddiesContextMenuWrapper(tree
            .selectTreeItemWithRegex(NODE_BUDDIES));
        return ContextMenusInBuddiesArea.getInstance();
    }

    public IContextMenusInBuddiesArea selectBuddy(JID buddyJID)
        throws RemoteException {
        if (getNickname(buddyJID) == null) {
            throw new RuntimeException("no buddy exists with the JID: "
                + buddyJID.getBase());
        }
        initBuddiesContextMenuWrapper(tree.selectTreeItemWithRegex(
            NODE_BUDDIES, getNickname(buddyJID) + ".*"));
        return ContextMenusInBuddiesArea.getInstance();
    }

    public IContextMenusInSessionArea selectSession() throws RemoteException {
        if (!isInSession())
            throw new RuntimeException("you are not in a session");
        initSessionContextMenuWrapper(tree
            .selectTreeItemWithRegex(NODE_SESSION));
        return ContextMenusInSessionArea.getInstance();
    }

    public IContextMenusInSessionArea selectNoSessionRunning()
        throws RemoteException {
        if (isInSession())
            throw new RuntimeException("you are in a session");
        initSessionContextMenuWrapper(tree
            .selectTreeItemWithRegex(NODE_NO_SESSION_RUNNING));
        return ContextMenusInSessionArea.getInstance();
    }

    public IContextMenusInSessionArea selectParticipant(final JID participantJID)
        throws RemoteException {
        if (!isInSession())
            throw new RuntimeException("you are not in a session");
        String participantLabel = getParticipantLabel(participantJID);
        initSessionContextMenuWrapper(tree.selectTreeItemWithRegex(
            NODE_SESSION, participantLabel));
        ContextMenusInSessionArea.getInstance().setParticipantJID(
            participantJID);
        return ContextMenusInSessionArea.getInstance();
    }

    public IChatroom selectChatroom() throws RemoteException {
        view.bot().cTabItem();
        return Chatroom.getInstance();
    }

    /**********************************************
     * 
     * state
     * 
     **********************************************/

    public boolean isConnected() throws RemoteException {
        return isToolbarButtonEnabled(TB_DISCONNECT);
    }

    public boolean isDisconnected() throws RemoteException {
        return isToolbarButtonEnabled(TB_CONNECT);
    }

    public String getNickname(JID buddyJID) throws RemoteException {
        Roster roster = saros.getRoster();
        if (roster.getEntry(buddyJID.getBase()) == null)
            return null;
        if (roster.getEntry(buddyJID.getBase()).getName() == null)
            return buddyJID.getBase();
        else
            return roster.getEntry(buddyJID.getBase()).getName();
    }

    public boolean hasNickName(JID buddyJID) throws RemoteException {
        if (getNickname(buddyJID) == null)
            return false;
        if (!getNickname(buddyJID).equals(buddyJID.getBase()))
            return true;
        return false;
    }

    public List<String> getAllBuddies() throws RemoteException {
        return tree.selectTreeItem(NODE_BUDDIES).getTextOfItems();
    }

    public boolean hasBuddy(JID buddyJID) throws RemoteException {
        String nickName = getNickname(buddyJID);
        if (nickName == null)
            return false;
        return tree.selectTreeItemWithRegex(NODE_BUDDIES)
            .existsSubItemWithRegex(nickName + ".*");
    }

    public boolean existsParticipant(JID participantJID) throws RemoteException {
        String participantLabel = getParticipantLabel(participantJID);
        List<String> nodes = tree.selectTreeItem(NODE_SESSION).getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            if (tree.selectTreeItem(NODE_SESSION).getNode(i).getText()
                .equals(participantLabel))
                return true;
        }
        return false;
    }

    public String getParticipantLabel(JID participantJID)
        throws RemoteException {
        String contactLabel;
        if (SuperBot.getInstance().getJID().equals(participantJID)) {
            // if (hasWriteAccessNoGUI())
            contactLabel = OWN_PARTICIPANT_NAME;
            // else
            // contactLabel = OWN_PARTICIPANT_NAME + " " + PERMISSION_NAME;
        } else if (SuperBot.getInstance().views().sarosView()
            .hasNickName(participantJID)) {
            // if (hasWriteAccessByNoGUI(participantJID))
            contactLabel = SuperBot.getInstance().views().sarosView()
                .getNickname(participantJID)
                + " \\(" + participantJID.getBase() + "\\)";
            // else
            // contactLabel = sarosBot().views().sarosView()
            // .getNickName(participantJID)
            // + " ("
            // + participantJID.getBase()
            // + ")"
            // + " "
            // + PERMISSION_NAME;
        } else {
            // if (hasWriteAccessByNoGUI(participantJID))
            contactLabel = participantJID.getBase();
            // else
            // contactLabel = participantJID.getBase() + " " + PERMISSION_NAME;
        }
        return contactLabel + ".*";
    }

    public boolean isInSession() throws RemoteException {
        if (view.existsToolbarButton(TB_STOP_SESSION))
            return view.toolbarButton(TB_STOP_SESSION).isEnabled();
        else if (view.existsToolbarButton(TB_LEAVE_SESSION))
            return view.toolbarButton(TB_LEAVE_SESSION).isEnabled();
        return false;
    }

    public boolean isHost() throws RemoteException {
        if (!isInSession())
            return false;
        String ownLabelsInSessionView = getParticipantLabel(SuperBot
            .getInstance().getJID());
        String talbeItem = tree.selectTreeItem(NODE_SESSION).getNode(0)
            .getText();
        if (talbeItem.matches(ownLabelsInSessionView))
            return true;
        return false;
    }

    public boolean isFollowing() throws RemoteException {
        JID followedBuddy = getFollowedBuddy();
        if (followedBuddy == null)
            return false;
        return selectParticipant(followedBuddy).isFollowing();
    }

    public List<String> getAllParticipants() throws RemoteException {
        return tree.selectTreeItem(NODE_SESSION).getNodes();

    }

    public JID getJID() throws RemoteException {
        return SuperBot.getInstance().getJID();
    }

    public JID getFollowedBuddy() throws RemoteException {
        if (editorManager.getFollowedUser() != null)
            return editorManager.getFollowedUser().getJID();
        else
            return null;
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/

    public void waitUntilIsConnected() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isConnected();
            }

            public String getFailureMessage() {
                return "unable to connect to server";
            }
        });
    }

    public void waitUntilIsDisconnected() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isDisconnected();
            }

            public String getFailureMessage() {
                return "unable to disconnect from server";
            }
        });
    }

    public void waitUntilIsInSession() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isInSession();
            }

            public String getFailureMessage() {
                return "joining the session failed";
            }
        });
    }

    public void waitUntilIsInviteeInSession(ISuperBot sarosBot)
        throws RemoteException {
        sarosBot.views().sarosView().waitUntilIsInSession();
    }

    public void waitUntilIsNotInSession() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isInSession();
            }

            public String getFailureMessage() {
                return "leaving the session failed";
            }
        });
    }

    public void waitUntilIsInviteeNotInSession(ISuperBot sarosBot)
        throws RemoteException {
        sarosBot.views().sarosView().waitUntilIsNotInSession();
    }

    public void waitUntilAllPeersLeaveSession(
        final List<JID> jidsOfAllParticipants) throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                for (JID jid : jidsOfAllParticipants) {
                    if (existsParticipant(jid))
                        return false;
                }
                return true;
            }

            public String getFailureMessage() {
                return "there are still users in the session";
            }
        });
    }

    public void waitUntilIsInconsistencyDetected() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return view.toolbarButtonWithRegex(
                    TB_INCONSISTENCY_DETECTED + ".*").isEnabled();
            }

            public String getFailureMessage() {
                return "the toolbar button " + TB_INCONSISTENCY_DETECTED
                    + " is not enabled";
            }
        });
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

    private boolean isToolbarButtonEnabled(String tooltip)
        throws RemoteException {
        if (!view.existsToolbarButton(tooltip))
            return false;
        return view.toolbarButton(tooltip).isEnabled();
    }

    private void clickToolbarButtonWithTooltip(String tooltipText)
        throws RemoteException {
        view.toolbarButtonWithRegex(tooltipText + ".*").click();
    }

    private void selectParticipant(JID jidOfSelectedUser, String message)
        throws RemoteException {
        if (SuperBot.getInstance().getJID().equals(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        selectParticipant(jidOfSelectedUser);
    }

    private void setViewWithTree(IRemoteBotView view) throws RemoteException {
        this.view = view;
        tree = view.bot().tree();
        // treeItem = null;
    }

    private void initSessionContextMenuWrapper(IRemoteBotTreeItem treeItem) {
        // this.treeItem = treeItem;
        ContextMenusInSessionArea.getInstance().setTree(tree);
        ContextMenusInSessionArea.getInstance().setTreeItem(treeItem);
        ContextMenusInSessionArea.getInstance().setSarosView(this);
    }

    private void initBuddiesContextMenuWrapper(IRemoteBotTreeItem treeItem) {
        // this.treeItem = treeItem;
        ContextMenusInBuddiesArea.getInstance().setTree(tree);
        ContextMenusInBuddiesArea.getInstance().setTreeItem(treeItem);
        ContextMenusInBuddiesArea.getInstance().setSarosView(this);
    }

}
