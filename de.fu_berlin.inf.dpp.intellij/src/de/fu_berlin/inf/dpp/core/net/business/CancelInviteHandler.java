/**
 *
 */
package de.fu_berlin.inf.dpp.core.net.business;

import de.fu_berlin.inf.dpp.communication.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.invitation.SessionNegotiation;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.SessionNegotiationObservable;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/**
 * Listens for {@link CancelInviteExtension}-packets that cancel the session
 * negotiation and cancels it locally.
 */
public class CancelInviteHandler {

    private static final Logger LOG = Logger
        .getLogger(CancelInviteHandler.class);

    private final SessionNegotiationObservable invitationProcesses;

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
        SessionNegotiationObservable invitationProcessObservable) {

        this.invitationProcesses = invitationProcessObservable;

        receiver.addPacketListener(cancelInvitationExtensionListener,
            CancelInviteExtension.PROVIDER.getPacketFilter());
    }

    /**
     * Cancels the local session invitation process by calling
     * {@link SessionNegotiation#remoteCancel(String)}.
     */
    private void invitationCanceled(JID sender, String invitationID,
        String errorMsg) {

        SessionNegotiation invitationProcess = invitationProcesses
            .get(sender, invitationID);

        if (invitationProcess == null) {
            LOG.warn("Inv[" + sender
                + "]: Received invitation cancel message for unknown invitation process. Ignoring...");
            return;
        }

        LOG.debug("Inv[" + sender + "]: Received invitation cancel message");

        invitationProcess.remoteCancel(errorMsg);
    }
}