package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;
import de.fu_berlin.inf.dpp.ui.GeneralPreferencePage;

public class SarosMImp extends EclipsePart implements SarosM {

    private static transient SarosMImp self;

    /**
     * {@link SarosMImp} is a singleton, but inheritance is possible.
     */
    public static SarosMImp getInstance() {
        if (self != null)
            return self;
        self = new SarosMImp();
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
    public void createAccountNoGUI(String server, String username,
        String password) throws RemoteException {
        xmppAccountStore.createNewAccount(username, password, server);
    }

    public void creatAccount(JID jid, String password,
        boolean usesThisAccountNow) throws RemoteException {
        menuW.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        confirmWindowCreateNewXMPPAccount(jid.getDomain(), jid.getName(),
            password, usesThisAccountNow);
    }

    public void confirmWindowCreateNewXMPPAccount(String xmppServer,
        String jid, String password, boolean usesThisAccountNow)
        throws RemoteException {
        if (!shellC.activateShellWithText(SHELL_CREATE_NEW_XMPP_ACCOUNT))
            shellC.waitUntilShellActive(SHELL_CREATE_NEW_XMPP_ACCOUNT);
        textW.setTextInTextWithLabel(xmppServer, LABEL_XMPP_JABBER_SERVER);
        textW.setTextInTextWithLabel(jid, LABEL_USER_NAME);
        textW.setTextInTextWithLabel(password, LABEL_PASSWORD);
        textW.setTextInTextWithLabel(password, LABEL_REPEAT_PASSWORD);
        buttonW.clickButton(FINISH);
        shellC.waitUntilShellClosed(SHELL_CREATE_NEW_XMPP_ACCOUNT);
    }

    public void createAccountInPeferences(String server, String username,
        String password) throws RemoteException {
        selectSarosPageInPreferences();
        bot.buttonInGroup(GeneralPreferencePage.ADD_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        shellC.activateShellWithWaitingOpen(SHELL_SAROS_CONFIGURATION);
        bot.buttonInGroup("Create new Jabber-Account").click();
        shellC.activateShellWithWaitingOpen("Create New User Account");
        textW.setTextInTextWithLabel(server, "Jabber Server");
        textW.setTextInTextWithLabel(username, "Username");
        textW.setTextInTextWithLabel(password, "Password");
        textW.setTextInTextWithLabel(password, "Repeat Password");
        buttonW.waitUntilButtonEnabled(FINISH);
        buttonW.clickButton(FINISH);
        shellC.waitUntilShellClosed("Create New User Account");
        buttonW.clickButton(NEXT);
        buttonW.clickButton(FINISH);
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void addAccount(JID jid, String password) throws RemoteException {
        selectSarosPageInPreferences();
        bot.buttonInGroup(GeneralPreferencePage.ADD_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        shellC.activateShellWithWaitingOpen(SHELL_SAROS_CONFIGURATION);
        textW.setTextInTextWithLabel(jid.getDomain(), "XMPP/Jabber Server");
        textW.setTextInTextWithLabel(jid.getName(), "Username");
        textW.setTextInTextWithLabel(password, "Password");
        buttonW.waitUntilButtonEnabled(NEXT);
        buttonW.clickButton(NEXT);
        buttonW.waitUntilButtonEnabled(FINISH);
        buttonW.clickButton(FINISH);
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public boolean isAccountExist(JID jid, String password)
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

    public void activateAccountNoGUI(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        xmppAccountStore.setAccountActive(account);
    }

    public void activateAccount(JID jid, String password)
        throws RemoteException {
        assert isAccountExist(jid, password) : "the account (" + jid.getBase()
            + ") doesn't exist yet!";
        if (isAccountActive(jid))
            return;
        selectSarosPageInPreferences();
        SWTBotList list = bot
            .listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
        list.select(jid.getBase());
        bot.buttonInGroup(GeneralPreferencePage.ACTIVATE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        assert labelW.existsLabel("Active: " + jid.getBase());
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void changeAccount(JID jid, String newUserName, String newPassword,
        String newServer) throws RemoteException {
        xmppAccountStore.changeAccountData(getXMPPAccount(jid).getId(),
            newUserName, newPassword, newServer);
    }

    public void changeAccountGUI(JID jid, String newUserName,
        String newPassword, String newServer) throws RemoteException {
        selectSarosPageInPreferences();
        SWTBotList list = bot
            .listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
        list.select(jid.getBase());
        bot.buttonInGroup(GeneralPreferencePage.CHANGE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        shellC.activateShellWithWaitingOpen("Change XMPP Account");
        textW.setTextInTextWithLabel(newServer, "Server");
        textW.setTextInTextWithLabel(newUserName, "Username:");
        textW.setTextInTextWithLabel(newPassword, "Password:");
        textW.setTextInTextWithLabel(newPassword, "Confirm:");
        buttonW.clickButton(FINISH);
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void deleteAccount(JID jid) throws RemoteException {
        xmppAccountStore.deleteAccount(getXMPPAccount(jid));
    }

    public void deleteAccountGUI(JID jid, String password)
        throws RemoteException {
        if (!isAccountExist(jid, password))
            return;
        selectSarosPageInPreferences();
        SWTBotList list = bot
            .listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
        String[] items = list.getItems();
        for (String item : items) {
            log.debug("added account: " + item + "!");
        }
        list.select(jid.getBase());
        bot.buttonInGroup(GeneralPreferencePage.DELETE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        if (isAccountActive(jid)) {
            shellC.activateShellWithWaitingOpen("Deleting active account");
            buttonW.clickButton(OK);
        }
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void setupSettingForScreensharing(int encoder, int videoResolution,
        int bandWidth, int capturedArea) throws RemoteException {
        clickMenuSarosPreferences();
        shellC.activateShellWithWaitingOpen(SHELL_PREFERNCES);
        SWTBotTree tree = bot.tree();
        tree.expandNode(TREE_ITEM_SAROS_IN_SHELL_PREFERENCES).select(
            "Screensharing");
        bot.ccomboBox(0).setSelection(encoder);
        bot.ccomboBox(1).setSelection(videoResolution);
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void disableAutomaticReminder() throws RemoteException {
        if (!feedbackManager.isFeedbackDisabled()) {
            feedbackManager.setFeedbackDisabled(true);
        }
    }

    public void disableAutomaticReminderGUI() throws RemoteException {
        if (feedbackManager.isFeedbackDisabled()) {
            clickMenuSarosPreferences();
            shellC.activateShellWithWaitingOpen(SHELL_PREFERNCES);
            SWTBotTree tree = bot.tree();
            tree.expandNode("Saros").select("Feedback");
            bot.radioInGroup(Messages.getString("feedback.page.radio.disable"),
                Messages.getString("feedback.page.group.interval")).click();
            bot.button("Apply").click();
            bot.button(OK).click();
            shellC.waitUntilShellClosed(SHELL_PREFERNCES);
        }
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isAccountExistGUI(JID jid, String password)
        throws RemoteException {
        selectSarosPageInPreferences();
        SWTBotList list = bot
            .listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
        String[] items = list.getItems();
        for (String item : items) {
            if ((jid.getBase()).equals(item)) {
                bot.button(CANCEL).click();
                shellC.waitUntilShellClosed(SHELL_PREFERNCES);
                return true;
            }
        }
        bot.button(CANCEL).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
        return false;
    }

    public boolean isAccountActive(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        if (account == null)
            return false;
        return account.isActive();
    }

    public boolean isAccountActiveGUI(JID jid) throws RemoteException {
        selectSarosPageInPreferences();
        boolean existLabel = labelW.existsLabel("Active: " + jid.getBase());
        bot.button(CANCEL).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
        return existLabel;
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
    private void selectSarosPageInPreferences() throws RemoteException {
        clickMenuSarosPreferences();
        shellC.activateShellWithWaitingOpen(SHELL_PREFERNCES);
        treeW.selectTreeItem(TREE_ITEM_SAROS_IN_SHELL_PREFERENCES);
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
        workbenchC.activateWorkbench();
        menuW.clickMenuWithTexts(MENU_SAROS, "Preferences");
    }

}
