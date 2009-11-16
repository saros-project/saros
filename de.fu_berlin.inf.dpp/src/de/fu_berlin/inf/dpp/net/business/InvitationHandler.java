package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.InvitationInfo;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.InvitationInfo.InvitationExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Business Logic for handling Invitation requests
 */
@Component(module = "net")
public class InvitationHandler {

    private static final Logger log = Logger.getLogger(InvitationHandler.class
        .getName());

    @Inject
    protected XMPPTransmitter transmitter;

    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected CancelInviteExtension cancelInviteExtension;

    @Inject
    protected SarosUI sarosUI;

    protected final SessionIDObservable sessionIDObservable;

    public InvitationHandler(XMPPReceiver receiver,
        SessionIDObservable sessionIDObservablePar,
        final InvitationExtensionProvider invExtProv) {
        this.sessionIDObservable = sessionIDObservablePar;
        receiver.addPacketListener(new PacketListener() {

            public void processPacket(Packet packet) {
                JID fromJID = new JID(packet.getFrom());
                InvitationInfo invInfo = invExtProv.getPayload(packet);

                if (invInfo == null) {
                    log.warn("Inv" + Util.prefix(fromJID)
                        + ": The received invitation packet's"
                        + " payload is null.");
                    return;
                }

                log
                    .debug("Inv" + Util.prefix(fromJID)
                        + ": Received invitation (invitationID: "
                        + invInfo.invitationID + ", sessionID: "
                        + invInfo.sessionID + ", projectName: "
                        + invInfo.projectName + ", colorID: " + invInfo.colorID
                        + ", sarosVersion: " + invInfo.versionInfo.version
                        + ", sarosComp: " + invInfo.versionInfo.compatibility
                        + ")");

                String sessionID = invInfo.sessionID;
                String invitationID = invInfo.invitationID;

                if (sessionIDObservable.getValue().equals(
                    SessionIDObservable.NOT_IN_SESSION)) {
                    sessionManager.invitationReceived(
                        new JID(packet.getFrom()), sessionID,
                        invInfo.projectName, invInfo.projectDesc,
                        invInfo.colorID, invInfo.versionInfo,
                        invInfo.sessionStart, sarosUI, invitationID);
                } else {
                    transmitter.sendMessageToUser(new JID(packet
                        .getFrom()), cancelInviteExtension.create(sessionID,
                        "I am already in a Saros-Session,"
                            + "try to contact me by chat first."));
                }
            }
        }, invExtProv.getPacketFilter());
    }
}