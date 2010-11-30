package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateImp;
import de.fu_berlin.inf.dpp.ui.RosterView;

/**
 * This implementation of {@link RosterViewComponent}
 * 
 * @author Lin
 */
public class RosterViewComponentImp extends EclipseComponent implements
    RosterViewComponent {

    // public static RosterViewObjectImp classVariable;

    private static transient RosterViewComponentImp self;

    /*
     * View infos
     */
    private final static String VIEWNAME = "Roster";
    private final static String VIEWID = "de.fu_berlin.inf.dpp.ui.RosterView";

    /*
     * title of shells which are pop up by performing the actions on the session
     * view.
     */
    public final static String SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED = "Request of subscription received";
    private final static String SHELL_CONTACT_ALREADY_ADDED = "Contact already added";
    private final static String SHELL_CREATE_XMPP_ACCOUNT = ""; // CreateNewAccountWizard.CREATE_XMPP_ACCOUNT;
    private final static String SHELL_NEW_CONTACT = "New Contact";
    private final static String SHELL_CONTACT_LOOKUP_FAILED = "Contact look-up failed";
    private final static String SHELL_REMOVAL_OF_SUBSCRIPTION = "Removal of subscription";

    /*
     * Tool tip text of toolbar buttons on the session view
     */
    private final static String TB_DISCONNECT = "Disconnect.*";
    private final static String TB_ADD_A_NEW_CONTACT = "Add a new contact";
    private final static String TB_CONNECT = "Connect";

    // Context menu of the table on the view
    private final static String CM_DELETE = "Delete";
    private final static String CM_RENAME = "Rename...";
    private final static String CM_SKYPE_THIS_USER = "Skype this user";
    private final static String CM_INVITE_USER = "Invite user...";
    private final static String CM_TEST_DATA_TRANSFER = "Test data transfer connection...";

    private final static String BUDDIES = "Buddies";

    private final static String SERVER = "Server";
    private final static String USERNAME = "Username:";
    private final static String PASSWORD = "Password:";
    private final static String JABBERID = "Jabber ID";
    private final static String CONFIRM = "Confirm:";

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

    public void openRosterView() throws RemoteException {
        if (!isRosterViewOpen())
            viewPart.openViewById(VIEWID);
    }

    public boolean isRosterViewOpen() throws RemoteException {
        return viewPart.isViewOpen(VIEWNAME);
    }

    public void setFocusOnRosterView() throws RemoteException {
        viewPart.setFocusOnViewByTitle(VIEWNAME);
    }

    public void closeRosterView() throws RemoteException {
        viewPart.closeViewById(VIEWID);
    }

    public void disconnect() throws RemoteException {
        precondition();
        if (isConnected()) {
            clickToolbarButtonWithTooltip(TB_DISCONNECT);
            waitUntilIsDisConnected();
        }
    }

    public SWTBotTreeItem selectBuddy(String baseJID) throws RemoteException {
        return viewPart.selectTreeWithLabelsInView(VIEWNAME, BUDDIES, baseJID);
    }

    public boolean isBuddyExist(String baseJID) throws RemoteException {
        precondition();
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        return treePart.isTreeItemWithMatchTextExist(tree, BUDDIES, baseJID
            + ".*");
    }

    public boolean isConnectedGUI() throws RemoteException {
        precondition();
        return isToolbarButtonEnabled(TB_DISCONNECT);
    }

    /**
     * This method returns true if {@link SarosStateImp} and the GUI
     * {@link RosterView} having the connected state.
     */
    public boolean isConnected() throws RemoteException {
        return state.isConnected() && isConnectedGUI();
    }

    public void clickAddANewContactToolbarButton() throws RemoteException {
        precondition();
        clickToolbarButtonWithTooltip(TB_ADD_A_NEW_CONTACT);
    }

    public void waitUntilIsConnected() throws RemoteException {
        waitUntil(SarosConditions.isConnect(getToolbarButtons(), TB_DISCONNECT));
    }

    public void waitUntilIsDisConnected() throws RemoteException {
        waitUntil(SarosConditions.isDisConnected(getToolbarButtons(),
            TB_CONNECT));
    }

    public void addANewContact(JID jid) throws RemoteException {
        if (!hasContactWith(jid)) {
            clickAddANewContactToolbarButton();
            confirmNewContactWindow(jid.getBase());
        }
    }

    public void confirmNewContactWindow(String baseJID) {
        windowPart.waitUntilShellActive(SHELL_NEW_CONTACT);
        basicPart.setTextInTextWithLabel(baseJID, JABBERID);
        basicPart.waitUntilButtonIsEnabled(FINISH);
        basicPart.clickButton(FINISH);
    }

    public boolean hasContactWith(JID jid) throws RemoteException {
        return state.hasContactWith(jid) && isBuddyExist(jid.getBase());
    }

    /**
     * Remove given contact from Roster, if contact was added before.
     */
    public void deleteContact(JID jid) throws RemoteException {
        if (!hasContactWith(jid))
            return;
        try {
            clickContextMenuOfBuddy(CM_DELETE, jid.getBase());
            windowPart.confirmDeleteWindow(YES);
        } catch (WidgetNotFoundException e) {
            log.info("Contact not found: " + jid.getBase(), e);
        }
    }

    public void clickContextMenuOfBuddy(String context, String baseJID)
        throws RemoteException {
        viewPart.clickContextMenuOfTreeInView(VIEWNAME, CM_DELETE, BUDDIES,
            baseJID);
    }

    public void confirmContactLookupFailedWindow(String buttonType)
        throws RemoteException {
        windowPart.confirmWindow(SHELL_CONTACT_LOOKUP_FAILED, buttonType);
    }

    public boolean isWindowContactLookupFailedActive() throws RemoteException {
        return windowPart.isShellActive(SHELL_CONTACT_LOOKUP_FAILED);
    }

    public boolean isWindowContactAlreadyAddedActive() throws RemoteException {
        return windowPart.isShellActive(SHELL_CONTACT_ALREADY_ADDED);
    }

    public void renameContact(String contact, String newName)
        throws RemoteException {
        SWTBotTree tree = viewPart.getTreeInView(VIEWNAME);
        SWTBotTreeItem item = treePart.getTreeItemWithMatchText(tree, BUDDIES
            + ".*", contact + ".*");
        item.contextMenu(CM_RENAME).click();
        windowPart.waitUntilShellActive("Set new nickname");
        bot.text(contact).setText(newName);
        bot.button(OK).click();
    }

    public void connect(JID jid, String password) throws RemoteException {
        precondition();
        log.trace("connectedByXMPP");
        if (!isConnected()) {
            log.trace("clickTBConnectInRosterView");
            clickToolbarButtonWithTooltip(TB_CONNECT);
            bot.sleep(100);
            if (isCreateXMPPAccountWindowActive()) {
                log.trace("confirmSarosConfigurationWindow");
                confirmSarosConfigurationWizard(jid.getDomain(), jid.getName(),
                    password);
            }
            waitUntilIsConnected();
        }
    }

    public boolean isCreateXMPPAccountWindowActive() throws RemoteException {
        return windowPart.isShellActive(SHELL_CREATE_XMPP_ACCOUNT);
    }

    /**
     * Fill up the configuration wizard with title "Saros Configuration".
     */
    public void confirmSarosConfigurationWizard(String xmppServer, String jid,
        String password) {
        windowPart.activateShellWithText(SHELL_CREATE_XMPP_ACCOUNT);
        basicPart.setTextInTextWithLabel(xmppServer, SERVER);
        bot.sleep(sleepTime);
        basicPart.setTextInTextWithLabel(jid, USERNAME);
        bot.sleep(sleepTime);
        basicPart.setTextInTextWithLabel(password, PASSWORD);
        basicPart.setTextInTextWithLabel(password, CONFIRM);
        basicPart.clickButton(FINISH);
    }

    public void confirmRemovelOfSubscriptionWindow() throws RemoteException {
        windowPart.waitUntilShellActive(SHELL_REMOVAL_OF_SUBSCRIPTION);
        windowPart.confirmWindow(SHELL_REMOVAL_OF_SUBSCRIPTION, OK);
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

    /**
     * 
     * Define the precondition which should be guaranteed when you want to
     * perform actions within the roster view.
     * 
     * @throws RemoteException
     */
    @Override
    protected void precondition() throws RemoteException {
        openRosterView();
        setFocusOnRosterView();
    }

    public void waitUntilContactLookupFailedIsActive() throws RemoteException {
        windowPart.waitUntilShellActive(SHELL_CONTACT_LOOKUP_FAILED);
    }

    public void waitUntilWindowContactAlreadyAddedIsActive()
        throws RemoteException {
        windowPart.waitUntilShellActive(SHELL_CONTACT_ALREADY_ADDED);
    }

    public void closeWindowContactAlreadyAdded() throws RemoteException {
        windowPart.closeShell(SHELL_CONTACT_ALREADY_ADDED);
    }

    public void confirmRequestOfSubscriptionReceivedWindow()
        throws RemoteException {
        windowPart.waitUntilShellActive(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED);
        windowPart.confirmWindow(SHELL_REQUEST_OF_SUBSCRIPTION_RECEIVED, OK);
    }

    protected boolean isToolbarButtonEnabled(String tooltip) {
        return viewPart.isToolbarInViewEnabled(VIEWNAME, tooltip);
    }

    protected void clickToolbarButtonWithTooltip(String tooltipText) {
        viewPart.clickToolbarButtonWithTooltipInView(VIEWNAME, tooltipText);
    }

    protected List<SWTBotToolbarButton> getToolbarButtons() {
        return viewPart.getToolbarButtonsOnView(VIEWNAME);
    }

}
