package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.exceptions.IllegalAWTContextException;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.actions.AbstractSarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.ConnectServerAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.DisconnectServerAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.NotImplementedAction;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.annotations.Inject;

/** Implementation of connect XMPP/jabber server button */
public class ConnectButton extends ToolbarButton {
  private static final Logger LOG = Logger.getLogger(ConnectButton.class);

  public static final String USERID_SEPARATOR = "@";

  private static final boolean ENABLE_CONFIGURE_ACCOUNTS =
      Boolean.getBoolean("saros.intellij.ENABLE_CONFIGURE_ACCOUNTS");

  private JPopupMenu popupMenu = new JPopupMenu();
  private JMenuItem menuItemAdd;
  private JMenuItem configure;
  private JMenuItem disconnect;

  private final AbstractSarosAction disconnectAction;
  private final ConnectServerAction connectAction;
  private final NotImplementedAction configureAccounts;

  @Inject private XMPPAccountStore accountStore;

  public ConnectButton() {
    super(ConnectServerAction.NAME, "Connect", IconManager.CONNECT_ICON);
    SarosPluginContext.initComponent(this);
    disconnectAction = new DisconnectServerAction();
    connectAction = new ConnectServerAction();

    configureAccounts = new NotImplementedAction("configure accounts");

    createDisconnectMenuItem();
    createAddAccountMenuItem();
    createConfigureAccountMenuItem();
    createMenuItems();

    addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent ev) {
            if (accountStore.isEmpty()) {
              XMPPAccount account = createNewAccount();

              if (account != null) {
                createMenuItems();

                askToConnectToAccount(account);

                return;
              }
            }

            popupMenu.show(ConnectButton.this, 0, getBounds().y + getBounds().height);
          }
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
    final String userName = account.getUsername() + USERID_SEPARATOR + account.getDomain();
    JMenuItem accountItem = createMenuItemForUser(userName);
    popupMenu.add(accountItem);
  }

  private JMenuItem createMenuItemForUser(final String userName) {
    JMenuItem accountItem = new JMenuItem(userName);
    accountItem.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            connectAction.executeWithUser(userName);
          }
        });
    return accountItem;
  }

  private void createDisconnectMenuItem() {
    disconnect = new JMenuItem("Disconnect server");
    disconnect.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            disconnectAction.execute();
          }
        });
  }

  private void createConfigureAccountMenuItem() {
    configure = new JMenuItem("Configure accounts...");
    configure.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent actionEvent) {
            configureAccounts.execute();
          }
        });
  }

  private void createAddAccountMenuItem() {
    menuItemAdd = new JMenuItem("Add account...");
    menuItemAdd.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            XMPPAccount account = createNewAccount();

            if (account == null) {
              return;
            }

            createMenuItems();

            askToConnectToAccount(account);
          }
        });
  }

  private void askToConnectToAccount(@NotNull XMPPAccount account) {
    try {
      Integer option =
          SafeDialogUtils.showYesNoDialog(
              Messages.ConnectButton_connect_to_new_account_message,
              Messages.ConnectButton_connect_to_new_account_title);

      if (option == com.intellij.openapi.ui.Messages.YES) {
        connectAction.executeWithUser(
            account.getUsername() + USERID_SEPARATOR + account.getDomain());
      }

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      SafeDialogUtils.showError(
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
              "Your User-ID, e.g. user@saros-con.imp.fu-berlin.de", "", "Login");

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      SafeDialogUtils.showError(
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
      SafeDialogUtils.showError("Entered user id is not valid.", "Account creation aborted");

      LOG.debug("Account creation failed as the user did not provide a " + "valid user id.");

      return null;
    }

    String username = jid.getName();
    String domain = jid.getDomain();

    final String password;

    try {
      password = SafeDialogUtils.showPasswordDialog("Password", "Password");

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      SafeDialogUtils.showError(
          "There was an error creating the account.\nDetails: " + e.getMessage(),
          "Account creation failed");

      return null;
    }

    if (password == null) {
      LOG.debug("Account creation canceled by user during password" + " entry.");

      return null;

    } else if (password.isEmpty()) {
      SafeDialogUtils.showError("No password entered.", "Account creation aborted");

      LOG.debug("Account creation failed as the user did not provide a " + "valid password.");

      return null;
    }

    int port = 0;

    String server;

    try {
      server =
          SafeDialogUtils.showInputDialog(
              "XMPP server (optional, not necessary in most cases)", "", "Server");

    } catch (IllegalAWTContextException e) {
      LOG.error("Account creation failed.", e);

      SafeDialogUtils.showError(
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
        portUserEntry = SafeDialogUtils.showInputDialog("XMPP server port", "", "Server port");

      } catch (IllegalAWTContextException e) {
        LOG.error("Account creation failed.", e);

        SafeDialogUtils.showError(
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

        SafeDialogUtils.showError("No valid server port " + "entered.", "Account creation aborted");

        LOG.debug("Account creation failed as the user did not " + "provide a valid server port.");

        return null;
      }
    }

    try {
      return accountStore.createAccount(username, password, domain, server, port, true, true);

    } catch (IllegalArgumentException e) {
      LOG.error("Account creation failed", e);

      SafeDialogUtils.showError(
          "There was an error creating the account.\nDetails: " + e.getMessage(),
          "Account creation failed");

      return null;
    }
  }
}
