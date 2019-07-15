package saros.intellij.ui.views.buttons;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
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
import saros.intellij.ui.actions.NotImplementedAction;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.util.SafeDialogUtils;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;

/** Implementation of connect XMPP/jabber server button */
public class ConnectButton extends ToolbarButton {
  private static final Logger LOG = Logger.getLogger(ConnectButton.class);

  private static final String USER_ID_SEPARATOR = "@";

  private static final boolean ENABLE_CONFIGURE_ACCOUNTS =
      Boolean.getBoolean("saros.intellij.ENABLE_CONFIGURE_ACCOUNTS");

  private final JPopupMenu popupMenu;
  private JMenuItem menuItemAdd;
  private JMenuItem configure;
  private JMenuItem disconnect;

  private final AbstractSarosAction disconnectAction;
  private final ConnectServerAction connectAction;
  private final NotImplementedAction configureAccounts;

  private final Project project;

  @Inject private XMPPAccountStore accountStore;

  public ConnectButton(@NotNull Project project) {
    super(ConnectServerAction.NAME, "Connect", IconManager.CONNECT_ICON);
    SarosPluginContext.initComponent(this);

    this.project = project;

    disconnectAction = new DisconnectServerAction(project);
    connectAction = new ConnectServerAction(project);

    popupMenu = new JBPopupMenu();

    popupMenu.setForeground(FOREGROUND_COLOR);
    popupMenu.setBackground(BACKGROUND_COLOR);

    configureAccounts = new NotImplementedAction("configure accounts");

    createDisconnectMenuItem();
    createAddAccountMenuItem();
    createConfigureAccountMenuItem();
    createMenuItems();

    addActionListener(
        actionEvent -> {
          if (accountStore.isEmpty()) {
            XMPPAccount account = createNewAccount();

            if (account != null) {
              createMenuItems();

              askToConnectToAccount(account);

              return;
            }
          }

          popupMenu.show(ConnectButton.this, 0, getBounds().y + getBounds().height);
        });
  }

  private void createMenuItems() {
    popupMenu.removeAll();
    for (XMPPAccount account : accountStore.getAllAccounts()) {
      createAccountMenuItem(account);
    }

    popupMenu.addSeparator();
    popupMenu.add(menuItemAdd);

    if (ENABLE_CONFIGURE_ACCOUNTS) {
      popupMenu.add(configure);
    }

    popupMenu.add(disconnect);
  }

  private void createAccountMenuItem(XMPPAccount account) {
    final String userName = account.getUsername() + USER_ID_SEPARATOR + account.getDomain();
    JMenuItem accountItem = createMenuItemForUser(userName);
    popupMenu.add(accountItem);
  }

  private JMenuItem createMenuItemForUser(final String userName) {
    JMenuItem accountItem = new JBMenuItem(userName);

    accountItem.setForeground(FOREGROUND_COLOR);
    accountItem.setBackground(BACKGROUND_COLOR);

    accountItem.addActionListener(actionEvent -> connectAction.executeWithUser(userName));

    return accountItem;
  }

  private void createDisconnectMenuItem() {
    disconnect = new JBMenuItem("Disconnect server");

    disconnect.setForeground(FOREGROUND_COLOR);
    disconnect.setBackground(BACKGROUND_COLOR);

    disconnect.addActionListener(actionEvent -> disconnectAction.execute());
  }

  private void createConfigureAccountMenuItem() {
    configure = new JBMenuItem("Configure accounts...");

    configure.setForeground(FOREGROUND_COLOR);
    configure.setBackground(BACKGROUND_COLOR);

    configure.addActionListener(actionEvent -> configureAccounts.execute());
  }

  private void createAddAccountMenuItem() {
    menuItemAdd = new JBMenuItem("Add account...");

    menuItemAdd.setForeground(FOREGROUND_COLOR);
    menuItemAdd.setBackground(BACKGROUND_COLOR);

    menuItemAdd.addActionListener(
        actionEvent -> {
          XMPPAccount account = createNewAccount();

          if (account == null) {
            return;
          }

          createMenuItems();

          askToConnectToAccount(account);
        });
  }

  private void askToConnectToAccount(@NotNull XMPPAccount account) {
    try {
      Integer option =
          SafeDialogUtils.showYesNoDialog(
              project,
              Messages.ConnectButton_connect_to_new_account_message,
              Messages.ConnectButton_connect_to_new_account_title);

      if (option == com.intellij.openapi.ui.Messages.YES) {
        connectAction.executeWithUser(
            account.getUsername() + USER_ID_SEPARATOR + account.getDomain());
      }

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      SafeDialogUtils.showError(
          project,
          "There was an error creating the account.\nDetails: " + e.getMessage(),
          "Account creation failed");
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
              project, "Your User-ID, e.g. user@saros-con.imp.fu-berlin.de", "", "Login");

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      SafeDialogUtils.showError(
          project,
          "There was an error creating the account.\nDetails: " + e.getMessage(),
          "Account creation failed");

      return null;
    }

    if (userID == null) {
      LOG.debug("Account creation canceled by user during user id" + " entry.");

      return null;
    }

    JID jid = new JID(userID);

    if (!jid.isValid() || jid.getName().isEmpty()) {
      SafeDialogUtils.showError(
          project, "Entered user id is not valid.", "Account creation aborted");

      LOG.debug("Account creation failed as the user did not provide a " + "valid user id.");

      return null;
    }

    String username = jid.getName();
    String domain = jid.getDomain();

    final String password;

    try {
      password = SafeDialogUtils.showPasswordDialog(project, "Password", "Password");

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      SafeDialogUtils.showError(
          project,
          "There was an error creating the account.\nDetails: " + e.getMessage(),
          "Account creation failed");

      return null;
    }

    if (password == null) {
      LOG.debug("Account creation canceled by user during password" + " entry.");

      return null;

    } else if (password.isEmpty()) {
      SafeDialogUtils.showError(project, "No password entered.", "Account creation aborted");

      LOG.debug("Account creation failed as the user did not provide a " + "valid password.");

      return null;
    }

    int port = 0;

    String server;

    try {
      server =
          SafeDialogUtils.showInputDialog(
              project, "XMPP server (optional, not necessary in most cases)", "", "Server");

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      SafeDialogUtils.showError(
          project,
          "There was an error creating the account.\nDetails: " + e.getMessage(),
          "Account creation failed");

      return null;
    }

    if (server == null) {
      LOG.debug("Account creation canceled by user during server entry.");

      return null;

    } else if (!server.isEmpty()) {
      String portUserEntry;

      try {
        portUserEntry =
            SafeDialogUtils.showInputDialog(project, "XMPP server port", "", "Server port");

      } catch (IllegalAWTContextException e) {
        LOG.error("Account creation failed.", e);

        SafeDialogUtils.showError(
            project,
            "There was an error creating the account.\nDetails: " + e.getMessage(),
            "Account creation failed");

        return null;
      }

      if (portUserEntry == null) {
        LOG.debug("Account creation canceled by user during server " + "port entry.");

        return null;
      }

      Scanner scanner = new Scanner(portUserEntry.trim());

      if (scanner.hasNextInt(10)) {
        port = scanner.nextInt(10);

        scanner.close();

      } else {
        scanner.close();

        SafeDialogUtils.showError(
            project, "No valid server port " + "entered.", "Account creation aborted");

        LOG.debug("Account creation failed as the user did not " + "provide a valid server port.");

        return null;
      }
    }

    try {
      return accountStore.createAccount(username, password, domain, server, port, true, true);

    } catch (IllegalArgumentException e) {
      LOG.error("Account creation failed", e);

      SafeDialogUtils.showError(
          project,
          "There was an error creating the account.\nDetails: " + e.getMessage(),
          "Account creation failed");

      return null;
    }
  }
}
