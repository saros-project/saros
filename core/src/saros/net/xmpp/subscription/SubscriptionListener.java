package saros.net.xmpp.subscription;

import saros.net.xmpp.JID;

/** Listener for {@link SubscriptionHandler} events */
public interface SubscriptionListener {
  /**
   * Gets called whenever an incoming subscription request is received.
   *
   * @param jid the {@linkplain JID} of the contact who requested subscription
   */
  public default void subscriptionRequestReceived(JID jid) {
    // NOP
  }

  /**
   * Gets called whenever an subscription cancel was received.
   *
   * @param jid the {@linkplain JID} of the contact who canceled the subscription
   */
  public default void subscriptionCanceled(JID jid) {
    // NOP
  }
}
