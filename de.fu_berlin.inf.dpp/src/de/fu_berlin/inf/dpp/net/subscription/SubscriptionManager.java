package de.fu_berlin.inf.dpp.net.subscription;

import java.text.MessageFormat;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SafePacketListener;
import de.fu_berlin.inf.dpp.net.SarosNet;

/**
 * This is class is responsible for handling XMPP subscriptions requests.
 * 
 * See also XMPP RFC 3921: http://xmpp.org/rfcs/rfc3921.html
 * 
 * @author chjacob
 * @author bkahlert
 */
@Component(module = "net")
public class SubscriptionManager {
    private static final Logger log = Logger
        .getLogger(SubscriptionManager.class);

    private Connection connection = null;

    private final CopyOnWriteArrayList<SubscriptionManagerListener> subscriptionListeners = new CopyOnWriteArrayList<SubscriptionManagerListener>();

    private final IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState connectionSate) {

            if (connectionSate == ConnectionState.CONNECTING)
                prepareConnection(connection);
            else if (connectionSate != ConnectionState.CONNECTED)
                disposeConnection();
        }
    };

    private final PacketListener packetListener = new SafePacketListener(log,
        new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                processPresence((Presence) packet);
            }
        });

    public SubscriptionManager(SarosNet sarosNet) {
        sarosNet.addListener(connectionListener);
    }

    private synchronized void prepareConnection(Connection connection) {

        disposeConnection();

        this.connection = connection;
        connection.addPacketListener(packetListener, new PacketTypeFilter(
            Presence.class));
    }

    private synchronized void disposeConnection() {
        if (connection != null)
            connection.removePacketListener(packetListener);
    }

    /**
     * Changes the subscription state for the given contact to
     * {@link Type#subscribed} allowing this contact to see the online and
     * offline status of the currently connected user.
     * 
     * @param jid
     *            the {@linkplain JID} of the contact
     * 
     * @param requestSubscription
     *            if <code>true</code> the contact a subscription request to the
     *            given contact will be made
     * 
     * @return <code>true</code> if the operation was successful or
     *         <code>false</code> if an error occurred or not connected to an
     *         XMPP server
     */
    public synchronized boolean addSubscription(JID jid,
        boolean requestSubscription) {
        if (connection == null)
            return false;

        boolean success = true;
        success &= sendSubscribedPresence(jid);

        if (requestSubscription)
            success &= requestSubscription(jid);

        return success;
    }

    /**
     * Changes the subscription state for the given contact to
     * {@link Type#unsubscribed} disallowing this contact to see the online and
     * offline status of the currently connected user. This method may also be
     * called to reject a subscription request from a contact.
     * 
     * @param jid
     *            the {@linkplain JID} of the contact
     * 
     * @return <code>true</code> if the operation was successful or
     *         <code>false</code> if an error occurred or not connected to an
     *         XMPP server
     */

    public synchronized boolean removeSubscription(JID jid) {
        if (connection == null)
            return false;

        return sendUnsubscribedPresence(jid);
    }

    /*
     * TODO the RFC states that we should send presence replies for subscribed
     * and unsubscribed presences ... check if this this handled correctly by
     * the server or we would create an infinite loop
     */
    private void processPresence(Presence presence) {

        if (presence.getFrom() == null)
            return;

        switch (presence.getType()) {
        case error:
            String message = MessageFormat.format(
                "received error presence package from "
                    + "{0}, condition: {1}, message: {2}", presence.getFrom(),
                presence.getError().getCondition(), presence.getError()
                    .getMessage());
            log.warn(message);
            return;

        case subscribed:
            log.debug("contact subscribed to us: " + presence.getFrom());
            break;

        case unsubscribed:
            log.debug("contact unsubscribed from us: " + presence.getFrom());
            break;

        case subscribe:
            log.debug("contact requests to subscribe to us: "
                + presence.getFrom());

            notifySubscriptionReceived(new JID(presence.getFrom()));
            break;

        case unsubscribe:
            log.debug("contact requests to unsubscribe from us: "
                + presence.getFrom());
            removeSubscription(new JID(presence.getFrom()));
            break;
        default:
            // do nothing
        }
    }

    private synchronized boolean requestSubscription(JID jid) {
        boolean error = false;

        RosterEntry entry = connection.getRoster().getEntry(jid.getBase());

        try {
            if (entry != null)
                sendPresence(Presence.Type.subscribe, jid.getBase());
            else
                // will send a subscribe presence
                connection.getRoster().createEntry(jid.getBase(), null, null);

        } catch (XMPPException e) {
            log.error("adding user to roster failed", e);
            error = true;
        } catch (IllegalStateException e) {
            log.error(
                "cannot add user to roster, not connected to a XMPP server", e);
            error = true;
        }

        return error;
    }

    private boolean sendSubscribedPresence(JID jid) {
        boolean error = false;

        try {
            sendPresence(Presence.Type.subscribed, jid.getBase());
        } catch (IllegalStateException e) {
            log.error(
                "failed to send subscribe message, not connected to a XMPP server",
                e);

            error = true;
        }

        return error;
    }

    private boolean sendUnsubscribedPresence(JID jid) {
        boolean error = false;

        try {
            sendPresence(Presence.Type.unsubscribed, jid.getBase());
        } catch (IllegalStateException e) {
            log.error(
                "failed to send unsubscribed message, not connected to a XMPP server",
                e);

            error = true;
        }

        return error;
    }

    private void sendPresence(Presence.Type type, String to) {
        Presence presence = new Presence(type);
        presence.setTo(to);
        presence.setFrom(connection.getUser());
        connection.sendPacket(presence);
    }

    /**
     * Adds a {@link SubscriptionManagerListener}
     * 
     * @param subscriptionManagerListener
     */
    public void addSubscriptionManagerListener(
        SubscriptionManagerListener subscriptionManagerListener) {
        subscriptionListeners.addIfAbsent(subscriptionManagerListener);
    }

    /**
     * Removes a {@link SubscriptionManagerListener}
     * 
     * @param subscriptionManagerListener
     */
    public void removeSubscriptionManagerListener(
        SubscriptionManagerListener subscriptionManagerListener) {
        subscriptionListeners.remove(subscriptionManagerListener);
    }

    /**
     * Notify all {@link SubscriptionManagerListener}s about an updated feature
     * support.
     * 
     */
    private void notifySubscriptionReceived(JID jid) {
        IncomingSubscriptionEvent event = new IncomingSubscriptionEvent(jid);

        for (SubscriptionManagerListener subscriptionManagerListener : subscriptionListeners)
            subscriptionManagerListener.subscriptionReceived(event);
    }
}
