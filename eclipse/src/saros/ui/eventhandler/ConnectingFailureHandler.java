package saros.ui.eventhandler;

import java.text.MessageFormat;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import saros.account.XMPPAccount;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectingFailureCallback;
import saros.ui.util.SWTUtils;
import saros.ui.util.WizardUtils;
import saros.ui.util.XMPPConnectionSupport;

/**
 * This UI handler is responsible for displaying error information to the user if a connection
 * attempt failed.
 */
public class ConnectingFailureHandler implements IConnectingFailureCallback {

  private static final Logger log = Logger.getLogger(ConnectingFailureHandler.class);

  private final ConnectionHandler connectionHandler;

  private boolean isHandling;

  public ConnectingFailureHandler(final ConnectionHandler connectionHandler) {
    this.connectionHandler = connectionHandler;
    this.connectionHandler.setCallback(this);
  }

  @Override
  public void connectingFailed(final XMPPAccount account, final Exception exception) {
    SWTUtils.runSafeSWTAsync(log, () -> handleConnectionFailed(account, exception));
  }

  private void handleConnectionFailed(XMPPAccount account, Exception exception) {

    try {

      // avoid mass dialog popups
      if (isHandling) return;

      isHandling = true;

      if (!(exception instanceof XMPPException)) {
        MessageDialog.openError(
            SWTUtils.getShell(),
            "Connecting Error",
            MessageFormat.format(
                "Could not connect to XMPP server. Unexpected error: {0}", exception.getMessage()));
        return;
      }

      final String errorMessage = generateHumanReadableErrorMessage((XMPPException) exception);

      final boolean editAccountAndConnectAgain =
          MessageDialog.openQuestion(SWTUtils.getShell(), "Connecting Error", errorMessage);

      if (!editAccountAndConnectAgain) return;

      if (WizardUtils.openEditXMPPAccountWizard(account) == null) return;

      XMPPConnectionSupport.getInstance().connect(account, false);

    } finally {
      isHandling = false;
    }
  }

  private String generateHumanReadableErrorMessage(XMPPException e) {

    // as of Smack 3.3.1 this is always null for connection attemps
    // Throwable cause = e.getWrappedThrowable();

    XMPPError error = e.getXMPPError();

    if (error != null && error.getCode() == 504)
      return "The XMPP server could not be found. Make sure that you entered the domain part of your JID correctly.\n\nIn case of DNS or SRV problems please try to manually configure the server address and port under the advanced settings for this account or update the hosts file of your OS.\n\n"
          + "Do you want to edit your current XMPP account now and try to connect again?"
          + "\n\nDetailed error:\nSMACK: "
          + error
          + "\n" //$NON-NLS-1$ //$NON-NLS-2$
          + e.getMessage();
    else if (error != null && error.getCode() == 502)
      return "Could not connect to the XMPP server. Make sure that a XMPP service is running on the given domain / IP address and port.\n\nIn case of DNS or SRV problems please try to manually configure the server address and port under the advanced settings for this account or update the hosts file of your OS.\n\n"
          + "Do you want to edit your current XMPP account now and try to connect again?"
          + "\n\nDetailed error:\nSMACK: "
          + error
          + "\n" //$NON-NLS-1$ //$NON-NLS-2$
          + e.getMessage();

    String question = null;

    String errorMessage = e.getMessage();

    if (errorMessage != null) {
      if (errorMessage
              .toLowerCase()
              .contains("invalid-authzid") // jabber.org got it wrong ... //$NON-NLS-1$
          || errorMessage.toLowerCase().contains("not-authorized") // SASL //$NON-NLS-1$
          || errorMessage.toLowerCase().contains("403") // non SASL //$NON-NLS-1$
          || errorMessage.toLowerCase().contains("401")) { // non SASL //$NON-NLS-1$

        question =
            "Invalid username or password.\n\n"
                + "Do you want to edit your current XMPP account now and try to connect again?";
      } else if (errorMessage.toLowerCase().contains("503")) { // $NON-NLS-1$
        question =
            "The XMPP server only allows authentication via SASL.\nPlease enable SASL for the current account in the account options and try again.\n\n"
                + "Do you want to edit your current XMPP account now and try to connect again?";
      }
    }

    if (question == null)
      question =
          "Could not connect to XMPP server.\n\n"
              + "Do you want to edit your current XMPP account now and try to connect again?";

    return question;
  }
}
