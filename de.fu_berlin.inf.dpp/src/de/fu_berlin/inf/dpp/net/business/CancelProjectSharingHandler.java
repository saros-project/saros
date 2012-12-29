package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelProjectNegotiationExtension;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.util.Utils;

public class CancelProjectSharingHandler {

    private static Logger log = Logger
        .getLogger(CancelProjectSharingHandler.class.getName());

    private ISarosSessionManager sessionManager;

    private SessionIDObservable sessionIDObservable;
    private ProjectNegotiationObservable projectExchangeProcesses;

    private IReceiver receiver;

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
            receiver.addPacketListener(cancelProjectNegotiationListener,
                CancelProjectNegotiationExtension.PROVIDER
                    .getPacketFilter(sessionIDObservable.getValue()));
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            receiver.removePacketListener(cancelProjectNegotiationListener);
        }
    };

    private PacketListener cancelProjectNegotiationListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            CancelProjectNegotiationExtension extension = CancelProjectNegotiationExtension.PROVIDER
                .getPayload(packet);
            projectSharingCanceled(new JID(packet.getFrom()),
                extension.getErrorMessage());
        }
    };

    public CancelProjectSharingHandler(IReceiver receiver,
        ISarosSessionManager sessionManager,
        SessionIDObservable sessionIDObservable,
        ProjectNegotiationObservable projectNegotiationObservable) {

        this.receiver = receiver;

        this.sessionManager = sessionManager;
        this.sessionIDObservable = sessionIDObservable;
        this.projectExchangeProcesses = projectNegotiationObservable;

        this.sessionManager.addSarosSessionListener(sessionListener);
    }

    public void projectSharingCanceled(JID sender, String errorMsg) {

        ProjectNegotiation process = projectExchangeProcesses
            .getProjectExchangeProcess(sender);
        if (process != null) {
            log.debug("Inv" + Utils.prefix(sender)
                + ": Received invitation cancel message");
            process.remoteCancel(errorMsg);
        } else {
            log.warn("Inv[unkown user]: Received invitation cancel message");
        }
    }
}
