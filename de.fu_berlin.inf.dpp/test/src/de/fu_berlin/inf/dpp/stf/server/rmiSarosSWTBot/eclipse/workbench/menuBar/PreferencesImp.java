package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.SarosComponentImp;
import de.fu_berlin.inf.dpp.ui.GeneralPreferencePage;

public class PreferencesImp extends SarosComponentImp implements Preferences {

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void createAccountInShellSarosPeferences(JID jid, String password)
        throws RemoteException {
        preCondition();
        buttonW.clickButtonInGroup(BUTTON_ADD_ACCOUNT,
            GROUP_TITLE_XMPP_JABBER_ACCOUNTS);
        shellW.activateShellAndWait(SHELL_SAROS_CONFIGURATION);
        buttonW.clickButtonInGroup(GROUP_TITLE_CREATE_NEW_XMPP_JABBER_ACCOUNT);
        shellW.activateShellAndWait(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        confirmShellCreateNewXMPPAccount(jid, password);
        buttonW.clickButton(NEXT);
        buttonW.clickButton(FINISH);
        buttonW.clickButton(APPLY);
        buttonW.clickButton(OK);
        shellW.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void addAccount(JID jid, String password) throws RemoteException {
        preCondition();
        buttonW.clickButtonInGroup(GeneralPreferencePage.ADD_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
        shellW.activateShellAndWait(SHELL_SAROS_CONFIGURATION);
        confirmWizardSarosConfiguration(jid, password);
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellW.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void activateAccount(JID jid) throws RemoteException {
        assert isAccountExist(jid) : "the account (" + jid.getBase()
            + ") doesn't exist yet!";
        if (isAccountActiveNoGUI(jid))
            return;
        preCondition();
        SWTBotList list = bot
            .listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
        list.select(jid.getBase());
        bot.buttonInGroup(GeneralPreferencePage.ACTIVATE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        assert labelW.existsLabel("Active: " + jid.getBase());
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellW.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void changeAccount(JID jid, String newUserName, String newPassword,
        String newServer) throws RemoteException {
        preCondition();
        listW.selectListItemInGroup(jid.getBase(),
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
        buttonW.clickButtonInGroup(GeneralPreferencePage.CHANGE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
        confirmShellChangeXMPPAccount(newServer, newUserName, newPassword);
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellW.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void confirmShellChangeXMPPAccount(String newServer,
        String newUserName, String newPassword) throws RemoteException {
        shellW.activateShellWithWaitingOpen(SHELL_CHANGE_ACCOUNT);
        textW.setTextInTextWithLabel(newServer, "Server");
        textW.setTextInTextWithLabel(newUserName, "Username:");
        textW.setTextInTextWithLabel(newPassword, "Password:");
        textW.setTextInTextWithLabel(newPassword, "Confirm:");
        buttonW.clickButton(FINISH);
    }

    public void deleteAccount(JID jid, String password) throws RemoteException {
        if (!isAccountExistNoGUI(jid, password))
            return;
        preCondition();
        listW.selectListItemInGroup(jid.getBase(),
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE);

        buttonW.clickButtonInGroup(GeneralPreferencePage.DELETE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
        if (isAccountActiveNoGUI(jid)) {
            shellW.activateShellAndWait("Deleting active account");
            assert shellW.isShellActive("Deleting active account");
            throw new RuntimeException(
                "It's not allowd to delete a active account");
        }
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellW.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void deleteAllNoActiveAccounts() throws RemoteException {
        preCondition();
        for (String item : listW
            .getListItemsInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)) {
            listW.selectListItemInGroup(item, GROUP_TITLE_XMPP_JABBER_ACCOUNTS);
            buttonW.clickButtonInGroup(GeneralPreferencePage.DELETE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
            if (shellW.isShellActive("Deleting active account")) {
                shellW.confirmShell("Deleting active account", OK);
                continue;
            }
        }
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellW.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void setupSettingForScreensharing(int encoder, int videoResolution,
        int bandWidth, int capturedArea) throws RemoteException {
        clickMenuSarosPreferences();
        shellW.activateShellWithWaitingOpen(SHELL_PREFERNCES);

        treeW.selectTreeItem(NODE_SAROS, NODE_SAROS_SCREENSHARING);
        buttonW.selectCComboBox(0, encoder);
        buttonW.selectCComboBox(1, videoResolution);

        buttonW.clickButton(APPLY);
        buttonW.clickButton(OK);
        shellW.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void disableAutomaticReminder() throws RemoteException {
        if (feedbackManager.isFeedbackDisabled()) {
            clickMenuSarosPreferences();
            shellW.activateShellAndWait(SHELL_PREFERNCES);
            treeW.selectTreeItem(NODE_SAROS, NODE_SAROS_FEEDBACK);
            bot.radioInGroup(Messages.getString("feedback.page.radio.disable"),
                Messages.getString("feedback.page.group.interval")).click();
            buttonW.clickButton(APPLY);
            buttonW.clickButton(OK);
            shellW.waitUntilShellClosed(SHELL_PREFERNCES);
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
                buttonW.clickButton(CANCEL);
                return true;
            }
        }
        buttonW.clickButton(CANCEL);
        shellW.waitUntilShellClosed(SHELL_PREFERNCES);
        return false;
    }

    public boolean isAccountActive(JID jid) throws RemoteException {
        preCondition();
        String activeAccount = labelW
            .getTextOfLabelInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS);
        boolean isActive = false;
        if (activeAccount.equals("Active: " + jid.getBase()))
            isActive = true;
        buttonW.clickButton(CANCEL);
        shellW.waitUntilShellClosed(SHELL_PREFERNCES);
        return isActive;
    }

    /**************************************************************
     * 
     * NO GUI
     * 
     *************************************************************/

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

    // States
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
    private void preCondition() throws RemoteException {
        clickMenuSarosPreferences();
        shellW.activateShellWithWaitingOpen(SHELL_PREFERNCES);
        treeW.selectTreeItem(NODE_SAROS);
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

    /**
     * click the main menu Saros-> Preferences
     * 
     * @throws RemoteException
     */
    private void clickMenuSarosPreferences() throws RemoteException {
        workbench.activateWorkbench();
        menuW.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
    }

}
