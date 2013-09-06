package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.joda.time.DateTime;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationOfferingExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.ProjectNegotiationOfferingExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * Business Logic for handling incoming Session- and ProjectNegotiation requests
 */
@Component(module = "net")
public class InvitationHandler {

    private static final Logger log = Logger.getLogger(InvitationHandler.class
        .getName());

    @Inject
    private ITransmitter transmitter;

    @Inject
    private ISarosSessionManager sessionManager;

    private final SessionIDObservable sessionIDObservable;

    public InvitationHandler(IReceiver receiver,
        SessionIDObservable sessionIDObservablePar) {
        this.sessionIDObservable = sessionIDObservablePar;

        /**
         * Adds the packetListener that listens to incoming Session Negotiation
         * requests to the Receiver
         */
        receiver.addPacketListener(new PacketListener() {

            @Override
            public void processPacket(Packet packet) {
                JID fromJID = new JID(packet.getFrom());

                InvitationOfferingExtension invitation = InvitationOfferingExtension.PROVIDER
                    .getPayload(packet);

                if (invitation == null) {
                    log.warn("received invitation from "
                        + Utils.prefix(fromJID)
                        + " that contains malformed payload");
                    return;
                }

                String sessionID = invitation.getSessionID();
                String invitationID = invitation.getInvitationID();
                DateTime sessionStartTime = invitation.getSessionStartTime();
                VersionInfo versionInfo = invitation.getVersionInfo();
                String description = invitation.getDescription();

                log.info("received invitation from " + Utils.prefix(fromJID)
                    + " [invitation id: " + invitationID + ", "
                    + "session id: " + sessionID + ", " + "version: "
                    + versionInfo.version + ", " + "compability: "
                    + versionInfo.compatibility + "]");

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
                if (sessionIDObservable.getValue().equals(
                    SessionIDObservable.NOT_IN_SESSION)) {
                    PacketExtension response = InvitationAcknowledgedExtension.PROVIDER
                        .create(new InvitationAcknowledgedExtension(
                            invitationID));
                    transmitter.sendExtension(fromJID, response);

                    sessionManager.invitationReceived(fromJID, sessionID,
                        invitationID, sessionStartTime, versionInfo,
                        description);
                } else {
                    // TODO This text should be replaced with a cancel ID
                    PacketExtension response = CancelInviteExtension.PROVIDER
                        .create(new CancelInviteExtension(invitationID,
                            "I am already in a Saros session and so cannot accept your invitation."));
                    transmitter.sendExtension(fromJID, response);
                }
            }
        }, InvitationOfferingExtension.PROVIDER.getPacketFilter());

        /**
         * Adds the packetListener that listens to incoming Session Negotiation
         * requests to the Receiver
         */
        receiver.addPacketListener(new PacketListener() {

            @Override
            public void processPacket(Packet packet) {
                JID fromJID = new JID(packet.getFrom());

                ProjectNegotiationOfferingExtension projectNegotiation = ProjectNegotiationOfferingExtension.PROVIDER
                    .getPayload(packet);

                if (projectNegotiation == null) {
                    log.warn("received project negotiation from "
                        + Utils.prefix(fromJID)
                        + " that contains malformed payload");
                    return;
                }

                String sessionID = projectNegotiation.getSessionID();
                String negotiationID = projectNegotiation.getNegotiationID();
                List<ProjectExchangeInfo> projectInfos = projectNegotiation
                    .getProjectInfos();

                if (!sessionIDObservable.getValue().equals(sessionID)) {
                    log.warn("received project negotiation from "
                        + Utils.prefix(fromJID)
                        + " that is not in the same session");
                    return;
                }

                log.info("received project negotiation from " + fromJID
                    + " with session id: " + sessionID
                    + " and negotiation id: " + negotiationID);

                sessionManager.incomingProjectReceived(fromJID, projectInfos,
                    negotiationID);

            }

        }, ProjectNegotiationOfferingExtension.PROVIDER.getPacketFilter());
    }
}