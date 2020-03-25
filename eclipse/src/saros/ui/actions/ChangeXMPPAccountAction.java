package saros.ui.actions;

import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import saros.SarosPluginContext;
import saros.account.IAccountStoreListener;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.net.ConnectionState;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSessionManager;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.WizardUtils;
import saros.ui.util.XMPPConnectionSupport;

/**
 * In addition to the connect/disconnect action, this allows the user to switch between accounts.
 */
public class ChangeXMPPAccountAction extends Action implements IMenuCreator, Disposable {

  public static final String ACTION_ID = ChangeXMPPAccountAction.class.getName();

  private static final Logger log = Logger.getLogger(ChangeXMPPAccountAction.class);

  private Menu accountMenu;

  @Inject private XMPPAccountStore accountService;

  @Inject private ConnectionHandler connectionHandler;

  @Inject private ISarosSessionManager sarosSessionManager;

  private boolean isConnectionError;

  private boolean defaultAccountChanged;

  private final IConnectionStateListener connectionStateListener =
      new IConnectionStateListener() {
        @Override
        public void connectionStateChanged(final ConnectionState state, final Exception error) {
          SWTUtils.runSafeSWTAsync(
              log,
              new Runnable() {

                @Override
                public void run() {
                  updateStatus(state);
                }
              });
        }
      };

  private final IAccountStoreListener accountStoreListener =
      new IAccountStoreListener() {
        @Override
        public void activeAccountChanged(final XMPPAccount activeAccount) {
          defaultAccountChanged = true;
        }
      };

  public ChangeXMPPAccountAction() {
    SarosPluginContext.initComponent(this);

    setText(Messages.ChangeXMPPAccountAction_connect);
    setId(ACTION_ID);

    connectionHandler.addConnectionStateListener(connectionStateListener);
    setMenuCreator(this);
    updateStatus(connectionHandler.getConnectionState());

    accountService.addListener(accountStoreListener);
  }

  @Override
  public void run() {

    if (connectionHandler.isConnected()) {
      XMPPConnectionSupport.getInstance().disconnect();
      return;
    }

    final XMPPAccount lastUsedAccount = XMPPConnectionSupport.getInstance().getCurrentXMPPAccount();

    final List<XMPPAccount> accounts = accountService.getAllAccounts();

    final boolean exists = accounts.indexOf(lastUsedAccount) != -1;

    final XMPPAccount defaultAccount = accountService.getDefaultAccount();

    final boolean isEmpty = accountService.isEmpty();

    if (!exists && (defaultAccount == null || isEmpty)) {
      if (!MessageDialog.openQuestion(
          SWTUtils.getShell(),
          "Default account missing",
          "A default account has not been set yet. Do you want set a default account?")) return;

      SWTUtils.runSafeSWTAsync(log, this::openPreferences);
      return;
    }

    final XMPPAccount accountToConnect;

    if (defaultAccountChanged || !exists) {
      defaultAccountChanged = false;
      accountToConnect = defaultAccount;
    } else {
      accountToConnect = lastUsedAccount;
    }

    XMPPConnectionSupport.getInstance().connect(accountToConnect, false, false);
  }

  @Override
  public void dispose() {
    connectionHandler.removeConnectionStateListener(connectionStateListener);
    accountService.removeListener(accountStoreListener);
  }

  @Override
  public Menu getMenu(Menu parent) {
    return null;
  }

  @Override
  public Menu getMenu(Control parent) {
    accountMenu = new Menu(parent);

    final List<XMPPAccount> accounts = accountService.getAllAccounts();

    final String connectionId = connectionHandler.getConnectionID();

    if (connectionHandler.isConnected() && connectionId != null) {

      final JID jid = new JID(connectionId);

      /*
       *  TODO this may filter out too much but this situation is somewhat rare (multiple accounts
       *  with same name and domain but different server
       */

      accounts.removeIf(
          a ->
              a.getUsername().equalsIgnoreCase(jid.getName())
                  && a.getDomain().equalsIgnoreCase(jid.getDomain()));
    }

    accounts.forEach(this::addMenuItem);

    new MenuItem(accountMenu, SWT.SEPARATOR);

    addActionToMenu(
        accountMenu,
        new Action(Messages.ChangeXMPPAccountAction_add_account) {
          @Override
          public void run() {
            WizardUtils.openAddXMPPAccountWizard();
          }
        });

    addActionToMenu(
        accountMenu,
        new Action(Messages.ChangeXMPPAccountAction_configure_account) {
          @Override
          public void run() {
            openPreferences();
          }
        });
    return accountMenu;
  }

  private void addMenuItem(final XMPPAccount account) {
    // The additional @ is needed because @ has special meaning in
    // Action#setText(), see JavaDoc of Action().

    String accountText = account.getUsername() + "@" + account.getDomain() + "@";
    Action action =
        new Action(accountText) {

          @Override
          public void run() {
            defaultAccountChanged = false;
            XMPPConnectionSupport.getInstance().connect(account, false, false);
          }
        };

    addActionToMenu(accountMenu, action);
  }

  private void addActionToMenu(Menu parent, Action action) {
    ActionContributionItem item = new ActionContributionItem(action);
    item.fill(parent, -1);
  }

  private void updateStatus(ConnectionState state) {
    try {
      switch (state) {
        case CONNECTED:
          isConnectionError = false;
          setText(Messages.ChangeXMPPAccountAction_disconnect);
          setImageDescriptor(
              ImageManager.getImageDescriptor("/icons/elcl16/xmpp_disconnect_tsk.png"));
          break;
        case CONNECTING:
          isConnectionError = false;
          setText(Messages.ChangeXMPPAccountAction_connecting);
          setDisabledImageDescriptor(
              ImageManager.getImageDescriptor("/icons/elcl16/xmpp_connecting_misc.png"));
          break;
        case ERROR:
          isConnectionError = true;
          setImageDescriptor(
              ImageManager.getImageDescriptor("/icons/elcl16/xmpp_connection_error_misc.png"));
          break;
        case NOT_CONNECTED:
          setText(Messages.ChangeXMPPAccountAction_connect);

          if (!isConnectionError)
            setImageDescriptor(
                ImageManager.getImageDescriptor("/icons/elcl16/xmpp_connect_tsk.png"));

          break;
        case DISCONNECTING:
        default:
          isConnectionError = false;
          setText(Messages.ChangeXMPPAccountAction_disconnecting);
          setDisabledImageDescriptor(
              ImageManager.getImageDescriptor("/icons/elcl16/xmpp_disconnecting_misc.png"));
          break;
      }

      setEnabled(
          state == ConnectionState.CONNECTED
              || state == ConnectionState.NOT_CONNECTED
              || state == ConnectionState.ERROR);

    } catch (RuntimeException e) {
      log.error("Internal error in ChangeXMPPAccountAction:", e);
    }
  }

  private void openPreferences() {
    IHandlerService service =
        PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow()
            .getActivePage()
            .getActivePart()
            .getSite()
            .getService(IHandlerService.class);
    try {
      service.executeCommand("saros.ui.commands.OpenSarosPreferences", null);
    } catch (Exception e) {
      log.debug("Could execute command", e);
    }
  }
}
