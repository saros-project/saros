package de.fu_berlin.inf.dpp.net.jingle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.jingle.JingleManager;
import org.jivesoftware.smackx.jingle.JingleNegotiatorState;
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

/**
 * This class manages all Jingle Peer to Peer Sessions. Jingle is a
 * XMPP-extension with id XEP-0166. Documentation can be found at
 * http://xmpp.org/extensions/xep-0166.html .
 * 
 * The main method is createOutgoingJingleFileTransfer which creates a Jingle
 * Session. To support file transfer even a user is behind a NAT, it uses a STUN
 * server to resolve the IP-addresses.
 * 
 * @author orieger
 * @author chjacob
 */
public class JingleFileTransferManager {

    private static Logger logger = Logger
            .getLogger(JingleFileTransferManager.class);

    /**
     * The FileMediaManager manages all FileTransferSessions. When a Jingle
     * Session is established the SMACK API calls the createMediaSessions method
     * and a new JingleFileTransferSession is created. To send a file with an
     * existing session use the transferFiles method.
     */
    private class FileMediaManager extends JingleMediaManager {

        private JingleFileTransferData[] transferData = new JingleFileTransferData[0];
        private HashMap<JID, JingleFileTransferSession> sessions;
        private JingleFileTransferSession session;

        public FileMediaManager(JingleTransportManager transportManager) {
            super(transportManager);
            sessions = new HashMap<JID, JingleFileTransferSession>();
        }

        @Override
        public JingleMediaSession createMediaSession(PayloadType payload,
                TransportCandidate tc1, TransportCandidate tc2,
                JingleSession jingleSession) {

            // get JID from other side
            JID jid = Saros.getDefault().getMyJID().toString().equals(
                    jingleSession.getInitiator()) ? new JID(jingleSession
                    .getResponder()) : new JID(jingleSession.getInitiator());

            JingleFileTransferSession newSession = new JingleFileTransferSession(
                    payload, tc1, tc2, null, jingleSession, transferData, jid,
                    listeners);

            sessions.put(jid, newSession);
            return newSession;
        }

        @Override
        public List<PayloadType> getPayloads() {
            List<PayloadType> result = new ArrayList<PayloadType>();
            result.add(new PayloadType.Audio(333, "fileshare"));
            return result;
        }

        public void setTransferFile(JingleFileTransferData[] transferData) {
            this.transferData = transferData;
        }

        public void transferFiles(JingleFileTransferData[] transferData, JID jid)
                throws JingleSessionException {
            JingleFileTransferSession session = sessions.get(jid);
            if (session != null) {
                session.sendFiles(transferData);
                transferData = null;
            }
        }

        public void removeJingleSession(JID jid) {
            if (sessions.containsKey(jid))
                sessions.remove(jid);
        }
    }

    private XMPPConnection xmppConnection;
    private JingleManager jm;

    private HashMap<JID, JingleSession> incomingSessions = null;
    private HashMap<JID, JingleSession> outgoingSessions = null;

    public static int JINGLE_TIME_OUT = 10000;

    /**
     * this map contains for all incoming and outgoing jingle sessions the
     * appropriate connection states. If an error occur the connection state
     * stay in list for call back setting.
     */
    private HashMap<JID, JingleConnectionState> connectionStates = null;
    private FileMediaManager mediaManager = null;
    private Set<IJingleFileTransferListener> listeners;

    public enum JingleConnectionState {
        INIT, ESTABLISHED, CLOSED, ERROR, DEFAULT
    }

    public JingleFileTransferManager(XMPPConnection connection,
            IJingleFileTransferListener listener) {
        this.xmppConnection = connection;
        incomingSessions = new HashMap<JID, JingleSession>();
        outgoingSessions = new HashMap<JID, JingleSession>();
        connectionStates = new HashMap<JID, JingleConnectionState>();
        this.listeners = new HashSet<IJingleFileTransferListener>();
        this.listeners.add(listener);
        logger.debug("initialized jingle file transfer manager.");
        initialize();
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

        List<JingleMediaManager> medias = new Vector<JingleMediaManager>();
        medias.add(mediaManager);

        jm = new JingleManager(xmppConnection, medias);
        JingleManager.setJingleServiceEnabled();

        jm.addJingleSessionRequestListener(new JingleSessionRequestListener() {
            public void sessionRequested(JingleSessionRequest request) {

                JID jid = new JID(request.getFrom());
                JingleSession incoming = incomingSessions.get(jid);

                if (incoming != null)
                    return;

                try {
                    // Accept the call
                    incoming = request.accept();

                    initJingleListener(incoming, new JID(incoming
                            .getInitiator()));
                    /* put to current session list. */
                    incomingSessions.put(jid, incoming);
                    // Start the call
                    incoming.startIncoming();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void initJingleListener(JingleSession js, final JID jid) {

        connectionStates.put(jid, JingleConnectionState.INIT);

        /* add media listener. */
        js.addMediaListener(new JingleMediaListener() {

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
        js.addListener(new JingleSessionListener() {

            public void sessionClosed(String arg0, JingleSession session) {
                logger.info("session closed : " + jid.toString());

                /*
                 * if session closed during pending process, fallback to
                 * XEP-0096 transfer
                 */
                if (session.getNegotiatorState() == JingleNegotiatorState.PENDING
                        && (connectionStates.get(jid) == JingleConnectionState.INIT)) {
                    logger.error("Session closed during pending process : "
                            + jid + " with current state : " + getState(jid));
                    connectionStates.remove(jid);
                    connectionStates.put(jid, JingleConnectionState.ERROR);
                    for (IJingleFileTransferListener listener : listeners) {
                        if (session.getInitiator().equals(
                                session.getConnection().getUser())) {
                            listener.failedToSendFileListWithJingle(jid,
                                    mediaManager.transferData[0]);
                        }
                    }

                }
                connectionStates.remove(jid);
                connectionStates.put(jid, JingleConnectionState.CLOSED);
            }

            public void sessionClosedOnError(XMPPException arg0,
                    JingleSession arg1) {
                logger.error("session closed on error : " + jid.toString());
                connectionStates.remove(jid);
                connectionStates.put(jid, JingleConnectionState.ERROR);
            }

            public void sessionDeclined(String arg0, JingleSession arg1) {
                logger.error("session declined : " + jid.toString());
                connectionStates.remove(jid);
                connectionStates.put(jid, JingleConnectionState.ERROR);
            }

            public void sessionEstablished(PayloadType arg0,
                    TransportCandidate arg1, TransportCandidate arg2,
                    JingleSession arg3) {
                logger.debug("session established : " + jid.toString());
                connectionStates.remove(jid);
                connectionStates.put(jid, JingleConnectionState.ESTABLISHED);
            }

            public void sessionMediaReceived(JingleSession arg0, String arg1) {
                // do nothing
            }

            public void sessionRedirected(String arg0, JingleSession arg1) {
                // do nothing
            }
        });

        /**
         * Transport Listener
         */
        js.addTransportListener(new JingleTransportListener() {

            public void transportClosed(TransportCandidate cand) {
                logger.debug("transport closed: " + jid.toString());
                connectionStates.remove(jid);
                connectionStates.put(jid, JingleConnectionState.CLOSED);
            }

            public void transportClosedOnError(XMPPException e) {
                logger.error("transport closed on error : " + jid.toString());
                connectionStates.remove(jid);
                connectionStates.put(jid, JingleConnectionState.ERROR);
            }

            public void transportEstablished(TransportCandidate local,
                    TransportCandidate remote) {
                logger.debug("transport established : " + jid.toString());
                connectionStates.remove(jid);
                connectionStates.put(jid, JingleConnectionState.ESTABLISHED);
            }
        });
    }

    /**
     * initiate a jingle session
     * 
     * @param jid
     * @param transferData
     * @param monitor
     * @throws XMPPException
     */
    public void createOutgoingJingleFileTransfer(JID jid,
            JingleFileTransferData[] transferData)
            throws JingleSessionException {

        JingleSession outgoing = outgoingSessions.get(jid);
        JingleSession incoming = incomingSessions.get(jid);

        if (outgoing != null) {
            /* send new data with current connection. */
            mediaManager.transferFiles(transferData, jid);
            return;
        }
        if (incoming != null) {
            /* send new data with current connection. */
            mediaManager.transferFiles(transferData, jid);
            return;
        }

        mediaManager.setTransferFile(transferData);

        try {
            outgoing = jm.createOutgoingJingleSession(jid.toString());

            initJingleListener(outgoing, jid);

            /* add to outgoing session list. */
            outgoingSessions.put(jid, outgoing);

            outgoing.startOutgoing();
        } catch (XMPPException e) {
            throw new JingleSessionException("Can't connect with Jingle");
        }

    }

    /**
     * remove all jingle sessions.
     */
    public void terminateAllJingleSessions() {

        logger.debug("Terminate all jingle sessions.");

        JingleSession outgoing = null;
        for (JID jid : outgoingSessions.keySet()) {
            outgoing = outgoingSessions.get(jid);
            if (outgoing != null) {
                try {
                    outgoing.terminate();
                } catch (XMPPException e1) {
                    e1.printStackTrace();
                } finally {
                    mediaManager.removeJingleSession(jid);
                    outgoingSessions.remove(jid);
                }
            }
        }

        JingleSession incoming = null;
        for (JID jid : incomingSessions.keySet()) {
            incoming = incomingSessions.get(jid);
            if (incoming != null) {
                try {
                    incoming.terminate();
                } catch (XMPPException e1) {
                    e1.printStackTrace();
                } finally {
                    mediaManager.removeJingleSession(jid);
                    incomingSessions.remove(jid);
                }
            }
        }

        /* reset connection state list */
        // connectionStates.clear();
    }

    /**
     * terminate and remove jingle session for jid.
     * 
     * @param jid
     */
    public void terminateJingleSession(JID jid) {
        JingleSession outgoing = outgoingSessions.get(jid);
        if (outgoing != null) {
            try {
                outgoing.terminate();
            } catch (XMPPException e1) {
                // e1.printStackTrace();
                logger.error(
                        "Error during terminate outgoing jingle session with JID : "
                                + jid, e1);
            } finally {
                mediaManager.removeJingleSession(jid);
                outgoingSessions.remove(jid);
                logger.debug("Terminate outgoing jingle session with JID : "
                        + jid);
            }
        }

        JingleSession incoming = incomingSessions.get(jid);
        if (incoming != null) {
            try {
                incoming.terminate();
            } catch (XMPPException e1) {
                // e1.printStackTrace();
                logger.error(
                        "Error during terminate incoming jingle session with JID : "
                                + jid, e1);
            } finally {
                mediaManager.removeJingleSession(jid);
                incomingSessions.remove(jid);
                logger.debug("Terminate incoming jingle session with JID : "
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
        JingleConnectionState state = connectionStates.get(jid);
        return state;
    }

    /**
     * Terminate Jingle connection and set error state for given peer.
     * 
     * @param jid
     */
    public void setJingleErrorState(JID jid) {
        if (jid != null) {
            logger.debug("Terminate Jingle Session for " + jid);
            terminateJingleSession(jid);
            connectionStates.remove(jid);
            connectionStates.put(jid, JingleConnectionState.ERROR);
        } else {
            logger.warn("JID is null. Jingle error state couldn't be set.");
        }
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
