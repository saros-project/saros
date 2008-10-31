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

    private final String SENDER_JID = "ori78@" + SERVER;

    private final int MAX_TRANSFER_RETRIES = 10000;

    private boolean incommingFile = false;

    @Override
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
	// transferManager.getProperties().setProperty(Socks5TransferNegotiatorManager.PROPERTIES_PORT,"7777");
	// transferManager.getProperties().setProperty(IBBTransferNegotiator.PROPERTIES_BLOCK_SIZE,
	// "40690");
	Thread.sleep(1000);
    }

    @Override
    public void tearDown() {
	connection.disconnect();

    }

    public void testReceivingFile() throws InterruptedException {
	transferManager.addFileTransferListener(this);

	logger.info("wait for receiver online state.");
	RosterEntry entry = connection.getRoster().getEntry(SENDER_JID);
	assertNotNull(entry);

	Presence p = connection.getRoster().getPresence(entry.getUser());
	assertNotNull(p);

	while (!incommingFile) {
	    System.out.print(".");
	    Thread.sleep(200);
	}

	logger.info("finished");

    }

    public void fileTransferRequest(FileTransferRequest request) {
	logger.info("INCOMMING FILE");

	IncomingFileTransfer transfer = request.accept();

	/* file path */
	String filename = "/home/troll/smack_test.jar";
	File infile = new File(filename);

	try {
	    logger.info("start receiving file");

	    FileTransferProcessMonitor monitor = new FileTransferProcessMonitor(
		    transfer);
	    /* receive file */
	    transfer.recieveFile(infile);
	    monitor.start();

	    System.out.println("wait for incomming datas ...");
	    while (monitor.isAlive() && monitor.isRunning()) {
		Thread.sleep(500);
		System.out.print(".");
	    }

	    monitor.closeMonitor(true);

	} catch (XMPPException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	logger.info("receiving finished.");
	incommingFile = true;
    }

}
