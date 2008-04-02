package de.fu_berlin.inf.dpp.test.net;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter;
import de.fu_berlin.inf.dpp.test.util.FileListHelper;

public class IBBFileTransferTest extends TestCase implements FileTransferListener{
	static {
		XMPPConnection.DEBUG_ENABLED = true;
	}

	private static Logger logger = Logger.getLogger(XMPPTransmitterTest.class.toString());

	private XMPPConnection connection1;
	private FileTransferManager transferManager1;

	private XMPPConnection connection2;
	private FileTransferManager transferManager2;
	
	public void setUp() throws Exception {

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

	}

	public void tearDown() {
		connection1.disconnect();
		connection2.disconnect();
	}
	
	
	public void testFileListTranfer() throws CoreException, XMPPException{
//		XMPPChatTransmitter receiver = new XMPPChatTransmitter(connection2);
		
		transferManager2.addFileTransferListener(this);
		
		ITransmitter transfer = new XMPPChatTransmitter(connection1);
		
		FileList list = FileListHelper.createFileListForDefaultProject();
		transfer.sendFileList(new JID(connection2.getUser()), list);		
	}

	public void fileTransferRequest(FileTransferRequest request) {
		String fileDescription = request.getDescription();
		
//		IncomingFileTransfer transfer = request.accept();
//		FileList fileList = receiveFileList(request);
		FileList fileList = receiveFileListBufferByteArray(request);
		assertNotNull(fileList);
	}
	
	private FileList receiveFileListBufferByteArray(FileTransferRequest request){
		FileList fileList = null;
		try {
			final IncomingFileTransfer transfer = request.accept();

			InputStream in = transfer.recieveFile();
			
			
			byte[] buffer = new byte[1024];
			int bytesRead;
			String sb = new String();
			while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
				sb += new String(buffer,0,buffer.length).toString();
				System.out.println("incomming: "+sb);
			}

			fileList = new FileList(sb.toString());
			


		} catch (Exception e) {
//			log.error(e.getMessage());
			e.printStackTrace();
			Saros.log("Exception while receiving file list", e);
			// TODO retry? but we dont catch any exception here,
			// smack might not throw them up
		}

		return fileList;
	}
	
	private FileList receiveFileList(FileTransferRequest request) {
//		log.info("Receiving file list");

		FileList fileList = null;
		try {
			final IncomingFileTransfer transfer = request.accept();

			InputStream in = transfer.recieveFile();
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuffer sb = new StringBuffer();

			try {
				String line = null;
				/* TODO: an dieser Stelle kommt es zu einem DeadLock. */
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
					sb.append(line + "\n");
				}
			} catch (Exception e) {
//				logger.error(e.getMessage());
				e.printStackTrace();
//				Saros.log("Error while receiving file list", e);
			} finally {
				reader.close();
			}

			fileList = new FileList(sb.toString());

//			log.info("Received file list");

		} catch (Exception e) {
//			log.error(e.getMessage());
			e.printStackTrace();
			Saros.log("Exception while receiving file list", e);
			// TODO retry? but we dont catch any exception here,
			// smack might not throw them up
		}

		return fileList;
	}
}
