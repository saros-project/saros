package de.fu_berlin.inf.dpp.test.net;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
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

public class ReceivedSingleFileListener implements FileTransferListener {

	private static Logger logger = Logger.getLogger(ReceivedSingleFileListener.class.toString());
	private static final String RESOURCE_TRANSFER_DESCRIPTION = "resourceAddActivity";
	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		IncomingFileTransfer transfer = request.accept();
		

		try {
			logger.info("received file: "+transfer.getFilePath() + " "+transfer.getFileName()+ " desc: "+request.getDescription());
			
			// transfer.recieveFile(new File(filename));
			

				InputStream input = transfer.recieveFile();
				IProject project = ResourceHelper.getProject(ResourceHelper.RECEIVED_TEST_PROJECT);
				
				String path = request.getDescription().substring(RESOURCE_TRANSFER_DESCRIPTION.length()+1);
				System.out.println("Path : "+path);
				IFile file = project.getFile(path);
				if (file.exists()) {
					// file.setReadOnly(false);
					logger.info("file already exists and will be update.");
					file.setContents(input, IResource.FORCE,
							new NullProgressMonitor());
					
				} else {
					if(!file.getParent().exists()){
						logger.info("create dir: "+new File(file.getParent().getFullPath().toString()).mkdirs());
					}
					IResource re = file.getParent();
					
					file.create(input, true, new NullProgressMonitor());
					logger.info("new file will be create.");
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
