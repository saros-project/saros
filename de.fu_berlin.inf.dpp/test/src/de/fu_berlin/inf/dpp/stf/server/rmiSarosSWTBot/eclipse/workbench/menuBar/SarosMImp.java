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

    /* name of the main menus */
    private static final String MENU_SAROS = "Saros";
    private static final String MENU_CREATE_ACCOUNT = "Create Account";

    /* title of shells which may pop up by clicking the main menus */
    private static final String SHELL_PREFERNCES = "Preferences";
    private static final String SHELL_SAROS_CONFIGURATION = "Saros Configuration";

    /* title of treeItem Saros and it's sub treeItems in the preferences dialog */
    private static final String P_SAROS = "Saros";

    /**
     * {@link SarosMImp} is a singleton, but inheritance is possible.
     */
    public static SarosMImp getInstance() {
        if (self != null)
            return self;
        self = new SarosMImp();
        return self;
    }

    /***********************************************************************
     * 
     * exported functions
     * 
     ***********************************************************************/

    /**********************************************
     * create and add an Account.
     * 
     **********************************************/
    public void createAccount(String server, String username, String password)
        throws RemoteException {
        xmppAccountStore.createNewAccount(username, password, server);
    }

    public void creatAccountWithMenuGUI(JID jid, String password,
        boolean usesThisAccountNow) throws RemoteException {
        basic.clickMenuWithTexts(MENU_SAROS, MENU_CREATE_ACCOUNT);
        rosterV.confirmWindowCreateXMPPAccount(jid.getDomain(), jid.getName(),
            password, usesThisAccountNow);
    }

    public void createAccountInPeferencesGUI(String server, String username,
        String password) throws RemoteException {
        selectSarosPageInPreferences();
        bot.buttonInGroup(GeneralPreferencePage.ADD_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        shellC.activateShellWaitingUntilOpened(SHELL_SAROS_CONFIGURATION);
        bot.buttonInGroup("Create new Jabber-Account").click();
        shellC.activateShellWaitingUntilOpened("Create New User Account");
        basic.setTextInTextWithLabel(server, "Jabber Server");
        basic.setTextInTextWithLabel(username, "Username");
        basic.setTextInTextWithLabel(password, "Password");
        basic.setTextInTextWithLabel(password, "Repeat Password");
        basic.waitUntilButtonEnabled(FINISH);
        basic.clickButton(FINISH);
        shellC.waitUntilShellClosed("Create New User Account");
        basic.clickButton(NEXT);
        basic.clickButton(FINISH);
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    public void addAccountGUI(JID jid, String password) throws RemoteException {
        selectSarosPageInPreferences();
        bot.buttonInGroup(GeneralPreferencePage.ADD_BTN_TEXT,
            GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        shellC.activateShellWaitingUntilOpened(SHELL_SAROS_CONFIGURATION);
        basic.setTextInTextWithLabel(jid.getDomain(), "Jabber Server");
        basic.setTextInTextWithLabel(jid.getName(), "Username");
        basic.setTextInTextWithLabel(password, "Password");
        basic.waitUntilButtonEnabled(NEXT);
        basic.clickButton(NEXT);
        basic.waitUntilButtonEnabled(FINISH);
        basic.clickButton(FINISH);
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

    /**********************************************
     * activate an Account.
     * 
     **********************************************/
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
        assert basic.existsLabel("Active: " + jid.getBase());
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
        boolean existLabel = basic.existsLabel("Active: " + jid.getBase());
        bot.button(CANCEL).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
        return existLabel;
    }

    /**********************************************
     * change an Account.
     * 
     **********************************************/
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
        shellC.activateShellWaitingUntilOpened("Change XMPP Account");
        basic.setTextInTextWithLabel(newServer, "Server");
        basic.setTextInTextWithLabel(newUserName, "Username:");
        basic.setTextInTextWithLabel(newPassword, "Password:");
        basic.setTextInTextWithLabel(newPassword, "Confirm:");
        basic.clickButton(FINISH);
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    /**********************************************
     * delete an Account.
     * 
     **********************************************/

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
            shellC.activateShellWaitingUntilOpened("Deleting active account");
            basic.clickButton(OK);
        }
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    /**********************************************
     * 
     * setting screensharing
     * 
     **********************************************/

    public void setupSettingForScreensharing(int encoder, int videoResolution,
        int bandWidth, int capturedArea) throws RemoteException {
        clickMenuSarosPreferences();
        shellC.activateShellWaitingUntilOpened(SHELL_PREFERNCES);
        SWTBotTree tree = bot.tree();
        tree.expandNode(P_SAROS).select("Screensharing");
        bot.ccomboBox(0).setSelection(encoder);
        bot.ccomboBox(1).setSelection(videoResolution);
        bot.button(APPLY).click();
        bot.button(OK).click();
        shellC.waitUntilShellClosed(SHELL_PREFERNCES);
    }

    /**********************************************
     * 
     * setting Feedback
     * 
     **********************************************/
    public void disableAutomaticReminder() throws RemoteException {
        if (!feedbackManager.isFeedbackDisabled()) {
            feedbackManager.setFeedbackDisabled(true);
        }
    }

    public void disableAutomaticReminderGUI() throws RemoteException {
        if (feedbackManager.isFeedbackDisabled()) {
            clickMenuSarosPreferences();
            shellC.activateShellWaitingUntilOpened(SHELL_PREFERNCES);
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
    /**
     * This is a convenient function to show the right setting-page of saros
     * item in the preferences dialog.
     */
    private void selectSarosPageInPreferences() throws RemoteException {
        clickMenuSarosPreferences();
        shellC.activateShellWaitingUntilOpened(SHELL_PREFERNCES);
        treeW.selectTreeItem(P_SAROS);
    }

    /**
     * 
     * @param jid
     *            a Jabber ID which is used to identify the users of the Jabber
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
        workbenchC.activateEclipseShell();
        basic.clickMenuWithTexts(MENU_SAROS, "Preferences");
    }

}
