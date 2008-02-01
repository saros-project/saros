package de.fu_berlin.inf.dpp.test.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IBBTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import de.fu_berlin.inf.dpp.test.util.ResourceHelper;

public class ReceiveFileListFileTransferListener implements
		FileTransferListener {

	@Override
	public void fileTransferRequest(FileTransferRequest request) {

		IncomingFileTransfer transfer = request.accept();
		String filename = request.getFileName()
				+ "."
				+ request.getRequestor().substring(0,
						request.getRequestor().indexOf("@"));
		try {
			System.out.println("received file");
			// transfer.recieveFile(new File(filename));
			

				InputStream input = transfer.recieveFile();
				IProject project = ResourceHelper.getDefaultProject();
				IFile file = project.getFile(request.getFileName());
				if (file.exists()) {
					// file.setReadOnly(false);
					System.out.println("file exist file");
					file.setContents(input, IResource.FORCE,
							new NullProgressMonitor());
					
				} else {
					System.out.println("create new file");
					file.create(input, false, new NullProgressMonitor());
					System.out.println("file created.");
				}
				
				/* 2. Test with direct writing file. */
//				java.io.File file = new File("Testfile");
//				System.out.println(file.getAbsolutePath());
//				transfer.recieveFile(file);
//				System.out.println("file created.");

			// catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// mock.resourceReceived(null, new
			// Path(transfer.getFileName()+".received"), input);
			} catch (CoreException e) {
				 
				e.printStackTrace();
			}
		 catch (XMPPException e) {
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
