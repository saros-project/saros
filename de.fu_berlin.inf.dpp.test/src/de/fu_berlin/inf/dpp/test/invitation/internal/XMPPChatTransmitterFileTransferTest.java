package de.fu_berlin.inf.dpp.test.invitation.internal;

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter;
import de.fu_berlin.inf.dpp.test.invitation.internal.mock.MockOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.test.util.FileListHelper;
import de.fu_berlin.inf.dpp.test.util.ResourceHelper;

/**
 * this testclass simulate the behavior of file transfer activities of the
 * XMPPChatTransmitter.
 * 
 * @author troll
 * 
 */
public class XMPPChatTransmitterFileTransferTest extends TestCase {

    static {
	XMPPConnection.DEBUG_ENABLED = true;
    }

    private static Logger logger = Logger
	    .getLogger(XMPPChatTransmitterFileTransferTest.class.toString());

    private XMPPConnection connection1;
    private FileTransferManager transferManager1;

    private XMPPConnection connection2;
    private FileTransferManager transferManager2;

    @Override
    public void setUp() throws Exception {
	// PropertyConfigurator.configureAndWatch("log4j.properties", 60 *
	// 1000);
	// Logger logger = Logger.getLogger("de.fu_berlin.inf.dpp");

	ConnectionConfiguration conConfig = new ConnectionConfiguration(
		"jabber.org");
	// conConfig.setSocketFactory(SocketFactory.getDefault());

	conConfig.setReconnectionAllowed(true);
	try {
	    connection1 = new XMPPConnection("jabber.cc");
	    // connection1 = new XMPPConnection(conConfig);

	    // while (!connection1.isAuthenticated()) {
	    // System.out.println("connecting user1");
	    connection1.connect();

	    connection1.login("ori79", "123456");
	    // }
	} catch (Exception e) {
	    e.printStackTrace();
	    XMPPConnection connection = new XMPPConnection("jabber.org");
	    connection.connect();
	    connection.getAccountManager().createAccount("ori78", "123456");
	}
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

    }

    @Override
    public void tearDown() {
	connection1.disconnect();
	connection2.disconnect();
    }

    /**
     * this method test the incomming file transfer of a file list for an
     * outgoing invitation process.
     * 
     * @throws CoreException
     * @throws XMPPException
     */
    public void testIncommingFileListForOutgoingInvitationProcess()
	    throws CoreException, XMPPException {

	// connection1 = new XMPPConnection(conConfig);

	IProject project = ResourceHelper.getDefaultProject();
	ITransmitter transmitter = new XMPPChatTransmitter(connection1);

	MockOutgoingInvitationProcess out = new MockOutgoingInvitationProcess(
		transmitter, new JID(connection2.getUser()), "TestFileList",
		project);
	// transmitter.addInvitationProcess(out);

	/* send filelist to outgoing process */
	FileList list = FileListHelper
		.createFielListForProject(ResourceHelper.RECEIVED_TEST_PROJECT);
	ITransmitter transmitter2 = new XMPPChatTransmitter(connection2);
	transmitter2.sendFileList(new JID(connection1.getUser()), list);
    }

}
