package de.fu_berlin.inf.dpp.net.jingle;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.jingle.JingleSession;
import org.jivesoftware.smackx.jingle.media.JingleMediaManager;
import org.jivesoftware.smackx.jingle.media.JingleMediaSession;
import org.jivesoftware.smackx.jingle.media.PayloadType;
import org.jivesoftware.smackx.jingle.nat.TransportCandidate;

public class FileTransferMediaManager extends JingleMediaManager{

	private List<PayloadType> payloads = new ArrayList<PayloadType>();
	
	private final XMPPConnection connection;
	
	public FileTransferMediaManager(){
		setupPayloads();
		this.connection = null;
	}
	

	
	@Override
	public JingleMediaSession createMediaSession(PayloadType payloadType,
			TransportCandidate remote, TransportCandidate local,
			JingleSession jingleSession) {
		FileTransferSession session = null;
		session = new FileTransferSession(payloadType,remote,local,"FileTransfer",jingleSession);

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

}
