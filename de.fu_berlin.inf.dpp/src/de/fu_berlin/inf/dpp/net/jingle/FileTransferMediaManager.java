package de.fu_berlin.inf.dpp.net.jingle;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.media.JingleMediaManager;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter.FileTransferData;

public class FileTransferMediaManager extends JingleMediaManager {

	private static Logger logger = Logger
			.getLogger(FileTransferMediaManager.class);

	private List<PayloadType> payloads = new ArrayList<PayloadType>();

	private final XMPPConnection connection;
	private IJingleFileTransferListener listener;
	private HashMap<JID, FileTransferSession> sessions;
	// private FileTransferSession session;

	/* tranfer data */
	private JingleFileTransferData[] transferData;
	private JingleFileTransferProcessMonitor monitor;

	public FileTransferMediaManager() {
		setupPayloads();
		this.connection = null;
		sessions = new HashMap<JID, FileTransferSession>();
	}

	@Override
	public JingleMediaSession createMediaSession(PayloadType payloadType,
			TransportCandidate remote, TransportCandidate local,
			JingleSession jingleSession) {
		/* get responder JID. */
		JID jid = new JID(jingleSession.getResponder());

		FileTransferSession session = sessions.get(jid);
		if (transferData == null) {
			/* session for incomming transfer. */
			session = new FileTransferSession(payloadType, remote, local,
					"FileTransfer", jingleSession);
		} else {
			/* session for outgoing transfer. */
			session = new FileTransferSession(payloadType, remote, local,
					"FileTransfer", jingleSession, transferData, monitor);
		}
		session.addJingleFileTransferListener(listener);
		// this.session = session;

		/* add to session list. */
		sessions.put(jid, session);
		return session;
	}

	/**
	 * Setup API supported Payloads
	 */
	private void setupPayloads() {
		payloads.add(new PayloadType.Audio(333, "fileshare"));
	}

	@Override
	public List<PayloadType> getPayloads() {
		return payloads;
	}

	public PayloadType getPreferredPayloadType() {
		return new PayloadType.Audio(333, "fileshare");
	}

	/**
	 * set the file data for transfer
	 * 
	 * @param transferData
	 */
	public void setTransferFile(JingleFileTransferData[] transferData,
			JingleFileTransferProcessMonitor monitor) {
		this.transferData = transferData;
		this.monitor = monitor;
	}

	/**
	 * send new transfer data over existing stream.
	 * 
	 * @param transferData
	 */
	public void setTransferFile(JingleFileTransferData[] transferData)
			throws JingleSessionException {
		// this.transferData = transferData;

		for (JingleFileTransferData data : transferData) {
			/* set data for appropriate session. */
			FileTransferSession session = sessions.get(data.recipient);
			if (session != null) {
				session.sendFileData(transferData);
			} else {
				/* incoming session registered to sender. */
				session = sessions.get(data.sender);
				if (session != null) {
					session.sendFileData(transferData);
				}
			}
		}
	}

	@Deprecated
	public void setTransferMonitor(JingleFileTransferProcessMonitor monitor) {
		this.monitor = monitor;
	}

	// public void sendFileData() throws JingleSessionException {
	// if(session == null){
	// throw new JingleSessionException("Jingle Session not exist.");
	// }
	//		
	//		
	// }

	/**
	 * add listener to all active sessions.
	 */
	public void addJingleFileTransferListener(
			IJingleFileTransferListener listener) {
		this.listener = listener;
		FileTransferSession session = null;
		for (JID jid : sessions.keySet()) {
			session = sessions.get(jid);
			if (session != null) {
				session.addJingleFileTransferListener(listener);
			}
		}
	}

	public void removeJingleFileTransferListener(
			IJingleFileTransferListener listener) {
		this.listener = null;
		FileTransferSession session = null;
		for (JID jid : sessions.keySet()) {
			session = sessions.get(jid);
			if (session != null) {
				session.removeJingleFileTransferListener(listener);
			}
		}

	}

	public void removeJingleSession(JID jid) {
		logger.debug("remove session with JID: " + jid);
		sessions.remove(jid);
	}

}
