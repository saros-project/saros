package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.Matcher;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.SarosComponentImp;

/**
 * This implementation of {@link RosterView}
 * 
 * @author Lin
 */
public class RosterViewImp extends SarosComponentImp implements RosterView {

    private static transient RosterViewImp self;

    /**
     * {@link RosterViewImp} is a singleton, but inheritance is possible.
     */
    public static RosterViewImp getInstance() {
        if (self != null)
            return self;
        self = new RosterViewImp();
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

    public void connectWith(JID jid, String password) throws RemoteException {
        precondition();
        log.trace("connectedByXMPP");
        if (!isConnected()) {
            log.trace("click the toolbar button \"Connect\" in the r�oster view");
            if (!sarosM.isAccountExistNoGUI(jid, password))
                sarosM.createAccountInShellSarosPeferences(jid, password);

            if (!sarosM.isAccountActiveNoGUI(jid))
                sarosM.activateAccount(jid);
            clickToolbarButtonWithTooltip(TB_CONNECT);

            waitUntilIsConnected();
        }
    }

    public void connectWithCurrentActiveAccount() throws RemoteException {
        precondition();
        if (!isConnected()) {
            // assert isAccountExistNoGUI(jid, password) : "the account ("
            // + jid.getBase() + ") doesn't exist yet!";
            clickToolbarButtonWithTooltip(TB_CONNECT);
            waitUntilIsConnected();
        }
    }

    public void selectBuddy(String baseJID) throws RemoteException {
        bot().view(VIEW_SAROS_BUDDIES).bot_().tree()
            .selectTreeItem(NODE_BUDDIES, baseJID);
    }

    public boolean hasBuddy(String buddyNickName) throws RemoteException {
        precondition();
        return bot().view(VIEW_SAROS_BUDDIES).bot_().tree()
            .selectTreeItem(NODE_BUDDIES)
            .existsSubItemWithRegex(buddyNickName + ".*");
    }

    public void deleteBuddy(JID buddyJID) throws RemoteException {
        String buddyNickName = getBuddyNickNameNoGUI(buddyJID);
        if (!hasBuddyNoGUI(buddyJID))
            return;
        try {

            bot()
                .view(VIEW_SAROS_BUDDIES)
                .bot_()
                .tree()
                .selectTreeItemWithRegex(NODE_BUDDIES + ".*",
                    buddyNickName + ".*").contextMenu(CM_DELETE).click();

            bot().shell(CONFIRM_DELETE).confirmShellAndWait(YES);
        } catch (WidgetNotFoundException e) {
            log.info("Contact not found: " + buddyJID.getBase(), e);
        }
    }

    public void confirmShellRemovelOfSubscription() throws RemoteException {
        if (!bot().isShellOpen(SHELL_REMOVAL_OF_SUBSCRIPTION))
            bot().waitUntilShellOpen(SHELL_REMOVAL_OF_SUBSCRIPTION);
        bot().shell(SHELL_REMOVAL_OF_SUBSCRIPTION).activate();
        bot().shell(SHELL_REMOVAL_OF_SUBSCRIPTION).confirm(OK);
    }

    public void renameBuddy(JID buddyJID, String newBuddyName)
        throws RemoteException {
        precondition();
        String buddyNickName = getBuddyNickNameNoGUI(buddyJID);
        if (buddyNickName == null)
            throw new RuntimeException(
                "the buddy dones't exist, which you want to rename.");

        bot().view(VIEW_SAROS_BUDDIES).bot_().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES + ".*", buddyNickName + ".*")
            .contextMenu(CM_RENAME).click();

        if (!bot().shell(SHELL_SET_NEW_NICKNAME).activate()) {
            bot().shell(SHELL_SET_NEW_NICKNAME).waitUntilActive();
        }
        bot.text(buddyNickName).setText(newBuddyName);
        bot.button(OK).click();
    }

    public void inviteBuddy(JID buddyJID) throws RemoteException {
        precondition();
        String buddyNickName = getBuddyNickNameNoGUI(buddyJID);
        if (buddyNickName == null)
            throw new RuntimeException(
                "the buddy dones't exist, which you want to invite.");

        SWTBotTreeItem item = bot().view(VIEW_SAROS_BUDDIES).bot_().tree()
            .selectTreeItemWithRegex(NODE_BUDDIES + ".*", buddyNickName + ".*")
            .getSwtBotTreeItem();

        if (!item.isEnabled()) {
            throw new RuntimeException("You can't invite this user "
                + buddyNickName + ", he isn't conntected yet");
        }
        if (!item.contextMenu(CM_RENAME).isEnabled()) {
            throw new RuntimeException("You can't invite this user "
                + buddyNickName
                + ", it's possible that you've already invite him");
        }
        item.contextMenu(CM_INVITE_BUDDY).click();
    }

    public void addANewBuddy(JID jid) throws RemoteException {
        if (!hasBuddyNoGUI(jid)) {
            precondition();
            clickToolbarButtonWithTooltip(TB_ADD_A_NEW_CONTACT);
            Map<String, String> labelsAndTexts = new HashMap<String, String>();
            labelsAndTexts.put("XMPP/Jabber ID", jid.getBase());

            bot().shell(SHELL_NEW_BUDDY).confirmWithTextFieldAndWait(
                labelsAndTexts, FINISH);
        }
    }

    public void disconnect() throws RemoteException {
        precondition();
        if (isConnected()) {
            clickToolbarButtonWithTooltip(TB_DISCONNECT);
            waitUntilDisConnected();
        }
    }

    /**********************************************
     * 
     * state
     * 
     **********************************************/

    public boolean isConnectedNoGUI() throws RemoteException {
        return saros.isConnected();
    }

    public boolean isConnected() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_DISCONNECT);
    }

    public boolean isDisConnected() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_CONNECT);
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/

    public void waitUntilIsConnected() throws RemoteException {
        precondition();
        waitUntil(SarosConditions.isConnect(getToolbarButtons(), TB_DISCONNECT));
    }

    public void waitUntilDisConnected() throws RemoteException {
        waitUntil(SarosConditions.isDisConnected(getToolbarButtons(),
            TB_CONNECT));
    }

    /**********************************************
     * 
     * No GUI
     * 
     **********************************************/

    public void connectNoGUI(JID jid, String password) throws RemoteException {
        precondition();
        log.trace("connectedByXMPP");
        if (!isConnectedNoGUI()) {
            log.trace("click the toolbar button \"Connect\" in the roster view");
            if (!sarosM.isAccountExistNoGUI(jid, password)) {
                sarosM.createAccountNoGUI(jid.getDomain(), jid.getName(),
                    password);
            }
            if (!sarosM.isAccountActiveNoGUI(jid))
                sarosM.activateAccountNoGUI(jid);
            saros.connect(true);
            waitUntil(new DefaultCondition() {
                public boolean test() throws Exception {
                    return isConnectedNoGUI();
                }

                public String getFailureMessage() {
                    return "Can't connect.";
                }
            });
        }
    }

    public void renameBuddyNoGUI(JID buddyJID, String newBuddyName)
        throws RemoteException {
        renameBuddyNoGUI(buddyJID.getBase(), newBuddyName);
    }

    public void renameBuddyNoGUI(String baseJID, String newBuddyName)
        throws RemoteException {
        Roster roster = saros.getRoster();
        roster.getEntry(baseJID).setName(newBuddyName);
    }

    public void resetAllBuddyNameNoGUI() throws RemoteException {
        List<String> allBuddies = getAllBuddiesNoGUI();
        if (allBuddies != null && !allBuddies.isEmpty())
            for (String buddyName : allBuddies) {
                renameBuddyNoGUI(buddyName, buddyName);
            }
    }

    public void deleteBuddyNoGUI(JID buddyJID) throws RemoteException,
        XMPPException {
        RosterUtils.removeFromRoster(saros.getConnection(), saros.getRoster()
            .getEntry(buddyJID.getBase()));
    }

    public String getBuddyNickNameNoGUI(JID buddyJID) throws RemoteException {
        Roster roster = saros.getRoster();
        if (roster.getEntry(buddyJID.getBase()) == null)
            return null;
        return roster.getEntry(buddyJID.getBase()).getName();
    }

    public boolean hasBuddyNickNameNoGUI(JID buddyJID) throws RemoteException {
        if (getBuddyNickNameNoGUI(buddyJID) == null)
            return false;
        if (!getBuddyNickNameNoGUI(buddyJID).equals(buddyJID.getBase()))
            return true;
        return false;
    }

    public boolean hasBuddyNoGUI(JID buddyJID) throws RemoteException {
        Roster roster = saros.getRoster();
        String baseJID = buddyJID.getBase();
        Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
            log.debug("roster entry.getName(): " + entry.getName());
            log.debug("roster entry.getuser(): " + entry.getUser());
            log.debug("roster entry.getStatus(): " + entry.getStatus());
            log.debug("roster entry.getType(): " + entry.getType());
        }
        return roster.contains(baseJID);
    }

    public List<String> getAllBuddiesNoGUI() throws RemoteException {
        Roster roster = saros.getRoster();
        if (roster == null)
            return null;
        if (roster.getEntries() == null)
            return null;
        List<String> allBuddyBaseJIDs = new ArrayList<String>();
        Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
            allBuddyBaseJIDs.add(entry.getUser());
        }
        return allBuddyBaseJIDs;
    }

    public void disconnectNoGUI() throws RemoteException {
        if (isConnectedNoGUI()) {
            saros.disconnect();
            waitUntilDisConnectedNoGUI();
        }
    }

    public void waitUntilIsConnectedNoGUI() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isConnectedNoGUI();
            }

            public String getFailureMessage() {
                return "can't connect.";
            }
        });
    }

    public boolean isDisConnectedNoGUI() throws RemoteException {
        return getXmppConnectionState() == ConnectionState.NOT_CONNECTED;
    }

    public void waitUntilDisConnectedNoGUI() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isConnectedNoGUI();
            }

            public String getFailureMessage() {
                return "can't connect.";
            }
        });
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    public void clickToolbarButtonWithTooltip(String tooltipText)
        throws RemoteException {
        stfToolbarButton.clickToolbarButtonWithRegexTooltipOnView(
            VIEW_SAROS_BUDDIES, tooltipText);
    }

    /**
     * @return the {@link ConnectionState}. It can be: NOT_CONNECTED,
     *         CONNECTING, CONNECTED, DISCONNECTING or ERROR
     * 
     */
    ConnectionState getXmppConnectionState() {
        return saros.getConnectionState();
    }

    /**
     * 
     * Define the basic precondition that guarantees you can perform actions
     * within the roster view successfully.
     * 
     * @throws RemoteException
     */
    protected void precondition() throws RemoteException {
        bot().openById(VIEW_SAROS_BUDDIES_ID);
        bot().view(VIEW_SAROS_BUDDIES).setFocus();
    }

    protected boolean isToolbarButtonEnabled(String tooltip)
        throws RemoteException {
        return stfToolbarButton.isToolbarButtonOnViewEnabled(
            VIEW_SAROS_BUDDIES, tooltip);
    }

    protected List<SWTBotToolbarButton> getToolbarButtons() {
        return stfToolbarButton.getAllToolbarButtonsOnView(VIEW_SAROS_BUDDIES);
    }

    @SuppressWarnings("static-access")
    private void selectConnectAccount(String baseJID) {
        SWTBotToolbarDropDownButton b = bot.viewById(VIEW_SAROS_BUDDIES_ID)
            .toolbarDropDownButton(TB_CONNECT);
        Matcher<MenuItem> withRegex = WidgetMatcherFactory.withRegex(baseJID
            + ".*");
        b.menuItem(withRegex).click();
        try {
            b.pressShortcut(KeyStroke.getInstance("ESC"));
        } catch (ParseException e) {
            log.debug("", e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean isConnectAccountExist(String baseJID) {
        Matcher matcher = allOf(widgetOfType(MenuItem.class));
        SWTBotToolbarDropDownButton b = bot.viewById(VIEW_SAROS_BUDDIES_ID)
            .toolbarDropDownButton(TB_CONNECT);
        List<? extends SWTBotMenu> accounts = b.menuItems(matcher);
        b.pressShortcut(Keystrokes.ESC);
        for (SWTBotMenu account : accounts) {
            log.debug("existed account: " + account.getText() + "hier");
            if (account.getText().trim().equals(baseJID)) {
                return true;
            }
        }
        return false;
    }

}
