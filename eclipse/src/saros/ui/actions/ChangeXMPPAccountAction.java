package saros.ui.actions;

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
import saros.SarosPluginContext;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.net.ConnectionState;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSessionManager;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.WizardUtils;
import saros.ui.util.XMPPConnectionSupport;

/**
 * In addition to the connect/disconnect action, this allows the user to switch between accounts. At
 * the moment, it is implemented by a drop-down in the RosterView.
 */
public class ChangeXMPPAccountAction extends Action implements IMenuCreator, Disposable {

  public static final String ACTION_ID = ChangeXMPPAccountAction.class.getName();

  private static final Logger LOG = Logger.getLogger(ChangeXMPPAccountAction.class);

  private Menu accountMenu;

  @Inject private XMPPAccountStore accountService;

  @Inject private ConnectionHandler connectionHandler;

  @Inject private ISarosSessionManager sarosSessionManager;

  private boolean isConnectionError;

  private final IConnectionStateListener connectionStateListener =
      new IConnectionStateListener() {
        @Override
        public void connectionStateChanged(final ConnectionState state, final Exception error) {
          SWTUtils.runSafeSWTAsync(
              LOG,
              new Runnable() {

                @Override
                public void run() {
                  updateStatus(state);
                }
              });
        }
      };

  public ChangeXMPPAccountAction() {
    SarosPluginContext.initComponent(this);

    setText(Messages.ChangeXMPPAccountAction_connect);
    setId(ACTION_ID);

    connectionHandler.addConnectionStateListener(connectionStateListener);
    setMenuCreator(this);
    updateStatus(connectionHandler.getConnectionState());
  }

  @Override
  public void run() {

    if (connectionHandler.isConnected()) {
      XMPPConnectionSupport.getInstance().disconnect();
    } else {
      XMPPConnectionSupport.getInstance()
          .connect(
              accountService.isEmpty() ? null : accountService.getActiveAccount(), true, false);
    }
  }

  @Override
  public void dispose() {
    connectionHandler.removeConnectionStateListener(connectionStateListener);
  }

  @Override
  public Menu getMenu(Menu parent) {
    return null;
  }

  @Override
  public Menu getMenu(Control parent) {
    accountMenu = new Menu(parent);

    XMPPAccount activeAccount = null;

    if (connectionHandler.isConnected()) activeAccount = accountService.getActiveAccount();

    for (XMPPAccount account : accountService.getAllAccounts()) {
      if (!account.equals(activeAccount)) addMenuItem(account);
    }

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
            XMPPConnectionSupport.getInstance().connect(account, true, false);
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
      LOG.error("Internal error in ChangeXMPPAccountAction:", e);
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
      LOG.debug("Could execute command", e);
    }
  }
}
