package de.fu_berlin.inf.dpp.net.jingle.receiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData;
import de.fu_berlin.inf.dpp.net.internal.JingleFileTransferData.FileTransferType;
import de.fu_berlin.inf.dpp.net.jingle.IFileTransferReceiver;
import de.fu_berlin.inf.dpp.net.jingle.IJingleFileTransferListener;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferProcessMonitor;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferTCPConnection;
import de.fu_berlin.inf.dpp.net.jingle.JingleSessionException;

public class FileTransferTCPReceiver extends JingleFileTransferTCPConnection implements IFileTransferReceiver {

	private static Logger logger = Logger
			.getLogger(FileTransferTCPReceiver.class);

	private InetAddress localHost;
	private InetAddress remoteHost;
	private int localPort;
	private int remotePort;
	public static final int tileWidth = 25;
	private boolean on = true;
	private boolean transmit = false;
	private boolean receive = false;

	
	/* transfer information */
//	private JingleFileTransferData receiveTransferData;

	/* transmit transfer data */
	private JingleFileTransferData[] transferData;

	private ServerSocket serverSocket = null;

	private final JingleFileTransferProcessMonitor monitor;
//	private IJingleFileTransferListener listener;

	public FileTransferTCPReceiver(final InetAddress remoteHost,
			final int remotePort, final int localPort) throws IOException {
		// try {

		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.localPort = localPort;

		try {
			serverSocket = new ServerSocket(localPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		transmit = false;
		receive = true;

		/* init process monitor. */
		monitor = new JingleFileTransferProcessMonitor();

		new Thread(new Runnable() {

			public void run() {

				try {
					final Socket socket = serverSocket.accept();

					InputStream input = socket.getInputStream();
					OutputStream output = socket.getOutputStream();
					
					while (on) {
						if (receive) {

							/* get number of file to be transfer. */
							int fileNumber = input.read();

							if(fileNumber > 0){
								logger.debug("file number: "+fileNumber);
							}

							for (int i = 0; i < fileNumber; i++) {
								/* receive file meta data */
								logger.debug("receive meta data for");
								receiveMetaData(input);

								if (receiveTransferData.type == FileTransferType.FILELIST_TRANSFER) {
									receiveFileListData(input);
									/* file list receive, in the next step remote file list have to be send. */
									receive = false;
								}
								if (receiveTransferData.type == FileTransferType.RESOURCE_TRANSFER) {
									/* receive file. */
									logger.info("File incoming: "+receiveTransferData.file_project_path);
									/* received by meta data. */
//									receiveFile(input,output);

									listener.incomingResourceFile(receiveTransferData, new ByteArrayInputStream(receiveTransferData.content));
									
								}

							}

							monitor.setComplete(true);
							
						}
						if (transmit) {
							
//							if(transferData == null){
//								throw new JingleSessionException("No Transfer Data for sending remote filelist.");
//							}
							
							int waitcount = 5;
							while(transferData == null){
								/* wait for transferData*/
								logger.info("No TransferData available. Try again.");
								Thread.sleep(50);
								waitcount--;
								if(waitcount < 1 && transferData == null){
									throw new JingleSessionException("No Transfer Data for sending remote filelist.");
								}
							}
							
							/* send file number. */
							output.write(transferData.length);

							for (int i = 0; i < transferData.length; i++) {

								/* send file meta data */
								sendMetaData(output, transferData[i]);

								if (transferData[i].type == FileTransferType.FILELIST_TRANSFER) {
									sendFileListData(output,
											transferData[i].file_list_content);
									/*
									 * if file list send, we expect remote file
									 * list to receive.
									 */
									receive = true;
								}
								// if (transferData[i].type ==
								// FileTransferType.RESOURCE_TRANSFER) {
								// sendFile(socket, transferData[i].file);
								// }

							}

							/* set monitor status complete :) */
							monitor.setComplete(true);

							transmit = false;

						}
					}
					output.close();
					input.close();
					socket.close();

				}

				catch (SocketException se) {
					if (listener != null) {
						listener.exceptionOccured(new JingleSessionException(se
								.getMessage()));
					}
					logger.error("Socket Exception",se);
					se.printStackTrace();
					return;
				} 
				catch(JingleSessionException jse){
					if (listener != null) {
						listener.exceptionOccured(jse);
					}
					logger.error("JingleSessionException",jse);
					return;
				}
				catch (Exception e1) {
					if (listener != null) {
						listener.exceptionOccured(new JingleSessionException(e1
								.getMessage()));
					}
					logger.error("Exception",e1);
					return;
				}
			}
		}).start();

	}
	
	@Deprecated
	private void receiveFile(InputStream input, OutputStream output) throws IOException {
//		InputStream input = socket.getInputStream();

		/* on the first receive data into stream. */
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 byte[] buffer = new byte[1024];
		 int length = 0;
		 long filesize = receiveTransferData.filesize;
		 long currentSize = 0;
		 int readSize = 1024;
		 /* zuvor informationen schicken, wie groß die Datei ist.*/
		 while(currentSize < filesize){
			 
			 /* check end of file*/
			 if((currentSize + readSize)>= filesize){
				readSize = (int)(filesize - currentSize); 
			 }
			 
			 if((length = input.read(buffer, 0, readSize)) >= 0){
				 bos.write(buffer, 0, length);
				 currentSize += length;
			 }
//		 System.out.println(new String(buffer,0,length));
		 }
		
		 /* inform listener */
		listener.incomingResourceFile(receiveTransferData, new ByteArrayInputStream(bos.toByteArray()));
		
		output.write(1);
		output.flush();
	}
	
	@Deprecated
	private void receiveString(Socket socket) throws IOException,
			ClassNotFoundException {
		ObjectOutputStream oo = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream ii = new ObjectInputStream(socket.getInputStream());

		String s1 = (String) ii.readObject();

		oo.writeObject("Result " + s1);
		oo.flush();
		ii.close();
		oo.close();
	}

	@Deprecated
	private void printContent(byte[] data) throws Exception {

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				data);
		byte[] buffer = new byte[4000];
		int len = byteArrayInputStream.read(buffer, 0, 4000);
		String str = new String(buffer, 0, len);

		System.out.println(str);
	}

	public InetAddress getLocalHost() {
		return localHost;
	}

	public InetAddress getRemoteHost() {
		return remoteHost;
	}

	public int getLocalPort() {
		return localPort;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void stop() {
		this.on = false;
		// socket.close();
	}

	public JingleFileTransferProcessMonitor getMonitor() {
		return this.monitor;
	}

	public void sendFileData(JingleFileTransferData[] transferData) {
		transmit = true;
		this.transferData = transferData;
	}

	public void addJingleFileTransferListener(
			IJingleFileTransferListener listener) {
		this.listener = listener;

	}

	public void removeJingleFileTransferListener(
			IJingleFileTransferListener listener) {
		this.listener = null;
	}
}
