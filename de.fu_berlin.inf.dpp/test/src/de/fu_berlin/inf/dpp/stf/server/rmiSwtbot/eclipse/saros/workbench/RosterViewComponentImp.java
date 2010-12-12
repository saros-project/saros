package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

/**
 * This implementation of {@link RosterViewComponent}
 * 
 * @author Lin
 */
public class RosterViewComponentImp extends EclipseComponent implements
    RosterViewComponent {

    private static transient RosterViewComponentImp self;

    /* View infos */
    private final static String VIEWNAME = "Roster";
    private final static String VIEWID = "de.fu_berlin.inf.dpp.ui.RosterView";

    /*
     * title of shells which are pop up by performing the actions on the
     * rosterview.
     */
    public final static String SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED = "Request of subscription received";
    private final static String SHELL_CONTACT_ALREADY_ADDED = "Contact already added";
    private final static String SHELL_CREATE_XMPP_ACCOUNT = "Create New User Account"; // CreateNewAccountWizard.CREATE_XMPP_ACCOUNT;
    private final static String SHELL_NEW_CONTACT = "New Contact";
    private final static String SHELL_CONTACT_LOOKUP_FAILED = "Contact look-up failed";
    private final static String SHELL_REMOVAL_OF_SUBSCRIPTION = "Removal of subscription";

    /* Tool tip text of toolbar buttons on the session view */
    private final static String TB_DISCONNECT = "Disconnect";
    private final static String TB_ADD_A_NEW_CONTACT = "Add a new contact";
    private final static String TB_CONNECT = "Connect";

    /* Context menu of the table on the view */
    private final static String CM_DELETE = "Delete";
    private final static String CM_RENAME = "Rename...";
    private final static String CM_SKYPE_THIS_USER = "Skype this user";
    private final static String CM_INVITE_USER = "Invite user...";
    private final static String CM_TEST_DATA_TRANSFER = "Test data transfer connection...";

    private final static String BUDDIES = "Buddies";

    private final static String SERVER = "Jabber Server";
    private final static String USERNAME = "Username";
    private final static String PASSWORD = "Password";
    private final static String JABBERID = "Jabber ID";
    private final static String REPEAT_PASSWORD = "Repeat Password";

    /**
     * {@link RosterViewComponentImp} is a singleton, but inheritance is
     * possible.
     */
    public static RosterViewComponentImp getInstance() {
        if (self != null)
            return self;
        self = new RosterViewComponentImp();
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
    public void openRosterView() throws RemoteException {
        if (!isRosterViewOpen())
            viewPart.openViewById(VIEWID);
    }

    public boolean isRosterViewOpen() throws RemoteException {
        return viewPart.isViewOpen(VIEWNAME);
    }

    public void closeRosterView() throws RemoteException {
        viewPart.closeViewById(VIEWID);
    }

    public void setFocusOnRosterView() throws RemoteException {
        viewPart.setFocusOnViewByTitle(VIEWNAME);
    }

    public boolean isRosterViewActive() throws RemoteException {
        return viewPart.isViewActive(VIEWNAME);
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
            if (!mainMenuC.isAccountExist(jid, password)) {
                mainMenuC.createAccount(jid.getName(), password,
                    jid.getDomain());
            }
            if (!mainMenuC.isAccountActive(jid))
                mainMenuC.activateAccount(jid);
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
            log.trace("click the toolbar button \"Connect\" in the rüoster view");
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

    public void confirmWindowCreateXMPPAccount(String xmppServer, String jid,
        String password) throws RemoteException {
        if (!windowPart.activateShellWithText(SHELL_CREATE_XMPP_ACCOUNT))
            windowPart.waitUntilShellActive(SHELL_CREATE_XMPP_ACCOUNT);
        basicPart.setTextInTextWithLabel(xmppServer, SERVER);
        basicPart.setTextInTextWithLabel(jid, USERNAME);
        basicPart.setTextInTextWithLabel(password, PASSWORD);
        basicPart.setTextInTextWithLabel(password, REPEAT_PASSWORD);
        basicPart.clickButton(FINISH);
        windowPart.waitUntilShellClosed(SHELL_CREATE_XMPP_ACCOUNT);
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
     * @throws XMPPException
     * 
     **********************************************/
    public void addANewContact(JID jid) throws RemoteException, XMPPException {
        // TODO add the correct implementation
        // saros.addContact(jid, jid.getBase(), null, null);
        throw new NotImplementedException();
    }

    public void addANewContactGUI(JID jid) throws RemoteException {
        if (!hasBuddy(jid)) {
            clickAddANewContactToolbarButton();
            confirmNewContactWindow(jid.getBase());
        }
    }

    public void confirmRequestOfSubscriptionReceivedWindow()
        throws RemoteException {
        if (!windowPart
            .activateShellWithText(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED))
            windowPart
                .waitUntilShellActive(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED);
        windowPart.confirmWindow(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED, OK);
    }

    public void confirmNewContactWindow(String baseJID) {
        if (!windowPart.activateShellWithText(SHELL_NEW_CONTACT))
            windowPart.waitUntilShellActive(SHELL_NEW_CONTACT);
        basicPart.setTextInTextWithLabel(baseJID, JABBERID);
        basicPart.waitUntilButtonIsEnabled(FINISH);
        basicPart.clickButton(FINISH);
    }

    public void clickAddANewContactToolbarButton() throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_ADD_A_NEW_CONTACT);
    }

    public void confirmContactLookupFailedWindow(String buttonType)
        throws RemoteException {
        windowPart.confirmWindow(SHELL_CONTACT_LOOKUP_FAILED, buttonType);
    }

    public void waitUntilContactLookupFailedIsActive() throws RemoteException {
        windowPart.waitUntilShellActive(SHELL_CONTACT_LOOKUP_FAILED);
    }

    public void waitUntilWindowContactAlreadyAddedIsActive()
        throws RemoteException {
        windowPart.waitUntilShellActive(SHELL_CONTACT_ALREADY_ADDED);
    }

    public boolean isWindowContactLookupFailedActive() throws RemoteException {
        return basicC.isShellActive(SHELL_CONTACT_LOOKUP_FAILED);
    }

    public void closeWindowContactAlreadyAdded() throws RemoteException {
        basicC.closeShell(SHELL_CONTACT_ALREADY_ADDED);
    }

    public boolean isWindowContactAlreadyAddedActive() throws RemoteException {
        return basicC.isShellActive(SHELL_CONTACT_ALREADY_ADDED);
    }

    /**********************************************
     * 
     * get buddy /buddyNickName on the roster view
     * 
     **********************************************/
    public SWTBotTreeItem selectBuddyGUI(String baseJID) throws RemoteException {
        return viewPart.selectTreeItemWithLabelsInView(VIEWNAME, BUDDIES,
            baseJID);
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
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        return treePart.isTreeItemWithMatchTextExist(tree, BUDDIES,
            buddyNickName + ".*");
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
            SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
            SWTBotTreeItem item = treePart.getTreeItemWithRegexNodes(tree,
                BUDDIES + ".*", buddyNickName + ".*");
            item.contextMenu(CM_DELETE).click();
            windowPart.confirmDeleteWindow(YES);
        } catch (WidgetNotFoundException e) {
            log.info("Contact not found: " + buddyJID.getBase(), e);
        }
    }

    public void confirmRemovelOfSubscriptionWindow() throws RemoteException {
        if (!windowPart.activateShellWithText(SHELL_REMOVAL_OF_SUBSCRIPTION))
            windowPart.waitUntilShellActive(SHELL_REMOVAL_OF_SUBSCRIPTION);
        windowPart.confirmWindow(SHELL_REMOVAL_OF_SUBSCRIPTION, OK);
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
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        SWTBotTreeItem item = treePart.getTreeItemWithRegexNodes(tree, BUDDIES
            + ".*", buddyNickName + ".*");
        item.contextMenu(CM_RENAME).click();
        if (!windowPart.activateShellWithText("Set new nickname")) {
            windowPart.waitUntilShellActive("Set new nickname");
        }
        bot.text(buddyNickName).setText(newBuddyName);
        bot.button(OK).click();
    }

    /**********************************************
     * 
     * context menu of a contact on the view: invite user
     * 
     **********************************************/

    public void inviteUser(JID buddyJID) throws RemoteException {
        // add the implementation.
    }

    public void inviteUserGUI(JID buddyJID) throws RemoteException {
        precondition();
        String buddyNickName = getBuddyNickName(buddyJID);
        if (buddyNickName == null)
            throw new RuntimeException(
                "the buddy dones't exist, which you want to invite.");
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        SWTBotTreeItem item = treePart.getTreeItemWithRegexNodes(tree, BUDDIES
            + ".*", buddyNickName + ".*");
        if (!item.isEnabled()) {
            throw new RuntimeException("You can't invite this user "
                + buddyNickName + ", he isn't conntected yet");
        }
        if (!item.contextMenu(CM_RENAME).isEnabled()) {
            throw new RuntimeException("You can't invite this user "
                + buddyNickName
                + ", it's possible that you've already invite him");
        }
        item.contextMenu(CM_INVITE_USER).click();
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
        openRosterView();
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
    protected boolean isToolbarButtonEnabled(String tooltip) {
        return viewPart.isToolbarInViewEnabled(VIEWNAME, tooltip);
    }

    /**
     * click the toolbar button specified with the given tooltip.
     * 
     * @param tooltipText
     *            the tooltip text of the toolbar button which you want to
     *            click.
     */
    protected void clickToolbarButtonWithTooltip(String tooltipText) {
        viewPart.clickToolbarButtonWithTooltipInView(VIEWNAME, tooltipText);
    }

    /**
     * 
     * @return all the {@link SWTBotToolbarButton} in this view.
     */

    protected List<SWTBotToolbarButton> getToolbarButtons() {
        return viewPart.getAllToolbarButtonsOnView(VIEWNAME);
    }

    private boolean isWizardCreateXMPPAccountActive() throws RemoteException {
        return basicC.isShellActive(SHELL_CREATE_XMPP_ACCOUNT);
    }

    @SuppressWarnings("static-access")
    private void selectConnectAccount(String baseJID) {
        SWTBotToolbarDropDownButton b = bot.viewById(VIEWID)
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
        SWTBotToolbarDropDownButton b = bot.viewById(VIEWID)
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
