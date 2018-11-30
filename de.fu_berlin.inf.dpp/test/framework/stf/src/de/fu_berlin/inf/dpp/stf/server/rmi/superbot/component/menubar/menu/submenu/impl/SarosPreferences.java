package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.impl;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.feedback.Messages;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.condition.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.menubar.menu.submenu.ISarosPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public final class SarosPreferences extends StfRemoteObject implements ISarosPreferences {

  private static final Logger log = Logger.getLogger(SarosPreferences.class);

  private static final SarosPreferences INSTANCE = new SarosPreferences();

  private static final int REFRESH_TIME_FOR_ACCOUNT_LIST = 500;

  public static SarosPreferences getInstance() {
    return INSTANCE;
  }

  @Override
  public void createAccount(JID jid, String password) throws RemoteException {
    SWTBotShell preferencesShell = preCondition();

    preferencesShell
        .bot()
        .buttonInGroup(BUTTON_ADD_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS)
        .click();

    SWTBotShell configurationShell = new SWTBot().shell(SHELL_SAROS_CONFIGURATION);
    configurationShell.activate();

    preferencesShell.bot().buttonInGroup(GROUP_TITLE_CREATE_NEW_XMPP_JABBER_ACCOUNT).click();

    SWTBotShell createAccountShell = new SWTBot().shell(SHELL_CREATE_XMPP_JABBER_ACCOUNT);

    createAccountShell.activate();
    SuperBot.getInstance().confirmShellCreateNewXMPPAccount(jid, password);
    createAccountShell.bot().button(NEXT).click();
    createAccountShell.bot().button(FINISH).click();
    createAccountShell.bot().button(APPLY).click();
    createAccountShell.bot().button(OK).click();

    preferencesShell.bot().waitUntil(SarosConditions.isShellClosed(preferencesShell));
  }

  @Override
  public void addAccount(JID jid, String password) throws RemoteException {
    SWTBotShell shell = preCondition();
    shell.bot().buttonInGroup(BUTTON_ADD_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS).click();

    SuperBot.getInstance().confirmShellAddXMPPAccount(jid, password);

    shell.bot().button(APPLY).click();
    shell.bot().button(OK).click();
    shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
  }

  @Override
  public void activateAccount(JID jid) throws RemoteException {
    assert existsAccount(jid) : "the account (" + jid.getBase() + ") doesn't exist yet!";

    if (isAccountActiveNoGUI(jid)) return;

    SWTBotShell shell = preCondition();

    shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).select(jid.getBase());

    shell.bot().buttonInGroup(BUTTON_ACTIVATE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS).click();

    SWTBotShell activateAccountConfirmationShell = shell.bot().shell(ACTIVATE_ACCOUNT_DIALOG_TITLE);

    activateAccountConfirmationShell.activate();
    activateAccountConfirmationShell.bot().button(OK).click();
    activateAccountConfirmationShell
        .bot()
        .waitUntil(Conditions.shellCloses(activateAccountConfirmationShell));

    assert shell.bot().label("Active: " + jid.getBase()).isVisible();

    shell.bot().button(APPLY).click();
    shell.bot().button(OK).click();
    shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
  }

  @Override
  public void editAccount(JID jid, String newXmppJabberID, String newPassword)
      throws RemoteException {
    SWTBotShell shell = preCondition();
    shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).select(jid.getBase());

    shell.bot().buttonInGroup(BUTTON_EDIT_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS).click();
    SuperBot.getInstance().confirmShellEditXMPPAccount(newXmppJabberID, newPassword);
    shell.bot().button(APPLY).click();
    shell.bot().button(OK).click();
    shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
  }

  @Override
  public void removeAccount(JID jid) throws RemoteException {

    if (!isAccountExistNoGUI(jid)) return;

    if (isAccountActiveNoGUI(jid))
      throw new RuntimeException("it is not allowed to remove an active account");

    SWTBotShell shell = preCondition();
    shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).select(jid.getBase());

    SWTBotButton removeButton =
        shell.bot().buttonInGroup(BUTTON_REMOVE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS);

    if (removeButton.isEnabled()) {
      removeButton.click();
      SWTBotShell removeAccountConfirmationShell = shell.bot().shell(REMOVE_ACCOUNT_DIALOG_TITLE);

      removeAccountConfirmationShell.activate();
      removeAccountConfirmationShell.bot().button(YES).click();
      removeAccountConfirmationShell
          .bot()
          .waitUntil(Conditions.shellCloses(removeAccountConfirmationShell));

      shell.bot().button(APPLY).click();
      shell.bot().button(OK).click();
      shell.bot().waitUntil(Conditions.shellCloses(shell));
    } else throw new RuntimeException("it is not allowed to remove an active account");
  }

  @Override
  public void removeAllNonActiveAccounts() throws RemoteException {

    SWTBotShell shell = preCondition();
    shell.activate();

    // wait for account list to update
    shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

    String[] items = shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();

    for (String item : items) {

      shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).select(item);

      SWTBotButton removeButton =
          shell.bot().buttonInGroup(BUTTON_REMOVE_ACCOUNT, GROUP_TITLE_XMPP_JABBER_ACCOUNTS);

      if (removeButton.isEnabled()) {
        removeButton.click();
        SWTBotShell removeAccountConfirmationShell = shell.bot().shell(REMOVE_ACCOUNT_DIALOG_TITLE);

        removeAccountConfirmationShell.activate();
        removeAccountConfirmationShell.bot().button(YES).click();
        removeAccountConfirmationShell
            .bot()
            .waitUntil(Conditions.shellCloses(removeAccountConfirmationShell));
      }
    }
    assert shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).itemCount() == 1;

    shell.bot().button(APPLY).click();
    shell.bot().button(OK).click();
    shell.bot().waitUntil(Conditions.shellCloses(shell));
  }

  @Override
  public void enableIBBOnlyTransfer() throws RemoteException {
    setIBBOnlyTransfer(true);
  }

  @Override
  public void disableIBBOnlyTransfer() throws RemoteException {
    setIBBOnlyTransfer(false);
  }

  private void setIBBOnlyTransfer(boolean check) throws RemoteException {

    clickMenuSarosPreferences();

    SWTBot bot = new SWTBot();

    SWTBotShell shell = bot.shell(SHELL_PREFERNCES);

    shell.activate();
    shell.bot().tree().expandNode(NODE_SAROS, NODE_SAROS_NETWORK).select();

    SWTBotCheckBox checkBox =
        shell
            .bot()
            .checkBoxInGroup(
                PREF_NODE_SAROS_NETWORK_ADVANCED_GROUP_FILE_TRANSFER_FORCE_IBB,
                PREF_NODE_SAROS_NETWORK_ADVANCED_GROUP_FILE_TRANSFER);

    if (check) checkBox.select();
    else checkBox.deselect();

    shell.bot().button(APPLY).click();
    shell.bot().button(OK).click();

    shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
  }

  @Override
  public void disableAutomaticReminder() throws RemoteException {
    if (!FeedbackManager.isFeedbackDisabled()) {
      clickMenuSarosPreferences();

      SWTBotShell shell = new SWTBot().shell(SHELL_PREFERNCES);
      shell.activate();

      shell.bot().tree().expandNode(NODE_SAROS, NODE_SAROS_FEEDBACK).select();
      shell
          .bot()
          .radioInGroup(
              Messages.getString("feedback.page.radio.disable"),
              Messages.getString("feedback.page.group.interval"))
          .click();
      shell.bot().button(APPLY).click();
      shell.bot().button(OK).click();
      shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
    }
  }

  @Override
  public void disableAutomaticReminderNoGUI() throws RemoteException {
    if (!FeedbackManager.isFeedbackDisabled()) {

      FeedbackManager.setFeedbackDisabled(true);
    }
  }

  @Override
  public boolean existsAccount() throws RemoteException {
    try {
      SWTBotShell shell = preCondition();

      shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

      String[] items = shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();
      if (items == null || items.length == 0) return false;
      else return true;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  public boolean existsAccount(JID jid) throws RemoteException {
    try {
      SWTBotShell shell = preCondition();

      shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

      String[] items = shell.bot().listInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getItems();

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

  @Override
  public JID getActiveAccount() throws RemoteException {

    SWTBotShell shell = preCondition();
    shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

    try {
      String activeAccount = shell.bot().labelInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getText();

      return new JID(activeAccount.substring(LABEL_ACTIVE_ACCOUNT_PREFIX.length()).trim());

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public boolean isAccountActive(JID jid) throws RemoteException {
    try {

      SWTBotShell shell = preCondition();
      shell.bot().sleep(REFRESH_TIME_FOR_ACCOUNT_LIST);

      String activeAccount = shell.bot().labelInGroup(GROUP_TITLE_XMPP_JABBER_ACCOUNTS).getText();

      boolean isActive =
          activeAccount.startsWith(LABEL_ACTIVE_ACCOUNT_PREFIX)
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
   * This is a convenient function to show the right setting-page of Saros item in the preferences
   * dialog.
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
    return getXmppAccountStore().exists(jid.getName(), jid.getDomain(), "", 0);
  }

  @Override
  public void preferInstantSessionStart() throws RemoteException {
    preferInstantSessionStart(true);
  }

  @Override
  public void preferArchiveSessionStart() throws RemoteException {
    preferInstantSessionStart(false);
  }

  private void preferInstantSessionStart(boolean check) throws RemoteException {

    clickMenuSarosPreferences();

    SWTBot bot = new SWTBot();

    SWTBotShell shell = bot.shell(SHELL_PREFERNCES);

    shell.activate();
    shell.bot().tree().expandNode(NODE_SAROS, NODE_SAROS_ADVANCED).select();

    SWTBotCheckBox checkBox =
        shell.bot().checkBox(PREF_NODE_SAROS_ADVANCED_INSTANT_SESSION_START_PREFERRED);

    if (check) checkBox.select();
    else checkBox.deselect();

    shell.bot().button(APPLY).click();
    shell.bot().button(OK).click();

    shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
  }

  @Override
  public void restoreDefaults() throws RemoteException {
    SWTBotShell shell = preCondition();
    shell.bot().button(RESTORE_DEFAULTS).click();
    shell.bot().button(APPLY).click();
    shell.bot().button(OK).click();
    shell.bot().waitUntil(SarosConditions.isShellClosed(shell));
  }
}
