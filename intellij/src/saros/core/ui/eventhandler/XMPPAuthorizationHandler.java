package saros.core.ui.eventhandler;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import java.text.MessageFormat;
import org.apache.log4j.Logger;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.DialogUtils;
import saros.intellij.ui.util.UIProjectUtils;
import saros.net.xmpp.JID;
import saros.net.xmpp.subscription.SubscriptionHandler;
import saros.net.xmpp.subscription.SubscriptionListener;

/** Handler for accepting or rejecting incoming XMPP subscription requests */
public class XMPPAuthorizationHandler {

  private static final Logger LOG = Logger.getLogger(XMPPAuthorizationHandler.class);

  private final UIProjectUtils projectUtils;

  private final SubscriptionListener subscriptionListener =
      new SubscriptionListener() {

        @Override
        public void subscriptionRequestReceived(final JID jid) {

          projectUtils.runWithProject(
              project ->
                  ApplicationManager.getApplication()
                      .invokeLater(
                          new Runnable() {
                            @Override
                            public void run() {
                              handleAuthorizationRequest(project, jid);
                            }
                          }));
        }
      };
  private final SubscriptionHandler subscriptionHandler;

  public XMPPAuthorizationHandler(
      final SubscriptionHandler subscriptionHandler, UIProjectUtils projectUtils) {
    this.subscriptionHandler = subscriptionHandler;
    this.subscriptionHandler.addSubscriptionListener(subscriptionListener);

    this.projectUtils = projectUtils;
  }

  private void handleAuthorizationRequest(Project project, final JID jid) {

    boolean accept =
        DialogUtils.showConfirm(
            project,
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
