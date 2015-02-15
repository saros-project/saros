package de.fu_berlin.inf.dpp.core.net.business;

import de.fu_berlin.inf.dpp.communication.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationOfferingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationOfferingExtension;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.annotations.Inject;

import java.util.List;

/**
 * Business Logic for handling incoming Session- and ProjectNegotiation requests
 */
public class InvitationHandler {

    private static final Logger LOG = Logger.getLogger(InvitationHandler.class);

    private final PacketListener invitationOfferingListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            JID fromJID = new JID(packet.getFrom());

            InvitationOfferingExtension invitation = InvitationOfferingExtension.PROVIDER
                .getPayload(packet);

            if (invitation == null) {
                LOG.warn("received invitation from " + fromJID
                    + " that contains malformed payload");
                return;
            }

            String sessionID = invitation.getSessionID();
            String invitationID = invitation.getNegotiationID();
            String version = invitation.getVersion();
            String description = invitation.getDescription();

            LOG.info("received invitation from " + fromJID + " [invitation id: "
                    + invitationID + ", " + "session id: " + sessionID + ", "
                    + "version: " + version + "]"
            );

            /**
             * @JTourBusStop 7, Invitation Process:
             *
             *               (3b) If the invited user (from now on referred
             *               to as "client") receives an invitation (and if
             *               he is not already in a running session), Saros
             *               will send an automatic response to the inviter
             *               (host). Afterwards, the control is handed over
             *               to the SessionManager.
             */
            if (sessionManager.getSarosSession() == null) {
                PacketExtension response = InvitationAcknowledgedExtension.PROVIDER
                    .create(new InvitationAcknowledgedExtension(invitationID));
                transmitter.sendPacketExtension(fromJID, response);

                sessionManager
                    .invitationReceived(fromJID, sessionID, invitationID,
                        version, description);
            } else {
                // TODO This text should be replaced with a cancel ID
                PacketExtension response = CancelInviteExtension.PROVIDER
                    .create(new CancelInviteExtension(invitationID,
                        "I am already in a Saros session and so cannot accept your invitation."));
                transmitter.sendPacketExtension(fromJID, response);
            }
        }
    };

    private final PacketListener projectOfferingListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            JID fromJID = new JID(packet.getFrom());

            ProjectNegotiationOfferingExtension projectNegotiation = ProjectNegotiationOfferingExtension.PROVIDER
                .getPayload(packet);

            if (projectNegotiation == null) {
                LOG.warn("received project negotiation from " + fromJID
                    + " that contains malformed payload");
                return;
            }

            ISarosSession session = sessionManager.getSarosSession();

            if (session == null) {
                LOG.warn("received project negotiation from " + fromJID
                    + " without being in a session.");
                return;
            }

            String sessionID = projectNegotiation.getSessionID();

            if (!session.getID().equals(sessionID)) {
                LOG.warn("received project negotiation from " + fromJID
                    + " that is not in the same session");
                return;
            }

            String negotiationID = projectNegotiation.getNegotiationID();
            List<ProjectNegotiationData> projectInfos = projectNegotiation
                .getProjectNegotiationData();

            LOG.info("received project negotiation from " + fromJID
                + " with session id: " + sessionID + " and negotiation id: "
                + negotiationID);

            sessionManager
                .incomingProjectReceived(fromJID, projectInfos, negotiationID);
        }
    };

    @Inject
    private ITransmitter transmitter;
    @Inject
    private ISarosSessionManager sessionManager;

    public InvitationHandler(IReceiver receiver) {

        receiver.addPacketListener(invitationOfferingListener,
            InvitationOfferingExtension.PROVIDER.getPacketFilter());
        receiver.addPacketListener(projectOfferingListener,
            ProjectNegotiationOfferingExtension.PROVIDER.getPacketFilter());
    }
}