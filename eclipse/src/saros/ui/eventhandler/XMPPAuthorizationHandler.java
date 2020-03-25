package saros.ui.eventhandler;

import java.text.MessageFormat;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import saros.net.xmpp.JID;
import saros.net.xmpp.subscription.SubscriptionHandler;
import saros.net.xmpp.subscription.SubscriptionListener;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;

/** Handler for accepting or rejecting incoming XMPP subscription requests */
public class XMPPAuthorizationHandler {

  private static final Logger log = Logger.getLogger(XMPPAuthorizationHandler.class);

  private final SubscriptionHandler subscriptionHandler;

  private final SubscriptionListener subscriptionListener =
      new SubscriptionListener() {

        @Override
        public void subscriptionRequestReceived(final JID jid) {

          SWTUtils.runSafeSWTAsync(
              log,
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
