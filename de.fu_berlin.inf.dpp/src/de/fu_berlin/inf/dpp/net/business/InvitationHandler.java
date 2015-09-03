package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.picocontainer.annotations.Inject;
import org.xmlpull.v1.XmlPullParser;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationOfferingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationOfferingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.VersionExchangeExtension;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;

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
         * Adds a listener to catch and answer 13.12.6 versionInfo objects,
         * because 14.10.31 uses an incompatible version format.
         * 
         * HACK: Introduced to ensure 13.12.* compatibility, do not merge into
         * master (this is part 1, see VersionManager for part 2)
         */
        ProviderManager.getInstance().addIQProvider("payload",
            "de.fu_berlin.inf.dpp", new IQProvider() {

                @Override
                public IQ parseIQ(XmlPullParser parser) throws Exception {

                    parser.next();
                    // <major>
                    parser.next();
                    // 13
                    final String major = parser.nextText();
                    parser.next();
                    // 12
                    final String minor = parser.nextText();
                    parser.next();
                    // 6
                    final String micro = parser.nextText();
                    parser.next();
                    // "" if empty, else contains the qualifier
                    final String qualifier = parser.nextText();
                    parser.next();

                    VersionExchangeExtension versionExchangeRequest = new VersionExchangeExtension();

                    String version = major + "." + minor + "." + micro
                        + (!qualifier.isEmpty() ? ("." + qualifier) : "");
                    versionExchangeRequest.set("version", version);
                    versionExchangeRequest.set("id", String.valueOf(1));

                    return VersionExchangeExtension.PROVIDER
                        .createIQ(versionExchangeRequest);
                }

            });

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
                    log.warn("received invitation from " + fromJID
                        + " that contains malformed payload");
                    return;
                }

                String sessionID = invitation.getSessionID();
                String invitationID = invitation.getNegotiationID();
                String version = invitation.getVersion();
                String description = invitation.getDescription();

                log.info("received invitation from " + fromJID
                    + " [invitation id: " + invitationID + ", "
                    + "session id: " + sessionID + ", " + "version: " + version
                    + "]");

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
                    transmitter.sendPacketExtension(fromJID, response);

                    sessionManager.invitationReceived(fromJID, sessionID,
                        invitationID, version, description);
                } else {
                    // TODO This text should be replaced with a cancel ID
                    PacketExtension response = CancelInviteExtension.PROVIDER
                        .create(new CancelInviteExtension(invitationID,
                            "I am already in a Saros session and so cannot accept your invitation."));
                    transmitter.sendPacketExtension(fromJID, response);
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
                    log.warn("received project negotiation from " + fromJID
                        + " that contains malformed payload");
                    return;
                }

                String sessionID = projectNegotiation.getSessionID();
                String negotiationID = projectNegotiation.getNegotiationID();
                List<ProjectNegotiationData> projectInfos = projectNegotiation
                    .getProjectNegotiationData();

                if (!sessionIDObservable.getValue().equals(sessionID)) {
                    log.warn("received project negotiation from " + fromJID
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