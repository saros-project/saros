package de.fu_berlin.inf.dpp.net.jingle;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.media.JingleMediaManager;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter.FileTransferData;

public class FileTransferMediaManager extends JingleMediaManager{

	private List<PayloadType> payloads = new ArrayList<PayloadType>();
	
	private final XMPPConnection connection;
	private IJingleFileTransferListener listener;
	
	/* tranfer data*/
	private JingleFileTransferData[] transferData;
	private JingleFileTransferProcessMonitor monitor;
	
	public FileTransferMediaManager(){
		setupPayloads();
		this.connection = null;
	}
	
	@Override
	public JingleMediaSession createMediaSession(PayloadType payloadType,
			TransportCandidate remote, TransportCandidate local,
			JingleSession jingleSession) {
		FileTransferSession session = null;
		if(transferData == null){
			/* session for incomming transfer. */
			session = new FileTransferSession(payloadType,remote,local,"FileTransfer",jingleSession);
		}else{
			/* session for outgoing transfer. */
			session = new FileTransferSession(payloadType,remote,local,"FileTransfer",jingleSession, transferData, monitor);
		}
		session.addJingleFileTransferListener(listener);
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
	 * @param transferData
	 */
	public void setTransferFile(JingleFileTransferData[] transferData, JingleFileTransferProcessMonitor monitor) {
		this.transferData = transferData;
		this.monitor = monitor;
	}
	
	public void setTransferMonitor(JingleFileTransferProcessMonitor monitor){
		this.monitor = monitor;
	}
	
	public void addJingleFileTransferListener(IJingleFileTransferListener listener){
		this.listener = listener;
	}
	
	public void removeJingleFileTransferListener(IJingleFileTransferListener listener){
		this.listener = null;
	}

}
