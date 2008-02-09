package de.fu_berlin.inf.dpp.test.net;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.test.net.mock.MockInvitationProcess;
import de.fu_berlin.inf.dpp.test.util.FileListHelper;
import de.fu_berlin.inf.dpp.test.util.ResourceHelper;
import junit.framework.TestCase;

public class XMPPTransmitterTest extends TestCase implements
		FileTransferListener, ITransmitter, IFileTransferCallback {
	
	static {
		XMPPConnection.DEBUG_ENABLED = true;
	}

	private static Logger logger = Logger.getLogger(XMPPTransmitterTest.class.toString());

	private XMPPConnection connection1;
	private FileTransferManager transferManager1;

	private XMPPConnection connection2;
	private FileTransferManager transferManager2;

	private MockInvitationProcess mock;
	
	public void setUp() throws Exception {
//		PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
//		Logger logger = Logger.getLogger("de.fu_berlin.inf.dpp");

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
		
//		mock = new MockInvitationProcess(this, null, null);
	}

	public void tearDown() {
		connection1.disconnect();
		connection2.disconnect();
	}
	
	public void xtestSendFileList() throws CoreException, XMPPException{
//		transferManager2.addFileTransferListener(new ReceivedSingleFileListener());
		transferManager2.addFileTransferListener(new ReceiveFileListFileTransferListener());
		
		ITransmitter transfer = new XMPPChatTransmitter(connection1);
		FileList list = FileListHelper.createFileListForDefaultProject();
		transfer.sendFileList(new JID(connection2.getUser()), list);
		
	}
	
	public void testTransferFileFunction() throws CoreException, XMPPException{
		transferManager2.addFileTransferListener(new ReceivedSingleFileListener());
		
		ITransmitter transfer = new XMPPChatTransmitter(connection1);
		//TODO: project initialisieren
//		transfer.sendFile(new JID(connection2.getUser()), ResourceHelper.getDefaultProject(), ResourceHelper.getDefaultFile().getProjectRelativePath(), null);
		transfer.sendFile(new JID(connection2.getUser()), ResourceHelper.getDefaultProject(), ResourceHelper.getFile("src/First.java").getProjectRelativePath(), null);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void xtestReceivedFileList() throws CoreException, XMPPException{
//		transferManager2.addFileTransferListener(new ReceiveFileListFileTransferListener());
		
		ITransmitter transfer = new XMPPChatTransmitter(connection1);

		FileList list = FileListHelper.createFileListForDefaultProject();
		transfer.sendFileList(new JID(connection2.getUser()), list);
	}
	
	/*
	 * 1. Invitation mock process einfügen 2. File Transfer simulieren.
	 */
//	public void testReceivedRessource() {
//		sendFileWithFileTransfer();
//	}
	
	

	/* file transfer methods */
	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		
		IncomingFileTransfer transfer = request.accept();
		String filename = request.getFileName()+"."+request.getRequestor().substring(0, request.getRequestor().indexOf("@"));
		logger.log(Level.FINE,filename);
		
		try {
			InputStream input = transfer.recieveFile();
			
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		// TODO Auto-generated method stub
//		// Check to see if the request should be accepted
//		// if(shouldAccept(request)) {
//		// Accept it
//
//		
//		
//		logger.info("Incomming file "+request.getRequestor());
//		IncomingFileTransfer transfer = request.accept();
//		String filename = request.getFileName()+"."+request.getRequestor().substring(0, request.getRequestor().indexOf("@"));
//		try {
//			
////			transfer.recieveFile(new File(filename));
//			InputStream input = transfer.recieveFile();
//			mock.resourceReceived(null, new Path(transfer.getFileName()+".received"), input);
//		} catch (XMPPException e) {
//			// TODO Auto-generated catch block
//			logger.log(Level.ALL,e.getMessage());
//		}
//
////		if (new File(filename).exists()) {
//////			new File("Testfile2.txt").deleteOnExit();
////			logger.debug("File exists and will delete.");
////		}
	}

//	/* helper methods. */
//	protected void sendFileWithFileTransfer() {
//		transferManager2
//				.addFileTransferListener(this);
//
//		// Create the outgoing file transfer
//		OutgoingFileTransfer transfer = transferManager1
//				.createOutgoingFileTransfer(connection2.getUser());
//
//		// Send the file
//		try {
//			transfer.sendFile(new File("Testfile.txt"),
//					"You won't believe this!");
//
//			while (!transfer.isDone()) {
//				if (transfer.getStatus().equals(Status.error)) {
//					logger.log(Level.WARNING,"ERROR!!! " + transfer.getError());
//				} else {
//					logger.log(Level.FINE,"Status : " + transfer.getStatus());
//					logger.log(Level.FINE,"Progress : " + transfer.getProgress());
//				}
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//
//			if (transfer.getStatus().equals(Status.complete)) {
//				logger.log(Level.FINE,"transfer complete");
//			}
//		} catch (XMPPException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	

	@Override
	public void addInvitationProcess(IInvitationProcess invitation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeInvitationProcess(IInvitationProcess invitation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendActivities(ISharedProject sharedProject,
			List<TimedActivity> activities) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendCancelInvitationMessage(JID jid, String errorMsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendFile(JID recipient, IProject project,
			IPath path, IFileTransferCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendFile(JID recipient, IProject project, IPath path,
			int timestamp, IFileTransferCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendFileList(JID jid, FileList fileList) throws XMPPException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendInviteMessage(ISharedProject sharedProject, JID jid,
			String description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendJoinMessage(ISharedProject sharedProject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendLeaveMessage(ISharedProject sharedProject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRemainingFiles() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRemainingMessages() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRequestForActivity(ISharedProject sharedProject,
			int timestamp, boolean andup) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRequestForFileListMessage(JID recipient) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendUserListTo(JID to, List<User> participants) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setXMPPConnection(XMPPConnection connection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileSent(IPath path) {
		logger.log(Level.FINE,"File sent "+path);
		
	}

	@Override
	public void fileTransferFailed(IPath path, Exception e) {
		logger.log(Level.WARNING,"File transfer failed: "+e.getMessage());
		
	}
}
