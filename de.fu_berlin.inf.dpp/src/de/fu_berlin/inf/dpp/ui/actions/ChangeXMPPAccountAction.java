package de.fu_berlin.inf.dpp.ui.actions;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.jivesoftware.smack.Connection;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.util.Utils;

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
        public void connectionStateChanged(Connection connection,
            ConnectionState newState) {
            updateStatus();
        }
    };

    public ChangeXMPPAccountAction() {
        SarosPluginContext.initComponent(this);
        this.setText(Messages.ChangeXMPPAccountAction_connect);
        saros.getSarosNet().addListener(connectionListener);
        setMenuCreator(this);
        updateStatus();
    }

    protected final AtomicBoolean running = new AtomicBoolean();

    // user clicks on Button
    @Override
    public void run() {

        Utils.runSafeAsync("ConnectDisconnectAction-", log, new Runnable() { //$NON-NLS-1$
            public void run() {
                try {
                    if (running.getAndSet(true)) {
                        log.info("User clicked too fast, running already a connect or disconnect."); //$NON-NLS-1$
                        return;
                    }
                    runConnectDisconnect();
                } finally {
                    running.set(false);
                }
            }
        });
    }

    protected void runConnectDisconnect() {
        try {
            if (saros.getSarosNet().isConnected()) {
                saros.getSarosNet().disconnect();
            } else {
                log.debug("Connect!!!"); //$NON-NLS-1$
                saros.connect(false);
            }

        } catch (RuntimeException e) {
            log.error("Internal error in ConnectDisconnectAction:", e); //$NON-NLS-1$
        }
    }

    protected void disconnect() {
        saros.getSarosNet().disconnect();
    }

    public Menu getMenu(Control parent) {
        accountMenu = new Menu(parent);
        for (XMPPAccount account : accountService.getAllAccounts()) {
            this.currentAccountId = account.getId();
            addMenuItem(account.toString());
        }
        new MenuItem(accountMenu, SWT.SEPARATOR);
        addActionToMenu(accountMenu, new Action(Messages.ChangeXMPPAccountAction_add_account) {
            @Override
            public void run() {
                WizardUtils.openAddXMPPAccountWizard();
            }
        });
        addActionToMenu(accountMenu, new Action(Messages.ChangeXMPPAccountAction_configure_account) {
            @Override
            public void run() {
                IHandlerService service = (IHandlerService) PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .getActivePart().getSite()
                    .getService(IHandlerService.class);
                try {
                    service
                        .executeCommand(
                            "de.fu_berlin.inf.dpp.ui.commands.OpenSarosPreferences", //$NON-NLS-1$
                            null);
                } catch (Exception e) {
                    log.debug("Could execute command", e); //$NON-NLS-1$
                }
            }
        });
        return accountMenu;
    }

    private void addMenuItem(String account) {
        // The additional @ is needed because @ has special meaning in
        // Action#setText(), see JavaDoc of Action().
        if (account.contains("@")) //$NON-NLS-1$
            account = account + "@"; //$NON-NLS-1$
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
        accountService.saveAccounts();
        Utils.runSafeAsync("ChangeXMPPAccountAction-", log, new Runnable() { //$NON-NLS-1$
            public void run() {
                reconnect();
            }
        });
    }

    protected void reconnect() {
        if (saros.getSarosNet().isConnected()) {
            saros.getSarosNet().disconnect();
        }
        saros.connect(false);
    }

    protected void addActionToMenu(Menu parent, Action action) {
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    protected void updateStatus() {
        try {
            ConnectionState state = saros.getSarosNet().getConnectionState();
            switch (state) {
            case CONNECTED:
                setText(Messages.ChangeXMPPAccountAction_disconnect);
                setImageDescriptor(ImageManager
                    .getImageDescriptor("/icons/elcl16/connect.png")); //$NON-NLS-1$
                break;
            case CONNECTING:
                setText(Messages.ChangeXMPPAccountAction_connecting);
                setImageDescriptor(ImageManager
                    .getImageDescriptor("/icons/elcl16/connecting.png")); //$NON-NLS-1$
                break;
            case ERROR:
                setImageDescriptor(ImageManager
                    .getImageDescriptor("/icons/elcl16/conn_err.png")); //$NON-NLS-1$
                break;
            case NOT_CONNECTED:
                setText(Messages.ChangeXMPPAccountAction_connect);
                setImageDescriptor(ImageManager
                    .getImageDescriptor("/icons/elcl16/disconnected.png")); //$NON-NLS-1$
                break;
            case DISCONNECTING:
            default:
                setText(Messages.ChangeXMPPAccountAction_disconnecting);
                setImageDescriptor(ImageManager
                    .getImageDescriptor("/icons/elcl16/disconnecting.png")); //$NON-NLS-1$
                break;
            }

            setEnabled(state == ConnectionState.CONNECTED
                || state == ConnectionState.NOT_CONNECTED
                || state == ConnectionState.ERROR);

        } catch (RuntimeException e) {
            log.error("Internal error in ChangeXMPPAccountAction:", e); //$NON-NLS-1$
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
