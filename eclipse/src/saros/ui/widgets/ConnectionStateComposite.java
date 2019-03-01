package saros.ui.widgets;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.packet.XMPPError;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.context.IContextKeyBindings.SarosVersion;
import saros.net.ConnectionState;
import saros.ui.Messages;
import saros.ui.util.FontUtils;
import saros.ui.util.LayoutUtils;
import saros.ui.util.SWTUtils;
import saros.ui.views.SarosView;

public class ConnectionStateComposite extends Composite {

  private static final Logger LOG = Logger.getLogger(ConnectionStateComposite.class);

  private static final String CONNECTED_TOOLTIP =
      Messages.ConnectionStateComposite_tooltip_connected;

  @Inject private ConnectionHandler connectionHandler;

  @Inject private @SarosVersion String version;

  @Inject private XMPPAccountStore accountStore;

  private final CLabel stateLabel;

  private ConnectionState lastConnectionState;

  private final IConnectionStateListener connectionListener =
      new IConnectionStateListener() {
        @Override
        public void connectionStateChanged(final ConnectionState state, final Exception error) {

          SWTUtils.runSafeSWTAsync(
              LOG,
              new Runnable() {
                @Override
                public void run() {
                  if (ConnectionStateComposite.this.isDisposed()) return;

                  updateLabel(state, error);
                }
              });
        }
      };

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

    updateLabel(connectionHandler.getConnectionState(), connectionHandler.getConnectionError());

    addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            connectionHandler.removeConnectionStateListener(connectionListener);
          }
        });
  }

  private void updateLabel(ConnectionState state, Exception error) {

    // do not hide the latest error
    if (lastConnectionState == ConnectionState.ERROR && state == ConnectionState.NOT_CONNECTED)
      return;

    stateLabel.setText(getDescription(state, error));
    stateLabel.setToolTipText(
        state == ConnectionState.CONNECTED ? String.format(CONNECTED_TOOLTIP, version) : null);

    layout();

    lastConnectionState = state;
  }

  /**
   * Returns a nice string description of the given state, which can be used to be shown in labels
   * (e.g. CONNECTING becomes "Connecting...").
   */
  private String getDescription(ConnectionState state, Exception error) {
    if (accountStore.isEmpty()) {
      return Messages.ConnectionStateComposite_info_add_jabber_account;
    }

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

        String displayText = id + Messages.ConnectionStateComposite_connected;
        return displayText;
      case DISCONNECTING:
        return Messages.ConnectionStateComposite_disconnecting;
      case ERROR:
        if (!(error instanceof XMPPException)
            || !(lastConnectionState == ConnectionState.CONNECTED
                || lastConnectionState == ConnectionState.CONNECTING))
          return Messages.ConnectionStateComposite_error_connection_lost;

        XMPPError xmppError = ((XMPPException) error).getXMPPError();

        StreamError streamError = ((XMPPException) error).getStreamError();

        // see http://xmpp.org/rfcs/rfc3921.html chapter 3

        if (lastConnectionState == ConnectionState.CONNECTED
            && (streamError == null || !"conflict".equalsIgnoreCase(streamError.getCode())))
          return Messages.ConnectionStateComposite_error_connection_lost;

        if (lastConnectionState == ConnectionState.CONNECTING
            && (xmppError == null || xmppError.getCode() != 409))
          return Messages.ConnectionStateComposite_error_connection_lost;

        if (lastConnectionState == ConnectionState.CONNECTING) {
          SarosView.showNotification("XMPP Connection lost", "You are already logged in.");
        } else {
          SarosView.showNotification(
              "XMPP Connection lost", Messages.ConnectionStateComposite_remote_login_warning);
        }

        return Messages.ConnectionStateComposite_error_resource_conflict;
      default:
        return Messages.ConnectionStateComposite_error_unknown;
    }
  }
}
