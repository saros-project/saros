package de.fu_berlin.inf.dpp.net.internal.subscriptionManager;

import java.text.MessageFormat;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.internal.SafePacketListener;
import de.fu_berlin.inf.dpp.net.internal.subscriptionManager.events.IncomingSubscriptionEvent;
import de.fu_berlin.inf.dpp.net.internal.subscriptionManager.events.SubscriptionManagerListener;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This is class is responsible for handling XMPP subscriptions requests.
 * 
 * If a request for subscription is received (when a buddy added the local user)
 * all {@link SubscriptionManagerListener}s are notified. If at least one set
 * the {@link IncomingSubscriptionEvent#autoSubscribe} flag to true the
 * subscription request is automatically confirmed. Otherwise a dialog is shown
 * to the user asking him/her to confirm the request. If he accepts the request
 * a new entry in the {@link Roster} will be created and a subscribed-message
 * sent.
 * 
 * If a request for removal is received (when a buddy deleted the local user
 * from his or her roster or rejected a request of subscription) the
 * corresponding entry is removed from the roster.
 * 
 * @author chjacob
 * @author bkahlert
 */
@Component(module = "net")
public class SubscriptionManager {
    private static Logger log = Logger.getLogger(SubscriptionManager.class);

    private Connection connection = null;
    /**
     * It is generally possible that a smack thread iterates over this list
     * whereas the GUI thread removes its listeners when its job is done
     * parallel. Therefore we need a {@link CopyOnWriteArrayList}.
     */
    private CopyOnWriteArrayList<SubscriptionManagerListener> subscriptionManagerListeners = new CopyOnWriteArrayList<SubscriptionManagerListener>();

    private IConnectionListener connectionListener = new IConnectionListener() {
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

    private synchronized void processPresence(Presence presence) {

        if (presence.getFrom() == null)
            return;

        switch (presence.getType()) {
        case error:
            String message = MessageFormat.format("Received error "
                + "presence package from {0}, condition: {1}, message: {2}",
                presence.getFrom(), presence.getError().getCondition(),
                presence.getError().getMessage());
            log.warn(message);
            return;

        case subscribed:
            log.info("Buddy subscribed to us: " + presence.getFrom());
            break;

        case unsubscribed:
            log.info("Buddy unsubscribed from us: " + presence.getFrom());
            break;

        case subscribe:
            log.info("Buddy requests to subscribe to us: " + presence.getFrom());

            /*
             * Notify listeners; if at least one set the autoSubscribe flag to
             * true, do not ask the user for confirmation
             */
            boolean autoSubscribe = notifySubscriptionReceived(new JID(
                presence.getFrom()));

            // ask user for confirmation of subscription
            if (autoSubscribe)
                addSubscription(presence);
            else
                askUserForSubscriptionConfirmation(presence);

            break;

        case unsubscribe:
            log.info("Buddy requests to unsubscribe from us: "
                + presence.getFrom());
            removeSubscription(presence);
            informUserAboutUnsubscription(presence.getFrom());
            break;
        default:
            // do nothing
        }
    }

    private synchronized void addSubscription(Presence presence) {

        try {
            // send message that we accept the request for
            // subscription
            sendPresence(Presence.Type.subscribed, presence.getFrom());
        } catch (IllegalStateException e) {
            log.warn(
                "failed to send subscribe message, not connected to a XMPP server",
                e);
        }

        // if no appropriate entry for request exists
        // create one
        RosterEntry entry = connection.getRoster().getEntry(presence.getFrom());

        if (entry != null)
            return;

        try {
            connection.getRoster().createEntry(presence.getFrom(),
                presence.getFrom(), null);
        } catch (XMPPException e) {
            log.error("adding user to roster failed", e);
            return;
        } catch (IllegalStateException e) {
            log.error(
                "cannot add user to roster, not connected to a XMPP server", e);
            return;
        }

    }

    private synchronized void removeSubscription(Presence presence) {

        try {
            sendPresence(Presence.Type.unsubscribed, presence.getFrom());
        } catch (IllegalStateException e) {
            log.warn(
                "failed to send unsubscribed message, not connected to a XMPP server",
                e);
        }

        // if appropriate entry exists remove that
        RosterEntry entry = connection.getRoster().getEntry(presence.getFrom());

        if (entry == null)
            return;

        try {
            connection.getRoster().removeEntry(entry);
        } catch (XMPPException e) {
            log.error("removing user from roster failed", e);
            return;
        } catch (IllegalStateException e) {
            log.error(
                "cannot remove user from roster, not connected to a XMPP server",
                e);
            return;
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

        if (!Saros.isWorkbenchAvailable())
            return;

        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                // TODO Should flash dialog
                boolean accept = MessageDialog.openConfirm(
                    EditorAPI.getShell(), "Request of subscription received",
                    "The buddy " + presence.getFrom()
                        + " has requested subscription.");

                if (accept)
                    addSubscription(presence);
                else {
                    try {
                        sendPresence(Presence.Type.unsubscribe,
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

    // FIXME REMOVE THIS METHOD FROM THE CLASS !!! Belongs to the UI, not to the
    // logic
    private void informUserAboutUnsubscription(final String from) {

        if (!Saros.isWorkbenchAvailable())
            return;

        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                MessageDialog.openInformation(EditorAPI.getShell(),
                    "Removal of subscription", "Buddy " + from
                        + " has rejected your request"
                        + " of subsription or has removed"
                        + " you from her or his roster.");
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
