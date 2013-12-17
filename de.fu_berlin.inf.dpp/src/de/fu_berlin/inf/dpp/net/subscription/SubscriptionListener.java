package de.fu_berlin.inf.dpp.net.subscription;

/**
 * Listener for {@link SubscriptionHandler} events
 */
public interface SubscriptionListener {
    /**
     * Gets called whenever an incoming subscription was received.
     * 
     * @param event
     */
    public void subscriptionReceived(IncomingSubscriptionEvent event);
}
