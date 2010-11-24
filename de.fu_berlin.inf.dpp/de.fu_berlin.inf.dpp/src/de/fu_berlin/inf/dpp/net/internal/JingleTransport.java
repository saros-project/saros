package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.jingle.JingleManager;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.JingleSessionRequest;
import org.jivesoftware.smackx.jingle.JingleSessionStatePending;
import org.jivesoftware.smackx.jingle.listeners.JingleSessionRequestListener;
import org.jivesoftware.smackx.jingle.media.JingleMediaManager;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.ICETransportManager;
import org.jivesoftware.smackx.jingle.nat.JingleTransportManager;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IBytestreamConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.ITransport;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferSession;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.util.CausedIOException;

public class JingleTransport implements ITransport {

    private class FileMediaManager extends JingleMediaManager {
        public FileMediaManager(JingleTransportManager transportManager) {
            super(transportManager);

        }

        @Override
        public JingleMediaSession createMediaSession(PayloadType payload,
            TransportCandidate remoteCandidate,
            TransportCandidate localCandidate, JingleSession jingleSession) {

            final JID remoteJID = isInitiator(jingleSession) ? new JID(
                jingleSession.getResponder()) : new JID(jingleSession
                .getInitiator());

            final JingleFileTransferSession newSession = new JingleFileTransferSession(
                payload, remoteCandidate, localCandidate, null, jingleSession);

            if (newSession.isConnected()) {
                log.debug("Jingle [" + remoteJID.getName()
                    + "] Media Session - Success using "
                    + NetTransferMode.JINGLETCP);
                if (!isInitiator(jingleSession)) {

                    try {
                        final BinaryChannel channel = new BinaryChannel(
                            newSession, getDefaultNetTransferMode());
                        listener.connectionChanged(remoteJID,
                            new BinaryChannelConnection(remoteJID, channel,
                                listener));
                    } catch (IOException e) {
                        new CausedIOException("Could not connect to "
                            + remoteJID.getName(), e);
                    }

                } else {
                    sessions.put(remoteJID.toString(), newSession);
                }
            } else {
                log.debug("Jingle [" + remoteJID.getName()
                    + "] Media Session - Failure");
            }
            return newSession;
        }

        protected List<PayloadType> payloads = Collections
            .singletonList((PayloadType) new PayloadType.Audio(333, "fileshare"));

        @Override
        public List<PayloadType> getPayloads() {
            return payloads;
        }

        public boolean isInitiator(JingleSession session) {
            return session.getInitiator().equals(
                session.getConnection().getUser());
        }
    }

    private Logger log = Logger.getLogger(JingleTransport.class);

    protected IBytestreamConnectionListener listener;
    protected XMPPConnection connection;
    protected Saros saros;
    protected JingleManager manager;
    protected static JingleTransport instance;
    private static final int timeout = 20;

    private Map<String, JingleFileTransferSession> sessions = Collections
        .synchronizedMap(new HashMap<String, JingleFileTransferSession>());

    private JingleTransport(Saros saros) {
        this.saros = saros;
    }

    public static JingleTransport getTransport(Saros saros) {
        if (instance == null)
            instance = new JingleTransport(saros);
        return instance;
    }

    public synchronized IBytestreamConnection connect(JID peer,
        SubMonitor progress) throws IOException, InterruptedException {

        progress.subTask("Try to initiate bytestream with " + toString());

        JingleSession out = null;
        try {
            out = manager.createOutgoingJingleSession(peer.toString());
            out.startOutgoing();
            waitForConnection(out);
        } catch (XMPPException e) {
            log.error("Could not connect to " + peer.toString());
            return null;
        }

        int i = 0;

        while (!sessions.containsKey(peer.toString()) && i < timeout) {
            Thread.sleep(500);
            i++;
        }

        final JingleFileTransferSession session = sessions
            .get(peer.toString());

        if (session == null)
            throw new IOException("Could not connect to " + peer.toString());

        sessions.remove(peer.toString());

        return new BinaryChannelConnection(peer, new BinaryChannel(session,
            this.getDefaultNetTransferMode()), listener);

    }

    public void waitForConnection(JingleSession session) {

        InterruptedException interrupted = null;
        int i = 0;

        while ((session.getSessionState() == JingleSessionStatePending
            .getInstance())
            && i < timeout) {

            if (i % 2 == 0)
                log.debug("Waiting for connection since " + (i * 500) / 1000
                    + "s");

            i++;

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                interrupted = e;
            }
        }
        if (interrupted != null) {
            log.error("Code not designed to be interruptable", interrupted);
            Thread.currentThread().interrupt();
        }
    }

    public void disposeXMPPConnection() {

        if (manager != null) {
            manager.removeJingleSessionRequestListener(jingleListener);
            sessions.clear();
        }
        manager = null;
    }

    public void prepareXMPPConnection(XMPPConnection connection,
        IBytestreamConnectionListener listener) {

        this.listener = listener;
        this.connection = connection;
        this.getManager();
        this.manager.addJingleSessionRequestListener(jingleListener);
    }

    private JingleSessionRequestListener jingleListener = new JingleSessionRequestListener() {

        public void sessionRequested(JingleSessionRequest request) {

            log.info("Received request to establish a "
                + getDefaultNetTransferMode() + " bytestream connection from "
                + request.getFrom());

            try {

                JingleSession incoming = request.accept();
                incoming.startIncoming();

            } catch (XMPPException e) {

                log.error("Could not connect to " + request.getFrom());
            }

        }

    };

    private void getManager() {

        log.info("Starting to initialize jingle.");

        JingleManager.setJingleServiceEnabled();
        IPreferenceStore prefStore = saros.getPreferenceStore();

        final String stunServer = prefStore.getString(PreferenceConstants.STUN);
        final int stunServerPort = Integer.parseInt(prefStore
            .getString(PreferenceConstants.STUN_PORT));

        final ICETransportManager icetm0 = new ICETransportManager(connection,
            stunServer, stunServerPort);

        // final STUNTransportManager sm = new STUNTransportManager();

        final FileMediaManager mediaManager = new FileMediaManager(icetm0);

        if (!connection.isConnected()) {
            throw new RuntimeException(
                "Jingle Manager could not be started because connection was closed in the meantime");
        }

        manager = new JingleManager(connection, Collections
            .singletonList((JingleMediaManager) mediaManager));

        log.debug("Initialized jingle.");
    }

    @Override
    public String toString() {
        return getDefaultNetTransferMode().getXEP();
    }

    public NetTransferMode getDefaultNetTransferMode() {
        return NetTransferMode.JINGLETCP;
    }

}
