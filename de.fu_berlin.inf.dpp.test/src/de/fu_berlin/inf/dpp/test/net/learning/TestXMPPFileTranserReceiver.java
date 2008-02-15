package de.fu_berlin.inf.dpp.test.net.learning;

import java.io.File;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

/**
 * this test class received xmpp file transfer data.
 * 
 * @author troll
 * 
 */
public class TestXMPPFileTranserReceiver extends TestCase implements
		FileTransferListener {

	static {
		XMPPConnection.DEBUG_ENABLED = true;
	}

	private static Logger logger = Logger
			.getLogger(TestXMPPFileTransferSender.class.toString());

	private XMPPConnection connection;
	private FileTransferManager transferManager;
	
	private final String SERVER = "jabber.org";
	
	private final String SENDER_JID = "ori78@"+SERVER;
	
	private boolean incommingFile = false;

	public void setUp() throws Exception {
		// PropertyConfigurator.configureAndWatch("log4j.properties", 60 *
		// 1000);
		// Logger logger = Logger.getLogger("de.fu_berlin.inf.dpp");

		connection = new XMPPConnection(SERVER);
		// while (!connection2.isAuthenticated()) {

		connection.connect();
		connection.login("ori79", "123456");
		// }
		logger.info("connection 1 established.");
		transferManager = new FileTransferManager(connection);

		Thread.sleep(1000);
	}

	public void tearDown() {
		connection.disconnect();

	}

	public void testReceivingFile() throws InterruptedException{
		transferManager.addFileTransferListener(this);
		
		logger.info("wait for receiver online state.");
		RosterEntry entry = connection.getRoster().getEntry(SENDER_JID);
		assertNotNull(entry);
		
		Presence p = connection.getRoster().getPresence(entry.getUser());
		assertNotNull(p);
		
		while(!incommingFile){
//			System.out.print(".");
			Thread.sleep(200);
		}
		
		logger.info("finished");
		
	}
	
	public void fileTransferRequest(FileTransferRequest request)  {
		
		IncomingFileTransfer transfer = request.accept();
		
		/*file path*/
		String filename = "/home/troll/smack_test.jar";
		File infile = new File(filename);
		
		
		try {
			logger.info("start receiving file");
			
			/*receive file*/
			transfer.recieveFile(infile);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		incommingFile = true;
		logger.info("receiving finished.");
	}

}
