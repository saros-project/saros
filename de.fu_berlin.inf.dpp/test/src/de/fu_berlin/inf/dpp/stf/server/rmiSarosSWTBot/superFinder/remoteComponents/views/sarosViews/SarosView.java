package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.hamcrest.Matcher;
import org.jivesoftware.smack.Roster;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotToolbarDropDownButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.ISuperBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.sarosView.IContextMenusInBuddiesArea;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.sarosView.IContextMenusInSessionArea;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.Views;
import de.fu_berlin.inf.dpp.stf.stfMessages.STFMessages;

/**
 * This implementation of {@link ISarosView}
 * 
 * @author lchen
 */
public class SarosView extends Views implements ISarosView {

    private static transient SarosView self;

    private IRemoteBotView view;
    private IRemoteBotTree tree;

    protected static Chatroom chatroom = Chatroom.getInstance();

    // private STFBotTreeItem treeItem;

    /**
     * {@link SarosView} is a singleton, but inheritance is possible.
     */
    public static SarosView getInstance() {
        if (self != null)
            return self;
        self = new SarosView();
        return self;
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

        log.trace("click the toolbar button \"Connect\" in the buddies view");
        if (!superBot().menuBar().saros().preferences().existsAccount(jid)) {
            superBot().menuBar().saros().preferences()
                .addAccount(jid, password);
        } else {
            if (!superBot().menuBar().saros().preferences()
                .isAccountActive(jid)) {
                superBot().menuBar().saros().preferences().activateAccount(jid);
            }
        }
        if (!isConnected()) {
            clickToolbarButtonWithTooltip(TB_CONNECT);
            waitUntilIsConnected();
        } else {
            clickToolbarButtonWithTooltip(TB_DISCONNECT);
            waitUntilDisConnected();

            clickToolbarButtonWithTooltip(TB_CONNECT);
            waitUntilIsConnected();
        }

    }

    public void connectWithActiveAccount() throws RemoteException {
        if (isDisConnected()) {
            if (!superBot().menuBar().saros().preferences().existsAccount()) {
                throw new RuntimeException(
                    "You need to at first add a account!");
            }
            clickToolbarButtonWithTooltip(TB_CONNECT);
            waitUntilIsConnected();
        }
    }

    public void disconnect() throws RemoteException {
        if (isConnected()) {
            clickToolbarButtonWithTooltip(TB_DISCONNECT);
            waitUntilDisConnected();
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

    public void addANewBuddy(JID jid) throws RemoteException {
        if (!hasBuddy(jid)) {
            clickToolbarButtonWithTooltip(TB_ADD_A_NEW_BUDDY);
            superBot().confirmShellAddBuddy(jid);
        }
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
        if (remoteBot().shell(SHELL_ERROR_IN_SAROS_PLUGIN).isActive()) {
            remoteBot().shell(SHELL_ERROR_IN_SAROS_PLUGIN).confirm(OK);
        }
    }

    public void leaveSession() throws RemoteException {
        if (isInSession()) {
            if (!isHost()) {
                clickToolbarButtonWithTooltip(TB_LEAVE_SESSION);
                remoteBot().waitUntilShellIsOpen(SHELL_CONFIRM_LEAVING_SESSION);
                remoteBot().shell(SHELL_CONFIRM_LEAVING_SESSION).activate();
                remoteBot().shell(SHELL_CONFIRM_LEAVING_SESSION).confirm(YES);
            } else {
                clickToolbarButtonWithTooltip(TB_STOP_SESSION);
                remoteBot().waitUntilShellIsOpen(SHELL_CONFIRM_CLOSING_SESSION);
                remoteBot().shell(SHELL_CONFIRM_CLOSING_SESSION).activate();
                remoteBot().shell(SHELL_CONFIRM_CLOSING_SESSION).confirm(YES);
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
     * Note: {@link STFMessages#TB_INCONSISTENCY_DETECTED} is not complete toolbarName,
     * so we need to use {@link IRemoteBotView#toolbarButtonWithRegex(String)}
     * to perform this action.
     */
    public void inconsistencyDetected() throws RemoteException {
        view.toolbarButtonWithRegex(TB_INCONSISTENCY_DETECTED + ".*").click();
        remoteBot().waitUntilShellIsClosed(SHELL_PROGRESS_INFORMATION);
    }

    /**********************************************
     * 
     * Content of Saros View
     * 
     **********************************************/

    public IContextMenusInBuddiesArea selectBuddies() throws RemoteException {
        initBuddiesContextMenuWrapper(tree
            .selectTreeItemWithRegex(NODE_BUDDIES));
        return buddiesContextMenu;
    }

    public IContextMenusInBuddiesArea selectBuddy(JID buddyJID)
        throws RemoteException {
        if (getNickName(buddyJID) == null) {
            throw new RuntimeException("No buddy with the ID "
                + buddyJID.getBase() + " existed!");
        }
        initBuddiesContextMenuWrapper(tree.selectTreeItemWithRegex(
            NODE_BUDDIES, getNickName(buddyJID) + ".*"));
        return buddiesContextMenu;
    }

    public IContextMenusInSessionArea selectSession() throws RemoteException {
        if (!isInSession())
            throw new RuntimeException("You are not in a session!");
        initSessionContextMenuWrapper(tree
            .selectTreeItemWithRegex(NODE_SESSION));
        return sessionContextMenu;
    }

    public IContextMenusInSessionArea selectNoSessionRunning()
        throws RemoteException {
        if (isInSession())
            throw new RuntimeException("You are in a session!");
        initSessionContextMenuWrapper(tree
            .selectTreeItemWithRegex(NODE_NO_SESSION_RUNNING));
        return sessionContextMenu;
    }

    public IContextMenusInSessionArea selectParticipant(final JID participantJID)
        throws RemoteException {
        if (!isInSession())
            throw new RuntimeException("You are not in a session!");
        String participantLabel = getParticipantLabel(participantJID);
        initSessionContextMenuWrapper(tree.selectTreeItemWithRegex(
            NODE_SESSION, participantLabel));
        sessionContextMenu.setParticipantJID(participantJID);
        return sessionContextMenu;
    }

    public IChatroom selectChatroom() throws RemoteException {
        view.bot().cTabItem();
        return chatroom;
    }

    /**********************************************
     * 
     * state
     * 
     **********************************************/

    public boolean isConnected() throws RemoteException {
        return isToolbarButtonEnabled(TB_DISCONNECT);
    }

    public boolean isDisConnected() throws RemoteException {
        return isToolbarButtonEnabled(TB_CONNECT);
    }

    public String getNickName(JID buddyJID) throws RemoteException {
        Roster roster = saros.getRoster();
        if (roster.getEntry(buddyJID.getBase()) == null)
            return null;
        if (roster.getEntry(buddyJID.getBase()).getName() == null)
            return buddyJID.getBase();
        else
            return roster.getEntry(buddyJID.getBase()).getName();
    }

    public boolean hasNickName(JID buddyJID) throws RemoteException {
        if (getNickName(buddyJID) == null)
            return false;
        if (!getNickName(buddyJID).equals(buddyJID.getBase()))
            return true;
        return false;
    }

    public List<String> getAllBuddies() throws RemoteException {
        return tree.selectTreeItem(NODE_BUDDIES).getTextOfItems();
    }

    /*
     * FIXME: there are some problems by clicking the toolbarDropDownButton.
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    private boolean isConnectAccountExist(String baseJID)
        throws RemoteException {
        Matcher matcher = allOf(widgetOfType(MenuItem.class));
        IRemoteBotToolbarDropDownButton b = view
            .toolbarDropDownButton(TB_CONNECT);
        List<? extends IRemoteBotMenu> accounts = b.menuItems(matcher);
        b.pressShortcut(Keystrokes.ESC);
        for (IRemoteBotMenu account : accounts) {
            log.debug("existed account: " + account.getText() + "hier");
            if (account.getText().trim().equals(baseJID)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBuddy(JID buddyJID) throws RemoteException {
        String nickName = getNickName(buddyJID);
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
        if (localJID.equals(participantJID)) {
            // if (hasWriteAccessNoGUI())
            contactLabel = OWN_PARTICIPANT_NAME;
            // else
            // contactLabel = OWN_PARTICIPANT_NAME + " " + PERMISSION_NAME;
        } else if (superBot().views().sarosView().hasNickName(participantJID)) {
            // if (hasWriteAccessByNoGUI(participantJID))
            contactLabel = superBot().views().sarosView()
                .getNickName(participantJID)
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
        String ownLabelsInSessionView = getParticipantLabel(localJID);
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
     * waits until
     * 
     **********************************************/

    public void waitUntilIsConnected() throws RemoteException {
        remoteBot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isConnected();
            }

            public String getFailureMessage() {
                return "Can't connect.";
            }
        });
    }

    public void waitUntilDisConnected() throws RemoteException {
        remoteBot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isDisConnected();
            }

            public String getFailureMessage() {
                return "Can't disconnect.";
            }
        });
    }

    public void waitUntilIsInSession() throws RemoteException {
        remoteBot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isInSession();
            }

            public String getFailureMessage() {
                return "can't open the session.";
            }
        });
    }

    public void waitUntilIsInviteeInSession(ISuperBot sarosBot)
        throws RemoteException {
        sarosBot.views().sarosView().waitUntilIsInSession();
    }

    public void waitUntilIsNotInSession() throws RemoteException {
        remoteBot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isInSession();
            }

            public String getFailureMessage() {
                return "can't close the session.";
            }
        });
    }

    public void waitUntilIsInviteeNotInSession(ISuperBot sarosBot)
        throws RemoteException {
        sarosBot.views().sarosView().waitUntilIsNotInSession();
    }

    public void waitUntilAllPeersLeaveSession(
        final List<JID> jidsOfAllParticipants) throws RemoteException {
        remoteBot().waitUntil(new DefaultCondition() {
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
        remoteBot().waitUntil(new DefaultCondition() {
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
        if (localJID.equals(jidOfSelectedUser)) {
            throw new RuntimeException(message);
        }
        selectParticipant(jidOfSelectedUser);
    }

    // private List<JID> getInvitees() {
    // List<JID> invitees = new ArrayList<JID>();
    // ISarosSession sarosSession = sessionManager.getSarosSession();
    // for (User user : sarosSession.getParticipants()) {
    // if (!user.isHost())
    // invitees.add(user.getJID());
    // }
    // return invitees;
    // }
    //
    // private boolean hasWriteAccessNoGUI() {
    // ISarosSession sarosSession = sessionManager.getSarosSession();
    // if (sarosSession == null)
    // return false;
    // return sarosSession.hasWriteAccess();
    // }
    //
    // private boolean hasWriteAccessByNoGUI(JID jid) {
    // ISarosSession sarosSession = sessionManager.getSarosSession();
    // if (sarosSession == null)
    // return false;
    // User user = sarosSession.getUser(jid);
    // if (user == null)
    // return false;
    // log.debug("isDriver(" + jid.toString() + ") == "
    // + sarosSession.getUsersWithWriteAccess().contains(user));
    // return sarosSession.getUsersWithWriteAccess().contains(user);
    // }

    private void setViewWithTree(IRemoteBotView view) throws RemoteException {
        this.view = view;
        tree = view.bot().tree();
        // treeItem = null;
    }

    private void initSessionContextMenuWrapper(IRemoteBotTreeItem treeItem) {
        // this.treeItem = treeItem;
        sessionContextMenu.setTree(tree);
        sessionContextMenu.setTreeItem(treeItem);
        sessionContextMenu.setSarosView(this);

    }

    private void initBuddiesContextMenuWrapper(IRemoteBotTreeItem treeItem) {
        // this.treeItem = treeItem;
        buddiesContextMenu.setTree(tree);
        buddiesContextMenu.setTreeItem(treeItem);
        buddiesContextMenu.setSarosView(this);
    }

}
