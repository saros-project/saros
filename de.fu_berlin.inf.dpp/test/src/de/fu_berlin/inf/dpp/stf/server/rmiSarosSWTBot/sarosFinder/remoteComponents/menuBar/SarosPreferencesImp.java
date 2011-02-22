package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.menuBar;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotShell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.SarosComponentImp;
import de.fu_berlin.inf.dpp.ui.preferencePages.GeneralPreferencePage;

public class SarosPreferencesImp extends SarosComponentImp implements
    SarosPreferences {

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void createAccountInShellSarosPeferences(JID jid, String password)
        throws RemoteException {
        STFBotShell shell_preferences = preCondition();
        bot.buttonInGroup(BUTTON_ADD_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .click();
        bot().waitUntilShellIsOpen(SHELL_SAROS_CONFIGURATION);
        bot().shell(SHELL_SAROS_CONFIGURATION).activate();
        bot.buttonInGroup(GROUP_TITLE_CREATE_NEW_XMPP_JABBER_ACCOUNT).click();

        bot().waitUntilShellIsOpen(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        bot().shell(SHELL_CREATE_NEW_XMPP_ACCOUNT).activate();
        confirmShellCreateNewXMPPAccount(jid, password);
        bot.button(NEXT).click();
        bot.button(FINISH).click();
        bot.button(APPLY).click();
        bot.button(OK).click();
        bot().waitsUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void addAccount(JID jid, String password) throws RemoteException {
        preCondition();
        bot.buttonInGroup(GeneralPreferencePage.ADD_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        bot().waitUntilShellIsOpen(SHELL_SAROS_CONFIGURATION);
        bot().shell(SHELL_SAROS_CONFIGURATION).activate();
        confirmWizardSarosConfiguration(jid, password);
        bot.button(APPLY).click();
        bot.button(OK).click();
        bot().waitsUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void activateAccount(JID jid) throws RemoteException {
        assert isAccountExist(jid) : "the account (" + jid.getBase()
            + ") doesn't exist yet!";
        if (isAccountActiveNoGUI(jid))
            return;
        STFBotShell shell = preCondition();
        SWTBotList list = bot
            .listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
        list.select(jid.getBase());
        bot.buttonInGroup(GeneralPreferencePage.ACTIVATE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        assert shell.bot_().existsLabel("Active: " + jid.getBase());
        bot.button(APPLY).click();
        bot.button(OK).click();
        bot().waitsUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void changeAccount(JID jid, String newUserName, String newPassword,
        String newServer) throws RemoteException {
        STFBotShell shell = preCondition();
        shell.bot_().listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE)
            .select(jid.getBase());

        bot.buttonInGroup(GeneralPreferencePage.CHANGE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        confirmShellChangeXMPPAccount(newServer, newUserName, newPassword);
        bot.button(APPLY).click();
        bot.button(OK).click();
        bot().waitsUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void confirmShellChangeXMPPAccount(String newServer,
        String newUserName, String newPassword) throws RemoteException {
        bot().waitUntilShellIsOpen(SHELL_CHANGE_ACCOUNT);
        STFBotShell shell = bot().shell(SHELL_CHANGE_ACCOUNT);
        shell.activate();
        shell.bot_().textWithLabel("Server").setText(newServer);
        shell.bot_().textWithLabel("Username:").setText(newUserName);
        shell.bot_().textWithLabel("Password:").setText(newPassword);
        shell.bot_().textWithLabel("Confirm:").setText(newPassword);

        bot.button(FINISH).click();
    }

    public void deleteAccount(JID jid, String password) throws RemoteException {
        if (!isAccountExistNoGUI(jid, password))
            return;
        STFBotShell shell = preCondition();
        shell.bot_().listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE)
            .select(jid.getBase());

        bot.buttonInGroup(GeneralPreferencePage.DELETE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        if (isAccountActiveNoGUI(jid)) {
            bot().waitUntilShellIsOpen(SHELL_DELETING_ACTIVE_ACCOUNT);
            bot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).activate();
            assert bot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).isActive();
            throw new RuntimeException(
                "It's not allowd to delete a active account");
        }
        bot.button(APPLY).click();
        bot.button(OK).click();
        bot().waitsUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void deleteAllNoActiveAccounts() throws RemoteException {
        STFBotShell shell = preCondition();
        String[] items = shell.bot_()
            .listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();
        for (String item : items) {
            shell.bot_().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
                .select(item);
            shell
                .bot_()
                .buttonInGroup(GeneralPreferencePage.DELETE_BTN_TEXT,
                    GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
            if (bot().isShellOpen(SHELL_DELETING_ACTIVE_ACCOUNT)
                && bot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).isActive()) {
                bot().shell(SHELL_DELETING_ACTIVE_ACCOUNT).bot_().button(OK)
                    .click();
                continue;
            }
        }
        bot.button(APPLY).click();
        bot.button(OK).click();
        bot().waitsUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void setupSettingForScreensharing(int encoder, int videoResolution,
        int bandWidth, int capturedArea) throws RemoteException {
        clickMenuSarosPreferences();
        bot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        bot().shell(SHELL_PREFERNCES).activate();

        bot().shell(SHELL_PREFERNCES).bot_().tree()
            .selectTreeItem(NODE_SAROS, NODE_SAROS_SCREENSHARING);
        bot().shell(SHELL_PREFERNCES).bot_().ccomboBox(0).setSelection(encoder);
        bot().shell(SHELL_PREFERNCES).bot_().ccomboBox(1)
            .setSelection(videoResolution);

        bot.button(APPLY).click();
        bot.button(OK).click();
        bot().waitsUntilShellIsClosed(SHELL_PREFERNCES);
    }

    public void disableAutomaticReminder() throws RemoteException {
        if (feedbackManager.isFeedbackDisabled()) {
            clickMenuSarosPreferences();
            bot().waitUntilShellIsOpen(SHELL_PREFERNCES);
            bot().shell(SHELL_PREFERNCES).activate();
            bot().shell(SHELL_PREFERNCES).bot_().tree()
                .selectTreeItem(NODE_SAROS, NODE_SAROS_FEEDBACK);
            bot.radioInGroup(Messages.getString("feedback.page.radio.disable"),
                Messages.getString("feedback.page.group.interval")).click();
            bot.button(APPLY).click();
            bot.button(OK).click();
            bot().waitsUntilShellIsClosed(SHELL_PREFERNCES);
        }
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public boolean isAccountExist(JID jid) throws RemoteException {
        preCondition();
        SWTBotList list = bot.listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS);
        String[] items = list.getItems();
        for (String item : items) {
            if ((jid.getBase()).equals(item)) {
                bot.button(CANCEL).click();
                return true;
            }
        }
        bot.button(CANCEL).click();
        bot().waitsUntilShellIsClosed(SHELL_PREFERNCES);
        return false;
    }

    public boolean isAccountActive(JID jid) throws RemoteException {

        STFBotShell shell = preCondition();
        String activeAccount = shell.bot_()
            .labelInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getText();
        boolean isActive = false;
        if (activeAccount.equals("Active: " + jid.getBase()))
            isActive = true;
        bot.button(CANCEL).click();
        bot().waitsUntilShellIsClosed(SHELL_PREFERNCES);
        return isActive;
    }

    /**********************************************
     * 
     * No GUI
     * 
     **********************************************/

    // Actions
    public void createAccountNoGUI(String server, String username,
        String password) throws RemoteException {
        xmppAccountStore.createNewAccount(username, password, server);
    }

    public void disableAutomaticReminderNoGUI() throws RemoteException {
        if (!feedbackManager.isFeedbackDisabled()) {
            feedbackManager.setFeedbackDisabled(true);
        }
    }

    public void deleteAccountNoGUI(JID jid) throws RemoteException {
        xmppAccountStore.deleteAccount(getXMPPAccount(jid));
    }

    public void changeAccountNoGUI(JID jid, String newUserName,
        String newPassword, String newServer) throws RemoteException {
        xmppAccountStore.changeAccountData(getXMPPAccount(jid).getId(),
            newUserName, newPassword, newServer);
    }

    public void activateAccountNoGUI(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        xmppAccountStore.setAccountActive(account);
    }

    // states
    public boolean isAccountActiveNoGUI(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        if (account == null)
            return false;
        return account.isActive();
    }

    public boolean isAccountExistNoGUI(JID jid, String password)
        throws RemoteException {
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

    /**************************************************************
     * 
     * Inner functions
     * 
     *************************************************************/
    /**
     * This is a convenient function to show the right setting-page of saros
     * item in the preferences dialog.
     */
    private STFBotShell preCondition() throws RemoteException {
        clickMenuSarosPreferences();
        bot().waitUntilShellIsOpen(SHELL_PREFERNCES);
        STFBotShell shell = bot().shell(SHELL_PREFERNCES);
        shell.activate();
        shell.bot_().tree().selectTreeItem(NODE_SAROS);
        return shell;
    }

    /**
     * click the main menu Saros-> Preferences
     * 
     * @throws RemoteException
     */
    private void clickMenuSarosPreferences() throws RemoteException {
        bot().activateWorkbench();
        bot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();
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

}
