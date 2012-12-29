/**
 * 
 */
package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.util.Utils;

@Component(module = "net")
public class CancelInviteHandler {

    private static final Logger log = Logger
        .getLogger(CancelInviteHandler.class.getName());

    private ProjectNegotiationObservable projectExchangeProcesses;
    private InvitationProcessObservable invitationProcesses;

    private PacketListener cancelInvitationExtensionListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            CancelInviteExtension extension = CancelInviteExtension.PROVIDER
                .getPayload(packet);

            invitationCanceled(new JID(packet.getFrom()),
                extension.getInvitationID(), extension.getErrorMessage());
        }
    };

    public CancelInviteHandler(IReceiver receiver,
        ProjectNegotiationObservable projectNegotiationObservable,
        InvitationProcessObservable invitationProcessObservable) {

        this.projectExchangeProcesses = projectNegotiationObservable;
        this.invitationProcesses = invitationProcessObservable;

        receiver.addPacketListener(cancelInvitationExtensionListener,
            CancelInviteExtension.PROVIDER.getPacketFilter());
    }

    public void invitationCanceled(JID sender, String invitationID,
        String errorMsg) {

        // FIXME: check the invitation ID !!!!
        InvitationProcess invitationProcess = invitationProcesses
            .getInvitationProcess(sender);

        ProjectNegotiation projectExchange = projectExchangeProcesses
            .getProjectExchangeProcess(sender);

        if (invitationProcess != null) {
            log.debug("Inv" + Utils.prefix(sender)
                + ": Received invitation cancel message");

            invitationProcess.remoteCancel(errorMsg);
        } else if (projectExchange != null) {
            projectExchange.remoteCancel(errorMsg);
        } else {
            log.warn("Inv[unkown buddy]: Received invitation cancel message for unknown invitation process. Ignoring...");
        }
    }
}