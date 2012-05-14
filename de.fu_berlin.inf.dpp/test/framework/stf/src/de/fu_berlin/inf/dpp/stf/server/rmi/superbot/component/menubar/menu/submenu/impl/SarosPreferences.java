package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.condition.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.ISarosPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

public final class SarosPreferences extends StfRemoteObject implements
    ISarosPreferences {

    private static final Logger log = Logger.getLogger(SarosPreferences.class);

    private static final SarosPreferences INSTANCE = new SarosPreferences();

    private static final int REFRESH_TIME_FOR_ACCOUNT_LIST = 500;

    public static SarosPreferences getInstance() {
        return INSTANCE;
    }

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
            .buttonInGroup(BUTTON_ADD_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .click();

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

        shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .select(jid.getBase());

        shell
            .bot()
            .buttonInGroup(BUTTON_ACTIVATE_ACCOUNT,
                GROUP_TITLE_XMPP_JABBER_ACCOUNTS).click();

        SWTBotShell activateAccountConfirmationShell = shell.bot().shell(
            ACTIVATE_ACCOUNT_DIALOG_TITLE);

        activateAccountConfirmationShell.activate();
        activateAccountConfirmationShell.bot().button(OK).click();
        activateAccountConfirmationShell.bot().waitUntil(
            Conditions.shellCloses(activateAccountConfirmationShell));

        assert shell.bot().label("Active: " + jid.getBase()).isVisible();

        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }

    public void editAccount(JID jid, String newXmppJabberID, String newPassword)
        throws RemoteException {
        SWTBotShell shell = preCondition();
        shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .select(jid.getBase());

        shell
            .bot()
            .buttonInGroup(BUTTON_EDIT_ACCOUNT,
                GROUP_TITLE_XMPP_JABBER_ACCOUNTS).click();
        SuperBot.getInstance().confirmShellEditXMPPJabberAccount(
            newXmppJabberID, newPassword);
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }

    public void removeAccount(JID jid) throws RemoteException {

        if (!isAccountExistNoGUI(jid))
            return;

        if (isAccountActiveNoGUI(jid))
            throw new RuntimeException(
                "it is not allowed to remove an active account");

        SWTBotShell shell = preCondition();
        shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .select(jid.getBase());

        SWTBotButton removeButton = shell.bot().buttonInGroup(
            BUTTON_REMOVE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS);

        if (removeButton.isEnabled()) {
            removeButton.click();
            SWTBotShell removeAccountConfirmationShell = shell.bot().shell(
                REMOVE_ACCOUNT_DIALOG_TITLE);

            removeAccountConfirmationShell.activate();
            removeAccountConfirmationShell.bot().button(YES).click();
            removeAccountConfirmationShell.bot().waitUntil(
                Conditions.shellCloses(removeAccountConfirmationShell));

            shell.bot().button(APPLY).click();
            shell.bot().button(OK).click();
            shell.bot().waitUntil(Conditions.shellCloses(shell));
        } else
            throw new RuntimeException(
                "it is not allowed to remove an active account");
    }

    public void removeAllNonActiveAccounts() throws RemoteException {

        SWTBotShell shell = preCondition();
        shell.activate();

        // wait for account list to update
        shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

        String[] items = shell.bot()
            .listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();

        for (String item : items) {

            shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
                .select(item);

            SWTBotButton removeButton = shell.bot().buttonInGroup(
                BUTTON_REMOVE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS);

            if (removeButton.isEnabled()) {
                removeButton.click();
                SWTBotShell removeAccountConfirmationShell = shell.bot().shell(
                    REMOVE_ACCOUNT_DIALOG_TITLE);

                removeAccountConfirmationShell.activate();
                removeAccountConfirmationShell.bot().button(YES).click();
                removeAccountConfirmationShell.bot().waitUntil(
                    Conditions.shellCloses(removeAccountConfirmationShell));
            }

        }
        assert shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
            .itemCount() == 1;

        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        shell.bot().waitUntil(Conditions.shellCloses(shell));
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
        shell.bot().tree().expandNode(NODE_SAROS, NODE_SAROS_NETWORK).select();

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
        if (!FeedbackManager.isFeedbackDisabled(getSaros())) {
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
        if (!FeedbackManager.isFeedbackDisabled(getSaros())) {
            FeedbackManager.setFeedbackDisabled(getSaros(), true);
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

            shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

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

    public JID getActiveAccount() throws RemoteException {

        SWTBotShell shell = preCondition();
        shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

        try {
            String activeAccount = shell.bot()
                .labelInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getText();

            return new JID(activeAccount.substring(
                LABEL_ACTIVE_ACCOUNT_PREFIX.length()).trim());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean isAccountActive(JID jid) throws RemoteException {
        try {

            SWTBotShell shell = preCondition();
            shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

            String activeAccount = shell.bot()
                .labelInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getText();

            boolean isActive = activeAccount
                .startsWith(LABEL_ACTIVE_ACCOUNT_PREFIX)
                && activeAccount.contains(jid.getBase());

            shell.bot().button(CANCEL).click();
            shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
            return isActive;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * This is a convenient function to show the right setting-page of Saros
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
            return account.getUsername().equals(jid.getName())
                && account.getDomain().equals(jid.getDomain());
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private boolean isAccountExistNoGUI(JID jid) {
        return getXmppAccountStore().exists(jid.getName(), jid.getDomain(), "",
            0);
    }

    public void setNeedBasedActivated(boolean activate) throws RemoteException {
        SWTBotShell shell = preCondition();
        shell.bot().sleep(500);
        SWTBotCheckBox checkBox = shell.bot().checkBox(
            CHECKBOX_NEEDS_BASED_SYNC);

        if (activate)
            checkBox.select();
        else
            checkBox.deselect();

        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();

        shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }

    public void restoreDefaults() throws RemoteException {
        SWTBotShell shell = preCondition();
        shell.bot().button(RESTORE_DEFAULTS).click();
        shell.bot().button(APPLY).click();
        shell.bot().button(OK).click();
        shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }
}
