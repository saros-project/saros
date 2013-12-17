package de.fu_berlin.inf.dpp.net.subscriptionmanager;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Event for incoming subscriptions
 * 
 * @author bkahlert
 */
public class IncomingSubscriptionEvent {
    /**
     * The contact who sent the subscription request
     */
    private JID contact;

    public IncomingSubscriptionEvent(JID contact) {
        super();
        this.contact = contact;
    }

    /**
     * Gets the contact who send the subscription request.
     */
    public JID getContact() {
        return contact;
    }
}
