package saros.intellij.ui.eventhandler;

import java.text.MessageFormat;
import org.jivesoftware.smack.XMPPException;
import saros.account.XMPPAccount;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectingFailureCallback;
import saros.intellij.ui.util.NotificationPanel;
import saros.ui.CoreMessages;

/** Callback handler informing the user of a failed XMPP connection attempt. */
public class ConnectingFailureHandler {

  @SuppressWarnings("FieldCanBeLocal")
  private final IConnectingFailureCallback connectingFailureCallback = this::handleConnectionFailed;

  public ConnectingFailureHandler(ConnectionHandler connectionHandler) {
    connectionHandler.setCallback(connectingFailureCallback);
  }

  private void handleConnectionFailed(XMPPAccount account, Exception exception) {
    if (!(exception instanceof XMPPException)) {
      NotificationPanel.showError(
          MessageFormat.format(
              CoreMessages.ConnectingFailureHandler_unknown_error_message,
              account,
              exception.getMessage()),
          CoreMessages.ConnectingFailureHandler_title);

      return;
    }

    final String errorMessage =
        ConnectionHandler.generateHumanReadableErrorMessage(account, (XMPPException) exception);

    // TODO offer user possibility of adjusting settings and re-connect
    NotificationPanel.showError(errorMessage, CoreMessages.ConnectingFailureHandler_title);
  }
}
