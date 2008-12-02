package de.fu_berlin.inf.dpp.net.jingle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
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

import de.fu_berlin.inf.dpp.net.JID;

public class JingleFileTransferManager {

    private static Logger logger = Logger
	    .getLogger(JingleFileTransferManager.class);

    private class FileMediaManager extends JingleMediaManager {

	public FileMediaManager(JingleTransportManager transportManager) {
	    super(transportManager);
	}

	@Override
	public JingleMediaSession createMediaSession(PayloadType payload,
		TransportCandidate tc1, TransportCandidate tc2,
		JingleSession jingleSession) {
	    return new JingleFileTransferSession(payload, tc1, tc2, null,
		    jingleSession);
	}

	@Override
	public List<PayloadType> getPayloads() {
	    List<PayloadType> result = new ArrayList<PayloadType>();
	    result.add(new PayloadType.Audio(333, "fileshare"));
	    return result;
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

    public enum JingleConnectionState {
	INIT, ESTABLISHED, CLOSED, ERROR, DEFAULT
    }

    public JingleFileTransferManager(XMPPConnection connection) {
	this.xmppConnection = connection;
	incomingSessions = new HashMap<JID, JingleSession>();
	outgoingSessions = new HashMap<JID, JingleSession>();
	connectionStates = new HashMap<JID, JingleConnectionState>();
	logger.debug("initialized jingle file transfer manager.");
	initialize();
    }

    public void initialize() {

	ICETransportManager icetm0 = new ICETransportManager(xmppConnection,
		"jivesoftware.com", 3478);

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

    /**
     * control time out of jingle session initiation
     * 
     * @param jid
     */
    private void timeOutCheck(final JID jid, final int timeout) {
	int count = 0;
	new Thread(new Runnable() {

	    public void run() {
		int count = 0;
		while (getState(jid) != (JingleConnectionState.ESTABLISHED)) {
		    try {
			Thread.sleep(200);

			if (count < timeout) {
			    count += 200;
			} else {
			    logger.error("Time out for : " + jid
				    + " with current state : " + getState(jid));
			    connectionStates.remove(jid);
			    connectionStates.put(jid,
				    JingleConnectionState.ERROR);
			    terminateJingleSession(jid);
			    return;
			}
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}

	    }
	}).start();

    }

    private void initJingleListener(JingleSession js, final JID jid) {

	connectionStates.put(jid, JingleConnectionState.INIT);

	/* add media listener. */
	js.addMediaListener(new JingleMediaListener() {

	    public void mediaClosed(PayloadType cand) {
		logger.debug("media closed : " + jid.toString());

		// NEGOTIATION = false;
		// notifyAll();
	    }

	    public void mediaEstablished(PayloadType pt) {
		logger.debug("media established : " + jid.toString());

		// NEGOTIATION = false;
		// notifyAll();
	    }
	});

	// /* add state listener. */
	// js.addStateListener(new JingleSessionStateListener() {
	//
	// public void afterChanged(State old, State newOne) {
	// // logger.debug("session state after change new state :
	// // "+newOne.toString()+" JID: "+jid_string);
	//
	// }
	//
	// public void beforeChange(State old, State newOne)
	// throws JingleException {
	// // logger.debug("session state before change :
	// // "+old.toString()+" new : "+newOne.toString()+" JID:
	// // "+jid_string);
	//
	// }
	// });

	js.addListener(new JingleSessionListener() {

	    public void sessionClosed(String arg0, JingleSession arg1) {
		logger.info("session closed : " + jid.toString());

		/*
		 * if session closed during pending process, fallback to
		 * XEP-0096 transfer
		 */
		if (arg1.getNegotiatorState() == JingleNegotiatorState.PENDING
			&& (connectionStates.get(jid) != JingleConnectionState.ESTABLISHED && connectionStates
				.get(jid) != JingleConnectionState.ERROR)) {
		    logger.error("Session closed during pending process : "
			    + jid + " with current state : " + getState(jid));
		    connectionStates.remove(jid);
		    connectionStates.put(jid, JingleConnectionState.ERROR);

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
		// TODO Auto-generated method stub

	    }

	    public void sessionEstablished(PayloadType arg0,
		    TransportCandidate arg1, TransportCandidate arg2,
		    JingleSession arg3) {
		logger.debug("session established : " + jid.toString());
		connectionStates.remove(jid);
		connectionStates.put(jid, JingleConnectionState.ESTABLISHED);

	    }

	    public void sessionMediaReceived(JingleSession arg0, String arg1) {
		// TODO Auto-generated method stub

	    }

	    public void sessionRedirected(String arg0, JingleSession arg1) {
		// TODO Auto-generated method stub

	    }

	});

	/* transport events */
	js.addTransportListener(new JingleTransportListener() {

	    public void transportClosed(TransportCandidate cand) {
		logger.debug("transport closed: " + jid.toString());
		connectionStates.remove(jid);
		connectionStates.put(jid, JingleConnectionState.CLOSED);
		// NEGOTIATION = false;
		// notifyAll();
	    }

	    public void transportClosedOnError(XMPPException e) {
		logger.error("transport closed on error : " + jid.toString());
		connectionStates.remove(jid);
		connectionStates.put(jid, JingleConnectionState.ERROR);
		// NEGOTIATION = false;
		// notifyAll();
	    }

	    public void transportEstablished(TransportCandidate local,
		    TransportCandidate remote) {
		logger.debug("transport established : " + jid.toString());
		connectionStates.remove(jid);
		connectionStates.put(jid, JingleConnectionState.ESTABLISHED);
		// NEGOTIATION = false;
		// notifyAll();
	    }
	});

	/* time out. */
	// timeOutCheck(jid, JINGLE_TIME_OUT);
    }

    /**
     * intiate a jingle session
     * 
     * @param jid
     * @param transferData
     * @param monitor
     */
    public void createOutgoingJingleFileTransfer(JID jid,
	    JingleFileTransferData[] transferData) {

	JingleSession incoming = incomingSessions.get(jid);
	if (incoming != null) {
	    /* an incoming session already exist. */
	    logger
		    .debug("Incoming stream exists. Send data with current stream.");

	    // TODO CJ
	    // mediaManager.transferFiles(transferData);

	    return;
	}

	JingleSession outgoing = outgoingSessions.get(jid);
	if (outgoing != null) {
	    /* send new data with current connection. */

	    // TODO CJ
	    // mediaManager.transferFiles(transferData);
	    return;
	}

	// TODO CJ
	// mediaManager.transferFiles(transferData);

	try {
	    outgoing = jm.createOutgoingJingleSession(jid.toString());
	} catch (XMPPException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	initJingleListener(outgoing, jid);

	/* add to outgoing session list. */
	outgoingSessions.put(jid, outgoing);
	outgoing.startOutgoing();
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
		    outgoing = null;
		    // mediaManager.removeJingleSession(jid);
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
		    incoming = null;
		    // mediaManager..removeJingleSession(jid);
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
		outgoing = null;
		// mediaManager.removeJingleSession(jid);
		outgoingSessions.get(jid).close();
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
		incoming = null;
		// mediaManager.removeJingleSession(jid);
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
}
