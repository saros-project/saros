package de.fu_berlin.inf.dpp.server.net;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.net.xmpp.subscription.SubscriptionListener;
import org.apache.log4j.Logger;

/**
 * A component which automatically authorizes all incoming presence subscription requests from
 * contacts.
 */
@Component(module = "server")
public class SubscriptionAuthorizer implements SubscriptionListener {

  private static final Logger LOG = Logger.getLogger(SubscriptionAuthorizer.class);

  private SubscriptionHandler subscriptionHandler;

  /**
   * Initializes the SubscriptionAuthorizer.
   *
   * @param subscriptionHandler the subscription handler to use
   */
  public SubscriptionAuthorizer(SubscriptionHandler subscriptionHandler) {
    this.subscriptionHandler = subscriptionHandler;
    subscriptionHandler.addSubscriptionListener(this);
  }

  @Override
  public void subscriptionRequestReceived(JID jid) {
    LOG.info("Accepting presence subscription request from " + jid);
    subscriptionHandler.addSubscription(jid, true);
  }
}
