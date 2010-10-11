package de.fu_berlin.inf.dpp.ui.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.jivesoftware.smack.XMPPConnection;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.wizards.CreateNewAccountWizard;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * In addition to the connect/disconnect action, this allows the user to switch
 * between accounts. At the moment, it is implemented by a drop-down in the
 * RosterView.
 */
public class ChangeXMPPAccountAction extends Action implements IMenuCreator {

    Menu accountMenu;
    @Inject
    XMPPAccountStore accountService;
    @Inject
    Saros saros;
    protected int currentAccountId;
    private static final Logger log = Logger
        .getLogger(ChangeXMPPAccountAction.class);

    protected IConnectionListener connectionListener = new IConnectionListener() {
        public void connectionStateChanged(XMPPConnection connection,
            ConnectionState newState) {
            updateStatus();
        }
    };

    public ChangeXMPPAccountAction() {
        Saros.reinject(this);
        this.setText("Connect");
        saros.addListener(connectionListener);
        setMenuCreator(this);
        updateStatus();
    }

    // user click on Button
    @Override
    public void run() {
        if (accountService.hasActiveAccount()) {
            runConnectDisconnect();
        } else {
            WizardDialog wizard = new WizardDialog(EditorAPI.getShell(),
                new CreateNewAccountWizard());
            if (Window.OK == wizard.open()) {
                saros.connect(false);
            }
        }
    }

    protected void runConnectDisconnect() {
        Util.runSafeAsync("ChangeXMPPAccountAction-", log, new Runnable() {
            public void run() {
                if (saros.isConnected()) {
                    disconnect();
                } else {
                    saros.connect(false);
                }
            }
        });
    }

    protected void disconnect() {
        saros.disconnect();
    }

    public Menu getMenu(Control parent) {
        accountMenu = new Menu(parent);
        List<XMPPAccount> accounts = accountService.getAllAccounts();
        for (XMPPAccount account : accounts) {
            this.currentAccountId = account.getId();
            addMenuItem(account.toString());
        }
        return accountMenu;
    }

    private void addMenuItem(String account) {
        Action action = new Action(account) {
            int id = currentAccountId;

            @Override
            public void run() {
                connectWithThisAccount(id);
            }
        };
        addActionToMenu(accountMenu, action);
    }

    protected void connectWithThisAccount(int accountID) {
        accountService.setAccountActive(accountService.getAccount(accountID));
        Util.runSafeAsync("ChangeXMPPAccountAction-", log, new Runnable() {
            public void run() {
                reconnect();
            }
        });
    }

    protected void reconnect() {
        if (saros.isConnected()) {
            saros.disconnect();
        }
        saros.connect(false);
    }

    protected void addActionToMenu(Menu parent, Action action) {
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    protected void updateStatus() {
        try {
            ConnectionState state = saros.getConnectionState();
            switch (state) {
            case CONNECTED:
                setText("Disconnect");
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/connect.png"));
                break;
            case CONNECTING:
                setText("Connecting");
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/connect.png"));
                break;
            case ERROR:
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/disconnect.png"));
                break;
            case NOT_CONNECTED:
                setText("Connect");
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/disconnect.png"));
                break;
            case DISCONNECTING:
            default:
                setText("Disconnecting");
                setImageDescriptor(SarosUI
                    .getImageDescriptor("/icons/disconnect.png"));
                break;
            }

            setEnabled(state == ConnectionState.CONNECTED
                || state == ConnectionState.NOT_CONNECTED
                || state == ConnectionState.ERROR);

        } catch (RuntimeException e) {
            log.error("Internal error in ChangeXMPPAccountAction:", e);
        }
    }

    public void dispose() {
        // Auto-generated method
    }

    public Menu getMenu(Menu parent) {
        // Auto-generated method
        return null;
    }

}
