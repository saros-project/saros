package de.fu_berlin.inf.dpp.test.net;

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;

public class JingleFileTransferTest extends TestCase {

    static {
	XMPPConnection.DEBUG_ENABLED = true;
    }

    private static Logger logger = Logger.getLogger(XMPPTransmitterTest.class
	    .toString());

    private XMPPConnection connection1;
    private FileTransferManager transferManager1;

    private XMPPConnection connection2;
    private FileTransferManager transferManager2;

    @Override
    public void setUp() throws Exception {
	// PropertyConfigurator.configureAndWatch("log4j.properties", 60 *
	// 1000);
	// Logger logger = Logger.getLogger("de.fu_berlin.inf.dpp");

	connection1 = new XMPPConnection("jabber.cc");
	// while (!connection1.isAuthenticated()) {
	// System.out.println("connecting user1");
	connection1.connect();
	connection1.login("ori79", "123456");
	// }
	transferManager1 = new FileTransferManager(connection1);
	logger.info("connection 1 established.");
	Thread.sleep(1000);

	connection2 = new XMPPConnection("jabber.cc");
	// while (!connection2.isAuthenticated()) {

	connection2.connect();
	connection2.login("ori78", "123456");
	// }
	logger.info("connection 1 established.");
	transferManager2 = new FileTransferManager(connection2);

	Thread.sleep(1000);

	// mock = new MockInvitationProcess(this, null, null);
    }

    @Override
    public void tearDown() {
	connection1.disconnect();
	connection2.disconnect();
    }

    public void testJingleFileListTransfer() {

    }
}
