/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

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
        connectAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent action) {
                createMenuItems();
            }
        });
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
                    connectAction.executeWithUser(account.getUsername());
                } else {
                    popupMenu.show(ConnectButton.this, 0,
                        getBounds().y + getBounds().height);
                }
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
            account.getUsername() + "@" + account.getDomain();
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
                    SafeDialogUtils
                        .showError("Account was not created.", "Error");
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
        if (userID.isEmpty()) {
            return null;
        }
        if (!userID.contains("@")) {
            SafeDialogUtils.showError("No @ found in the ID!", "Error");
            return null;
        }

        String[] fields = userID.split(USERID_SEPARATOR);
        String username = fields[0];
        String domain = fields[1];

        final String password = Messages
            .showPasswordDialog("Password", "Password");
        if (password.isEmpty()) {
            return null;
        }
        String server = SafeDialogUtils.showInputDialog(
            "XMPP server (optional, not necessary in most cases)", "",
            "Server");

        try {
            return accountStore
                .createAccount(username, password, domain, server, 0, true,
                    true);
        } catch (IllegalArgumentException e) {
            LOG.error("Error creating account", e);
            SafeDialogUtils.showError(
                "There was an error creating the account.\n Details:\n\n" + e
                    .getMessage(), "Error");
        }
        return null;
    }

    public void addActionListenerToActions(ActionListener listener) {
        disconnectAction.addActionListener(listener);
        connectAction.addActionListener(listener);
    }
}
