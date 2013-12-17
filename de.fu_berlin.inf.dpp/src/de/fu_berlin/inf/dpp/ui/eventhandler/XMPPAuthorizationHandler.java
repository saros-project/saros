package de.fu_berlin.inf.dpp.ui.eventhandler;

import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.subscriptionmanager.IncomingSubscriptionEvent;
import de.fu_berlin.inf.dpp.net.subscriptionmanager.SubscriptionManager;
import de.fu_berlin.inf.dpp.net.subscriptionmanager.SubscriptionManagerListener;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

/**
 * Handler for accepting or rejecting incoming XMPP subscription requests
 */
public class XMPPAuthorizationHandler {

    private static final Logger LOG = Logger
        .getLogger(XMPPAuthorizationHandler.class);

    private final SubscriptionManager subscriptionHandler;

    private final SubscriptionManagerListener subscriptionListener = new SubscriptionManagerListener() {

        @Override
        public void subscriptionReceived(final IncomingSubscriptionEvent event) {

            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    handleAuthorizationRequest(event.getContact());
                }
            });
        }
    };

    public XMPPAuthorizationHandler(
        final SubscriptionManager subscriptionHandler) {
        this.subscriptionHandler = subscriptionHandler;
        this.subscriptionHandler
            .addSubscriptionManagerListener(subscriptionListener);
    }

    private void handleAuthorizationRequest(final JID jid) {

        boolean accept = MessageDialog
            .openConfirm(
                SWTUtils.getShell(),
                Messages.SubscriptionManager_incoming_subscription_request_title,
                MessageFormat
                    .format(
                        Messages.SubscriptionManager_incoming_subscription_request_message,
                        jid.getBareJID()));

        if (accept)
            subscriptionHandler.addSubscription(jid, true);
        else
            subscriptionHandler.removeSubscription(jid);
    }
}
