package de.fu_berlin.inf.dpp.net.jingle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.jingle.JingleManager;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.JingleSessionRequest;
import org.jivesoftware.smackx.jingle.listeners.JingleMediaListener;
import org.jivesoftware.smackx.jingle.listeners.JingleSessionListener;
import org.jivesoftware.smackx.jingle.listeners.JingleSessionRequestListener;
import org.jivesoftware.smackx.jingle.listeners.JingleTransportListener;
import org.jivesoftware.smackx.jingle.media.JingleMediaManager;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.ICETransportManager;
import org.jivesoftware.smackx.jingle.nat.JingleTransportManager;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.IConnection;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.JingleConnectionState;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This class manages all Jingle Peer to Peer Sessions. Jingle is a
 * XMPP-extension with id XEP-0166. Documentation can be found at
 * http://xmpp.org/extensions/xep-0166.html .
 * 
 * The main method is send(...) which creates a Jingle Session if none exists.
 * To support file transfer even a user is behind a NAT, it uses a STUN server
 * to resolve the IP-addresses.
 * 
 * @author orieger
 * @author chjacob
 * @author oezbek
 */
public class JingleFileTransferManager {

    public class FileTransferConnection implements IConnection {

        protected final JID jid;

        protected JingleConnectionState state;

        protected JingleSession session = null;

        protected JingleFileTransferSession fileTransferSession = null;

        public FileTransferConnection(JID jid) {
            this.jid = jid;
            this.state = JingleConnectionState.INIT;
        }

        public void setState(JingleConnectionState newState) {
            this.state = newState;

            for (IJingleStateListener listener : stateListeners) {
                listener.setState(jid, state);
            }
        }

        public JingleConnectionState getState() {
            return state;
        }

        public NetTransferMode getTransferMode() {
            if (fileTransferSession != null) {
                return fileTransferSession.getConnectionType();
            } else {
                return NetTransferMode.UNKNOWN;
            }
        }

        public JingleSession getSession() {
            return session;
        }

        public JingleFileTransferSession getFileTransfer() {
            return fileTransferSession;
        }

        public synchronized void close() {
            if (fileTransferSession != null) {
                fileTransferSession.shutdown();
                fileTransferSession = null;
            }
        }

        public NetTransferMode getMode() {
            return getTransferMode();
        }

        public JID getPeer() {
            return jid;
        }

        public void send(TransferDescription data, byte[] content,
            SubMonitor progress) throws IOException, SarosCancellationException {

            try {

                fileTransferSession.getConnection().send(data, content,
                    progress);
            } catch (IOException e) {
                close();
                throw e;
            }
        }
    }

    private static Logger log = Logger
        .getLogger(JingleFileTransferManager.class);

    private XMPPConnection xmppConnection;

    private JingleManager jm;

    private Map<JID, FileTransferConnection> connections = Collections
        .synchronizedMap(new HashMap<JID, FileTransferConnection>());

    /**
     * The FileMediaManager manages all FileTransferSessions. When a Jingle
     * Session is established the SMACK API calls the createMediaSessions method
     * and a new JingleFileTransferSession is created. To send a file with an
     * existing session use the transferFiles method.
     */
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
                payload, remoteCandidate, localCandidate, null, jingleSession,
                dataTransferManager, remoteJID);

            newSession.initialize();

            connections.get(remoteJID).fileTransferSession = newSession;

            if (newSession.isConnected()) {
                log.debug("Jingle [" + remoteJID.getName()
                    + "] Media Session - Success using "
                    + newSession.getConnectionType());
                connections.get(remoteJID).setState(
                    JingleConnectionState.ESTABLISHED);

                if (!isInitiator(jingleSession)) {
                    dataTransferManager.connectionChanged(remoteJID,
                        connections.get(remoteJID));
                }

            } else {
                log.debug("Jingle [" + remoteJID.getName()
                    + "] Media Session - Failure");
                connections.get(remoteJID)
                    .setState(JingleConnectionState.ERROR);
            }

            return newSession;
        }

        protected List<PayloadType> payloads = Collections
            .singletonList((PayloadType) new PayloadType.Audio(333, "fileshare"));

        @Override
        public List<PayloadType> getPayloads() {
            return payloads;
        }
    }

    public static String toString(TransportCandidate candidate) {
        StringBuilder sb = new StringBuilder();
        sb.append(candidate.getIp()).append(":").append(candidate.getPort());
        if (candidate.getSymmetric() != null) {
            sb.append("[symmetric=").append(candidate.getSymmetric().getIp())
                .append(":").append(candidate.getSymmetric().getPort()).append(
                    "]");
        }
        return sb.toString();
    }

    private FileMediaManager mediaManager;

    protected Saros saros;

    protected DataTransferManager dataTransferManager;

    public JingleFileTransferManager(Saros saros, XMPPConnection connection,
        DataTransferManager dataTransferManager) {
        this.xmppConnection = connection;
        this.saros = saros;

        // Add another layer of indirection
        this.dataTransferManager = dataTransferManager;

        log.debug("Starting to initialize jingle file transfer manager.");
        initialize();
        log.debug("Initialized jingle file transfer manager.");
    }

    public void initialize() {

        // get STUN Server from Preferences
        IPreferenceStore prefStore = saros.getPreferenceStore();
        final String stunServer = prefStore.getString(PreferenceConstants.STUN);
        final int stunServerPort = Integer.parseInt(prefStore
            .getString(PreferenceConstants.STUN_PORT));

        ICETransportManager icetm0 = new ICETransportManager(xmppConnection,
            stunServer, stunServerPort);

        mediaManager = new FileMediaManager(icetm0);

        if (!xmppConnection.isConnected()) {
            throw new RuntimeException(
                "Jingle Manager could not be started because connection was closed in the meantime");
        }

        jm = new JingleManager(xmppConnection, Collections
            .singletonList((JingleMediaManager) mediaManager));

        jm.addJingleSessionRequestListener(new JingleSessionRequestListener() {
            public void sessionRequested(JingleSessionRequest request) {

                try {
                    JID jid = new JID(request.getFrom());
                    FileTransferConnection incoming = connections.get(jid);
                    // If we already have a session, which is not CLOSED then
                    // return
                    if (incoming != null
                        && !(incoming.state == JingleConnectionState.CLOSED)) {

                        // TODO If the user is once in a situation where his
                        // JingleConnectionState is in ERROR it can never be
                        // restarted

                        log.info(Util.prefix(jid)
                            + "Receiving Jingle Session Request but "
                            + "local user already has a "
                            + "FileTransferConnection " + "in state "
                            + incoming.state + ". Thus rejecting");

                        request.reject();
                        return;
                    }
                    log.debug("Receiving Jingle Session Request from " + jid);

                    incoming = new FileTransferConnection(jid);
                    connections.put(jid, incoming);
                    // Inform listeners
                    incoming.setState(incoming.getState());

                    try {
                        // Accept the call
                        JingleSession session = request.accept();
                        incoming.session = session;

                        // Hook listeners
                        initJingleListener(incoming, jid);

                        // Start the call
                        session.startIncoming();
                    } catch (XMPPException e) {
                        log.error("Failed to start JingleSession", e);
                        incoming.session = null;
                        incoming.setState(JingleConnectionState.ERROR);
                    }
                } catch (Exception e) {
                    log.error("Starting Session failed: ", e);
                }
            }
        });
    }

    public static boolean isInitiator(JingleSession session) {
        return session.getInitiator().equals(session.getConnection().getUser());
    }

    private void initJingleListener(final FileTransferConnection connection,
        final JID jid) {

        // Add media listener (only for debugging)
        connection.getSession().addMediaListener(new JingleMediaListener() {

            public void mediaClosed(PayloadType cand) {
                log.debug("Media closed " + Util.prefix(jid));
            }

            public void mediaEstablished(PayloadType pt) {
                log.debug("Media established " + Util.prefix(jid));
            }
        });

        /**
         * Jingle Session Listener
         */
        connection.getSession().addListener(new JingleSessionListener() {

            public void sessionClosed(String arg0, JingleSession session) {
                log.info(Util.prefix(jid) + "JingleSession closed");

                if (connection.state != JingleConnectionState.ERROR) {
                    connection.setState(JingleConnectionState.CLOSED);
                }
            }

            public void sessionClosedOnError(XMPPException arg0,
                JingleSession arg1) {
                log.info("JingleSession closed on error " + Util.prefix(jid));
                connection.setState(JingleConnectionState.ERROR);
            }

            public void sessionDeclined(String arg0, JingleSession arg1) {
                log.error("session declined : " + jid.toString());
                connection.setState(JingleConnectionState.CLOSED);
            }

            public void sessionEstablished(PayloadType arg0,
                TransportCandidate arg1, TransportCandidate arg2,
                JingleSession arg3) {
                // Do nothing, this message is useless, because the MediaSession
                // does not yet exist
            }

            public void sessionMediaReceived(JingleSession arg0, String arg1) {
                // Do nothing
            }

            public void sessionRedirected(String arg0, JingleSession arg1) {
                // Do nothing
            }
        });

        /**
         * Transport Listener
         */
        connection.getSession().addTransportListener(
            new JingleTransportListener() {

                public void transportClosed(TransportCandidate cand) {
                    // do nothing, because we will be notified about this
                    // directly on the Session as a sessionClosedOnError
                }

                public void transportClosedOnError(XMPPException e) {
                    assert false : "This method is not called from Smack";
                }

                public void transportEstablished(TransportCandidate local,
                    TransportCandidate remote) {
                    // do nothing, because we will be notified about an
                    // successfully established session by the
                    // SessionListener
                    log.info("Jingle [" + jid.toString()
                        + "] Transport candidate found - Local: "
                        + JingleFileTransferManager.toString(local)
                        + " -> Remote: "
                        + JingleFileTransferManager.toString(remote));

                }
            });
    }

    /**
     * remove all jingle sessions.
     */
    public void terminateAllJingleSessions() {

        log.debug("Terminate all jingle sessions.");

        for (JID jid : new ArrayList<JID>(connections.keySet())) {
            terminateJingleSession(jid);
        }
    }

    /**
     * terminate and remove jingle session for jid.
     * 
     * @param jid
     */
    public void terminateJingleSession(JID jid) {

        FileTransferConnection outgoing = connections.remove(jid);
        if (outgoing != null) {
            try {
                outgoing.session.terminate();
                log.info("JingleSession Terminated " + Util.prefix(jid));
            } catch (XMPPException e1) {
                log.error(
                    "Error during terminating outgoing jingle session with JID : "
                        + jid, e1);
            } finally {
                outgoing.session = null;
                outgoing.fileTransferSession = null;
            }
        }
    }

    /**
     * This method returns the connection state for the given JID or null if no
     * connection was found.
     * 
     * @param jid
     *            identify the jingle session
     * @return JingleConnectionState for given JID, or null if no connection has
     *         found.
     */
    public JingleConnectionState getState(JID jid) {

        if (connections.containsKey(jid)) {
            return connections.get(jid).state;
        } else {
            return null;
        }
    }

    /**
     * This method returns the jingle connection for the JID or null, if none
     * was found.
     */
    public FileTransferConnection getConnection(JID jid) {
        return connections.get(jid);
    }

    public interface IJingleStateListener {
        public void setState(JID jid, JingleConnectionState state);
    }

    protected List<IJingleStateListener> stateListeners = new ArrayList<IJingleStateListener>();

    public void addJingleStateListener(IJingleStateListener listener) {
        stateListeners.add(listener);
    }

    public void removeJingleStateListener(IJingleStateListener listener) {
        stateListeners.remove(listener);
    }

    public IConnection connect(JID toJID, SubMonitor progress)
        throws JingleSessionException, IOException {
        log.debug(Util.prefix(toJID) + "Start Session");

        FileTransferConnection connection = new FileTransferConnection(toJID);

        connections.put(toJID, connection);
        try {
            // Hook listeners

            JingleSession session = jm.createOutgoingJingleSession(toJID
                .toString());
            connection.session = session;
            initJingleListener(connection, toJID);

            // Start the call
            session.startOutgoing();
        } catch (XMPPException e) {
            throw new JingleSessionException("Jingle [" + toJID.getName()
                + "] Error connecting", e);
        }

        InterruptedException interrupted = null;

        int i = 0;
        while (connection.state == JingleConnectionState.INIT && i < 60) {
            if (i % 2 == 0)
                log.debug(Util.prefix(connection.jid)
                    + "Waiting for Init since " + (i * 500) / 1000 + "s");
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

        if (connection.state != JingleConnectionState.ESTABLISHED)
            throw new IOException();

        return connection;
    }

    static FutureTask<JingleFileTransferManager> jingleManager;

    public boolean isNUll() {
        return jingleManager == null;
    }

    public void cancelThread() {
        jingleManager.cancel(true);

    }

    public void setNull() {
        jingleManager = null;
    }

    public static JingleFileTransferManager getManager(final Saros saros,
        final XMPPConnection connection, final DataTransferManager dtm) {
        jingleManager = new FutureTask<JingleFileTransferManager>(
            new Callable<JingleFileTransferManager>() {
                public JingleFileTransferManager call() throws Exception {
                    return new JingleFileTransferManager(saros, connection, dtm);
                }
            });

        Thread executor = new Thread(jingleManager);
        executor.start();
        JingleFileTransferManager tmp = null;
        try {
            tmp = jingleManager.get();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return tmp;
    }
}
