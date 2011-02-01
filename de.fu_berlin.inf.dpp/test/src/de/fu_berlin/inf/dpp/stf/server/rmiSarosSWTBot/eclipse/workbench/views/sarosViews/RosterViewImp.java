package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.Matcher;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

/**
 * This implementation of {@link RosterView}
 * 
 * @author Lin
 */
public class RosterViewImp extends EclipseComponentImp implements RosterView {

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
     * open/close/activate the roster view
     * 
     **********************************************/
    public void openSarosBuddiesView() throws RemoteException {
        if (!isSarosBuddiesViewOpen())
            viewW.openViewById(VIEW_SAROS_BUDDIES_ID);
    }

    public boolean isSarosBuddiesViewOpen() throws RemoteException {
        return viewW.isViewOpen(VIEW_SAROS_BUDDIES);
    }

    public void closeSarosBuddiesView() throws RemoteException {
        viewW.closeViewById(VIEW_SAROS_BUDDIES_ID);
    }

    public void setFocusOnRosterView() throws RemoteException {
        viewW.setFocusOnViewByTitle(VIEW_SAROS_BUDDIES);
    }

    public boolean isSarosBuddiesViewActive() throws RemoteException {
        return viewW.isViewActive(VIEW_SAROS_BUDDIES);
    }

    /**********************************************
     * 
     * toolbar buttons on the view: connect/disconnect
     * 
     **********************************************/

    public void connect(JID jid, String password) throws RemoteException {
        precondition();
        log.trace("connectedByXMPP");
        if (!isConnected()) {
            log.trace("click the toolbar button \"Connect\" in the roster view");
            if (!sarosM.isAccountExist(jid, password)) {
                sarosM.createAccountNoGUI(jid.getDomain(), jid.getName(),
                    password);
            }
            if (!sarosM.isAccountActive(jid))
                sarosM.activateAccountNoGUI(jid);
            saros.connect(true);
            waitUntil(new DefaultCondition() {
                public boolean test() throws Exception {
                    return isConnected();
                }

                public String getFailureMessage() {
                    return "Can't connect.";
                }
            });
        }
    }

    /**
     * connect using GUI-variant.
     * <p>
     * <b>Note</b>: This method isn't completely implemented yet, because
     * GUI-people will make big change on the roster view.
     * 
     * @param jid
     * @param password
     * @throws RemoteException
     */
    public void connectGUI(JID jid, String password) throws RemoteException {
        precondition();
        log.trace("connectedByXMPP");
        if (!isConnectedGUI()) {
            log.trace("click the toolbar button \"Connect\" in the r�oster view");
            /*
             * TODO if the test-account doesn't exists, we need to first create
             * it.
             */

            /*
             * TODO if the test-account isn't active, we need to activate it
             * first.
             */
            clickToolbarButtonWithTooltip(TB_CONNECT);
            waitUntilConnectedGUI();
        }
    }

    private void confirmWindowSarosConfiguration(String xmppServer, String jid,
        String password) throws RemoteException {
        if (!shellC.activateShellWithText(SHELL_SAROS_CONFIGURATION))
            shellC.waitUntilShellActive(SHELL_SAROS_CONFIGURATION);
        textW.setTextInTextWithLabel(xmppServer, LABEL_XMPP_JABBER_SERVER);
        textW.setTextInTextWithLabel(jid, LABEL_USER_NAME);
        textW.setTextInTextWithLabel(password, LABEL_PASSWORD);
        buttonW.clickButton(NEXT);
        buttonW.clickButton(FINISH);
    }

    public boolean isConnected() throws RemoteException {
        return saros.isConnected();
    }

    public boolean isConnectedGUI() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_DISCONNECT);
    }

    public void waitUntilConnected() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isConnected();
            }

            public String getFailureMessage() {
                return "can't connect.";
            }
        });
    }

    public void waitUntilConnectedGUI() throws RemoteException {
        precondition();
        waitUntil(SarosConditions.isConnect(getToolbarButtons(), TB_DISCONNECT));
    }

    public void disconnect() throws RemoteException {
        if (isConnected()) {
            saros.disconnect();
            waitUntilDisConnected();
        }
    }

    public void disconnectGUI() throws RemoteException {
        precondition();
        if (isConnectedGUI()) {
            clickToolbarButtonWithTooltip(TB_DISCONNECT);
            waitUntilDisConnectedGUI();
        }
    }

    public boolean isDisConnected() throws RemoteException {
        return getXmppConnectionState() == ConnectionState.NOT_CONNECTED;
    }

    public boolean isDisConnectedGUI() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_CONNECT);
    }

    public void waitUntilDisConnected() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return !isConnected();
            }

            public String getFailureMessage() {
                return "can't connect.";
            }
        });
    }

    public void waitUntilDisConnectedGUI() throws RemoteException {
        waitUntil(SarosConditions.isDisConnected(getToolbarButtons(),
            TB_CONNECT));
    }

    /**********************************************
     * 
     * toolbar buttons on the view: add a new contact
     * 
     * @throws RemoteException
     * 
     **********************************************/

    public void addANewBuddyGUI(JID jid) throws RemoteException {
        if (!hasBuddy(jid)) {
            clickToolbarButtonAddANewBuddy();
            confirmWindowNewBuddy(jid.getBase());
        }
    }

    public void confirmShellRequestOfSubscriptionReceived()
        throws RemoteException {
        if (!shellC
            .activateShellWithText(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED))
            shellC.waitUntilShellActive(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED);
        shellC.confirmShell(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED, OK);
    }

    public void confirmWindowNewBuddy(String baseJID) throws RemoteException {
        if (!shellC.activateShellWithText(SHELL_NEW_BUDDY))
            shellC.waitUntilShellActive(SHELL_NEW_BUDDY);
        textW.setTextInTextWithLabel(baseJID, "XMPP/Jabber ID");
        buttonW.waitUntilButtonEnabled(FINISH);
        buttonW.clickButton(FINISH);
    }

    public void clickToolbarButtonAddANewBuddy() throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_ADD_A_NEW_CONTACT);
    }

    public void confirmShellBuddyLookupFailed(String buttonType)
        throws RemoteException {
        shellC.confirmShell(SHELL_BUDDY_LOOKUP_FAILED, buttonType);
    }

    public void waitUntilIsShellBuddyLookupFailedActive()
        throws RemoteException {
        shellC.waitUntilShellActive(SHELL_BUDDY_LOOKUP_FAILED);
    }

    public void waitUntilIsShellBuddyAlreadyAddedActive()
        throws RemoteException {
        shellC.waitUntilShellActive(SHELL_BUDDY_ALREADY_ADDED);
    }

    public boolean isShellBuddyLookupFailedActive() throws RemoteException {
        return shellC.isShellActive(SHELL_BUDDY_LOOKUP_FAILED);
    }

    public void closeShellBuddyAlreadyAdded() throws RemoteException {
        shellC.closeShell(SHELL_BUDDY_ALREADY_ADDED);
    }

    public boolean isShellBuddyAlreadyAddedActive() throws RemoteException {
        return shellC.isShellActive(SHELL_BUDDY_ALREADY_ADDED);
    }

    /**********************************************
     * 
     * get buddy /buddyNickName on the roster view
     * 
     **********************************************/
    public void selectBuddyGUI(String baseJID) throws RemoteException {
        treeW.getTreeItemInView(VIEW_SAROS_BUDDIES, TREE_ITEM_BUDDIES, baseJID);
    }

    public boolean hasBuddy(JID buddyJID) throws RemoteException {
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

    public List<String> getAllBuddies() throws RemoteException {
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

    public boolean hasBuddyGUI(String buddyNickName) throws RemoteException {
        precondition();
        SWTBotTree tree = treeW.getTreeInView(VIEW_SAROS_BUDDIES);
        return treeW.existsTreeItemWithRegexs(tree, TREE_ITEM_BUDDIES, buddyNickName
            + ".*");
    }

    public String getBuddyNickName(JID buddyJID) throws RemoteException {
        Roster roster = saros.getRoster();
        if (roster.getEntry(buddyJID.getBase()) == null)
            return null;
        return roster.getEntry(buddyJID.getBase()).getName();
    }

    public String getBuddyNickNameGUI(JID buddyJID) throws RemoteException {
        // TODO add the implementation
        return null;
    }

    public boolean hasBuddyNickName(JID buddyJID) throws RemoteException {
        if (getBuddyNickName(buddyJID) == null)
            return false;
        if (!getBuddyNickName(buddyJID).equals(buddyJID.getBase()))
            return true;
        return false;
    }

    public boolean hasBuddyNickNameGUI(JID buddyJID) throws RemoteException {
        // TODO add the implementation
        return false;
    }

    /**********************************************
     * 
     * context menu of a contact on the view: delete Contact
     * 
     * @throws XMPPException
     * 
     **********************************************/
    public void deleteBuddy(JID buddyJID) throws RemoteException, XMPPException {
        saros.removeContact(saros.getRoster().getEntry(buddyJID.getBase()));
    }

    public void deleteBuddyGUI(JID buddyJID) throws RemoteException {
        String buddyNickName = getBuddyNickName(buddyJID);
        if (!hasBuddy(buddyJID))
            return;
        try {
            treeW.clickContextMenuOfTreeItemInView(VIEW_SAROS_BUDDIES, CM_DELETE, TREE_ITEM_BUDDIES
                + ".*", buddyNickName + ".*");
            shellC.confirmShellDelete(YES);
        } catch (WidgetNotFoundException e) {
            log.info("Contact not found: " + buddyJID.getBase(), e);
        }
    }

    public void confirmRemovelOfSubscriptionWindow() throws RemoteException {
        if (!shellC.activateShellWithText(SHELL_REMOVAL_OF_SUBSCRIPTION))
            shellC.waitUntilShellActive(SHELL_REMOVAL_OF_SUBSCRIPTION);
        shellC.confirmShell(SHELL_REMOVAL_OF_SUBSCRIPTION, OK);
    }

    /**********************************************
     * 
     * context menu of a contact on the view: rename Contact
     * 
     **********************************************/
    public void renameBuddy(JID buddyJID, String newBuddyName)
        throws RemoteException {
        renameBuddy(buddyJID.getBase(), newBuddyName);
    }

    public void renameBuddy(String baseJID, String newBuddyName)
        throws RemoteException {
        Roster roster = saros.getRoster();
        roster.getEntry(baseJID).setName(newBuddyName);
    }

    public void resetAllBuddyName() throws RemoteException {
        List<String> allBuddies = getAllBuddies();
        if (allBuddies != null && !allBuddies.isEmpty())
            for (String buddyName : allBuddies) {
                renameBuddy(buddyName, buddyName);
            }
    }

    public void renameBuddyGUI(JID buddyJID, String newBuddyName)
        throws RemoteException {
        precondition();
        String buddyNickName = getBuddyNickName(buddyJID);
        if (buddyNickName == null)
            throw new RuntimeException(
                "the buddy dones't exist, which you want to rename.");
        treeW.clickContextMenuOfTreeItemInView(VIEW_SAROS_BUDDIES, CM_RENAME, TREE_ITEM_BUDDIES
            + ".*", buddyNickName + ".*");
        if (!shellC.activateShellWithText("Set new nickname")) {
            shellC.waitUntilShellActive("Set new nickname");
        }
        bot.text(buddyNickName).setText(newBuddyName);
        bot.button(OK).click();
    }

    /**********************************************
     * 
     * context menu of a contact on the view: invite user
     * 
     **********************************************/

    public void inviteBuddy(JID buddyJID) throws RemoteException {
        // add the implementation.
    }

    public void inviteBuddyGUI(JID buddyJID) throws RemoteException {
        precondition();
        String buddyNickName = getBuddyNickName(buddyJID);
        if (buddyNickName == null)
            throw new RuntimeException(
                "the buddy dones't exist, which you want to invite.");
        SWTBotTree tree = treeW.getTreeInView(VIEW_SAROS_BUDDIES);
        SWTBotTreeItem item = treeW.getTreeItemWithRegexs(tree, TREE_ITEM_BUDDIES + ".*",
            buddyNickName + ".*");
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

    public void clickToolbarButtonWithTooltip(String tooltipText)
        throws RemoteException {
        toolbarButtonW.clickToolbarButtonWithRegexTooltipInView(VIEW_SAROS_BUDDIES,
            tooltipText);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

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
        openSarosBuddiesView();
        setFocusOnRosterView();
    }

    /**
     * 
     * @param tooltip
     *            the tooltip text of the toolbar button which you want to know,
     *            if it is enabled.
     * @return <tt>true</tt>, if the toolbar button specified with the given
     *         tooltip is enabled
     */
    protected boolean isToolbarButtonEnabled(String tooltip)
        throws RemoteException {
        return toolbarButtonW.isToolbarButtonInViewEnabled(VIEW_SAROS_BUDDIES, tooltip);
    }

    /**
     * 
     * @return all the {@link SWTBotToolbarButton} in this view.
     */

    protected List<SWTBotToolbarButton> getToolbarButtons()
        throws RemoteException {
        return toolbarButtonW.getAllToolbarButtonsInView(VIEW_SAROS_BUDDIES);
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
