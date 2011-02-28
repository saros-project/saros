package de.fu_berlin.inf.dpp.net.internal.subscriptionManager.events;

import de.fu_berlin.inf.dpp.net.internal.subscriptionManager.SubscriptionManager;

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
