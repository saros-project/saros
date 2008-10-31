package de.fu_berlin.inf.dpp.test.net.learning;

import java.io.File;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import de.fu_berlin.inf.dpp.test.SarosTest;

public class TestXMPPFileTransferSender extends SarosTest {

    static {
	XMPPConnection.DEBUG_ENABLED = true;
    }

    private FileTransferManager transferManager;

    private final String RESOURCE = "/Smack";

    private static final int MAX_TRANSFER_RETRIES = 10000;

    @Override
    public void setUp() throws XMPPException {

	super.setUp();

	transferManager = new FileTransferManager(connection);
    }

    @Override
    public void tearDown() {
	connection.disconnect();
    }

    /**
     * this method check the file transfer between two separate computers.
     * 
     * @throws InterruptedException
     * @throws XMPPException
     */
    public void testSendLargeFile() throws InterruptedException, XMPPException {

	RosterEntry entry = connection.getRoster().getEntry(User2);
	assertNotNull(entry);

	Presence p = connection.getRoster().getPresence(entry.getUser());
	assertNotNull(p);

	/* wait for receiver online status */
	int waitIntervals = 50;
	while (waitIntervals-- > 0) {
	    if (p.getMode() == Presence.Mode.available) {
		break;
	    }
	    Thread.sleep(200);
	}

	// Trying to send file
	sendFile();
    }

    private void sendFile() throws XMPPException, InterruptedException {

	Thread.sleep(1000);

	// Get the Smack.jar for sending it to the other user
	String filename = "lib/smack.jar";
	File file = new File(filename);

	assertTrue(file.exists());

	/* create output stream */
	OutgoingFileTransfer.setResponseTimeout(MAX_TRANSFER_RETRIES);
	OutgoingFileTransfer transfer = transferManager
		.createOutgoingFileTransfer(User2 + RESOURCE);

	FileTransferProcessMonitor monitor = new FileTransferProcessMonitor(
		transfer);

	/* send file. */
	transfer.sendFile(file, "Smack lib");
	monitor.start();

	int maxTime = 10;

	/* wait for transfer file finished. */
	while (monitor.isAlive() && monitor.isRunning()) {
	    Thread.sleep(1000);

	    if (maxTime-- < 0)
		fail("Transfer did not finish with 10 seconds");

	}
	monitor.closeMonitor(true);

    }
}
