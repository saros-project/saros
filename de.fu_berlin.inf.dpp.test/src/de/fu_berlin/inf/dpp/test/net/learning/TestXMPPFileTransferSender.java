package de.fu_berlin.inf.dpp.test.net.learning;

import java.io.File;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.net.XMPPTransmitterTest;
import de.fu_berlin.inf.dpp.test.net.mock.MockInvitationProcess;

public class TestXMPPFileTransferSender extends TestCase implements PacketListener {

	static {
		XMPPConnection.DEBUG_ENABLED = true;
	}

	private static Logger logger = Logger.getLogger(TestXMPPFileTransferSender.class.toString());

	private XMPPConnection connection;
	private FileTransferManager transferManager;

	private final String SERVER = "jabber.org";
	
	private final String RECEIVER_JID = "ori79@"+SERVER;
	
	
	private static final int MAX_TRANSFER_RETRIES = 5000;
	
	private boolean send = false;

	public void setUp() throws Exception {
//		PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
//		Logger logger = Logger.getLogger("de.fu_berlin.inf.dpp");

		connection = new XMPPConnection(SERVER);
		// while (!connection1.isAuthenticated()) {
		// System.out.println("connecting user1");
		connection.connect();
		connection.login("ori78", "123456");
		// }
		transferManager = new FileTransferManager(connection);
		logger.info("connection 1 established.");
		Thread.sleep(1000);

		
//		mock = new MockInvitationProcess(this, null, null);
	}
	
	public void tearDown(){
		connection.disconnect();
	}
	
	/**
	 * this method check the file transfer between two separate 
	 * computers.
	 * @throws InterruptedException 
	 * @throws XMPPException 
	 */
	public void testSendLargeFile() throws InterruptedException, XMPPException{
		
		connection.addPacketListener(this, null);
		
		RosterEntry entry = connection.getRoster().getEntry(RECEIVER_JID);
		assertNotNull(entry);
		
		Presence p = connection.getRoster().getPresence(entry.getUser());
		assertNotNull(p);
	
		/*wait for receiver online status*/
		logger.info("wait for receiver online state.");
		while(!send){
			System.out.print(".");
			Thread.sleep(200);
		}
		
		logger.info("try to send file");
		sendFile();
		
		logger.info("file sended. ");
	}

	private void sendFile() throws XMPPException{
		String filename = "lib/smack.jar";
		File file = new File(filename);
		
		assertTrue(file.exists());
		
		/*create output stream */
		OutgoingFileTransfer.setResponseTimeout(MAX_TRANSFER_RETRIES);
		OutgoingFileTransfer transfer = transferManager.createOutgoingFileTransfer(RECEIVER_JID);
		/* send file. */
		transfer.sendFile(file, "Smack lib");
		
		/* wait for transfer file finished.*/
		
	}

	public void processPacket(Packet packet) {
		if (!packet.getFrom().equals(connection.getUser())) {

			/*
			 * 1. überprüfen ob es eine subscribe anfrage ist 2. überprüfen, ob
			 * user im in liste ist. 3. hinzufügen des accounts.
			 */
			if (packet instanceof Presence) {

				Presence p = (Presence) packet;

				/* this states handled by roster listener. */
				if (p.getType().equals(Presence.Type.available)) {
					
					if(StringUtils.parseBareAddress(p.getFrom()).equals(RECEIVER_JID)){
						logger.info("Presence RECEIVER_JID " + p.getFrom() + " " + p.getType());
						send = true;
						return;
					}
					
					logger.info("Presence " + p.getFrom() + " " + p.getType());
					
					return;
				}
			}
		}
	}


}
