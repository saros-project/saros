package de.fu_berlin.inf.dpp.net.jingle;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.jingle.IncomingJingleSession;
import org.jivesoftware.smackx.jingle.JingleManager;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.JingleSessionRequest;
import org.jivesoftware.smackx.jingle.OutgoingJingleSession;
import org.jivesoftware.smackx.jingle.JingleNegotiator.JingleException;
import org.jivesoftware.smackx.jingle.JingleNegotiator.State;
import org.jivesoftware.smackx.jingle.listeners.JingleMediaListener;
import org.jivesoftware.smackx.jingle.listeners.JingleSessionRequestListener;
import org.jivesoftware.smackx.jingle.listeners.JingleSessionStateListener;
import org.jivesoftware.smackx.jingle.listeners.JingleTransportListener;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.ICETransportManager;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter.FileTransferData;

public class JingleFileTransferManager {

	private static Logger logger = Logger
			.getLogger(JingleFileTransferManager.class);

	private XMPPConnection xmppConnection;
	private IJingleFileTransferListener transmitter;
	private JingleManager jm;

	private HashMap<JID, IncomingJingleSession> incomingSessions = null;
	private HashMap<JID, OutgoingJingleSession> outgoingSessions = null;
	
	public static int JINGLE_TIME_OUT = 7000;

	/**
	 * this map contains for all incoming and outgoing jingle sessions the
	 * appropriate connection states. If an error occur the connection state
	 * stay in list for call back setting.
	 */
	private HashMap<JID, JingleConnectionState> connectionStates = null;
	private FileTransferMediaManager mediaManager = null;

	public enum JingleConnectionState {
		INIT, ESTABLISHED, CLOSED, ERROR, DEFAULT
	}

	public JingleFileTransferManager(XMPPConnection connection,
			IJingleFileTransferListener transmitter) {
		this.xmppConnection = connection;
		this.transmitter = transmitter;
		incomingSessions = new HashMap<JID, IncomingJingleSession>();
		outgoingSessions = new HashMap<JID, OutgoingJingleSession>();
		connectionStates = new HashMap<JID, JingleConnectionState>();
		logger.debug("initialized jingle file transfer manager.");
		initialize();
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
							logger.error("Time out for : "+jid + " with current state : "+getState(jid));
							connectionStates.remove(jid);
							connectionStates.put(jid, JingleConnectionState.ERROR);
							transmitter.exceptionOccured(new JingleSessionException("Time out Exception"));
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

		/* add state listener. */
		js.addStateListener(new JingleSessionStateListener() {

			public void afterChanged(State old, State newOne) {
				// logger.debug("session state after change new state :
				// "+newOne.toString()+" JID: "+jid_string);

			}

			public void beforeChange(State old, State newOne)
					throws JingleException {
				// logger.debug("session state before change :
				// "+old.toString()+" new : "+newOne.toString()+" JID:
				// "+jid_string);

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
		timeOutCheck(jid, JINGLE_TIME_OUT);
	}

	public void initialize() {

		/* other stun server. */
		ICETransportManager icetm0 = new ICETransportManager(xmppConnection,
				"jivesoftware.com", 3478);

		mediaManager = new FileTransferMediaManager();
		mediaManager.addJingleFileTransferListener(transmitter);

		jm = new JingleManager(xmppConnection, icetm0, mediaManager);
		jm.addCreationListener(icetm0);

		jm.addJingleSessionRequestListener(new JingleSessionRequestListener() {
			public void sessionRequested(JingleSessionRequest request) {

				JID jid = new JID(request.getFrom());
				IncomingJingleSession incoming = incomingSessions.get(jid);

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
					incoming.start();
				} catch (XMPPException e) {
					e.printStackTrace();
				}

			}
		});

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

		final String jid_string = jid.toString();

		IncomingJingleSession incoming = incomingSessions.get(jid);
		if (incoming != null) {
			/* an incoming session already exist. */
			try {
				logger
						.debug("Incoming stream exists. Send data with current stream.");
				mediaManager.setTransferFile(transferData);
			} catch (JingleSessionException jse) {
				jse.printStackTrace();
			}
			return;
		}

		OutgoingJingleSession outgoing = outgoingSessions.get(jid);
		if (outgoing != null) {
			/* send new data with current connection. */
			try {
				mediaManager.setTransferFile(transferData);
			} catch (JingleSessionException jse) {
				jse.printStackTrace();
			}
			return;
		}
		try {
			// Set file info for media manager

			mediaManager.setTransferFile(transferData, monitor);
			outgoing = jm.createOutgoingJingleSession(jid.toString());

			initJingleListener(outgoing, jid);

			/* add to outgoing session list. */
			outgoingSessions.put(jid, outgoing);
			outgoing.start();
		} catch (XMPPException e1) {
			e1.printStackTrace();
		}
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

	/**
	 * remove all jingle sessions.
	 */
	public void terminateAllJingleSessions() {

		OutgoingJingleSession outgoing = null;
		for (JID jid : outgoingSessions.keySet()) {
			outgoing = outgoingSessions.get(jid);
			if (outgoing != null) {
				try {
					outgoing.terminate();
				} catch (XMPPException e1) {
					e1.printStackTrace();
				} finally {
					outgoing = null;
					mediaManager.removeJingleSession(jid);
					outgoingSessions.remove(jid);
				}
			}
		}

		IncomingJingleSession incoming = null;
		for (JID jid : incomingSessions.keySet()) {
			incoming = incomingSessions.get(jid);
			if (incoming != null) {
				try {
					incoming.terminate();
				} catch (XMPPException e1) {
					e1.printStackTrace();
				} finally {
					incoming = null;
					mediaManager.removeJingleSession(jid);
					incomingSessions.remove(jid);
				}
			}
		}

		/* reset connection state list */
		connectionStates.clear();
	}

	/**
	 * terminate and remove jingle session for jid.
	 * 
	 * @param jid
	 */
	public void terminateJingleSession(JID jid) {
		OutgoingJingleSession outgoing = outgoingSessions.get(jid);
		if (outgoing != null) {
			try {
				outgoing.terminate();
			} catch (XMPPException e1) {
				e1.printStackTrace();
			} finally {
				outgoing = null;
				mediaManager.removeJingleSession(jid);
				outgoingSessions.remove(jid);

			}
		}

		IncomingJingleSession incoming = incomingSessions.get(jid);
		if (incoming != null) {
			try {
				incoming.terminate();
			} catch (XMPPException e1) {
				e1.printStackTrace();
			} finally {
				incoming = null;
				mediaManager.removeJingleSession(jid);
				incomingSessions.remove(jid);
			}
		}

//		if(connectionStates.get(jid) != JingleConnectionState.ERROR){
			connectionStates.remove(jid);
//		}
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
}
