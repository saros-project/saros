package de.fu_berlin.inf.dpp.net.subscriptionmanager;

import java.text.MessageFormat;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SafePacketListener;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

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

    private CopyOnWriteArrayList<SubscriptionManagerListener> subscriptionManagerListeners = new CopyOnWriteArrayList<SubscriptionManagerListener>();

    private IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState connectionSate) {

            if (connectionSate == ConnectionState.CONNECTING)
                prepareConnection(connection);
            else if (connectionSate != ConnectionState.CONNECTED)
                disposeConnection();
        }
    };

    private PacketListener packetListener = new SafePacketListener(log,
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

    /*
     * TODO the RFC states that we should send presence replies for subscribed
     * and unsubscribed presences ... check if this this handled correctly by
     * the server or we would create an infinite loop
     */
    private synchronized void processPresence(Presence presence) {

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

            /*
             * Notify listeners; if at least one set the autoSubscribe flag to
             * true, do not ask the user for confirmation
             */
            boolean autoSubscribe = notifySubscriptionReceived(new JID(
                presence.getFrom()));

            // ask user for confirmation of subscription
            if (autoSubscribe)
                handleSubscriptionRequest(presence);
            else
                askUserForSubscriptionConfirmation(presence);

            break;

        case unsubscribe:
            log.debug("contact requests to unsubscribe from us: "
                + presence.getFrom());
            acknowledgeUnsubscribingRequest(presence);
            break;
        default:
            // do nothing
        }
    }

    private synchronized void handleSubscriptionRequest(Presence from) {
        acknowledgeSubscriptionRequest(from);
        requestSubscription(from);
    }

    private synchronized void requestSubscription(Presence to) {
        RosterEntry entry = connection.getRoster().getEntry(to.getFrom());

        try {
            if (entry != null)
                sendPresence(Presence.Type.subscribe, to.getFrom());
            else
                // will send a subscribe presence
                connection.getRoster().createEntry(to.getFrom(), null, null);

        } catch (XMPPException e) {
            log.error("adding user to roster failed", e);
            return;
        } catch (IllegalStateException e) {
            log.error(
                "cannot add user to roster, not connected to a XMPP server", e);
            return;
        }
    }

    private synchronized void acknowledgeSubscriptionRequest(Presence to) {
        try {
            sendPresence(Presence.Type.subscribed, to.getFrom());
        } catch (IllegalStateException e) {
            log.error(
                "failed to send subscribe message, not connected to a XMPP server",
                e);
        }
    }

    private synchronized void acknowledgeUnsubscribingRequest(Presence presence) {
        try {
            sendPresence(Presence.Type.unsubscribed, presence.getFrom());
        } catch (IllegalStateException e) {
            log.error(
                "failed to send unsubscribed message, not connected to a XMPP server",
                e);
        }
    }

    private synchronized void sendPresence(Presence.Type type, String to) {
        Presence presence = new Presence(type);
        presence.setTo(to);
        presence.setFrom(connection.getUser());
        connection.sendPacket(presence);
    }

    // FIXME REMOVE THIS METHOD FROM THE CLASS !!! Belongs to the UI, not to the
    // logic

    private void askUserForSubscriptionConfirmation(final Presence presence) {

        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                boolean accept = MessageDialog.openConfirm(
                    EditorAPI.getShell(),
                    Messages.SubscriptionManager_incoming_buddy_request_title,
                    MessageFormat
                        .format(
                            Messages.SubscriptionManager_incoming_buddy_request_message,
                            presence.getFrom()));

                if (accept)
                    handleSubscriptionRequest(presence);
                else {
                    try {
                        sendPresence(Presence.Type.unsubscribed,
                            presence.getFrom());
                    } catch (IllegalStateException e) {
                        log.warn(
                            "failed to send unsubscribe message, not connected to a XMPP server",
                            e);
                    }
                }
            }
        });
    }

    /**
     * Adds a {@link SubscriptionManagerListener}
     * 
     * @param subscriptionManagerListener
     */
    public void addSubscriptionManagerListener(
        SubscriptionManagerListener subscriptionManagerListener) {
        this.subscriptionManagerListeners
            .addIfAbsent(subscriptionManagerListener);
    }

    /**
     * Removes a {@link SubscriptionManagerListener}
     * 
     * @param subscriptionManagerListener
     */
    public void removeSubscriptionManagerListener(
        SubscriptionManagerListener subscriptionManagerListener) {
        this.subscriptionManagerListeners.remove(subscriptionManagerListener);
    }

    /**
     * Notify all {@link SubscriptionManagerListener}s about an updated feature
     * support.
     * 
     * @return true if subscription request should automatically answered
     */
    private boolean notifySubscriptionReceived(JID jid) {
        boolean autoSubscribe = false;
        for (SubscriptionManagerListener subscriptionManagerListener : this.subscriptionManagerListeners) {
            IncomingSubscriptionEvent event = new IncomingSubscriptionEvent(jid);
            subscriptionManagerListener.subscriptionReceived(event);
            if (event.autoSubscribe)
                autoSubscribe = true;
        }
        return autoSubscribe;
    }
}
