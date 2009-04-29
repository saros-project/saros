package de.fu_berlin.inf.dpp.net.jingle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
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

import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager.NetTransferMode;

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

    public enum JingleConnectionState {
        /**
         * A jingle connection starts in this state. It is not ready for sending
         * yet. Trying to send will block.
         */
        INIT,
        /**
         * The jingle connection is ready for sending.
         */
        ESTABLISHED,
        /**
         * The jingle connection has been closed correctly. Trying to send will
         * reopen the connection.
         */
        CLOSED,
        /**
         * The jingle connection has been closed on error. Trying to send will
         * always return immediately.
         */
        ERROR
    }

    public class FileTransferConnection {

        final JID jid;

        JingleConnectionState state;

        JingleSession session = null;

        JingleFileTransferSession fileTransfer = null;

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
            if (fileTransfer != null) {
                return fileTransfer.getConnectionType();
            } else {
                return NetTransferMode.UNKNOWN;
            }
        }

        public JingleSession getSession() {
            return session;
        }
    }

    private static Logger logger = Logger
        .getLogger(JingleFileTransferManager.class);

    private XMPPConnection xmppConnection;

    private JingleManager jm;

    private Map<JID, FileTransferConnection> connections = Collections
        .synchronizedMap(new HashMap<JID, FileTransferConnection>());

    private Set<IJingleFileTransferListener> listeners = new HashSet<IJingleFileTransferListener>();

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
            TransportCandidate tc1, TransportCandidate tc2,
            JingleSession jingleSession) {

            final JID remoteJID = isInitiator(jingleSession) ? new JID(
                jingleSession.getResponder()) : new JID(jingleSession
                .getInitiator());

            logger.debug("Jingle [" + remoteJID.getName()
                + "] Media session - Start");

            final JingleFileTransferSession newSession = new JingleFileTransferSession(
                payload, tc1, tc2, null, jingleSession, listeners, remoteJID);

            // TODO Make sure we don't need to do this asynchronously
            newSession.initialize();

            connections.get(remoteJID).fileTransfer = newSession;

            if (newSession.isConnected()) {
                logger.debug("Jingle [" + remoteJID.getName()
                    + "] Media Session - Success using "
                    + newSession.getConnectionType());
                connections.get(remoteJID).setState(
                    JingleConnectionState.ESTABLISHED);
            } else {
                logger.debug("Jingle [" + remoteJID.getName()
                    + "] Media Session - Failure");
                connections.get(remoteJID)
                    .setState(JingleConnectionState.ERROR);
            }

            return newSession;
        }

        List<PayloadType> payloads = Collections
            .singletonList((PayloadType) new PayloadType.Audio(333, "fileshare"));

        @Override
        public List<PayloadType> getPayloads() {
            return payloads;
        }
    }

    private FileMediaManager mediaManager;

    protected Saros saros;

    public JingleFileTransferManager(Saros saros, XMPPConnection connection,
        IJingleFileTransferListener listener) {
        this.xmppConnection = connection;
        this.saros = saros;

        logger.debug("Starting to initialize jingle file transfer manager.");

        this.listeners.add(listener);
        initialize();

        logger.debug("Initialized jingle file transfer manager.");
    }

    public void initialize() {

        // get STUN Server from Preferences
        IPreferenceStore prefStore = saros.getPreferenceStore();
        final String stunServer = prefStore.getString(PreferenceConstants.STUN);
        final int stunServerPort = Integer.parseInt(prefStore
            .getString(PreferenceConstants.STUN_PORT));

        // ICETransportManager icetm0 = new ICETransportManager(xmppConnection,
        // "stunserver.org", 3478);
        // STUNTransportManager stun = new STUNTransportManager();

        ICETransportManager icetm0 = new ICETransportManager(xmppConnection,
            stunServer, stunServerPort);

        mediaManager = new FileMediaManager(icetm0);

        if (!xmppConnection.isConnected()) {
            throw new RuntimeException(
                "Jingle Manager could not be started because connection was closed in the meantime");
        }

        jm = new JingleManager(xmppConnection, Collections
            .singletonList((JingleMediaManager) mediaManager));

        // TODO [CO] This seems to be wrong
        JingleManager.setJingleServiceEnabled();

        jm.addJingleSessionRequestListener(new JingleSessionRequestListener() {
            public void sessionRequested(JingleSessionRequest request) {

                JID jid = new JID(request.getFrom());
                FileTransferConnection incoming = connections.get(jid);

                // If we already have a session, which is not CLOSED then return
                if (incoming != null
                    && !(incoming.state == JingleConnectionState.CLOSED)) {

                    return;
                }

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
                    logger.error("Failed to start JingleSession", e);
                    incoming.session = null;
                    incoming.setState(JingleConnectionState.ERROR);
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
                logger.debug("Media closed [" + jid.getBase() + "]");
            }

            public void mediaEstablished(PayloadType pt) {
                logger.debug("Media established [" + jid.getBase() + "]");
            }
        });

        /**
         * Jingle Session Listener
         */
        connection.getSession().addListener(new JingleSessionListener() {

            public void sessionClosed(String arg0, JingleSession session) {
                logger.info("JingleSession closed [" + jid.getBase() + "]");

                if (connection.state != JingleConnectionState.ERROR) {
                    connection.setState(JingleConnectionState.CLOSED);
                }
            }

            public void sessionClosedOnError(XMPPException arg0,
                JingleSession arg1) {
                logger.info("JingleSession closed on error [" + jid.getBase()
                    + "]");
                connection.setState(JingleConnectionState.ERROR);
            }

            public void sessionDeclined(String arg0, JingleSession arg1) {
                logger.error("session declined : " + jid.toString());
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
                }
            });
    }

    /**
     * Send the given binary content via jingle using the given transfer
     * settings. Will create a jingle session on demand and block until the data
     * has been send.
     * 
     * Note that only one JingleSession can be created at the same time.
     */
    public NetTransferMode send(final TransferDescription transferDescription,
        final byte[] content) throws JingleSessionException {

        JID toJID = transferDescription.getRecipient();

        FileTransferConnection connection;

        synchronized (this) {

            connection = connections.get(toJID);

            if (connection == null
                || connection.state == JingleConnectionState.CLOSED) {
                connection = startJingleSession(toJID);
            }

            int i = 0;

            if (connection.state == JingleConnectionState.INIT) {
                // TODO observe state rather than sleep
                while (connection.state == JingleConnectionState.INIT && i < 60) {
                    try {
                        logger.debug("Jingle [" + toJID
                            + "] Waiting for Init since " + (i * 500) / 1000
                            + "s");
                        i++;
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        if (connection.state == JingleConnectionState.ESTABLISHED) {
            return connection.fileTransfer.send(transferDescription, content);
        } else {
            // If we want to reconnect, then we should not set this to ERROR
            connection.setState(JingleConnectionState.ERROR);
            throw new JingleSessionException(
                "Could not establish connection when trying to send");
        }
    }

    private FileTransferConnection startJingleSession(JID toJID)
        throws JingleSessionException {

        logger.debug("Jingle [" + toJID.getName() + "] Start Session");

        FileTransferConnection connection = new FileTransferConnection(toJID);
        connections.put(toJID, connection);
        // Inform listeners
        connection.setState(connection.getState());

        try {
            JingleSession session = jm.createOutgoingJingleSession(toJID
                .toString());
            connection.session = session;

            // Hook listeners
            initJingleListener(connection, toJID);

            // Start the call
            session.startOutgoing();
        } catch (XMPPException e) {
            connection.session = null;
            connection.setState(JingleConnectionState.ERROR);

            throw new JingleSessionException("Jingle [" + toJID.getName()
                + "] Error connecting", e);
        }
        return connection;
    }

    /**
     * remove all jingle sessions.
     */
    public void terminateAllJingleSessions() {

        logger.debug("Terminate all jingle sessions.");

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
                logger.info("JingleSession Terminated [" + jid.getBase() + "]");
            } catch (XMPPException e1) {
                logger.error(
                    "Error during terminating outgoing jingle session with JID : "
                        + jid, e1);
            } finally {
                outgoing.session = null;
                outgoing.fileTransfer = null;
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

    /**
     * To add a JingleFileTransferListener
     * 
     * @param listener
     */
    public void addJingleFileTransferListener(
        IJingleFileTransferListener listener) {
        listeners.add(listener);
    }

    public interface IJingleStateListener {
        public void setState(JID jid, JingleConnectionState state);
    }

    List<IJingleStateListener> stateListeners = new ArrayList<IJingleStateListener>();

    public void addJingleStateListener(IJingleStateListener listener) {
        stateListeners.add(listener);
    }

    public void removeJingleStateListener(IJingleStateListener listener) {
        stateListeners.remove(listener);
    }
}
