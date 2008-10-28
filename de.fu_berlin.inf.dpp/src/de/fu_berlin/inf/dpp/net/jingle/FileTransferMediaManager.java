package de.fu_berlin.inf.dpp.net.jingle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.media.JingleMediaManager;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.JingleTransportManager;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;

public class FileTransferMediaManager extends JingleMediaManager {

    private static Logger logger = Logger
	    .getLogger(FileTransferMediaManager.class);

    private final List<PayloadType> payloads = new ArrayList<PayloadType>();

    private IJingleFileTransferListener listener;
    private final HashMap<JID, FileTransferSession> sessions;
    private final JingleTransportManager transportManager;

    /* tranfer data */
    private JingleFileTransferData[] transferData;
    private JingleFileTransferProcessMonitor monitor;

    public FileTransferMediaManager(JingleTransportManager transportManager) {

	super(transportManager);
	this.transportManager = transportManager;
	setupPayloads();
	this.sessions = new HashMap<JID, FileTransferSession>();
    }

    @Override
    public JingleTransportManager getTransportManager() {
	return this.transportManager;
    }

    @Override
    public JingleMediaSession createMediaSession(PayloadType payloadType,
	    TransportCandidate remote, TransportCandidate local,
	    JingleSession jingleSession) {
	/* get responder JID. */
	JID jid = new JID(jingleSession.getResponder());

	FileTransferSession session = this.sessions.get(jid);
	if (this.transferData == null) {
	    /* session for incomming transfer. */
	    session = new FileTransferSession(payloadType, remote, local,
		    "FileTransfer", jingleSession);
	} else {
	    /* session for outgoing transfer. */
	    session = new FileTransferSession(payloadType, remote, local,
		    "FileTransfer", jingleSession, this.transferData,
		    this.monitor);
	}
	session.addJingleFileTransferListener(this.listener);
	// this.session = session;

	/* add to session list. */
	this.sessions.put(jid, session);
	return session;
    }

    /**
     * Setup API supported Payloads
     */
    private void setupPayloads() {
	this.payloads.add(new PayloadType.Audio(333, "fileshare"));
    }

    @Override
    public List<PayloadType> getPayloads() {
	return this.payloads;
    }

    @Override
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
	    FileTransferSession session = this.sessions.get(data.recipient);
	    if (session != null) {
		session.sendFileData(transferData);
	    } else {
		/* incoming session registered to sender. */
		session = this.sessions.get(data.sender);
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
	for (JID jid : this.sessions.keySet()) {
	    session = this.sessions.get(jid);
	    if (session != null) {
		session.addJingleFileTransferListener(listener);
	    }
	}
    }

    public void removeJingleFileTransferListener(
	    IJingleFileTransferListener listener) {
	this.listener = null;
	FileTransferSession session = null;
	for (JID jid : this.sessions.keySet()) {
	    session = this.sessions.get(jid);
	    if (session != null) {
		session.removeJingleFileTransferListener(listener);
	    }
	}

    }

    public void removeJingleSession(JID jid) {
	FileTransferMediaManager.logger
		.debug("remove session with JID: " + jid);
	this.sessions.remove(jid);
    }

}
