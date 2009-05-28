package de.fu_berlin.inf.dpp.net.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.ConnectionSessionListener;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This is class is responsible for handling XMPP subscriptions requests.
 * 
 * If a request for subscription is received (when a remote user added the local
 * user) a dialog is shown to the user asking him/her to confirm the request. If
 * he accepts the request a new entry in the roster will be created and a
 * subscribed-message sent.
 * 
 * If a request for removal is received (when a remote user deleted the local
 * user from his or her roster or rejected a request of subscription) the
 * corresponding entry is removed from the roster.
 * 
 * @author chjacob
 * 
 */
@Component(module = "net")
public class SubscriptionListener implements ConnectionSessionListener {

    private static Logger log = Logger.getLogger(SubscriptionListener.class);

    protected XMPPConnection connection = null;

    protected PacketListener packetListener = new SafePacketListener(log,
        new PacketListener() {

            public void processPacket(Packet packet) {

                if (!(packet instanceof Presence)) {
                    // Only care for presence packages
                    return;
                }

                if (packet.getFrom().equals(connection.getUser())) {
                    // If we receive a presence package from ourself, then we
                    // are too lazy to manage subscription events by ourself.
                    connection.getRoster().reload();
                    return;
                }

                final Presence presence = (Presence) packet;

                log.debug("Received presence packet from: "
                    + presence.getFrom() + " " + presence);

                switch (presence.getType()) {
                case available:
                case unavailable:
                    // Don't care for these presence infos
                    return;
                }

                processPresence(presence);
            }
        });

    public void processPresence(Presence presence) {

        switch (presence.getType()) {
        case error:
            log.warn("Received error presence package - condition: "
                + presence.getError().getCondition() + " message: "
                + presence.getError().getMessage());
            return;

        case subscribed:
            log.debug("User subscribed to us: " + presence.getFrom());
            break;

        case unsubscribed:
            log.debug("User unsubscribed from us: " + presence.getFrom());
            break;

        case subscribe:
            log.debug("User requests to subscribe to" + " us: "
                + presence.getFrom());

            // ask user for confirmation of subscription
            if (askUserForSubscriptionConfirmation(presence.getFrom())) {

                // send message that we accept the request for
                // subscription
                sendPresence(Presence.Type.subscribed, presence.getFrom());

                // if no appropriate entry for request exists
                // create one
                RosterEntry e = connection.getRoster().getEntry(
                    presence.getFrom());
                if (e == null) {
                    try {
                        connection.getRoster().createEntry(presence.getFrom(),
                            presence.getFrom(), null);
                    } catch (XMPPException e1) {
                        log.error(e1);
                    }
                }
            } else {
                // user has rejected request
                sendPresence(Presence.Type.unsubscribe, presence.getFrom());
            }
            break;

        case unsubscribe:
            log.debug("User requests to unsubscribe from us: "
                + presence.getFrom());
            // if appropriate entry exists remove that
            RosterEntry e = connection.getRoster().getEntry(presence.getFrom());
            if (e != null) {
                try {
                    connection.getRoster().removeEntry(e);
                } catch (XMPPException e1) {
                    log.error(e1);
                }
            }
            sendPresence(Presence.Type.unsubscribed, presence.getFrom());
            informUserAboutUnsubscription(presence.getFrom());
            break;
        }

        connection.getRoster().reload();
    }

    protected void sendPresence(Presence.Type type, String to) {
        Presence presence = new Presence(type);
        presence.setTo(to);
        presence.setFrom(connection.getUser());
        connection.sendPacket(presence);
    }

    protected static boolean askUserForSubscriptionConfirmation(
        final String from) {
        final AtomicReference<Boolean> result = new AtomicReference<Boolean>();
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                // TODO Should flash dialog
                result.set(MessageDialog.openConfirm(EditorAPI.getShell(),
                    "Request of subscription received", "The User " + from
                        + " has requested subscription."));
            }
        });
        return result.get();
    }

    protected static void informUserAboutUnsubscription(final String from) {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                MessageDialog.openInformation(EditorAPI.getShell(),
                    "Removal of subscription", "User " + from
                        + " has rejected your request"
                        + " of subsription or has removed"
                        + " you from her or his roster.");
            }
        });
    }

    public void disposeConnection() {
        if (connection != null) {
            connection.removePacketListener(packetListener);
            connection = null;
        }
    }

    public void prepareConnection(XMPPConnection connection) {
        this.connection = connection;
        connection.addPacketListener(packetListener, new PacketTypeFilter(
            Presence.class));
    }

    public void startConnection() {
        // ignore
    }

    public void stopConnection() {
        // ignore
    }
}
