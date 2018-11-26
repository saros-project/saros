package de.fu_berlin.inf.dpp.ui.eventhandler;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.communication.connection.IConnectingFailureCallback;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.text.MessageFormat;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;

/**
 * This UI handler is responsible for displaying error information to the user if a connection
 * attempt failed.
 */
public class ConnectingFailureHandler implements IConnectingFailureCallback {

  private static final Logger LOG = Logger.getLogger(ConnectingFailureHandler.class);

  private final ConnectionHandler connectionHandler;
  private final XMPPAccountStore accountStore;

  private boolean isHandling;

  public ConnectingFailureHandler(
      final ConnectionHandler connectionHandler, final XMPPAccountStore accountStore) {
    this.connectionHandler = connectionHandler;
    this.connectionHandler.setCallback(this);
    this.accountStore = accountStore;
  }

  @Override
  public void connectingFailed(final Exception exception) {
    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {

          @Override
          public void run() {
            handleConnectionFailed(exception);
          }
        });
  }

  private void handleConnectionFailed(Exception exception) {

    // account store is empty
    if (exception == null) {
      SWTUtils.runSafeSWTAsync(
          LOG,
          new Runnable() {

            @Override
            public void run() {
              // Wizard will perform a connection attempt if it is
              // finished
              WizardUtils.openSarosConfigurationWizard();
            }
          });
      return;
    }

    // avoid mass dialog popups
    if (isHandling) return;

    try {
      isHandling = true;

      if (!(exception instanceof XMPPException)) {

        DialogUtils.popUpFailureMessage(
            "Connecting Error",
            MessageFormat.format(
                "Could not connect to XMPP server. Unexpected error: {0}", exception.getMessage()),
            false);

        return;
      }

      if (DialogUtils.popUpYesNoQuestion(
          "Connecting Error",
          generateHumanReadableErrorMessage((XMPPException) exception),
          false)) {

        if (WizardUtils.openEditXMPPAccountWizard(accountStore.getActiveAccount()) == null) return;

        final XMPPAccount account = accountStore.getActiveAccount();

        ThreadUtils.runSafeAsync(
            LOG,
            new Runnable() {
              @Override
              public void run() {
                connectionHandler.connect(account, false);
              }
            });
      }

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
          + "Do you want to edit your current XMPP account now?"
          + "\n\nDetailed error:\nSMACK: "
          + error
          + "\n" //$NON-NLS-1$ //$NON-NLS-2$
          + e.getMessage();
    else if (error != null && error.getCode() == 502)
      return "Could not connect to the XMPP server. Make sure that a XMPP service is running on the given domain / IP address and port.\n\nIn case of DNS or SRV problems please try to manually configure the server address and port under the advanced settings for this account or update the hosts file of your OS.\n\n"
          + "Do you want to edit your current XMPP account now?"
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
                + "Do you want to edit your current XMPP account now?";
      } else if (errorMessage.toLowerCase().contains("503")) { // $NON-NLS-1$
        question =
            "The XMPP server only allows authentication via SASL.\nPlease enable SASL for the current account in the account options and try again.\n\n"
                + "Do you want to edit your current XMPP account now?";
      }
    }

    if (question == null)
      question =
          "Could not connect to XMPP server.\n\n"
              + "Do you want to edit your current XMPP account now?";

    return question;
  }
}
