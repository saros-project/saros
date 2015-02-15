package de.fu_berlin.inf.dpp.core.net.business;

import de.fu_berlin.inf.dpp.communication.extensions.CancelProjectNegotiationExtension;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionListener;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.NullSarosSessionListener;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/**
 * Listens for {@link CancelProjectNegotiationExtension} packets that signal a
 * canceled project negotiation and cancels it locally.
 */
public class CancelProjectSharingHandler {

    private static final Logger LOG = Logger
        .getLogger(CancelProjectSharingHandler.class);

    private final ISarosSessionManager sessionManager;

    private final ProjectNegotiationObservable projectExchangeProcesses;

    private final IReceiver receiver;
    private final PacketListener cancelProjectNegotiationListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            CancelProjectNegotiationExtension extension = CancelProjectNegotiationExtension.PROVIDER
                .getPayload(packet);
            projectSharingCanceled(new JID(packet.getFrom()),
                extension.getErrorMessage());
        }
    };
    private final ISarosSessionListener sessionListener = new NullSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
            receiver.addPacketListener(cancelProjectNegotiationListener,
                CancelProjectNegotiationExtension.PROVIDER
                    .getPacketFilter(session.getID())
            );
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            receiver.removePacketListener(cancelProjectNegotiationListener);
        }
    };

    public CancelProjectSharingHandler(IReceiver receiver,
        ISarosSessionManager sessionManager,
        ProjectNegotiationObservable projectNegotiationObservable) {

        this.receiver = receiver;

        this.sessionManager = sessionManager;
        this.projectExchangeProcesses = projectNegotiationObservable;

        this.sessionManager.addSarosSessionListener(sessionListener);
    }

    private void projectSharingCanceled(JID sender, String errorMsg) {

        ProjectNegotiation process = projectExchangeProcesses
            .getProjectExchangeProcess(sender);
        if (process != null) {
            LOG.debug(
                "Inv[" + sender + "]: Received invitation cancel message");
            process.remoteCancel(errorMsg);
        } else {
            LOG.warn(
                "Inv[" + sender + "]: Received invitation cancel message for" +
                    "an invitation process that does not exist");
        }
    }
}
