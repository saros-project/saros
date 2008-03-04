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
import org.jivesoftware.smackx.jingle.JingleSessionRequest;
import org.jivesoftware.smackx.jingle.OutgoingJingleSession;
import org.jivesoftware.smackx.jingle.listeners.JingleSessionRequestListener;
import org.jivesoftware.smackx.jingle.nat.ICETransportManager;

import de.fu_berlin.inf.dpp.FileList;
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
	private FileTransferMediaManager mediaManager = null;

	public JingleFileTransferManager(XMPPConnection connection,
			IJingleFileTransferListener transmitter) {
		this.xmppConnection = connection;
		this.transmitter = transmitter;
		incomingSessions = new HashMap<JID, IncomingJingleSession>();
		outgoingSessions = new HashMap<JID, OutgoingJingleSession>();
		initialize();
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
					incomingSessions.remove(jid);
				}
			}
		}
	}

	/**
	 * terminate and remove jingle session for jid.
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
				incomingSessions.remove(jid);
			}
		}
	}
}
