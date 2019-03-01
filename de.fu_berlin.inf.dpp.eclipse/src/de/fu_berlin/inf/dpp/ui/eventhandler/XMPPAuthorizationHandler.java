package de.fu_berlin.inf.dpp.ui.eventhandler;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.net.xmpp.subscription.SubscriptionListener;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import java.text.MessageFormat;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;

/** Handler for accepting or rejecting incoming XMPP subscription requests */
public class XMPPAuthorizationHandler {

  private static final Logger LOG = Logger.getLogger(XMPPAuthorizationHandler.class);

  private final SubscriptionHandler subscriptionHandler;

  private final SubscriptionListener subscriptionListener =
      new SubscriptionListener() {

        @Override
        public void subscriptionRequestReceived(final JID jid) {

          SWTUtils.runSafeSWTAsync(
              LOG,
              new Runnable() {
                @Override
                public void run() {
                  handleAuthorizationRequest(jid);
                }
              });
        }
      };

  public XMPPAuthorizationHandler(final SubscriptionHandler subscriptionHandler) {
    this.subscriptionHandler = subscriptionHandler;
    this.subscriptionHandler.addSubscriptionListener(subscriptionListener);
  }

  private void handleAuthorizationRequest(final JID jid) {

    boolean accept =
        MessageDialog.openConfirm(
            SWTUtils.getShell(),
            Messages.SubscriptionManager_incoming_subscription_request_title,
            MessageFormat.format(
                Messages.SubscriptionManager_incoming_subscription_request_message,
                jid.getBareJID()));

    if (accept) subscriptionHandler.addSubscription(jid, true);
    else subscriptionHandler.removeSubscription(jid);
  }
}
