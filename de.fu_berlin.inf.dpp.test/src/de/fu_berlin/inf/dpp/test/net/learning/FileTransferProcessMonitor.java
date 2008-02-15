package de.fu_berlin.inf.dpp.test.net.learning;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

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
					
					if(transfer instanceof OutgoingFileTransfer){
						OutgoingFileTransfer out = (OutgoingFileTransfer) transfer;
						
					}
					// if (transfer
					// .getStatus()
					// .equals(
					// org.jivesoftware.smackx.filetransfer.FileTransfer.Status.error))
					// {
					// System.out.println("ERROR!!! " + transfer.getError());
					// } else {
					// System.out.print(".");
					// // logger.info("Status : " + transfer.getStatus()+"
					// Progress
					// : " + transfer.getProgress());
					// }
					// try {
					// /* check response time out. */
					// if (time < 10000) {
					// Thread.sleep(100);
					// time += 100;
					// }
					// else{
					// System.out.println("File transfer response error.");
					// try {
					// throw new XMPPException("File transfer response error.");
					// } catch (XMPPException e) {
					//						
					// }
					// }
					// } catch (InterruptedException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }

					Thread.sleep(200);
				}
				this.running = false;

				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
