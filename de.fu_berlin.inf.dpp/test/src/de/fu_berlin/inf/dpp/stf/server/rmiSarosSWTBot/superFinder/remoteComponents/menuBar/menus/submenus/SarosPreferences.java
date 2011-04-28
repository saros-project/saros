package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.menuBar.menus.submenus;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.ui.preferencePages.GeneralPreferencePage;

public class SarosPreferences extends Component implements ISarosPreferences {

    private static transient SarosPreferences self;

    /**
     * {@link SarosPreferences} is a singleton, but inheritance is possible.
     */
    public static SarosPreferences getInstance() {
        if (self != null)
            return self;
        self = new SarosPreferences();

        return self;
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void createAccount(JID jid, String password) throws RemoteException {
        IRemoteBotShell shell_preferences = preCondition();
        shell_preferences
            .bot()
            .buttonInGroup(BUTTON_ADD_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .click();
        remoteBot().waitUntilShellIsOpen(SHELL_SAROS_CONFIGURATION);
        remoteBot().shell(SHELL_SAROS_CONFIGURATION).activate();
        shell_preferences.bot()
            .buttonInGroup(GROUP_TITLE_CREATE_NEW_XMPP_JABBER_ACCOUNT).click();

        remoteBot().waitUntilShellIsOpen(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        IRemoteBotShell shell = remoteBot().shell(SHELL_CREATE_XMPP_JABBER_ACCOUNT);
        shell.activate();
        superBot().confirmShellCreateNewXMPPJabberAccount(jid, password);
        shell.bot().button(NEXT).click();
        shell.bot().button(FINISH).click();
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        remoteBot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void addAccount(JID jid, String password) throws RemoteException {
        IRemoteBotShell shell_pref = preCondition();
        shell_pref
            .bot()
            .buttonInGroup(GeneralPreferencePage.ADD_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        superBot().confirmShellAddXMPPJabberAccount(jid, password);
        remoteBot().shell(SHELL_PREFERNCES).bot().button(APPLY).click();
        remoteBot().shell(SHELL_PREFERNCES).bot().button(OK).click();
        remoteBot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void activateAccount(JID jid) throws RemoteException {
        assert existsAccount(jid) : "the account (" + jid.getBase()
            + ") doesn't exist yet!";
        if (isAccountActiveNoGUI(jid))
            return;
        IRemoteBotShell shell = preCondition();
        shell.bot().listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE)
            .select(jid.getBase());

        shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.ACTIVATE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        assert shell.bot().existsLabel("Active: " + jid.getBase());
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        remoteBot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void changeAccount(JID jid, String newXmppJabberID,
        String newPassword) throws RemoteException {
        IRemoteBotShell shell = preCondition();
        shell.bot().listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE)
            .select(jid.getBase());

        shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.CHANGE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        superBot().confirmShellEditXMPPJabberAccount(newXmppJabberID,
            newPassword);
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        remoteBot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void deleteAccount(JID jid, String password) throws RemoteException {
        if (!isAccountExistNoGUI(jid, password))
            return;
        IRemoteBotShell shell = preCondition();
        shell.bot().listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE)
            .select(jid.getBase());

        shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.DELETE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        if (isAccountActiveNoGUI(jid)) {
            remoteBot().waitUntilShellIsOpen(SHELL_DELETING_ACTIVE_ACCOUNT);
            remoteBot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).activate();
            assert remoteBot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).isActive();
            throw new RuntimeException(
                "It's not allowd to delete a active account");
        }
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        remoteBot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void deleteAllNoActiveAccounts() throws RemoteException {
        IRemoteBotShell shell = preCondition();
        String[] items = shell.bot()
            .listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();
        for (String item : items) {
            shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
                .select(item);
            shell
                .bot()
                .buttonInGroup(GeneralPreferencePage.DELETE_BTN_TEXT,
                    GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
            if (remoteBot().isShellOpen(SHELL_DELETING_ACTIVE_ACCOUNT)
                && remoteBot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).isActive()) {
                remoteBot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).bot().button(OK)
                    .click();
                continue;
            }
        }
        assert shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .itemCount() == 1;
        remoteBot().shell(SHELL_PREFERNCES).bot().button(APPLY).click();
        remoteBot().shell(SHELL_PREFERNCES).bot().button(OK).click();
        remoteBot().waitUntilShellIsClosed(SHELL_PREFERNCES);
        remoteBot().sleep(300);
    }

    public void setupSettingForScreensharing(int encoder, int videoResolution,
        int bandWidth, int capturedArea) throws RemoteException {
        clickMenuSarosPreferences();
        remoteBot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        IRemoteBotShell shell = remoteBot().shell(SHELL_PREFERNCES);
        shell.activate();

        shell.bot().tree().selectTreeItem(NODE_SAROS, NODE_SAROS_SCREENSHARING);
        shell.bot().ccomboBox(0).setSelection(encoder);
        shell.bot().ccomboBox(1).setSelection(videoResolution);

        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        remoteBot().waitUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void disableAutomaticReminder() throws RemoteException {
        if (feedbackManager.isFeedbackDisabled()) {
            clickMenuSarosPreferences();
            remoteBot().waitUntilShellIsOpen(SHELL_PREFERNCES);
            IRemoteBotShell shell = remoteBot().shell(SHELL_PREFERNCES);
            shell.activate();
            shell.bot().tree().selectTreeItem(NODE_SAROS, NODE_SAROS_FEEDBACK);
            shell
                .bot()
                .radioInGroup(
                    Messages.getString("feedback.page.radio.disable"),
                    Messages.getString("feedback.page.group.interval")).click();
            shell.bot().button(APPLY).click();
            shell.bot().button(OK).click();
            remoteBot().waitUntilShellIsClosed(SHELL_PREFERNCES);
        }
    }

    public void disableAutomaticReminderNoGUI() throws RemoteException {
        if (!feedbackManager.isFeedbackDisabled()) {
            feedbackManager.setFeedbackDisabled(true);
        }
    }

    public boolean existsAccount() throws RemoteException {
        IRemoteBotShell shell = preCondition();
        String[] items = shell.bot()
            .listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();
        if (items == null || items.length == 0)
            return false;
        else
            return true;
    }

    public boolean existsAccount(JID jid) throws RemoteException {
        IRemoteBotShell shell = preCondition();

        String[] items = shell.bot()
            .listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();
        for (String item : items) {
            if ((jid.getBase()).equals(item)) {
                shell.bot().button(CANCEL).click();
                return true;
            }
        }
        shell.bot().button(CANCEL).click();
        remoteBot().waitUntilShellIsClosed(SHELL_PREFERNCES);
        return false;
    }

    public boolean isAccountActive(JID jid) throws RemoteException {

        IRemoteBotShell shell = preCondition();
        String activeAccount = shell.bot()
            .labelInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getText();
        boolean isActive = false;
        if (activeAccount.equals("Active: " + jid.getBase()))
            isActive = true;
        shell.bot().button(CANCEL).click();
        remoteBot().waitUntilShellIsClosed(SHELL_PREFERNCES);
        return isActive;
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     *************************************************************/
    /**
     * This is a convenient function to show the right setting-page of saros
     * item in the preferences dialog.
     */
    private IRemoteBotShell preCondition() throws RemoteException {
        clickMenuSarosPreferences();
        remoteBot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        IRemoteBotShell shell = remoteBot().shell(SHELL_PREFERNCES);
        shell.activate();
        shell.bot().tree().selectTreeItem(NODE_SAROS);
        return shell;
    }

    /**
     * click the main menu Saros-> Preferences
     * 
     * @throws RemoteException
     */
    private void clickMenuSarosPreferences() throws RemoteException {
        remoteBot().activateWorkbench();
        remoteBot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();
    }

    private boolean isAccountActiveNoGUI(JID jid) {
        XMPPAccount account = getXMPPAccount(jid);
        if (account == null)
            return false;
        return account.isActive();
    }

    /**
     * 
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @return {@link XMPPAccount} of the given jid.
     */
    private XMPPAccount getXMPPAccount(JID jid) {
        for (XMPPAccount account : xmppAccountStore.getAllAccounts()) {
            if (jid.getName().equals(account.getUsername())
                && jid.getDomain().equals(account.getServer())) {
                return account;
            }
        }
        return null;
    }

    private boolean isAccountExistNoGUI(JID jid, String password) {
        for (XMPPAccount account : xmppAccountStore.getAllAccounts()) {
            log.debug("account id: " + account.getId());
            log.debug("account username: " + account.getUsername());
            log.debug("account password: " + account.getPassword());
            log.debug("account server: " + account.getServer());
            if (jid.getName().equals(account.getUsername())
                && jid.getDomain().equals(account.getServer())
                && password.equals(account.getPassword())) {
                return true;
            }
        }
        return false;
    }

}
