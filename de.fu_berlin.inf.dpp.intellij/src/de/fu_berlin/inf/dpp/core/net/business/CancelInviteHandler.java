/**
 *
 */
package de.fu_berlin.inf.dpp.core.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.communication.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.negotiation.SessionNegotiation;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.SessionNegotiationObservable;

/**
 * Listens for {@link CancelInviteExtension}-packets that cancel the session
 * negotiation and cancels it locally.
 */
public class CancelInviteHandler {

    private static final Logger LOG = Logger
        .getLogger(CancelInviteHandler.class);

    private final SessionNegotiationObservable sessionNegotiations;

    private final PacketListener cancelInvitationExtensionListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            CancelInviteExtension extension = CancelInviteExtension.PROVIDER
                .getPayload(packet);

            invitationCanceled(new JID(packet.getFrom()),
                extension.getNegotiationID(), extension.getErrorMessage());
        }
    };

    public CancelInviteHandler(IReceiver receiver,
        SessionNegotiationObservable sessionNegotiations) {

        this.sessionNegotiations = sessionNegotiations;

        receiver.addPacketListener(cancelInvitationExtensionListener,
            CancelInviteExtension.PROVIDER.getPacketFilter());
    }

    /**
     * Cancels the local session negotiation by calling
     * {@link SessionNegotiation#remoteCancel(String)}.
     */
    private void invitationCanceled(JID sender, String invitationID,
        String errorMsg) {

        SessionNegotiation negotiation = sessionNegotiations.get(sender,
            invitationID);

        if (negotiation == null) {
            LOG.warn("Inv["
                + sender
                + "]: Received invitation cancel message for unknown session negotiation. Ignoring...");
            return;
        }

        LOG.debug("Inv[" + sender + "]: Received invitation cancel message");

        negotiation.remoteCancel(errorMsg);
    }
}