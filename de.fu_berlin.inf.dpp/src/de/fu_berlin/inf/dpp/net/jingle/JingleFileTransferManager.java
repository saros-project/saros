package de.fu_berlin.inf.dpp.net.jingle;

import java.io.File;

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

	private XMPPConnection xmppConnection;
	private IJingleFileTransferListener transmitter;
	private JingleManager jm;
	
	private IncomingJingleSession incoming = null;
	private OutgoingJingleSession outgoing = null;
	private FileTransferMediaManager mediaManager = null;
	
	public JingleFileTransferManager(XMPPConnection connection, IJingleFileTransferListener transmitter){
		this.xmppConnection = connection;
		this.transmitter = transmitter;
		initialize();
	}
	
	public void initialize() {

		/* other stun server. */
		ICETransportManager icetm0 = new ICETransportManager(xmppConnection,
				"jivesoftware.com", 3478);
		
		mediaManager = new FileTransferMediaManager();
		mediaManager.addJingleFileTransferListener(transmitter);
		
		jm = new JingleManager(xmppConnection, icetm0,mediaManager);
		jm.addCreationListener(icetm0);

		jm.addJingleSessionRequestListener(new JingleSessionRequestListener() {
			public void sessionRequested(JingleSessionRequest request) {

				
				if (incoming != null)
					return;

				try {
					// Accept the call
					incoming = request.accept();

					// Start the call
					incoming.start();
				} catch (XMPPException e) {
					e.printStackTrace();
				}

			}
		});
	
	}
	
	public void createOutgoingJingleFileTransfer(JID jid, JingleFileTransferData[] transferData, JingleFileTransferProcessMonitor monitor){
		if (outgoing != null) return;
        try {
        	//Set file info for media manager
        	
        	mediaManager.setTransferFile(transferData, monitor);
            outgoing = jm.createOutgoingJingleSession(jid.toString());
            outgoing.start();
        }
        catch (XMPPException e1) {
            e1.printStackTrace();
        }
	}
	
	public void terminateJingleSession(){
		if (outgoing != null)
            try {
                outgoing.terminate();
            }
            catch (XMPPException e1) {
                e1.printStackTrace();
            }
            finally {
                outgoing = null;
            }
        if (incoming != null)
            try {
                incoming.terminate();
            }
            catch (XMPPException e1) {
                e1.printStackTrace();
            }
            finally {
                incoming = null;
            }
	}
}
