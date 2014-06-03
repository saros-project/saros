package de.fu_berlin.inf.dpp.ui.eventhandler;

import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;

import de.fu_berlin.inf.dpp.Messages;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.communication.connection.IConnectingFailureCallback;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * This UI handler is responsible for displaying error information to the user
 * if a connection attempt failed.
 */
public class ConnectingFailureHandler implements IConnectingFailureCallback {

    private static final Logger LOG = Logger
        .getLogger(ConnectingFailureHandler.class);

    private final ConnectionHandler connectionHandler;
    private final XMPPAccountStore accountStore;

    private boolean isHandling;

    public ConnectingFailureHandler(final ConnectionHandler connectionHandler,
        final XMPPAccountStore accountStore) {
        this.connectionHandler = connectionHandler;
        this.connectionHandler.setCallback(this);
        this.accountStore = accountStore;
    }

    @Override
    public void connectingFailed(final Exception exception) {
        SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

            @Override
            public void run() {
                handleConnectionFailed(exception);
            }
        });
    }

    private void handleConnectionFailed(Exception exception) {

        // account store is empty
        if (exception == null) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

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
        if (isHandling)
            return;

        try {
            isHandling = true;

            if (!(exception instanceof XMPPException)) {

                DialogUtils.popUpFailureMessage(
                    Messages.Saros_connecting_error_title, MessageFormat
                        .format(Messages.Saros_connecting_internal_error,
                            exception.getMessage()), false);

                return;
            }

            if (DialogUtils.popUpYesNoQuestion(
                Messages.Saros_connecting_error_title,
                generateHumanReadableErrorMessage((XMPPException) exception),
                false)) {

                if (WizardUtils.openEditXMPPAccountWizard(accountStore
                    .getActiveAccount()) == null)
                    return;

                ThreadUtils.runSafeAsync(LOG, new Runnable() {
                    @Override
                    public void run() {
                        connectionHandler.connect(false);
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
            return Messages.Saros_connecting_unknown_host
                + Messages.Saros_connecting_modify_account
                + "\n\nDetailed error:\nSMACK: " + error + "\n"
                + e.getMessage();
        else if (error != null && error.getCode() == 502)
            return Messages.Saros_connecting_connect_error
                + Messages.Saros_connecting_modify_account
                + "\n\nDetailed error:\nSMACK: " + error + "\n"
                + e.getMessage();

        String question = null;

        String errorMessage = e.getMessage();

        if (errorMessage != null) {
            if (errorMessage.toLowerCase().contains("invalid-authzid") //jabber.org got it wrong ... //$NON-NLS-1$
                || errorMessage.toLowerCase().contains("not-authorized") // SASL //$NON-NLS-1$
                || errorMessage.toLowerCase().contains("403") // non SASL //$NON-NLS-1$
                || errorMessage.toLowerCase().contains("401")) { // non SASL //$NON-NLS-1$

                question = Messages.Saros_connecting_invalid_username_password
                    + Messages.Saros_connecting_modify_account;
            } else if (errorMessage.toLowerCase().contains("503")) { //$NON-NLS-1$
                question = Messages.Saros_connecting_sasl_required
                    + Messages.Saros_connecting_modify_account;
            }
        }

        if (question == null)
            question = Messages.Saros_connecting_failed
                + Messages.Saros_connecting_modify_account;

        return question;

    }
}
