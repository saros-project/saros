package de.fu_berlin.inf.dpp.net.subscriptionmanager;

/**
 * Listener for {@link SubscriptionManager} events
 */
public interface SubscriptionManagerListener {
    /**
     * Gets called whenever an incoming subscription was received.
     * 
     * @param event
     */
    public void subscriptionReceived(IncomingSubscriptionEvent event);
}
