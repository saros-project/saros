package de.fu_berlin.inf.dpp.net.jingle;

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
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.ICETransportManager;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;

public class JingleFileTransferManager {

    public enum JingleConnectionState {
	CLOSED, DEFAULT, ERROR, ESTABLISHED, INIT
    }

    public static int JINGLE_TIME_OUT = 10000;
    private static Logger logger = Logger
	    .getLogger(JingleFileTransferManager.class);
    /**
     * this map contains for all incoming and outgoing jingle sessions the
     * appropriate connection states. If an error occur the connection state
     * stay in list for call back setting.
     */
    private HashMap<JID, JingleConnectionState> connectionStates = null;

    private HashMap<JID, JingleSession> incomingSessions = null;
    private JingleManager jm;

    private FileTransferMediaManager mediaManager = null;

    private HashMap<JID, JingleSession> outgoingSessions = null;
    private final IJingleFileTransferListener transmitter;

    private final XMPPConnection xmppConnection;

    public JingleFileTransferManager(XMPPConnection connection,
	    IJingleFileTransferListener transmitter) {
	this.xmppConnection = connection;
	this.transmitter = transmitter;
	this.incomingSessions = new HashMap<JID, JingleSession>();
	this.outgoingSessions = new HashMap<JID, JingleSession>();
	this.connectionStates = new HashMap<JID, JingleConnectionState>();
	JingleFileTransferManager.logger
		.debug("initialized jingle file transfer manager.");
	initialize();
    }

    /**
     * intiate a jingle session
     * 
     * @param jid
     * @param transferData
     * @param monitor
     */
    public void createOutgoingJingleFileTransfer(JID jid,
	    JingleFileTransferData[] transferData,
	    JingleFileTransferProcessMonitor monitor) {

	jid.toString();

	JingleSession incoming = this.incomingSessions.get(jid);
	if (incoming != null) {
	    /* an incoming session already exist. */
	    try {
		JingleFileTransferManager.logger
			.debug("Incoming stream exists. Send data with current stream.");
		this.mediaManager.setTransferFile(transferData);
	    } catch (JingleSessionException jse) {
		jse.printStackTrace();
	    }
	    return;
	}

	JingleSession outgoing = this.outgoingSessions.get(jid);
	if (outgoing != null) {
	    /* send new data with current connection. */
	    try {
		this.mediaManager.setTransferFile(transferData);
	    } catch (JingleSessionException jse) {
		jse.printStackTrace();
	    }
	    return;
	}
	try {
	    // Set file info for media manager

	    this.mediaManager.setTransferFile(transferData, monitor);
	    outgoing = this.jm.createOutgoingJingleSession(jid.toString());

	    initJingleListener(outgoing, jid);

	    /* add to outgoing session list. */
	    this.outgoingSessions.put(jid, outgoing);
	    outgoing.startOutgoing();
	} catch (XMPPException e1) {
	    e1.printStackTrace();
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
	JingleConnectionState state = this.connectionStates.get(jid);
	return state;
    }

    public void initialize() {

	/* other stun server. */
	ICETransportManager icetm0 = new ICETransportManager(
		this.xmppConnection, "jivesoftware.com", 3478);

	this.mediaManager = new FileTransferMediaManager(icetm0);
	this.mediaManager.addJingleFileTransferListener(this.transmitter);

	List<JingleMediaManager> medias = new Vector<JingleMediaManager>();
	medias.add(this.mediaManager);

	this.jm = new JingleManager(this.xmppConnection, medias);
	this.jm.addCreationListener(icetm0);

	this.jm
		.addJingleSessionRequestListener(new JingleSessionRequestListener() {
		    public void sessionRequested(JingleSessionRequest request) {

			JID jid = new JID(request.getFrom());
			JingleSession incoming = JingleFileTransferManager.this.incomingSessions
				.get(jid);

			if (incoming != null) {
			    return;
			}

			try {

			    // Accept the call
			    incoming = request.accept();

			    initJingleListener(incoming, new JID(incoming
				    .getInitiator()));
			    /* put to current session list. */
			    JingleFileTransferManager.this.incomingSessions
				    .put(jid, incoming);
			    // Start the call
			    incoming.startIncoming();
			} catch (XMPPException e) {
			    e.printStackTrace();
			}

		    }
		});

    }

    // private IncomingJingleSession getIncomingJingleSession(JID jid){
    // return incomingSessions.get(jid);
    // }
    //	
    // private OutgoingJingleSession getOutgoingJingleSession(JID jid){
    // return outgoingSessions.get(jid);
    // }

    // /**
    // * send datas with active jingle session.
    // * @param transferData
    // */
    // public void sendFileDatas(JingleFileTransferData[] transferData){
    //		
    // }

    private void initJingleListener(JingleSession js, final JID jid) {

	this.connectionStates.put(jid, JingleConnectionState.INIT);

	/* add media listener. */
	js.addMediaListener(new JingleMediaListener() {

	    public void mediaClosed(PayloadType cand) {
		JingleFileTransferManager.logger.debug("media closed : "
			+ jid.toString());

		// NEGOTIATION = false;
		// notifyAll();
	    }

	    public void mediaEstablished(PayloadType pt) {
		JingleFileTransferManager.logger.debug("media established : "
			+ jid.toString());

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
		JingleFileTransferManager.logger.info("session closed : "
			+ jid.toString());

		/*
		 * if session closed during pending process, fallback to
		 * XEP-0096 transfer
		 */
		if ((arg1.getNegotiatorState() == JingleNegotiatorState.PENDING)
			&& ((JingleFileTransferManager.this.connectionStates
				.get(jid) != JingleConnectionState.ESTABLISHED) && (JingleFileTransferManager.this.connectionStates
				.get(jid) != JingleConnectionState.ERROR))) {
		    JingleFileTransferManager.logger
			    .error("Session closed during pending process : "
				    + jid + " with current state : "
				    + getState(jid));
		    JingleFileTransferManager.this.connectionStates.remove(jid);
		    JingleFileTransferManager.this.connectionStates.put(jid,
			    JingleConnectionState.ERROR);
		    JingleFileTransferManager.this.transmitter
			    .exceptionOccured(new JingleSessionException(
				    "Session closed during establishing process",
				    jid));

		}
		JingleFileTransferManager.this.connectionStates.remove(jid);
		JingleFileTransferManager.this.connectionStates.put(jid,
			JingleConnectionState.CLOSED);

	    }

	    public void sessionClosedOnError(XMPPException arg0,
		    JingleSession arg1) {
		JingleFileTransferManager.logger
			.error("session closed on error : " + jid.toString());
		JingleFileTransferManager.this.connectionStates.remove(jid);
		JingleFileTransferManager.this.connectionStates.put(jid,
			JingleConnectionState.ERROR);

	    }

	    public void sessionDeclined(String arg0, JingleSession arg1) {
		// TODO Auto-generated method stub

	    }

	    public void sessionEstablished(PayloadType arg0,
		    TransportCandidate arg1, TransportCandidate arg2,
		    JingleSession arg3) {
		JingleFileTransferManager.logger.debug("session established : "
			+ jid.toString());
		JingleFileTransferManager.this.connectionStates.remove(jid);
		JingleFileTransferManager.this.connectionStates.put(jid,
			JingleConnectionState.ESTABLISHED);

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
		JingleFileTransferManager.logger.debug("transport closed: "
			+ jid.toString());
		JingleFileTransferManager.this.connectionStates.remove(jid);
		JingleFileTransferManager.this.connectionStates.put(jid,
			JingleConnectionState.CLOSED);
		// NEGOTIATION = false;
		// notifyAll();
	    }

	    public void transportClosedOnError(XMPPException e) {
		JingleFileTransferManager.logger
			.error("transport closed on error : " + jid.toString());
		JingleFileTransferManager.this.connectionStates.remove(jid);
		JingleFileTransferManager.this.connectionStates.put(jid,
			JingleConnectionState.ERROR);
		// NEGOTIATION = false;
		// notifyAll();
	    }

	    public void transportEstablished(TransportCandidate local,
		    TransportCandidate remote) {
		JingleFileTransferManager.logger
			.debug("transport established : " + jid.toString());
		JingleFileTransferManager.this.connectionStates.remove(jid);
		JingleFileTransferManager.this.connectionStates.put(jid,
			JingleConnectionState.ESTABLISHED);
		// NEGOTIATION = false;
		// notifyAll();
	    }
	});

	/* time out. */
	// timeOutCheck(jid, JINGLE_TIME_OUT);
    }

    /**
     * Terminate Jingle connection and set error state for given peer.
     * 
     * @param jid
     */
    public void setJingleErrorState(JID jid) {
	if (jid != null) {
	    JingleFileTransferManager.logger
		    .debug("Terminate Jingle Session for " + jid);
	    terminateJingleSession(jid);
	    this.connectionStates.remove(jid);
	    this.connectionStates.put(jid, JingleConnectionState.ERROR);
	} else {
	    JingleFileTransferManager.logger
		    .warn("JID is null. Jingle error state couldn't be set.");
	}
    }

    /**
     * remove all jingle sessions.
     */
    public void terminateAllJingleSessions() {

	JingleFileTransferManager.logger
		.debug("Terminate all jingle sessions.");

	JingleSession outgoing = null;
	for (JID jid : this.outgoingSessions.keySet()) {
	    outgoing = this.outgoingSessions.get(jid);
	    if (outgoing != null) {
		try {
		    outgoing.terminate();
		} catch (XMPPException e1) {
		    e1.printStackTrace();
		} finally {
		    outgoing = null;
		    this.mediaManager.removeJingleSession(jid);
		    this.outgoingSessions.remove(jid);
		}
	    }
	}

	JingleSession incoming = null;
	for (JID jid : this.incomingSessions.keySet()) {
	    incoming = this.incomingSessions.get(jid);
	    if (incoming != null) {
		try {
		    incoming.terminate();
		} catch (XMPPException e1) {
		    e1.printStackTrace();
		} finally {
		    incoming = null;
		    this.mediaManager.removeJingleSession(jid);
		    this.incomingSessions.remove(jid);
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
	JingleSession outgoing = this.outgoingSessions.get(jid);
	if (outgoing != null) {
	    try {
		outgoing.terminate();
	    } catch (XMPPException e1) {
		// e1.printStackTrace();
		JingleFileTransferManager.logger.error(
			"Error during terminate outgoing jingle session with JID : "
				+ jid, e1);
	    } finally {
		outgoing = null;
		this.mediaManager.removeJingleSession(jid);
		this.outgoingSessions.get(jid).close();
		this.outgoingSessions.remove(jid);
		JingleFileTransferManager.logger
			.debug("Terminate outgoing jingle session with JID : "
				+ jid);
	    }
	}

	JingleSession incoming = this.incomingSessions.get(jid);
	if (incoming != null) {
	    try {
		incoming.terminate();
	    } catch (XMPPException e1) {
		// e1.printStackTrace();
		JingleFileTransferManager.logger.error(
			"Error during terminate incoming jingle session with JID : "
				+ jid, e1);
	    } finally {
		incoming = null;
		this.mediaManager.removeJingleSession(jid);
		this.incomingSessions.remove(jid);
		JingleFileTransferManager.logger
			.debug("Terminate incoming jingle session with JID : "
				+ jid);
	    }
	}

	// if(connectionStates.get(jid) != JingleConnectionState.ERROR){
	// connectionStates.remove(jid);
	// }
    }
}
