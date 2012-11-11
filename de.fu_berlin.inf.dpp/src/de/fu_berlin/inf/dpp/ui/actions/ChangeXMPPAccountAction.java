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
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * In addition to the connect/disconnect action, this allows the user to switch
 * between accounts. At the moment, it is implemented by a drop-down in the
 * RosterView.
 */
public class ChangeXMPPAccountAction extends Action implements IMenuCreator {

    private static final Logger log = Logger
        .getLogger(ChangeXMPPAccountAction.class);

    protected Menu accountMenu;

    @Inject
    protected XMPPAccountStore accountService;

    @Inject
    protected Saros saros;

    @Inject
    protected ISarosSessionManager sarosSessionManager;

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
        if (saros.getSarosNet().isConnected())
            disconnect();
        else
            connect(accountService.isEmpty() ? null : accountService
                .getActiveAccount());
    }

    @Override
    public Menu getMenu(Control parent) {
        accountMenu = new Menu(parent);

        XMPPAccount activeAccount = null;

        if (saros.getSarosNet().isConnected())
            activeAccount = accountService.getActiveAccount();

        for (XMPPAccount account : accountService.getAllAccounts()) {
            if (!account.equals(activeAccount))
                addMenuItem(account);
        }

        new MenuItem(accountMenu, SWT.SEPARATOR);

        addActionToMenu(accountMenu, new Action(
            Messages.ChangeXMPPAccountAction_add_account) {
            @Override
            public void run() {
                WizardUtils.openAddXMPPAccountWizard();
            }
        });

        addActionToMenu(accountMenu, new Action(
            Messages.ChangeXMPPAccountAction_configure_account) {
            @Override
            public void run() {
                IHandlerService service = (IHandlerService) PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .getActivePart().getSite()
                    .getService(IHandlerService.class);
                try {
                    service
                        .executeCommand(
                            "de.fu_berlin.inf.dpp.ui.commands.OpenSarosPreferences",
                            null);
                } catch (Exception e) {
                    log.debug("Could execute command", e);
                }
            }
        });
        return accountMenu;
    }

    private void addMenuItem(final XMPPAccount account) {
        // The additional @ is needed because @ has special meaning in
        // Action#setText(), see JavaDoc of Action().

        String accountText = account.getUsername() + "@" + account.getDomain()
            + "@";
        Action action = new Action(accountText) {

            @Override
            public void run() {
                connectWithThisAccount(account);
            }
        };
        addActionToMenu(accountMenu, action);
    }

    protected void connectWithThisAccount(final XMPPAccount account) {

        if (sarosSessionManager.getSarosSession() == null) {
            connect(account);
            return;
        }

        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                boolean proceed = DialogUtils.openQuestionMessageDialog(
                    EditorAPI.getShell(),
                    "Disconnecting from the current Saros Session",
                    "Connecting with a different account will disconnect you from your current Saros session. Do you wish to continue ?");

                if (proceed)
                    connect(account);
            }
        });
    }

    protected void connect(XMPPAccount account) {
        if (account != null)
            accountService.setAccountActive(account);

        Utils.runSafeAsync("ConnectAction-", log, new Runnable() {
            public void run() {
                try {
                    if (running.getAndSet(true)) {
                        log.info("User clicked too fast, running already a connect or disconnect.");
                        return;
                    }
                    saros.connect(false);
                } finally {
                    running.set(false);
                }
            }
        });
    }

    protected void disconnect() {
        Utils.runSafeAsync("DisconnectAction-", log, new Runnable() {
            public void run() {
                try {
                    if (running.getAndSet(true)) {
                        log.info("User clicked too fast, running already a connect or disconnect.");
                        return;
                    }
                    saros.getSarosNet().disconnect();
                } finally {
                    running.set(false);
                }
            }
        });
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
                    .getImageDescriptor("/icons/elcl16/connect.png"));
                break;
            case CONNECTING:
                setText(Messages.ChangeXMPPAccountAction_connecting);
                setImageDescriptor(ImageManager
                    .getImageDescriptor("/icons/elcl16/connecting.png"));
                break;
            case ERROR:
                setImageDescriptor(ImageManager
                    .getImageDescriptor("/icons/elcl16/conn_err.png"));
                break;
            case NOT_CONNECTED:
                setText(Messages.ChangeXMPPAccountAction_connect);
                setImageDescriptor(ImageManager
                    .getImageDescriptor("/icons/elcl16/disconnected.png"));
                break;
            case DISCONNECTING:
            default:
                setText(Messages.ChangeXMPPAccountAction_disconnecting);
                setImageDescriptor(ImageManager
                    .getImageDescriptor("/icons/elcl16/disconnecting.png"));
                break;
            }

            setEnabled(state == ConnectionState.CONNECTED
                || state == ConnectionState.NOT_CONNECTED
                || state == ConnectionState.ERROR);

        } catch (RuntimeException e) {
            log.error("Internal error in ChangeXMPPAccountAction:", e);
        }
    }

    @Override
    public void dispose() {
        // NOP
    }

    @Override
    public Menu getMenu(Menu parent) {
        return null;
    }

}
