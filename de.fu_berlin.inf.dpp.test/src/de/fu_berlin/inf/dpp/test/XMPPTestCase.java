package de.fu_berlin.inf.dpp.test;

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;

import de.fu_berlin.inf.dpp.test.invitation.internal.XMPPChatTransmitterFileTransferTest;

public abstract class XMPPTestCase extends TestCase {

    static {
	XMPPConnection.DEBUG_ENABLED = true;
    }

    private static Logger logger = Logger
	    .getLogger(XMPPChatTransmitterFileTransferTest.class.toString());

    protected XMPPConnection connection1 = null;
    protected FileTransferManager transferManager1 = null;

    protected XMPPConnection connection2 = null;
    protected FileTransferManager transferManager2 = null;

    private String server;

    public XMPPTestCase() {
	this.server = "jabber.cc";
    }

    public XMPPTestCase(String server) {
	this.server = server;
    }

    @Override
    public void setUp() throws Exception {
	// PropertyConfigurator.configureAndWatch("log4j.properties", 60 *
	// 1000);
	// Logger logger = Logger.getLogger("de.fu_berlin.inf.dpp");

	ConnectionConfiguration conConfig = new ConnectionConfiguration(
		"jabber.org");
	// conConfig.setSocketFactory(SocketFactory.getDefault());

	conConfig.setReconnectionAllowed(true);
	// try{
	connection1 = new XMPPConnection(server);
	// connection1 = new XMPPConnection(conConfig);

	// while (!connection1.isAuthenticated()) {
	// System.out.println("connecting user1");
	connection1.connect();

	connection1.login("ori79", "123456");
	// }
	// } catch(Exception e){
	// e.printStackTrace();
	// XMPPConnection connection = new XMPPConnection("jabber.org");
	// connection.connect();
	// connection.getAccountManager().createAccount("ori78", "123456");
	// }
	transferManager1 = new FileTransferManager(connection1);
	logger.info("connection 1 established.");
	Thread.sleep(1000);

	connection2 = new XMPPConnection(server);
	// while (!connection2.isAuthenticated()) {

	connection2.connect();
	connection2.login("ori78", "123456");
	// }
	logger.info("connection 1 established.");
	transferManager2 = new FileTransferManager(connection2);

	Thread.sleep(1000);

    }

    @Override
    public void tearDown() {
	connection1.disconnect();
	connection2.disconnect();
    }
}
