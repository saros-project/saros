package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.MainMenuComponentImp;
import de.fu_berlin.inf.dpp.ui.GeneralPreferencePage;

public class SarosMainMenuComponentImp extends MainMenuComponentImp implements
    SarosMainMenuComponent {

    private static transient SarosMainMenuComponentImp self;

    /* name of the main menus */
    private static final String MENU_SAROS = "Saros";
    private static final String MENU_CREATE_ACCOUNT = "Create Account";
    private static final String MENU_PREFERENCES = "Preferences";

    /* title of shells which are pop up by clicking the main menus */
    private static final String SHELL_PREFERNCES = "Preferences";
    private static final String SHELL_SAROS_CONFIGURATION = "Saros Configuration";

    /* title of treeItem Saros and all sub treeItems in the preferences dialog */
    private static final String P_SAROS = "Saros";

    /**
     * {@link SarosMainMenuComponentImp} is a singleton, but inheritance is
     * possible.
     */
    public static SarosMainMenuComponentImp getInstance() {
        if (self != null)
            return self;
        self = new SarosMainMenuComponentImp();
        return self;
    }

    /***********************************************************************
     * 
     * exported functions
     * 
     ***********************************************************************/

    /**********************************************
     * 
     * actions about Account.
     * 
     **********************************************/
    public void createAccount(String server, String username, String password)
        throws RemoteException {
        xmppAccountStore.createNewAccount(username, password, server);
    }

    public void creatAccountWithMenuGUI(JID jid, String password,
        boolean usesThisAccountNow) throws RemoteException {
        mainMenuC.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        rosterVC.confirmWindowCreateXMPPAccount(jid.getDomain(), jid.getName(),
            password, usesThisAccountNow);
    }

    public void createAccountInPeferencesGUI(String server, String username,
        String password) throws RemoteException {
        clickMenuSarosPreferences();
        shellC.activateShellWaitingUntilOpened(SHELL_PREFERNCES);
        selectTreeItemSarosInShellPreferences();
        bot.buttonInGroup(GeneralPreferencePage.ADD_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        shellC.activateShellWaitingUntilOpened(SHELL_SAROS_CONFIGURATION);
        bot.buttonInGroup("Create new Jabber-Account").click();
        shellC.activateShellWaitingUntilOpened("Create New User Account");
        basicC.setTextInTextWithLabel(server, "Jabber Server");
        basicC.setTextInTextWithLabel(username, "Username");
        basicC.setTextInTextWithLabel(password, "Password");
        basicC.setTextInTextWithLabel(password, "Repeat Password");
        basicC.waitUntilButtonEnabled(FINISH);
        basicC.clickButton(FINISH);
        shellC.waitUntilShellClosed("Create New User Account");
        basicC.clickButton(NEXT);
        basicC.clickButton(FINISH);
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void addAccountGUI(JID jid, String password) throws RemoteException {
        clickMenuSarosPreferences();
        shellC.activateShellWaitingUntilOpened(SHELL_PREFERNCES);
        selectTreeItemSarosInShellPreferences();
        bot.buttonInGroup(GeneralPreferencePage.ADD_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        shellC.activateShellWaitingUntilOpened(SHELL_SAROS_CONFIGURATION);
        basicC.setTextInTextWithLabel(jid.getDomain(), "Jabber Server");
        basicC.setTextInTextWithLabel(jid.getName(), "Username");
        basicC.setTextInTextWithLabel(password, "Password");
        basicC.waitUntilButtonEnabled(NEXT);
        basicC.clickButton(NEXT);
        basicC.waitUntilButtonEnabled(FINISH);
        basicC.clickButton(FINISH);
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

    public boolean isAccountExistGUI(JID jid, String password)
        throws RemoteException {
        clickMenuSarosPreferences();
        shellC.activateShellWaitingUntilOpened(SHELL_PREFERNCES);
        selectTreeItemSarosInShellPreferences();
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

    public void activateAccount(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        xmppAccountStore.setAccountActive(account);
    }

    public void activateAccountGUI(JID jid, String password)
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
        assert basicC.existsLabel("Active: " + jid.getBase());

        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public boolean isAccountActive(JID jid) throws RemoteException {
        XMPPAccount account = getXMPPAccount(jid);
        if (account == null)
            return false;
        return account.isActive();
    }

    public boolean isAccountActiveGUI(JID jid) throws RemoteException {
        selectSarosPageInPreferences();
        boolean existLabel = basicC.existsLabel("Active: " + jid.getBase());
        bot.button(CANCEL).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
        return existLabel;
    }

    public void changeAccount(JID jid, String newUserName, String newPassword,
        String newServer) throws RemoteException {
        xmppAccountStore.changeAccountData(getXMPPAccount(jid).getId(),
            newUserName, newPassword, newServer);
    }

    public void changeAccountGUI(JID jid, String newUserName,
        String newPassword, String newServer) throws RemoteException {
        clickMenuSarosPreferences();
        shellC.activateShellWaitingUntilOpened(SHELL_PREFERNCES);
        selectTreeItemSarosInShellPreferences();
        SWTBotList list = bot
            .listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE);
        list.select(jid.getBase());
        bot.buttonInGroup(GeneralPreferencePage.CHANGE_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        shellC.activateShellWaitingUntilOpened("Change XMPP Account");
        basicC.setTextInTextWithLabel(newServer, "Server");
        basicC.setTextInTextWithLabel(newUserName, "Username:");
        basicC.setTextInTextWithLabel(newPassword, "Password:");
        basicC.setTextInTextWithLabel(newPassword, "Confirm:");
        basicC.clickButton(FINISH);
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void deleteAccount(JID jid) throws RemoteException {
        xmppAccountStore.deleteAccount(getXMPPAccount(jid));
    }

    public void deleteAccountGUI(JID jid, String password)
        throws RemoteException {
        assert isAccountExist(jid, password) : "the account (" + jid.getBase()
            + ") doesn't exists yet!";
        clickMenuSarosPreferences();
        shellC.activateShellWaitingUntilOpened(SHELL_PREFERNCES);
        selectTreeItemSarosInShellPreferences();
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
            shellC.activateShellWaitingUntilOpened("Deleting active account");
            basicC.clickButton(OK);
        }
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    /**********************************************
     * 
     * setting for screensharing
     * 
     **********************************************/

    public void setupSettingForScreensharing(int encoder, int videoResolution,
        int bandWidth, int capturedArea) throws RemoteException {
        clickMenuSarosPreferences();
        SWTBotTree tree = bot.tree();
        tree.expandNode("Saros").select("Screensharing");
        bot.ccomboBox(0).setSelection(encoder);
        bot.ccomboBox(1).setSelection(videoResolution);
        bot.button("Apply").click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    /**********************************************
     * 
     * setting for Feedback
     * 
     **********************************************/
    public void disableAutomaticReminder() throws RemoteException {
        if (!feedbackManager.isFeedbackDisabled()) {
            feedbackManager.setFeedbackDisabled(true);
            // clickMenuSarosPreferences();
            // SWTBotTree tree = bot.tree();
            // tree.expandNode("Saros").select("Feedback");
            // bot.radioInGroup(Messages.getString("feedback.page.radio.disable"),
            // Messages.getString("feedback.page.group.interval")).click();
            // bot.button("Apply").click();
            // bot.button(OK).click();
            // windowPart.waitUntilShellClosed(SHELL_PREFERNCES);
        }
    }

    public void disableAutomaticReminderGUI() throws RemoteException {
        if (feedbackManager.isFeedbackDisabled()) {
            clickMenuSarosPreferences();
            SWTBotTree tree = bot.tree();
            tree.expandNode("Saros").select("Feedback");
            bot.radioInGroup(Messages.getString("feedback.page.radio.disable"),
                Messages.getString("feedback.page.group.interval")).click();
            bot.button("Apply").click();
            bot.button(OK).click();
            shellC.waitUntilShellClosed(SHELL_PREFERNCES);
        }
    }

    /**************************************************************
     * 
     * Inner functions
     * 
     * @throws RemoteException
     * 
     *************************************************************/

    private void selectSarosPageInPreferences() throws RemoteException {
        clickMenuSarosPreferences();
        shellC.activateShellWaitingUntilOpened(SHELL_PREFERNCES);
        selectTreeItemSarosInShellPreferences();
    }

    private void selectTreeItemSarosInShellPreferences() {
        SWTBotTree tree = bot.tree();
        tree.expandNode(P_SAROS).select();
    }

    private XMPPAccount getXMPPAccount(JID jid) {
        for (XMPPAccount account : xmppAccountStore.getAllAccounts()) {
            if (jid.getName().equals(account.getUsername())
                && jid.getDomain().equals(account.getServer())) {
                return account;
            }
        }
        return null;
    }

    private void clickMenuSarosPreferences() throws RemoteException {
        precondition();
        mainMenuC.clickMenuWithTexts(MENU_SAROS, MENU_PREFERENCES);
    }

    public void addAccountGUI(String server, String username, String password)
        throws RemoteException {
        // TODO Auto-generated method stub

    }

    public void deleteAccountGUI(String server, String username, String password)
        throws RemoteException {
        // TODO Auto-generated method stub

    }

}
