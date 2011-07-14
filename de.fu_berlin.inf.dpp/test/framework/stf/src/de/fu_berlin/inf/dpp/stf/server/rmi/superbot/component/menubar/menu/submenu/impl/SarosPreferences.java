package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.bot.condition.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.ISarosPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;
import de.fu_berlin.inf.dpp.ui.preferencePages.GeneralPreferencePage;

public final class SarosPreferences extends StfRemoteObject implements
    ISarosPreferences {

    private static final Logger log = Logger.getLogger(SarosPreferences.class);

    private static final SarosPreferences INSTANCE = new SarosPreferences();

    private static final int REFRESH_TIME_FOR_ACCOUNT_LIST = 500;

    public static SarosPreferences getInstance() {
        return INSTANCE;
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void createAccount(JID jid, String password) throws RemoteException {
        SWTBotShell preferencesShell = preCondition();

        preferencesShell
            .bot()
            .buttonInGroup(BUTTON_ADD_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .click();

        SWTBotShell configurationShell = new SWTBot()
            .shell(SHELL_SAROS_CONFIGURATION);
        configurationShell.activate();

        preferencesShell.bot()
            .buttonInGroup(GROUP_TITLE_CREATE_NEW_XMPP_JABBER_ACCOUNT).click();

        SWTBotShell createAccountShell = new SWTBot()
            .shell(SHELL_CREATE_XMPP_JABBER_ACCOUNT);

        createAccountShell.activate();
        SuperBot.getInstance().confirmShellCreateNewXMPPJabberAccount(jid,
            password);
        createAccountShell.bot().button(NEXT).click();
        createAccountShell.bot().button(FINISH).click();
        createAccountShell.bot().button(APPLY).click();
        createAccountShell.bot().button(OK).click();

        preferencesShell.bot().waitUntil(
            SarosConditions.isShellClosed(preferencesShell));

    }

    public void addAccount(JID jid, String password) throws RemoteException {
        SWTBotShell shell = preCondition();
        shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.ADD_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();

        SuperBot.getInstance().confirmShellAddXMPPJabberAccount(jid, password);

        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }

    public void activateAccount(JID jid) throws RemoteException {
        assert existsAccount(jid) : "the account (" + jid.getBase()
            + ") doesn't exist yet!";
        if (isAccountActiveNoGUI(jid))
            return;

        SWTBotShell shell = preCondition();
        shell.bot().listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE)
            .select(jid.getBase());

        shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.ACTIVATE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();

        assert shell.bot().label("Active: " + jid.getBase()).isVisible();

        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }

    public void changeAccount(JID jid, String newXmppJabberID,
        String newPassword) throws RemoteException {
        SWTBotShell shell = preCondition();
        shell.bot().listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE)
            .select(jid.getBase());

        shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.CHANGE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        SuperBot.getInstance().confirmShellEditXMPPJabberAccount(
            newXmppJabberID, newPassword);
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }

    public void deleteAccount(JID jid, String password) throws RemoteException {

        if (!isAccountExistNoGUI(jid, password))
            return;

        if (isAccountActiveNoGUI(jid))
            throw new RuntimeException(
                "it is not allowed to delete a active account");

        SWTBotShell shell = preCondition();
        shell.bot().listInGroup(GeneralPreferencePage.ACCOUNT_GROUP_TITLE)
            .select(jid.getBase());

        shell
            .bot()
            .buttonInGroup(GeneralPreferencePage.DELETE_BTN_TEXT,
                GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }

    public void deleteAllNoActiveAccounts() throws RemoteException {

        SWTBotShell shell = preCondition();
        shell.activate();

        // wait for account list to update
        shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

        String[] items = shell.bot()
            .listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();

        for (String item : items) {

            shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
                .select(item);

            shell
                .bot()
                .buttonInGroup(GeneralPreferencePage.DELETE_BTN_TEXT,
                    GeneralPreferencePage.ACCOUNT_GROUP_TITLE).click();

            SWTBot bot = new SWTBot();
            try {
                bot.waitUntil(SarosConditions.isShellOpen(new SarosSWTBot(),
                    SHELL_DELETING_ACTIVE_ACCOUNT), 100);
                SWTBotShell errorShell = bot
                    .shell(SHELL_DELETING_ACTIVE_ACCOUNT);
                errorShell.activate();
                errorShell.bot().button(OK).click();
            } catch (TimeoutException ignore) {
                //
            }
        }
        assert shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .itemCount() == 1;

        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }

    public void setupSettingForScreensharing(int encoder, int videoResolution,
        int bandWidth, int capturedArea) throws RemoteException {
        clickMenuSarosPreferences();

        SWTBotShell shell = new SWTBot().shell(SHELL_PREFERNCES);

        shell.activate();

        shell.bot().tree().expandNode(NODE_SAROS, NODE_SAROS_SCREENSHARING)
            .select();
        shell.bot().ccomboBox(0).setSelection(encoder);
        shell.bot().ccomboBox(1).setSelection(videoResolution);

        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }

    public void enableIBBOnlyTransfer() throws RemoteException {
        setIBBOnlyTransfer(true);
    }

    public void disableIBBOnlyTransfer() throws RemoteException {
        setIBBOnlyTransfer(false);
    }

    private void setIBBOnlyTransfer(boolean check) throws RemoteException {

        clickMenuSarosPreferences();

        SWTBot bot = new SWTBot();

        SWTBotShell shell = bot.shell(SHELL_PREFERNCES);

        shell.activate();
        shell.bot().tree().expandNode(NODE_SAROS, NODE_SAROS_ADVANCED).select();

        SWTBotCheckBox checkBox = shell.bot().checkBoxInGroup(
            SAROS_ADVANCED_GROUP_FILE_TRANSFER_FORCE_IBB,
            SAROS_ADVANCED_GROUP_FILE_TRANSFER);

        if (check)
            checkBox.select();
        else
            checkBox.deselect();

        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();

        shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }

    public void disableAutomaticReminder() throws RemoteException {
        if (!getFeedbackManager().isFeedbackDisabled()) {
            clickMenuSarosPreferences();

            SWTBotShell shell = new SWTBot().shell(SHELL_PREFERNCES);
            shell.activate();

            shell.bot().tree().expandNode(NODE_SAROS, NODE_SAROS_FEEDBACK)
                .select();
            shell
                .bot()
                .radioInGroup(
                    Messages.getString("feedback.page.radio.disable"),
                    Messages.getString("feedback.page.group.interval")).click();
            shell.bot().button(APPLY).click();
            shell.bot().button(OK).click();
            shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
        }
    }

    public void disableAutomaticReminderNoGUI() throws RemoteException {
        if (!getFeedbackManager().isFeedbackDisabled()) {
            getFeedbackManager().setFeedbackDisabled(true);
        }
    }

    public boolean existsAccount() throws RemoteException {
        try {
            SWTBotShell shell = preCondition();

            shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

            String[] items = shell.bot()
                .listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();
            if (items == null || items.length == 0)
                return false;
            else
                return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

    public boolean existsAccount(JID jid) throws RemoteException {
        try {
            SWTBotShell shell = preCondition();

            shell.bot().sleep(500);

            String[] items = shell.bot()
                .listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();

            for (String item : items) {
                if ((jid.getBase()).equals(item)) {
                    shell.bot().button(CANCEL).click();
                    return true;
                }
            }
            shell.bot().button(CANCEL).click();
            shell.bot().waitUntil(SarosConditions.isShellClosed(shell));

            return false;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean isAccountActive(JID jid) throws RemoteException {
        try {

            SWTBotShell shell = preCondition();
            shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

            String activeAccount = shell.bot()
                .labelInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getText();

            boolean isActive = false;

            if (activeAccount.equals("Active: " + jid.getBase()))
                isActive = true;

            shell.bot().button(CANCEL).click();
            shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
            return isActive;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
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
    private SWTBotShell preCondition() throws RemoteException {
        clickMenuSarosPreferences();
        SWTBotShell shell = new SWTBot().shell(SHELL_PREFERNCES);
        shell.activate();
        shell.bot().tree().expandNode(NODE_SAROS).select();
        return shell;
    }

    /**
     * click the main menu Saros-> Preferences
     * 
     * @throws RemoteException
     */
    private void clickMenuSarosPreferences() throws RemoteException {
        RemoteWorkbenchBot.getInstance().activateWorkbench();
        new SWTBot().menu(MENU_SAROS).menu(MENU_PREFERENCES).click();
    }

    private boolean isAccountActiveNoGUI(JID jid) {
        XMPPAccount account = null;
        try {
            account = getXmppAccountStore().getActiveAccount();
            assert account.isActive();
            return account.getUsername().equals(jid.getName());
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private boolean isAccountExistNoGUI(JID jid, String password) {
        for (XMPPAccount account : getXmppAccountStore().getAllAccounts()) {
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
