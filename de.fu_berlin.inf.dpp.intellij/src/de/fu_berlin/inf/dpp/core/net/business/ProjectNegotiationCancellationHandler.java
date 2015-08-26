package de.fu_berlin.inf.dpp.core.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.communication.extensions.CancelProjectNegotiationExtension;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.NullSessionLifecycleListener;

/**
 * Listens for {@link CancelProjectNegotiationExtension} packets that signal a
 * canceled project negotiation and cancels it locally.
 */
public class ProjectNegotiationCancellationHandler {

    private static final Logger LOG = Logger
        .getLogger(ProjectNegotiationCancellationHandler.class);

    private final ISarosSessionManager sessionManager;

    private final ProjectNegotiationObservable projectNegotiations;

    private final IReceiver receiver;
    private final PacketListener cancelProjectNegotiationListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            CancelProjectNegotiationExtension extension = CancelProjectNegotiationExtension.PROVIDER
                .getPayload(packet);
            projectNegotiationCanceled(new JID(packet.getFrom()),
                extension.getErrorMessage());
        }
    };
    private final ISessionLifecycleListener sessionLifecycleListener = new NullSessionLifecycleListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
            receiver.addPacketListener(cancelProjectNegotiationListener,
                CancelProjectNegotiationExtension.PROVIDER
                    .getPacketFilter(session.getID()));
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            receiver.removePacketListener(cancelProjectNegotiationListener);
        }
    };

    public ProjectNegotiationCancellationHandler(IReceiver receiver,
        ISarosSessionManager sessionManager,
        ProjectNegotiationObservable projectNegotiationObservable) {

        this.receiver = receiver;

        this.sessionManager = sessionManager;
        this.projectNegotiations = projectNegotiationObservable;

        this.sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    }

    private void projectNegotiationCanceled(JID sender, String errorMsg) {

        ProjectNegotiation negotiation = projectNegotiations.get(sender);
        if (negotiation != null) {
            LOG.debug("PN[" + sender
                + "]: Received project negotiation cancel message");
            negotiation.remoteCancel(errorMsg);
        } else {
            LOG.warn("PN[" + sender + "]: Received cancel message for"
                + "a project negotiation that doesn't exist");
        }
    }
}
