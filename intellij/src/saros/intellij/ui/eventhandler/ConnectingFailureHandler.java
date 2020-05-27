package saros.intellij.ui.eventhandler;

import saros.communication.connection.ConnectionHandler;
import saros.intellij.ui.util.NotificationPanel;
import saros.ui.CoreMessages;

/** Callback handler informing the user of a failed XMPP connection attempt. */
public class ConnectingFailureHandler {

  public ConnectingFailureHandler(ConnectionHandler connectionHandler) {
    connectionHandler.setCallback(
        (account, errorMessage) -> {
          // TODO offer user possibility of adjusting settings and re-connect
          NotificationPanel.showError(errorMessage, CoreMessages.ConnectingFailureHandler_title);
        });
  }
}
