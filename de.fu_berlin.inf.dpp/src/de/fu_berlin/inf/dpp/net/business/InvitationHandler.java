package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationParametersExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Business Logic for handling Invitation requests
 */
@Component(module = "net")
public class InvitationHandler {

    private static final Logger log = Logger.getLogger(InvitationHandler.class
        .getName());

    @Inject
    private ITransmitter transmitter;

    @Inject
    private ISarosSessionManager sessionManager;

    @Inject
    private SarosUI sarosUI;

    private final SessionIDObservable sessionIDObservable;

    public InvitationHandler(IReceiver receiver,
        SessionIDObservable sessionIDObservablePar) {
        this.sessionIDObservable = sessionIDObservablePar;
        receiver.addPacketListener(new PacketListener() {

            @Override
            public void processPacket(Packet packet) {
                JID fromJID = new JID(packet.getFrom());
                InvitationParametersExtension invInfo = InvitationParametersExtension.PROVIDER
                    .getPayload(packet);

                if (invInfo == null) {
                    log.warn("Inv" + Utils.prefix(fromJID)
                        + ": The received invitation packet's"
                        + " payload is null.");
                    return;
                }

                log.debug("Inv" + Utils.prefix(fromJID)
                    + ": Received invitation (invitationID: "
                    + invInfo.getInvitationID() + ", sessionID: "
                    + invInfo.getSessionID() + ", colorID: " + invInfo.colorID
                    + ", sarosVersion: " + invInfo.versionInfo.version
                    + ", sarosComp: " + invInfo.versionInfo.compatibility + ")");

                String sessionID = invInfo.getSessionID();
                String invitationID = invInfo.getInvitationID();

                if (sessionIDObservable.getValue().equals(
                    SessionIDObservable.NOT_IN_SESSION)) {
                    sessionManager.invitationReceived(
                        new JID(packet.getFrom()), sessionID, invInfo.colorID,
                        invInfo.versionInfo, invInfo.sessionStart, sarosUI,
                        invitationID, invInfo.comPrefs, invInfo.description,
                        invInfo.host, invInfo.inviterColorID);
                } else {
                    PacketExtension response = CancelInviteExtension.PROVIDER
                        .create(new CancelInviteExtension(invitationID,
                            "I am already in a Saros session and so cannot accept your invitation."));
                    transmitter.sendMessageToUser(new JID(packet.getFrom()),
                        response);
                }
            }
        }, InvitationParametersExtension.PROVIDER.getPacketFilter());
    }
}