package de.fu_berlin.inf.dpp.net.jingle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

        JingleConnectionState state = JingleConnectionState.INIT;

        JingleSession session = null;

        JingleFileTransferSession fileTransfer = null;

        public void setState(JingleConnectionState newState) {
            this.state = newState;
        }

        public JingleSession getSession() {
            return session;
        }
    }

    private static Logger logger = Logger
        .getLogger(JingleFileTransferManager.class);

    private XMPPConnection xmppConnection;

    private JingleManager jm;

    private HashMap<JID, FileTransferConnection> connections = new HashMap<JID, FileTransferConnection>();

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

            JID remoteJID = isInitiator(jingleSession) ? new JID(jingleSession
                .getResponder()) : new JID(jingleSession.getInitiator());

            JingleFileTransferSession newSession = new JingleFileTransferSession(
                payload, tc1, tc2, null, jingleSession, listeners);

            connections.get(remoteJID).fileTransfer = newSession;
            logger.debug("Session established : " + remoteJID.toString());
            connections.get(remoteJID).setState(
                JingleConnectionState.ESTABLISHED);

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

    public JingleFileTransferManager(XMPPConnection connection,
        IJingleFileTransferListener listener) {
        this.xmppConnection = connection;

        logger.debug("Starting to initialized jingle file transfer manager.");

        this.listeners.add(listener);
        initialize();

        logger.debug("Initialized jingle file transfer manager.");
    }

    public void initialize() {

        // get STUN Server from Preferences
        IPreferenceStore prefStore = Saros.getDefault().getPreferenceStore();
        final String stunServer = prefStore.getString(PreferenceConstants.STUN);
        final int stunServerPort = Integer.parseInt(prefStore
            .getString(PreferenceConstants.STUN_PORT));

        ICETransportManager icetm0 = new ICETransportManager(xmppConnection,
            stunServer, stunServerPort);

        // ICETransportManager icetm0 = new ICETransportManager(xmppConnection,
        // "stunserver.org", 3478);
        // STUNTransportManager stun = new STUNTransportManager();

        mediaManager = new FileMediaManager(icetm0);

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

                incoming = new FileTransferConnection();
                connections.put(jid, incoming);

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
                    incoming.state = JingleConnectionState.ERROR;
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
                logger.debug("media closed : " + jid.toString());
            }

            public void mediaEstablished(PayloadType pt) {
                logger.debug("media established : " + jid.toString());
            }
        });

        /**
         * Jingle Session Listener
         */
        connection.getSession().addListener(new JingleSessionListener() {

            public void sessionClosed(String arg0, JingleSession session) {
                logger.info("Session closed : " + jid.toString());
                connection.setState(JingleConnectionState.CLOSED);
            }

            public void sessionClosedOnError(XMPPException arg0,
                JingleSession arg1) {
                logger.error("session closed on error : " + jid.toString());
                connection.setState(JingleConnectionState.ERROR);
            }

            public void sessionDeclined(String arg0, JingleSession arg1) {
                logger.error("session declined : " + jid.toString());
                connection.setState(JingleConnectionState.CLOSED);
            }

            public void sessionEstablished(PayloadType arg0,
                TransportCandidate arg1, TransportCandidate arg2,
                JingleSession arg3) {
                // Do nothing, this message is useless, because the mediasession
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
     * Send via jingle. Will create a jingle session on demand.
     * 
     * @param toJID
     * @param transferDescription
     * @throws JingleSessionException
     */
    public void send(final TransferDescription transferDescription,
        final byte[] content) throws JingleSessionException {

        logger.debug("Sending with Jingle");

        JID toJID = transferDescription.getRecipient();

        FileTransferConnection connection = connections.get(toJID);

        if (connection == null
            || connection.state == JingleConnectionState.CLOSED) {
            connection = startJingleSession(toJID);

            logger.debug("Started Jingle");
        }

        if (connection.state != JingleConnectionState.ESTABLISHED) {
            // TODO observe state rather than sleep
            while (connection.state == JingleConnectionState.INIT) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            logger.debug("Init done");
        }

        if (connection.state == JingleConnectionState.ESTABLISHED) {
            connection.fileTransfer.send(transferDescription, content);
        } else {
            throw new JingleSessionException(
                "Could not establish connection when trying to send");
        }
    }

    private FileTransferConnection startJingleSession(JID toJID)
        throws JingleSessionException {

        logger.debug("Starting JingleSession");

        FileTransferConnection connection = new FileTransferConnection();
        connections.put(toJID, connection);

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
            connection.state = JingleConnectionState.ERROR;

            throw new JingleSessionException("Can't connect with Jingle", e);
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

        FileTransferConnection outgoing = connections.get(jid);
        if (outgoing != null) {
            try {
                outgoing.session.terminate();
            } catch (XMPPException e1) {
                logger.error(
                    "Error during terminate outgoing jingle session with JID : "
                        + jid, e1);
            } finally {
                outgoing.session = null;
                outgoing.fileTransfer = null;

                logger.debug("Terminate outgoing jingle session with JID : "
                    + jid);
            }
        }
    }

    /**
     * this method returns the appropriate connection state of active jingle
     * session.
     * 
     * @param jid
     *            identify the jingle session
     * @return JingleConnectionState for given jabber id, or null if non jingle
     *         session has found.
     */
    public JingleConnectionState getState(JID jid) {
        return connections.get(jid).state;
    }

    /**
     * to add a JingleFileTransferListener
     * 
     * @param listener
     */
    public void addJingleFileTransferListener(
        IJingleFileTransferListener listener) {
        listeners.add(listener);
    }
}
