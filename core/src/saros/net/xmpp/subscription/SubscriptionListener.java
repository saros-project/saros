package saros.net.xmpp.subscription;

import saros.net.xmpp.JID;

/** Listener for {@link SubscriptionHandler} events */
public interface SubscriptionListener {
  /**
   * Gets called whenever an incoming subscription request is received.
   *
   * @param jid the {@linkplain JID} of the contact who requested subscription
   */
  public void subscriptionRequestReceived(JID jid);
}
