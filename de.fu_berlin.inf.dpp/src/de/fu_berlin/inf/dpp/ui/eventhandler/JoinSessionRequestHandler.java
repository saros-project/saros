package de.fu_berlin.inf.dpp.ui.eventhandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.communication.extensions.JoinSessionRejectedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.JoinSessionRequestExtension;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;

public final class JoinSessionRequestHandler {

    private static final Logger LOG = Logger
        .getLogger(JoinSessionRequestHandler.class);

    private final ISarosSessionManager sessionManager;

    private final ITransmitter transmitter;

    private final IReceiver receiver;

    private final IPreferenceStore preferenceStore;

    private final PacketListener joinSessionRequestListener = new PacketListener() {

        @Override
        public void processPacket(final Packet packet) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    handleInvitationRequest(new JID(packet.getFrom()),
                        JoinSessionRequestExtension.PROVIDER.getPayload(packet));
                }
            });
        }
    };

    public JoinSessionRequestHandler(ISarosSessionManager sessionManager,
        ITransmitter transmitter, IReceiver receiver,
        IPreferenceStore preferenceStore) {
        this.sessionManager = sessionManager;
        this.transmitter = transmitter;
        this.receiver = receiver;
        this.preferenceStore = preferenceStore;

        if (Boolean.getBoolean("de.fu_berlin.inf.dpp.server.SUPPORTED")) {
            this.receiver.addPacketListener(joinSessionRequestListener,
                JoinSessionRequestExtension.PROVIDER.getPacketFilter());
        }
    }

    private void handleInvitationRequest(JID from,
        JoinSessionRequestExtension extension) {

        ISarosSession session = sessionManager.getSarosSession();

        if (session != null && !session.isHost())
            return;

        if (!preferenceStore.getBoolean(PreferenceConstants.SERVER_ACTIVATED)
            || (session != null && extension.isNewSessionRequested())
            || (session == null && !extension.isNewSessionRequested())) {
            sendRejection(from);
            return;
        }

        List<JID> list = Collections.singletonList(from);

        // TODO remove calls to CollaborationUtils
        if (extension.isNewSessionRequested()) {
            CollaborationUtils.startSession(new ArrayList<IResource>(), list);
        } else {
            CollaborationUtils.addContactsToSession(list);
        }
    }

    private void sendRejection(JID to) {
        transmitter.sendPacketExtension(to, JoinSessionRejectedExtension.PROVIDER
            .create(new JoinSessionRejectedExtension()));
    }
}
