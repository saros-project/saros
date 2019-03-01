package saros.core.ui.eventhandler;

import com.intellij.openapi.application.ApplicationManager;
import java.text.MessageFormat;
import org.apache.log4j.Logger;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.DialogUtils;
import saros.net.xmpp.JID;
import saros.net.xmpp.subscription.SubscriptionHandler;
import saros.net.xmpp.subscription.SubscriptionListener;

/** Handler for accepting or rejecting incoming XMPP subscription requests */
public class XMPPAuthorizationHandler {

  private static final Logger LOG = Logger.getLogger(XMPPAuthorizationHandler.class);
  private final SubscriptionListener subscriptionListener =
      new SubscriptionListener() {

        @Override
        public void subscriptionRequestReceived(final JID jid) {

          ApplicationManager.getApplication()
              .invokeLater(
                  new Runnable() {
                    @Override
                    public void run() {
                      handleAuthorizationRequest(jid);
                    }
                  });
        }
      };
  private final SubscriptionHandler subscriptionHandler;

  public XMPPAuthorizationHandler(final SubscriptionHandler subscriptionHandler) {
    this.subscriptionHandler = subscriptionHandler;
    this.subscriptionHandler.addSubscriptionListener(subscriptionListener);
  }

  private void handleAuthorizationRequest(final JID jid) {

    boolean accept =
        DialogUtils.showConfirm(
            null,
            Messages.SubscriptionManager_incoming_subscription_request_title,
            MessageFormat.format(
                Messages.SubscriptionManager_incoming_subscription_request_message,
                jid.getBareJID()));
    if (accept) {
      subscriptionHandler.addSubscription(jid, true);
    } else {
      subscriptionHandler.removeSubscription(jid);
    }
  }
}
