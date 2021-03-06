package saros.ui.handlers.toolbar;

import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import saros.SarosPluginContext;
import saros.account.IAccountStoreListener;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.net.ConnectionState;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.XMPPConnectionSupport;

/**
 * In addition to the connect/disconnect action, this allows the user to switch between accounts.
 */
public class ChangeXMPPAccountHandler {

  static class AddAccountToMenuHandler {

    @Execute
    public void execute(MDirectMenuItem item) {
      defaultAccountChanged = false;

      Map<String, Object> objectData = item.getTransientData();
      Object data = objectData.get("account");

      if (data instanceof XMPPAccount) {
        XMPPAccount account = (XMPPAccount) data;
        XMPPConnectionSupport.getInstance().connect(account, false, false);
      }
    }
  }

  public static final String ID = ChangeXMPPAccountHandler.class.getName();

  private static final String PLATFORM_PLUGIN_SAROS_ECLIPSE = "platform:/plugin/saros.eclipse";

  private static final Logger log = Logger.getLogger(ChangeXMPPAccountHandler.class);

  @Inject private XMPPAccountStore accountService;

  @Inject private ConnectionHandler connectionHandler;

  private boolean isConnectionError;

  protected static boolean defaultAccountChanged;

  private final IConnectionStateListener connectionStateListener =
      (state, error) -> SWTUtils.runSafeSWTAsync(log, () -> updateStatus(state));

  private final IAccountStoreListener accountStoreListener =
      new IAccountStoreListener() {
        @Override
        public void activeAccountChanged(final XMPPAccount activeAccount) {
          defaultAccountChanged = true;
        }
      };

  private boolean isEnabled = false;

  private MToolItem connectToolItem;

  private ECommandService commandService;

  public ChangeXMPPAccountHandler() {
    SarosPluginContext.initComponent(this);
  }

  @PostConstruct
  public void postConstruct(
      EModelService service, MPart sarosView, ECommandService commandService) {
    this.commandService = commandService;

    MUIElement toolBarElement = service.find(ID, sarosView.getToolbar());
    if (toolBarElement instanceof MToolItem) {
      connectToolItem = (MToolItem) toolBarElement;
    }

    connectionHandler.addConnectionStateListener(connectionStateListener);
    updateStatus(connectionHandler.getConnectionState());

    accountService.addListener(accountStoreListener);
  }

  @Execute
  public void execute() {
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

  @PreDestroy
  public void dispose() {
    connectionHandler.removeConnectionStateListener(connectionStateListener);
    accountService.removeListener(accountStoreListener);
  }

  @CanExecute
  public boolean canExecute() {
    return isEnabled;
  }

  private void updateStatus(ConnectionState state) {
    if (connectToolItem == null) {
      return;
    }
    try {
      switch (state) {
        case CONNECTED:
          isConnectionError = false;
          connectToolItem.setLabel(Messages.ChangeXMPPAccountAction_disconnect);
          connectToolItem.setIconURI(
              PLATFORM_PLUGIN_SAROS_ECLIPSE + "/icons/elcl16/xmpp_disconnect_tsk.png");
          break;
        case CONNECTING:
          isConnectionError = false;
          connectToolItem.setLabel(Messages.ChangeXMPPAccountAction_connecting);
          connectToolItem.setIconURI(
              PLATFORM_PLUGIN_SAROS_ECLIPSE + "/icons/elcl16/xmpp_connecting_misc.png");
          break;
        case ERROR:
          isConnectionError = true;
          connectToolItem.setIconURI(
              PLATFORM_PLUGIN_SAROS_ECLIPSE + "/icons/elcl16/xmpp_connection_error_misc.png");
          break;
        case NOT_CONNECTED:
          connectToolItem.setLabel(Messages.ChangeXMPPAccountAction_connect);

          if (!isConnectionError)
            connectToolItem.setIconURI(
                PLATFORM_PLUGIN_SAROS_ECLIPSE + "/icons/elcl16/xmpp_connect_tsk.png");
          break;
        case DISCONNECTING:
        default:
          isConnectionError = false;
          connectToolItem.setLabel(Messages.ChangeXMPPAccountAction_disconnecting);
          connectToolItem.setIconURI(
              PLATFORM_PLUGIN_SAROS_ECLIPSE + "/icons/elcl16/xmpp_disconnecting_misc.png");
          break;
      }

      boolean setEnabled =
          state == ConnectionState.CONNECTED
              || state == ConnectionState.NOT_CONNECTED
              || state == ConnectionState.ERROR;

      isEnabled = setEnabled;

      connectToolItem.setEnabled(setEnabled);

    } catch (RuntimeException e) {
      log.error("Internal error in ChangeXMPPAccountAction:", e);
    }
  }

  private void openPreferences() {
    Command openPreferencesCmd =
        commandService.getCommand("saros.ui.commands.OpenSarosPreferences");
    try {
      openPreferencesCmd.executeWithChecks(new ExecutionEvent());
    } catch (Exception e) {
      log.debug("Could execute command", e);
    }
  }
}
