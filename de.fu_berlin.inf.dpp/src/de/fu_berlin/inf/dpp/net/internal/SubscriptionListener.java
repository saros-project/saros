package de.fu_berlin.inf.dpp.net.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.ui.IRosterTree;
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
public class SubscriptionListener implements PacketListener {

    private static Logger log = Logger.getLogger(SubscriptionListener.class);

    protected XMPPConnection connection;

    protected IRosterTree rtree;

    public SubscriptionListener(XMPPConnection conn, IRosterTree rtree) {
        this.connection = conn;
        this.rtree = rtree;
    }

    public void processPacket(final Packet packet) {

        if (!(packet instanceof Presence)) {
            // Only care for presence packages
            return;
        }

        if (packet.getFrom().equals(this.connection.getUser())) {
            // Don't care for presence packages from ourself
            return;
        }

        final Presence p = (Presence) packet;

        log.debug("Received presence packet from " + packet.getFrom());

        // Somebody subscribed to us
        if (p.getType() == Presence.Type.subscribed) {
            log.debug("User subscribed to us: " + p.getFrom());
        }

        // Received information that somebody unsubscribed from us
        if (p.getType() == Presence.Type.unsubscribed) {
            log.debug("User unsubscribed from us: " + p.getFrom());
        }

        // Request of removal of subscription
        else if (p.getType() == Presence.Type.unsubscribe) {
            log.debug("User requests to unsubscribe from us: " + p.getFrom());

            // if appropriate entry exists remove that
            RosterEntry e = connection.getRoster().getEntry(packet.getFrom());
            if (e != null) {
                try {
                    connection.getRoster().removeEntry(e);
                } catch (XMPPException e1) {
                    log.error(e1);
                }
            }
            sendPresence(Presence.Type.unsubscribed, packet.getFrom());
            informUserAboutUnsubscription(packet.getFrom());
        }

        // Request for subscription
        else if (p.getType().equals(Presence.Type.subscribe)) {
            log.debug("User requests to subscribe to us: " + p.getFrom());

            // ask user for confirmation of subscription
            if (askUserForSubscriptionConfirmation(packet.getFrom())) {

                // send message that we accept the request for
                // subscription
                sendPresence(Presence.Type.subscribed, packet.getFrom());

                // if no appropriate entry for request exists
                // create one
                RosterEntry e = connection.getRoster().getEntry(
                    packet.getFrom());
                if (e == null) {
                    try {
                        connection.getRoster().createEntry(packet.getFrom(),
                            packet.getFrom(), null);
                    } catch (XMPPException e1) {
                        log.error(e1);
                    }
                }
            } else {
                // user has rejected request
                sendPresence(Presence.Type.unsubscribe, packet.getFrom());
            }
        }

        connection.getRoster().reload();
        this.rtree.refreshRosterTree(true);

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
}
