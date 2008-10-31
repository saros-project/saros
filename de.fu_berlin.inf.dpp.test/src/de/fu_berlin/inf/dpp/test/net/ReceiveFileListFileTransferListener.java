package de.fu_berlin.inf.dpp.test.net;

import java.io.InputStream;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import de.fu_berlin.inf.dpp.test.util.ResourceHelper;

public class ReceiveFileListFileTransferListener implements
	FileTransferListener {

    private static Logger logger = Logger
	    .getLogger(ReceiveFileListFileTransferListener.class.toString());

    public void fileTransferRequest(FileTransferRequest request) {

	IncomingFileTransfer transfer = request.accept();
	String filename = request.getFileName()
		+ "."
		+ request.getRequestor().substring(0,
			request.getRequestor().indexOf("@"));
	try {
	    logger.info("Received File list: " + request.getFileName());
	    // transfer.recieveFile(new File(filename));

	    final InputStream input = transfer.recieveFile();
	    IProject project = ResourceHelper
		    .getProject(ResourceHelper.RECEIVED_TEST_PROJECT);
	    final IFile file = project.getFile(request.getFileName());
	    if (file.exists()) {
		// file.setReadOnly(false);
		System.out.println("file exist file");
		new Thread(new Runnable() {

		    public void run() {
			try {
			    file.setContents(input, IResource.FORCE,
				    new NullProgressMonitor());
			} catch (CoreException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
		    }

		}).start();

	    } else {
		System.out.println("create new file");
		new Thread(new Runnable() {

		    public void run() {
			try {
			    file
				    .create(input, false,
					    new NullProgressMonitor());
			} catch (CoreException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}

		    }

		}).start();

	    }

	    logger.info("receiving finished.");
	    /* 2. Test with direct writing file. */
	    // java.io.File file = new File("Testfile");
	    // System.out.println(file.getAbsolutePath());
	    // transfer.recieveFile(file);
	    // System.out.println("file created.");
	    // catch (IOException e) {
	    // // TODO Auto-generated catch block
	    // e.printStackTrace();
	    // }
	    // mock.resourceReceived(null, new
	    // Path(transfer.getFileName()+".received"), input);
	} catch (CoreException e) {

	    e.printStackTrace();
	} catch (XMPPException e) {
	    // TODO Auto-generated catch block
	    System.out.println(e);
	    // logger.log(Level.ALL,e.getMessage());
	}

	// if (new File(filename).exists()) {
	// // new File("Testfile2.txt").deleteOnExit();
	// logger.debug("File exists and will delete.");
	// }

    }

}
