package saros.intellij.ui.views.buttons;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.util.TextRange;
import java.text.MessageFormat;
import java.util.Scanner;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.SarosPluginContext;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.exceptions.IllegalAWTContextException;
import saros.intellij.ui.Messages;
import saros.intellij.ui.actions.AbstractSarosAction;
import saros.intellij.ui.actions.ConnectServerAction;
import saros.intellij.ui.actions.DisconnectServerAction;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.util.SafeDialogUtils;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;

/** Implementation of connect XMPP/jabber server button */
public class ConnectButton extends AbstractToolbarButton {
  private static final Logger LOG = Logger.getLogger(ConnectButton.class);

  private static final String USER_ID_SEPARATOR = "@";

  private static final boolean ENABLE_CONFIGURE_ACCOUNTS =
      Boolean.getBoolean("saros.intellij.ENABLE_CONFIGURE_ACCOUNTS");

  private final JPopupMenu popupMenu;

  private final JMenuItem addAccountItem;
  private final JMenuItem configureAccountItem;
  private final JMenuItem disconnectItem;

  private final AbstractSarosAction disconnectAction;
  private final ConnectServerAction connectAction;
  private final AbstractSarosAction configureAccounts;

  @Inject private XMPPAccountStore accountStore;

  public ConnectButton(@NotNull Project project) {
    super(
        project,
        ConnectServerAction.NAME,
        Messages.ConnectButton_tooltip,
        IconManager.CONNECT_ICON);

    SarosPluginContext.initComponent(this);

    disconnectAction = new DisconnectServerAction(project);
    connectAction = new ConnectServerAction(project);
    configureAccounts =
        new AbstractSarosAction() {
          @Override
          public String getActionName() {
            throw new UnsupportedOperationException("Not yet implemented");
          }

          @Override
          public void execute() {
            throw new UnsupportedOperationException("Not yet implemented");
          }
        };

    popupMenu = new JBPopupMenu();

    addAccountItem = createAddAccountMenuItem();
    configureAccountItem = createConfigureAccountMenuItem();
    disconnectItem = createDisconnectMenuItem();

    popupMenu.setForeground(FOREGROUND_COLOR);
    popupMenu.setBackground(BACKGROUND_COLOR);

    addMenuItems();

    addActionListener(
        actionEvent -> {
          if (accountStore.isEmpty()) {
            XMPPAccount account = createNewAccount();

            if (account != null) {
              addMenuItems();

              askToConnectToAccount(account);

              return;
            }
          }

          popupMenu.show(ConnectButton.this, 0, getBounds().y + getBounds().height);
        });
  }

  private JMenuItem createAddAccountMenuItem() {
    JMenuItem addAccountItem = getPreconfiguredMenuItem(Messages.ConnectButton_add_account);

    addAccountItem.addActionListener(
        actionEvent -> {
          XMPPAccount account = createNewAccount();

          if (account == null) {
            return;
          }

          addMenuItems();

          askToConnectToAccount(account);
        });

    return addAccountItem;
  }

  private JMenuItem createConfigureAccountMenuItem() {
    JMenuItem configureAccountItem =
        getPreconfiguredMenuItem(Messages.ConnectButton_configure_accounts);

    configureAccountItem.addActionListener(actionEvent -> configureAccounts.execute());

    return configureAccountItem;
  }

  private JMenuItem createDisconnectMenuItem() {
    JMenuItem disconnectItem = getPreconfiguredMenuItem(Messages.ConnectButton_disconnect);

    disconnectItem.addActionListener(actionEvent -> disconnectAction.execute());

    return disconnectItem;
  }

  @Override
  public void dispose() {
    // NOP
  }

  private void addMenuItems() {
    popupMenu.removeAll();
    for (XMPPAccount account : accountStore.getAllAccounts()) {
      createAccountMenuItem(account);
    }

    popupMenu.addSeparator();
    popupMenu.add(addAccountItem);

    if (ENABLE_CONFIGURE_ACCOUNTS) {
      popupMenu.add(configureAccountItem);
    }

    popupMenu.add(disconnectItem);
  }

  private void createAccountMenuItem(XMPPAccount account) {
    final String userName = account.getUsername() + USER_ID_SEPARATOR + account.getDomain();
    JMenuItem accountItem = createMenuItemForUser(userName);
    popupMenu.add(accountItem);
  }

  private JMenuItem createMenuItemForUser(final String userName) {
    JMenuItem accountItem = getPreconfiguredMenuItem(userName);

    accountItem.addActionListener(actionEvent -> connectAction.executeWithUser(userName));

    return accountItem;
  }

  private void askToConnectToAccount(@NotNull XMPPAccount account) {
    try {
      boolean choseYes =
          SafeDialogUtils.showYesNoDialog(
              project,
              Messages.ConnectButton_connect_to_new_account_message,
              Messages.ConnectButton_connect_to_new_account_title);

      if (choseYes) {
        connectAction.executeWithUser(
            account.getUsername() + USER_ID_SEPARATOR + account.getDomain());
      }

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      showAccountCreationFailedError(e);
    }
  }

  /**
   * Asks for user id (name@domain), password, and server for a new XMPP account.
   *
   * @return an <code>XMPPAccount</code> with the values entered by the user or <code>null</code> if
   *     the creation was canceled by the user or the user entered at least one illegal value
   */
  private XMPPAccount createNewAccount() {
    final String userID;

    try {
      userID =
          SafeDialogUtils.showInputDialog(
              project,
              Messages.ConnectButton_account_creation_jid_message,
              Messages.ConnectButton_account_creation_jid_initial_input,
              Messages.ConnectButton_account_creation_jid_title,
              new TextRange(0, 0));

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      showAccountCreationFailedError(e);

      return null;
    }

    if (userID == null) {
      LOG.debug("Account creation canceled by user during user id entry.");

      return null;
    }

    JID jid = new JID(userID);

    if (!jid.isValid() || jid.getName().isEmpty()) {
      SafeDialogUtils.showError(
          project,
          Messages.ConnectButton_account_creation_invalid_jid_message,
          Messages.ConnectButton_account_creation_invalid_jid_title);

      LOG.debug("Account creation failed as the user did not provide a valid user id.");

      return null;
    }

    String username = jid.getName();
    String domain = jid.getDomain();

    final String password;

    try {
      password =
          SafeDialogUtils.showPasswordDialog(
              project,
              Messages.ConnectButton_account_creation_password_message,
              Messages.ConnectButton_account_creation_password_title);

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      showAccountCreationFailedError(e);

      return null;
    }

    if (password == null) {
      LOG.debug("Account creation canceled by user during password entry.");

      return null;

    } else if (password.isEmpty()) {
      SafeDialogUtils.showError(
          project,
          Messages.ConnectButton_account_creation_invalid_password_message,
          Messages.ConnectButton_account_creation_invalid_password_title);

      LOG.debug("Account creation failed as the user did not provide a valid password.");

      return null;
    }

    int port = 0;

    String server;

    try {
      server =
          SafeDialogUtils.showInputDialog(
              project,
              Messages.ConnectButton_account_creation_xmpp_server_message,
              Messages.ConnectButton_account_creation_xmpp_server_initial_input,
              Messages.ConnectButton_account_creation_xmpp_server_title);

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      showAccountCreationFailedError(e);

      return null;
    }

    if (server == null) {
      LOG.debug("Account creation canceled by user during server entry.");

      return null;

    } else if (!server.isEmpty()) {
      String portUserEntry;

      try {
        portUserEntry =
            SafeDialogUtils.showInputDialog(
                project,
                Messages.ConnectButton_account_creation_xmpp_server_port_title,
                Messages.ConnectButton_account_creation_xmpp_server_port_initial_input,
                Messages.ConnectButton_account_creation_xmpp_server_port_message);

      } catch (IllegalAWTContextException e) {
        LOG.error("Account creation failed.", e);

        showAccountCreationFailedError(e);

        return null;
      }

      if (portUserEntry == null) {
        LOG.debug("Account creation canceled by user during server port entry.");

        return null;
      }

      Scanner scanner = new Scanner(portUserEntry.trim());

      if (scanner.hasNextInt(10)) {
        port = scanner.nextInt(10);

        scanner.close();

      } else {
        scanner.close();

        SafeDialogUtils.showError(
            project,
            Messages.ConnectButton_account_creation_xmpp_server_invalid_port_message,
            Messages.ConnectButton_account_creation_xmpp_server_invalid_port_title);

        LOG.debug("Account creation failed as the user did not provide a valid server port.");

        return null;
      }
    }

    try {
      return accountStore.createAccount(username, password, domain, server, port, true, true);

    } catch (IllegalArgumentException e) {
      LOG.error("Account creation failed", e);

      showAccountCreationFailedError(e);

      return null;
    }
  }

  /**
   * Displays an error notification to the user stating that the account creation failed and
   * displaying the message of the given exception.
   *
   * @param e the exception whose message to display
   */
  private void showAccountCreationFailedError(Exception e) {
    SafeDialogUtils.showError(
        project,
        MessageFormat.format(
            Messages.ConnectButton_account_creation_failed_message, e.getMessage()),
        Messages.ConnectButton_account_creation_failed_title);
  }

  private JMenuItem getPreconfiguredMenuItem(String text) {
    JMenuItem jMenuItem = new JBMenuItem(text);

    jMenuItem.setForeground(FOREGROUND_COLOR);
    jMenuItem.setBackground(BACKGROUND_COLOR);

    return jMenuItem;
  }
}
