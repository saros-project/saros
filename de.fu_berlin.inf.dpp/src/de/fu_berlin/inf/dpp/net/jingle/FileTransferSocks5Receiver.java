package de.fu_berlin.inf.dpp.net.jingle;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IBBTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.Socks5TransferNegotiatorManager;

public class FileTransferSocks5Receiver implements IFileTransferReceiver,
		FileTransferListener {

	private InetAddress localHost;
	private InetAddress remoteHost;
	private int localPort;
	private int remotePort;
	public static final int tileWidth = 25;
	private boolean on = true;
	private boolean transmit = false;

	private ServerSocket serverSocket = null;

	public FileTransferSocks5Receiver(final InetAddress remoteHost,
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

		transmit = true;

		new Thread(new Runnable() {

			
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					if (transmit) {
						try {
							final Socket socket = serverSocket.accept();

							receiveFile(socket);

						} catch (Exception e1) {

							e1.printStackTrace();
						}
					}
				}
			}

		}).start();

		System.out.println("receiver started.");
	}

	private void receiveFile(Socket socket) throws IOException {
		InputStream input = socket.getInputStream();
		
		byte[] buffer = new byte[1024];
		System.out.println("Binded");

		FileOutputStream fos = new FileOutputStream(new File("/home/troll/receivedFile.jar"));
		while (input.read(buffer,0,1024) != -1) {
			fos.write(buffer);
			fos.flush();
		}
		
		fos.close();
		input.close();
		socket.close();
		
	}
	
	private void receiveString(Socket socket) throws IOException, ClassNotFoundException{
		ObjectOutputStream oo = new ObjectOutputStream(
				socket.getOutputStream());
		ObjectInputStream ii = new ObjectInputStream(socket
				.getInputStream());

		String s1 = (String) ii.readObject();

		oo.writeObject("Result " + s1);
		oo.flush();
		ii.close();
		oo.close();
	}
	
	private void receiveInt(Socket socket) throws IOException{
		InputStream input = socket.getInputStream();
		OutputStream output = socket.getOutputStream();

		int zahl1 = input.read();
		int zahl2 = input.read();

		output.write(zahl1 + zahl2);
		output.flush();
		input.close();
		output.close();
	}

	private void startByteReceiver() {
		// /* Übertragung zwischen zwei Partnern. */
		// final Socket socket = serverSocket.accept();
		// // System.out.println("Angemeldet: ");
		//		
		// System.out.println("Incomming Message: ");
		// ByteArrayInputStream input = (ByteArrayInputStream)
		// socket.getInputStream();
		// byte[] buffer = new byte[256];
		// int length = 0;
		// /* zuvor informationen schicken, wie groß die Datei ist.*/
		// while((length = input.read(buffer, 0, 256))!= 0){
		// System.out.println(new String(buffer,0,length));
		// }
		// on = false;
		//		
		// Thread.sleep(2000);
		// transmit = false;
		// socket.close();
	}

	private void printContent(byte[] data) throws Exception {

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				data);
		byte[] buffer = new byte[4000];
		int len = byteArrayInputStream.read(buffer, 0, 4000);
		String str = new String(buffer, 0, len);

		System.out.println(str);
	}

	/**
	 * Return given file as byte array representation.
	 */
	private static void writeFile(byte[] data) throws Exception {

		/* testing area */
		File f = new File("/home/troll/text.txt");
		if (f.exists()) {
			f.delete();
		}
		/* end of testing area */

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				data);
		// FileOutputStream fileOutputStream = new
		// FileOutputStream(f.getAbsolutePath());
		byte[] buffer = new byte[4000];
		int len = byteArrayInputStream.read(buffer, 0, 4000);
		String str = new String(buffer, 0, len);

		System.out.println(str);
		// fileOutputStream.write(buffer);

		// fileOutputStream.close();
		// System.out.println(new String(byteArrayOutputStream.toByteArray(),
		// "UTF-8"));

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

	// public DatagramSocket getDatagramSocket() {
	// return socket;
	// }

	public void stop() {
		this.on = false;
		// socket.close();
	}

	public void fileTransferRequest(FileTransferRequest request) {
		System.out.println("File Transfer Request ... ");
		IncomingFileTransfer transfer = request.accept();
		try {
			InputStream in = transfer.recieveFile();
		} catch (XMPPException e) {
			e.printStackTrace();
		}

	}
}
