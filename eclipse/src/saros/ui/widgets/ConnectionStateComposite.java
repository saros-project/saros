package saros.ui.widgets;

import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import saros.SarosPluginContext;
import saros.account.IAccountStoreListener;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.communication.connection.IConnectionStateListener.ErrorType;
import saros.context.IContextKeyBindings.SarosVersion;
import saros.net.ConnectionState;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.Messages;
import saros.ui.util.FontUtils;
import saros.ui.util.LayoutUtils;
import saros.ui.util.SWTUtils;
import saros.ui.views.SarosView;

/**
 * The ConnectionStateComposite can be used to display the current state of the connection. It
 * basically pretty prints the {@linkplain ConnectionState connection states}.
 *
 * <p>In addition it keeps track of errors that can occur and ensures that these errors stay
 * visible.
 */
// FIXME displaying account related information is not the best decision.
public class ConnectionStateComposite extends Composite {

  private static final Logger log = Logger.getLogger(ConnectionStateComposite.class);

  private static final String CONNECTED_TOOLTIP =
      Messages.ConnectionStateComposite_tooltip_connected;

  @Inject private ConnectionHandler connectionHandler;

  @Inject private @SarosVersion String version;

  @Inject private XMPPAccountStore accountStore;

  private final CLabel stateLabel;

  private ConnectionState lastConnectionState;
  private ErrorType lastError;

  private final IAccountStoreListener accountStoreListener =
      new IAccountStoreListener() {
        @Override
        public void accountsChanged(List<XMPPAccount> currentAccounts) {
          SWTUtils.runSafeSWTAsync(log, () -> updateLabel(null, null));
        }
      };

  private final IConnectionStateListener connectionListener =
      (state, error) -> SWTUtils.runSafeSWTAsync(log, () -> updateLabel(state, error));

  public ConnectionStateComposite(Composite parent, int style) {
    super(parent, style);

    SarosPluginContext.initComponent(this);

    setLayout(LayoutUtils.createGridLayout(1, false, 10, 3, 0, 0));
    stateLabel = new CLabel(this, SWT.NONE);
    stateLabel.setLayoutData(LayoutUtils.createFillHGrabGridData());
    FontUtils.makeBold(stateLabel);

    stateLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

    stateLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

    setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

    connectionHandler.addConnectionStateListener(connectionListener);

    accountStore.addListener(accountStoreListener);

    updateLabel(connectionHandler.getConnectionState(), connectionHandler.getConnectionError());

    addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            connectionHandler.removeConnectionStateListener(connectionListener);
            accountStore.removeListener(accountStoreListener);
          }
        });
  }

  /**
   * Updates the composite label using the given connection state. If <i>state</i> is <code>null
   * </code> the label is simply updated with the information that is currently available e.g the
   * latest connection state.
   *
   * @param state the current connection state or <code>null</code>
   * @param error additional error information or <code>null</code>
   */
  private void updateLabel(ConnectionState state, ErrorType error) {
    if (isDisposed()) return;

    // do not hide the latest error
    if (lastConnectionState == ConnectionState.ERROR && state == ConnectionState.NOT_CONNECTED)
      return;

    String labelDescription = null;

    if (accountStore.isEmpty()) {
      labelDescription = Messages.ConnectionStateComposite_info_add_jabber_account;
    } else if (state != null) {
      labelDescription = getDescription(state, error);
    } else if (lastConnectionState != null) {
      labelDescription = getDescription(lastConnectionState, lastError);
    }

    if (labelDescription == null) return;

    stateLabel.setText(labelDescription);
    stateLabel.setToolTipText(
        state == ConnectionState.CONNECTED ? String.format(CONNECTED_TOOLTIP, version) : null);

    layout();

    if (state != null) {
      lastConnectionState = state;
      lastError = error;
    }
  }

  /**
   * Returns a nice string description of the given state, which can be used to be shown in labels
   * (e.g. CONNECTING becomes "Connecting...").
   */
  private String getDescription(ConnectionState state, ErrorType error) {

    switch (state) {
      case NOT_CONNECTED:
        return Messages.ConnectionStateComposite_not_connected;
      case CONNECTING:
        return Messages.ConnectionStateComposite_connecting;
      case CONNECTED:
        String id = connectionHandler.getConnectionID();

        /*
         * as we run async the return value may not be the same as described
         * in the javadoc so an error or something else may occurred in the
         * meantime
         */
        if (id == null) return Messages.ConnectionStateComposite_error_unknown;

        return id + Messages.ConnectionStateComposite_connected;
      case DISCONNECTING:
        return Messages.ConnectionStateComposite_disconnecting;
      case ERROR:
        switch (error) {
          case CONNECTION_LOST:
            return Messages.ConnectionStateComposite_error_connection_lost;
          case RESOURCE_CONFLICT:
            if (lastConnectionState == ConnectionState.CONNECTING) {
              SarosView.showNotification("XMPP Connection lost", "You are already logged in.");
            } else {
              SarosView.showNotification(
                  "XMPP Connection lost", Messages.ConnectionStateComposite_remote_login_warning);
            }

            return Messages.ConnectionStateComposite_error_resource_conflict;
          default:
        }
        // $FALL-THROUGH$
      default:
        return Messages.ConnectionStateComposite_error_unknown;
    }
  }
}
