package saros.intellij.ui.eventhandler;

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

  public void handleConnectionFailed(XMPPAccount account, String errorMessage) {
    // TODO offer user possibility of adjusting settings and re-connect
    NotificationPanel.showError(errorMessage, CoreMessages.ConnectingFailureHandler_title);
  }
}
