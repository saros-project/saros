package de.fu_berlin.inf.dpp.net.internal.subscriptionManager.events;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.subscriptionManager.SubscriptionManager;

/**
 * Event for incoming subscriptions
 * 
 * @author bkahlert
 */
public class IncomingSubscriptionEvent {
    /**
     * The buddy who sent the subscription request
     */
    protected JID buddy;

    /**
     * If set to true the throwing {@link SubscriptionManager} will
     * automatically subscribe.
     */
    public boolean autoSubscribe = false;

    public IncomingSubscriptionEvent(JID buddy) {
        super();
        this.buddy = buddy;
    }

    /**
     * Gets the buddy who send the subscription request.
     */
    public JID getBuddy() {
        return buddy;
    }
}
