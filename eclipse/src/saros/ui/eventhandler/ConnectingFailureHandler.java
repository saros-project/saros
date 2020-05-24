package saros.ui.eventhandler;

import java.text.MessageFormat;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jivesoftware.smack.XMPPException;
import saros.account.XMPPAccount;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectingFailureCallback;
import saros.ui.CoreMessages;
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
            CoreMessages.ConnectingFailureHandler_title,
            MessageFormat.format(
                CoreMessages.ConnectingFailureHandler_unknown_error_message,
                account,
                exception.getMessage()));
        return;
      }

      final String errorMessage =
          ConnectionHandler.generateHumanReadableErrorMessage(account, (XMPPException) exception);

      final boolean editAccountAndConnectAgain =
          MessageDialog.openQuestion(
              SWTUtils.getShell(), CoreMessages.ConnectingFailureHandler_title, errorMessage);

      if (!editAccountAndConnectAgain) return;

      if (WizardUtils.openEditXMPPAccountWizard(account) == null) return;

      XMPPConnectionSupport.getInstance().connect(account, false);

    } finally {
      isHandling = false;
    }
  }
}
