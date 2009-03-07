package de.fu_berlin.inf.dpp.net.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.ui.IRosterTree;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This Class implements a manual subscription. If a request for subscription is
 * received (when a remote user added the local user) the user is asked about
 * confirmation. If he accepts the request a new entry in the roster will be
 * created and a subscribed-message sent. If a request of removal are received
 * (when a remote user deleted the local user from his or her roster or rejected
 * a request of subscription) the appropriate entry are removed from the roster.
 * 
 * @author chjacob
 * 
 */
public class SubscriptionListener implements PacketListener {

    private static Logger log = Logger.getLogger(SubscriptionListener.class);

    private final XMPPConnection connection;

    private final IRosterTree rtree;

    public SubscriptionListener(XMPPConnection conn, IRosterTree rtree) {
        this.connection = conn;
        this.rtree = rtree;
    }

    public void processPacket(final Packet packet) {

        SubscriptionListener.log.debug("Packet called. " + packet.getFrom());

        if (!packet.getFrom().equals(this.connection.getUser())) {

            if (packet instanceof Presence) {
                final Presence p = (Presence) packet;

                // subscribed
                if (p.getType() == Presence.Type.subscribed) {
                    SubscriptionListener.log.debug("subcribed from "
                        + p.getFrom());
                }

                // unsubscribed
                if (p.getType() == Presence.Type.unsubscribed) {
                    SubscriptionListener.log.debug("unsubcribed from "
                        + p.getFrom());
                }

                // Request of removal of subscription
                else if (p.getType() == Presence.Type.unsubscribe) {
                    SubscriptionListener.log.debug("unsubcribe from "
                        + p.getFrom());

                    // if appropriate entry exists remove that
                    RosterEntry e = connection.getRoster().getEntry(
                        packet.getFrom());
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

                // request of subscription
                else if (p.getType().equals(Presence.Type.subscribe)) {
                    log.debug("subscribe from " + p.getFrom());

                    // ask user for confirmation of subscription
                    if (askUserForSubscriptionConfirmation(packet.getFrom())) {

                        // send subscribed presence packet
                        sendPresence(Presence.Type.subscribed, packet.getFrom());

                        // if no appropriate entry for request exists
                        // create one
                        RosterEntry e = connection.getRoster().getEntry(
                            packet.getFrom());
                        if (e == null) {
                            try {
                                connection.getRoster().createEntry(
                                    packet.getFrom(), packet.getFrom(), null);
                            } catch (XMPPException e1) {
                                log.error(e1);
                            }
                        }
                    } else {
                        // user has rejected request
                        sendPresence(Presence.Type.unsubscribe, packet
                            .getFrom());
                    }
                }
            }
            connection.getRoster().reload();
            this.rtree.refreshRosterTree(true);
        }
    }

    private void sendPresence(Presence.Type type, String to) {
        Presence presence = new Presence(type);
        presence.setTo(to);
        presence.setFrom(connection.getUser());
        connection.sendPacket(presence);
    }

    private static boolean askUserForSubscriptionConfirmation(final String from) {
        final AtomicReference<Boolean> result = new AtomicReference<Boolean>();
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                result.set(MessageDialog.openConfirm(Display.getDefault()
                    .getActiveShell(), "Request of subscription received",
                    "The User " + from + " has requested subscription."));
            }
        });
        return result.get();
    }

    private static void informUserAboutUnsubscription(final String from) {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                MessageDialog
                    .openInformation(
                        Display.getDefault().getActiveShell(),
                        "Removal of subscription",
                        "User "
                            + from
                            + " has rejected your request of subsription or has removed you from her or his roster.");
            }
        });
    }
}
