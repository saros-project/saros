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
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.util.Utils;

@Component(module = "net")
public class CancelInviteHandler {

    private static final Logger log = Logger
        .getLogger(CancelInviteHandler.class.getName());

    private ISarosSessionManager sessionManager;

    private SessionIDObservable sessionIDObservable;
    private ProjectNegotiationObservable projectExchangeProcesses;
    private InvitationProcessObservable invitationProcesses;

    private XMPPReceiver receiver;
    private CancelInviteExtension.Provider provider;

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
            receiver.addPacketListener(cancelInvitationExtensionListener,
                provider.getPacketFilter(sessionIDObservable.getValue()));
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            receiver.removePacketListener(cancelInvitationExtensionListener);
        }
    };

    private PacketListener cancelInvitationExtensionListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            CancelInviteExtension extension = provider.getPayload(packet);
            invitationCanceled(new JID(packet.getFrom()),
                extension.getErrorMessage());
        }
    };

    public CancelInviteHandler(XMPPReceiver receiver,
        CancelInviteExtension.Provider provider,
        ISarosSessionManager sessionManager,
        SessionIDObservable sessionIDObservable,
        ProjectNegotiationObservable projectNegotiationObservable,
        InvitationProcessObservable invitationProcessObservable) {

        this.provider = provider;
        this.receiver = receiver;

        this.sessionManager = sessionManager;
        this.sessionIDObservable = sessionIDObservable;
        this.projectExchangeProcesses = projectNegotiationObservable;
        this.invitationProcesses = invitationProcessObservable;

        this.sessionManager.addSarosSessionListener(sessionListener);
    }

    public void invitationCanceled(JID sender, String errorMsg) {
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