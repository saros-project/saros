package de.fu_berlin.inf.dpp.net.internal;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransfer;

/**
 * for information on monitoring the process of a file tranfer
 * 
 * @author troll
 * 
 */
public class FileTransferProcessMonitor extends Thread {

	FileTransfer transfer;

	private boolean running = true;

	private boolean closeMonitor = false;

	public FileTransferProcessMonitor(FileTransfer transfer) {
		this.transfer = transfer;
		start();
	}

	public boolean isRunning() throws XMPPException {
		return this.running;
	}

	public String getException() {
		return null;
	}

	public void closeMonitor(boolean close) {
		this.closeMonitor = close;
	}

	public void run() {
		int time = 0;

		while (!closeMonitor ) {
			try {
				while (!transfer.isDone() && (transfer.getProgress()<1.0)) {

					/* check negotiator process */
					System.out.println("Status: " + transfer.getStatus()
							+ " Progress : " + transfer.getProgress());


					Thread.sleep(500);
				}
				this.running = false;

				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
