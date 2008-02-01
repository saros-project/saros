package de.fu_berlin.inf.dpp.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;

public class SmackFileTransferTest extends TestCase {
	static {
		XMPPConnection.DEBUG_ENABLED = true;
	}

	private static Logger logger = Logger
			.getLogger(SmackFileTransferTest.class);

	private static final int MAX_TRANSFER_RETRIES = 5;

	protected class FileTransferListenerImpl implements FileTransferListener {

		@Override
		public void fileTransferRequest(FileTransferRequest request) {
			// Check to see if the request should be accepted
			// if(shouldAccept(request)) {
			// Accept it

			logger.info("Incomming file "+request.getRequestor());
			IncomingFileTransfer transfer = request.accept();
			String filename = request.getFileName()+"."+request.getRequestor().substring(0, request.getRequestor().indexOf("@"));
			try {
				
				transfer.recieveFile(new File(filename));
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				logger.error(e);
			}

			if (new File(filename).exists()) {
//				new File("Testfile2.txt").deleteOnExit();
				logger.debug("File exists and will delete.");
			}
			// } else {
			// // Reject it
			// request.reject();
			// }

		}

	}

	private XMPPConnection connection1;
	private FileTransferManager transferManager1;

	private XMPPConnection connection2;
	private FileTransferManager transferManager2;

	protected void setUp() throws Exception {
		PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
		Logger logger = Logger.getLogger("de.fu_berlin.inf.dpp");

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

	protected void tearDown() throws Exception {
		connection1.disconnect();
		connection2.disconnect();
	}

	public void XtestFileTransferWithStrings() {
		transferManager2
				.addFileTransferListener(new FileTransferListenerImpl());

		// Create the outgoing file transfer
		OutgoingFileTransfer transfer = transferManager1
				.createOutgoingFileTransfer(connection2.getUser());

		// Send the file
		try {
			transfer.sendFile(new File("Testfile.txt"),
					"You won't believe this!");

			while (!transfer.isDone()) {
				if (transfer.getStatus().equals(Status.error)) {
					logger.error("ERROR!!! " + transfer.getError());
				} else {
					logger.debug("Status : " + transfer.getStatus());
					logger.debug("Progress : " + transfer.getProgress());
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (transfer.getStatus().equals(Status.complete)) {
				logger.debug("transfer complete");
			}
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void testWithViceVersaFileTransfer() throws Exception{
		transferManager2.addFileTransferListener(new FileTransferListenerImpl());
		transferManager1.addFileTransferListener(new FileTransferListenerImpl());
		
		sendFile("Testfile.txt",transferManager1,connection2.getUser() );
		Thread.sleep(1000);
		sendFile("Testfile.txt",transferManager2,connection1.getUser() );
	}
	
//	public void testFileTransferWithPeers() {
//		transferManager2
//				.addFileTransferListener(new FileTransferListenerImpl());
//
//		// Create the outgoing file transfer
//		OutgoingFileTransfer transfer = transferManager1
//				.createOutgoingFileTransfer(connection2.getUser());
//
//		// Send the file
//		try {
//			OutputStream out = transfer.sendFile("Testfile.txt", new File(
//					"Testfile.txt").length(), "You won't believe this!");
//			try {
//				out
//						.write(new String(
//								"Der Inhalt der zu übertragenen Testdatei")
//								.getBytes());
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			while (!transfer.isDone()) {
//				if (transfer.getStatus().equals(Status.error)) {
//					logger.error("ERROR!!! " + transfer.getError());
//				} else {
//					logger.debug("Status : " + transfer.getStatus());
//					logger.debug("Progress : " + transfer.getProgress());
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
//				logger.debug("transfer complete");
//			}
//		} catch (XMPPException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	private void sendFile(String file, FileTransferManager manager, String user ) {

		// Create the outgoing file transfer
		OutgoingFileTransfer transfer = manager
				.createOutgoingFileTransfer(user);

		// Send the file
		try {
			File sendFile = new File(file);
			if(!sendFile.exists()){
				return;
			}
			transfer.sendFile(new File(file),
					"You won't believe this!");

			while (!transfer.isDone()) {
				if (transfer.getStatus().equals(Status.error)) {
					logger.error("ERROR!!! " + transfer.getError());
				} else {
					logger.debug("Status : " + transfer.getStatus());
					logger.debug("Progress : " + transfer.getProgress());
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					logger.error(e);
				}
			}

			if (transfer.getStatus().equals(Status.complete)) {
				logger.debug("transfer complete");
			}
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}

	public void XtestWriteStringIntoFileAndTranfer() {
		transferManager2
				.addFileTransferListener(new FileTransferListenerImpl());

		// Create the outgoing file transfer
		OutgoingFileTransfer transfer = transferManager1
				.createOutgoingFileTransfer(connection2.getUser());

		try {
			if (new File("WriteTestfile.txt").exists()) {
				new File("WriteTestfile.txt").delete();
			}

			FileWriter writer = new FileWriter("WriteTestfile.txt");
			writer.append("Dies ist der inhalt des File" + '\n'
					+ "Mal Schauen, wie der ankommt.");
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Send the file
		try {
			transfer.sendFile(new File("WriteTestfile.txt"),
					"You won't believe this!");

			while (!transfer.isDone()) {
				if (transfer.getStatus().equals(Status.error)) {
					logger.error("ERROR!!! " + transfer.getError());
				} else {
					logger.debug("Status : " + transfer.getStatus());
					logger.debug("Progress : " + transfer.getProgress());
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// public void testSingleFileTransfer() throws XMPPException, IOException {
	// System.out.println("=== testSingleFileTransfer ===");
	//        
	// transferManager2.addFileTransferListener(new FileTransferListener() {
	// public void fileTransferRequest(FileTransferRequest request) {
	// receiveText(request);
	// }
	// });
	//        
	// sendText(transferManager1, "saros3@jabber.org/Smack", "HEHE");
	// }
	//    
	// public void testViceVersaFileTransfer() throws XMPPException, IOException
	// {
	// System.out.println("=== testViceVersaFileTransfer ===");
	//        
	// transferManager2.addFileTransferListener(new FileTransferListener() {
	// public void fileTransferRequest(FileTransferRequest request) {
	// try {
	// receiveText(request);
	// sendText(transferManager2, "saros1@jabber.org/Smack", "TETE");
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	//        
	// transferManager1.addFileTransferListener(new FileTransferListener() {
	// public void fileTransferRequest(FileTransferRequest request) {
	// try {
	// receiveText(request);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	//        
	// sendText(transferManager1, "saros3@jabber.org/Smack", "HEHE");
	// }
	//    
	// public void testConcurrentFileTransfers() throws XMPPException,
	// IOException {
	// System.out.println("=== testViceVersaFileTransfer ===");
	//        
	// transferManager2.addFileTransferListener(new FileTransferListener() {
	// public void fileTransferRequest(FileTransferRequest request) {
	// try {
	// receiveText(request);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	//        
	// asyncSendText(transferManager1, "saros3@jabber.org/Smack", "HEHE1");
	// asyncSendText(transferManager1, "saros3@jabber.org/Smack", "HEHE2");
	// asyncSendText(transferManager1, "saros3@jabber.org/Smack", "HEHE3");
	// }
	//    
	// public static void main(String[] args) {
	// try {
	// SmackFileTransferTest test = new SmackFileTransferTest();
	// test.setUp();
	// test.testConcurrentFileTransfers();
	//            
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//    
	// private void sendText(FileTransferManager transferManager, String to,
	// String text) throws XMPPException, IOException {
	//        
	// System.out.println("Sending text("+text+") to "+to);
	//        
	// OutgoingFileTransfer transfer =
	// transferManager.createOutgoingFileTransfer(to);
	//        
	// OutputStream out = transfer.sendFile("test file",
	// text.getBytes().length, "test desc");
	//
	// BufferedWriter writer = new BufferedWriter(new PrintWriter(out));
	// writer.write(text);
	// writer.flush();
	// writer.close();
	//        
	// System.out.println("Sent text("+text+") to "+to);
	// }
	//    
	// private String receiveText(FileTransferRequest request) {
	// String text = null;
	//        
	// System.out.println("Receiving text from "+request.getRequestor());
	//        
	// final IncomingFileTransfer transfer = request.accept();
	//        
	// try {
	// InputStream in = transfer.recieveFile();
	// BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	// StringBuffer sb = new StringBuffer();
	//            
	// try {
	// String line = null;
	//                
	// while((line=reader.readLine()) != null) {
	// sb.append(line);
	// }
	// } catch(Throwable e) {
	// e.printStackTrace();
	//                
	// } finally {
	// reader.close();
	// }
	//            
	// text = sb.toString();
	//            
	// System.out.println("Received text("+text+") from
	// "+request.getRequestor());
	//            
	// } catch (Throwable e) {
	// e.printStackTrace();
	// }
	//        
	// return text;
	// }
	//
	// private void asyncSendText(final FileTransferManager transferManager,
	// final String to, final String text) {
	//        
	// new Thread(new Runnable() {
	// public void run() {
	// try {
	// sendText(transferManager, to, text);
	// } catch (XMPPException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }).start();
	// }
}
