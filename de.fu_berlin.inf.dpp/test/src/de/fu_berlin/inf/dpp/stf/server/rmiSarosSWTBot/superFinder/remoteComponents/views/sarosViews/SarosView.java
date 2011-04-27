package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.hamcrest.Matcher;
import org.jivesoftware.smack.Roster;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotToolbarDropDownButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.ISuperBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.IBuddiesContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.ISarosContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.Views;

/**
 * This implementation of {@link ISarosView}
 * 
 * @author lchen
 */
public class SarosView extends Views implements ISarosView {

    private static transient SarosView self;

    private IRemoteBotView view;
    private IRemoteBotTree tree;

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
        if (!sarosBot().menuBar().saros().preferences().existsAccount(jid)) {
            sarosBot().menuBar().saros().preferences()
                .addAccount(jid, password);
        } else {
            if (!sarosBot().menuBar().saros().preferences()
                .isAccountActive(jid)) {
                sarosBot().menuBar().saros().preferences().activateAccount(jid);
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
            if (!sarosBot().menuBar().saros().preferences().existsAccount()) {
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
            sarosBot().confirmShellAddBuddy(jid);
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
     * TODO: With {@link IRemoteBotView#toolbarButtonWithRegex(String)} to
     * perform this action you will get WidgetNotFoundException.
     */

    public void addBuddyToSession(String... jidOfInvitees)
        throws RemoteException {
        view.toolbarButton(TB_ADD_BUDDY_TO_SESSION).click();
        sarosBot().confirmShellAddBuddyToSession(jidOfInvitees);
    }

    // public void addANewBuddy(JID jidOfInvitee) throws RemoteException {
    // view.toolbarButton(TB_ADD_A_NEW_BUDDY).click();
    // sarosBot().confirmShellAddBuddy(jidOfInvitee);
    // }

    /**
     * Note: {@link STF#TB_INCONSISTENCY_DETECTED} is not complete toolbarName,
     * so we need to use {@link IRemoteBotView#toolbarButtonWithRegex(String)}
     * to perform this action.
     */
    public void inconsistencyDetected() throws RemoteException {
        view.toolbarButtonWithRegex(TB_INCONSISTENCY_DETECTED + ".*").click();
        bot().waitUntilShellIsClosed(SHELL_PROGRESS_INFORMATION);
    }

    /**********************************************
     * 
     * Content of Saros View
     * 
     **********************************************/

    public IBuddiesContextMenuWrapper selectBuddies() throws RemoteException {
        initSarosContextMenuWrapper(tree.selectTreeItemWithRegex(NODE_BUDDIES));
        buddiesContextMenu.setSarosView(this);
        return buddiesContextMenu;
    }

    public IBuddiesContextMenuWrapper selectBuddy(JID buddyJID)
        throws RemoteException {
        if (getNickName(buddyJID) == null) {
            throw new RuntimeException("No buddy with the ID "
                + buddyJID.getBase() + " existed!");
        }
        initSarosContextMenuWrapper(tree.selectTreeItemWithRegex(NODE_BUDDIES,
            getNickName(buddyJID) + ".*"));
        buddiesContextMenu.setSarosView(this);
        return buddiesContextMenu;
    }

    public ISarosContextMenuWrapper selectParticipant(final JID participantJID)
        throws RemoteException {
        if (!isInSession())
            throw new RuntimeException("You are not in a session!");
        String participantLabel = getParticipantLabel(participantJID);
        for (int i = 0; i < tree.selectTreeItem(NODE_SESSION).getTextOfItems()
            .size(); i++) {
            System.out.println("bla: "
                + tree.selectTreeItem(NODE_SESSION).getTextOfItems().get(i));
        }
        initSarosContextMenuWrapper(tree.selectTreeItem(NODE_SESSION,
            participantLabel));

        sarosContextMenu.setParticipantJID(participantJID);
        sarosContextMenu.setSarosView(this);
        return sarosContextMenu;
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
            if (hasWriteAccessNoGUI())
                contactLabel = OWN_PARTICIPANT_NAME;
            else
                contactLabel = OWN_PARTICIPANT_NAME + " " + PERMISSION_NAME;
        } else if (sarosBot().views().sarosView().hasNickName(participantJID)) {
            if (hasWriteAccessByNoGUI(participantJID))
                contactLabel = sarosBot().views().sarosView()
                    .getNickName(participantJID)
                    + " (" + participantJID.getBase() + ")";
            else
                contactLabel = sarosBot().views().sarosView()
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
        if (isFollowing()) {
            contactLabel += " (following)";
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
        return view.bot().existsLabelInGroup("Session");
    }

    public boolean isHost() throws RemoteException {
        if (!isInSession())
            return false;
        String ownLabelsInSessionView = getParticipantLabel(localJID);
        String talbeItem = tree.selectTreeItem(NODE_SESSION).getNode(0)
            .getText();
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
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isConnected();
            }

            public String getFailureMessage() {
                return "Can't connect.";
            }
        });
    }

    public void waitUntilDisConnected() throws RemoteException {
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isDisConnected();
            }

            public String getFailureMessage() {
                return "Can't disconnect.";
            }
        });
    }

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

    public void waitUntilIsInviteeInSession(ISuperBot sarosBot)
        throws RemoteException {
        sarosBot.views().sarosView().waitUntilIsInSession();
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

    public void waitUntilIsInviteeNotInSession(ISuperBot sarosBot)
        throws RemoteException {
        sarosBot.views().sarosView().waitUntilIsNotInSession();
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
     * inner functions
     * 
     **************************************************************/

    private boolean isToolbarButtonEnabled(String tooltip)
        throws RemoteException {
        if (!view.existsToolbarButton(tooltip))
            return false;
        return view.toolbarButton(tooltip).isEnabled();
    }

    // private void clickToolbarButtonWithTooltip(String tooltipText)
    // throws RemoteException {
    // if (!view.existsToolbarButton(tooltipText))
    // throw new RuntimeException("The toolbarbutton " + tooltipText
    // + " doesn't exist!");
    // view.toolbarButton(tooltipText).click();
    // }

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

    private void setViewWithTree(IRemoteBotView view) throws RemoteException {
        this.view = view;
        tree = view.bot().tree();
        // treeItem = null;
    }

    private void initSarosContextMenuWrapper(IRemoteBotTreeItem treeItem) {
        // this.treeItem = treeItem;
        sarosContextMenu.setTree(tree);
        sarosContextMenu.setTreeItem(treeItem);
    }

}
