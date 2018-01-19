package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import com.intellij.openapi.ui.Messages;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.intellij.ui.actions.AbstractSarosAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.ConnectServerAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.DisconnectServerAction;
import de.fu_berlin.inf.dpp.intellij.ui.actions.NotImplementedAction;
import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Implementation of connect XMPP/jabber server button
 */
public class ConnectButton extends ToolbarButton {
    public static final String CONNECT_ICON_PATH = "/icons/famfamfam/connect.png";

    private static final Logger LOG = Logger.getLogger(ConnectButton.class);

    public static final String USERID_SEPARATOR = "@";

    private JPopupMenu popupMenu = new JPopupMenu();
    private JMenuItem menuItemAdd;
    private JMenuItem configure;
    private JMenuItem disconnect;

    private final AbstractSarosAction disconnectAction;
    private final ConnectServerAction connectAction;
    private final NotImplementedAction configureAccounts;

    @Inject
    private XMPPAccountStore accountStore;

    public ConnectButton() {
        super(ConnectServerAction.NAME, "Connect", CONNECT_ICON_PATH,
            "Connect to XMPP/Jabber server");
        SarosPluginContext.initComponent(this);
        disconnectAction = new DisconnectServerAction();
        connectAction = new ConnectServerAction();

        configureAccounts = new NotImplementedAction("configure accounts");

        createDisconnectMenuItem();
        createAddAccountMenuItem();
        createConfigureAccountMenuItem();
        createMenuItems();

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                if (accountStore.isEmpty()) {
                    XMPPAccount account = createNewAccount();

                    if (account != null) {
                        createMenuItems();
                        connectAction.executeWithUser(account.getUsername() +
                            USERID_SEPARATOR + account.getDomain());

                        return;
                    }
                }

                popupMenu.show(ConnectButton.this, 0,
                    getBounds().y + getBounds().height);

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
        popupMenu.add(configure);
        popupMenu.add(disconnect);
    }

    private void createAccountMenuItem(XMPPAccount account) {
        final String userName =
            account.getUsername() + USERID_SEPARATOR + account.getDomain();
        JMenuItem accountItem = createMenuItemForUser(userName);
        popupMenu.add(accountItem);
    }

    private JMenuItem createMenuItemForUser(final String userName) {
        JMenuItem accountItem = new JMenuItem(userName);
        accountItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectAction.executeWithUser(userName);
            }
        });
        return accountItem;
    }

    private void createDisconnectMenuItem() {
        disconnect = new JMenuItem("Disconnect server");
        disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectAction.execute();
            }
        });
    }

    private void createConfigureAccountMenuItem() {
        configure = new JMenuItem("Configure accounts...");
        configure.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                configureAccounts.execute();
            }
        });
    }

    private void createAddAccountMenuItem() {
        menuItemAdd = new JMenuItem("Add account...");
        menuItemAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                XMPPAccount account = createNewAccount();

                if (account == null) {
                    return;
                }

                createMenuItems();
            }
        });
    }

    /**
     * Asks for Name, Password and domain and server for a new XMPP account.
     */
    protected XMPPAccount createNewAccount() {
        final String userID = SafeDialogUtils.showInputDialog(
            "Your User-ID, e.g. user@saros-con.imp.fu-berlin.de", "", "Login");
        if (userID == null) {
            LOG.debug("Account creation canceled by user during user id"
                + " entry.");

            return null;
        }
        if (userID.isEmpty()) {
            SafeDialogUtils.showError("No user id entered.",
                "Account creation aborted");

            return null;
        }
        if (!userID.contains(USERID_SEPARATOR)) {
            SafeDialogUtils.showError("No " + USERID_SEPARATOR +
                    " found in the ID.", "Account creation aborted");

            return null;
        }

        String[] fields = userID.split(USERID_SEPARATOR, 2);

        if (fields.length < 2 || fields[1].isEmpty() ||
            fields[1].contains(USERID_SEPARATOR)) {

            SafeDialogUtils.showError("No acceptable domain entered.",
                "Account creation aborted");

            return null;
        }

        String username = fields[0];
        String domain = fields[1];

        if(username.isEmpty()){
            SafeDialogUtils.showError("No acceptable user name entered.",
                "Account creation aborted");

            return null;
        }

        final String password = Messages
            .showPasswordDialog("Password", "Password");
        if (password == null) {
            LOG.debug("Account creation canceled by user during password"
                + " entry.");

            return null;
        }
        if (password.isEmpty()) {
            SafeDialogUtils.showError("No password entered.",
                "Account creation aborted");

            return null;
        }

        // TODO query port
        String server = SafeDialogUtils.showInputDialog(
            "XMPP server (optional, not necessary in most cases)", "",
            "Server");

        if (server == null) {
            LOG.debug("Account creation canceled by user during server entry.");

            return null;
        }

        try {
            return accountStore
                .createAccount(username, password, domain, server, 0, true,
                    true);
        } catch (IllegalArgumentException e) {
            LOG.error("Account creation failed", e);
            SafeDialogUtils.showError(
                "There was an error creating the account. Details:\n"
                    + e.getMessage(), "Account creation failed");
        }
        return null;
    }
}
