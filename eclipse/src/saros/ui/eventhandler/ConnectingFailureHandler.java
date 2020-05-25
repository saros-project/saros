package saros.ui.eventhandler;

import java.text.MessageFormat;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import saros.account.XMPPAccount;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectingFailureCallback;
import saros.ui.CoreMessages;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.WizardUtils;
import saros.ui.util.XMPPConnectionSupport;

/**
 * This UI handler is responsible for displaying error information to the user if a connection
 * attempt failed.
 */
public class ConnectingFailureHandler implements IConnectingFailureCallback {

  private static final Logger log = Logger.getLogger(ConnectingFailureHandler.class);

  private boolean isHandling;

  public ConnectingFailureHandler(final ConnectionHandler connectionHandler) {
    connectionHandler.setCallback(this);
  }

  @Override
  public void connectingFailed(XMPPAccount account, String errorMessage) {
    SWTUtils.runSafeSWTAsync(log, () -> handleConnectionFailed(account, errorMessage));
  }

  private void handleConnectionFailed(XMPPAccount account, String errorMessage) {
    // avoid mass dialog popups
    if (isHandling) return;

    try {
      isHandling = true;

      boolean editAccountAndConnectAgain =
          MessageDialog.openQuestion(
              SWTUtils.getShell(),
              CoreMessages.ConnectingFailureHandler_title,
              MessageFormat.format(
                  Messages.ConnectingFailureHandler_ask_retry_error_message, errorMessage));
      if (!editAccountAndConnectAgain) return;

      if (WizardUtils.openEditXMPPAccountWizard(account) == null) return;

      XMPPConnectionSupport.getInstance().connect(account, false);
    } finally {
      isHandling = false;
    }
  }
}
