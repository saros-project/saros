package saros.intellij.ui.eventhandler;

import java.text.MessageFormat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import saros.account.XMPPAccount;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectingFailureCallback;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.NotificationPanel;

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
              Messages.ConnectingFailureHandler_unknown_error_message,
              account,
              exception.getMessage()),
          Messages.ConnectingFailureHandler_title);

      return;
    }

    final String errorMessage =
        generateHumanReadableErrorMessage(account, (XMPPException) exception);

    // TODO offer user possibility of adjusting settings and re-connect
    NotificationPanel.showError(errorMessage, Messages.ConnectingFailureHandler_title);
  }

  // TODO unify XMPP-protocol specific error message generation in core
  // TODO adjust once settings are implemented
  // largely copied from eclipse/src/saros/ui/eventhandler/ConnectingFailureHandler.java
  private String generateHumanReadableErrorMessage(
      XMPPAccount account, XMPPException xmppException) {

    // as of Smack 3.3.1 this is always null for connection attempts
    // Throwable cause = e.getWrappedThrowable();

    XMPPError error = xmppException.getXMPPError();

    if (error != null && error.getCode() == 504)
      return "The XMPP server "
          + account.getDomain()
          + " could not be found. Make sure that you are connected to the internet and that you entered the domain part of your JID correctly.\n\n"
          // TODO re-add when settings are implemented
          /*+ "In case of DNS or SRV problems please try to manually configure the server address and port under the advanced settings for this account or update the hosts file of your OS.\n\n"*/
          + "Detailed error:\nSMACK: "
          + error
          + "\n" //$NON-NLS-1$ //$NON-NLS-2$
          + xmppException.getMessage();
    else if (error != null && error.getCode() == 502)
      return "Could not connect to the XMPP server "
          + account.getDomain()
          + (account.getPort() != 0 ? (":" + account.getPort()) : "")
          + ". Make sure that a XMPP service is running on the given domain / IP address and port.\n\n"
          // TODO re-add when settings are implemented
          /*+ "In case of DNS or SRV problems please try to manually configure the server address and port under the advanced settings for this account or update the hosts file of your OS.\n\n"*/
          + "Detailed error:\nSMACK: "
          + error
          + "\n" //$NON-NLS-1$ //$NON-NLS-2$
          + xmppException.getMessage();

    String message = null;

    String errorMessage = xmppException.getMessage();

    if (errorMessage != null) {
      if (errorMessage
              .toLowerCase()
              .contains("invalid-authzid") // jabber.org got it wrong ... //$NON-NLS-1$
          || errorMessage.toLowerCase().contains("not-authorized") // SASL //$NON-NLS-1$
          || errorMessage.toLowerCase().contains("403") // non SASL //$NON-NLS-1$
          || errorMessage.toLowerCase().contains("401")) { // non SASL //$NON-NLS-1$

        message =
            MessageFormat.format(
                Messages.ConnectingFailureHandler_invalid_username_password_message,
                account.getUsername(),
                account.getDomain());

      } else if (errorMessage.toLowerCase().contains("503")) { // $NON-NLS-1$
        message =
            "The XMPP server only allows authentication via SASL.\n"
                // TODO replace when settings are implemented
                /*+ "Please enable SASL for the current account in the account options and try again.";*/
                + "This is currently not supported by the Saros/I client.";
      }
    }

    if (message == null) {
      message =
          MessageFormat.format(
              Messages.ConnectingFailureHandler_unknown_error_message, account, xmppException);
    }

    return message;
  }
}
