package de.fu_berlin.inf.dpp.test.invitation.internal.mock;

import java.io.InputStream;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import java.util.List;
import java.util.logging.Logger;
import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.internal.InvitationProcess;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;


public class MockOutgoingInvitationProcess extends InvitationProcess implements
IOutgoingInvitationProcess, IFileTransferCallback {

	IProject project;
	
	private static Logger logger = Logger.getLogger(MockOutgoingInvitationProcess.class.toString());
	
	private FileList remoteFileList;
	
	public MockOutgoingInvitationProcess(ITransmitter transmitter, JID peer,
			String description, IProject project) {
		super(transmitter, peer, description);
		this.project = project;
		// TODO Auto-generated constructor stub
	}


	public int getProgressCurrent() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getProgressInfo() {
		// TODO Auto-generated method stub
		return null;
	}


	public int getProgressMax() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public void startSynchronization() {
		assertState(State.GUEST_FILELIST_SENT);

		setState(State.SYNCHRONIZING);

		try {
			FileList local = new FileList(project);
			FileList diff = remoteFileList.diff(local);

			List<IPath> added = diff.getAddedPaths();
			List<IPath> altered = diff.getAlteredPaths();
			System.out.println("");
//			toSend = new ArrayList<IPath>(added.size() + altered.size());
//			toSend.addAll(added);
//			toSend.addAll(altered);
//			
//			progress_max = toSend.size();
//			progress_done= 0;
//			
//			sendNext();
//
//			if (!blockUntilFilesSent() || !blockUntilJoinReceived())
//				cancel(null, false);

		} catch (CoreException e) {
			failed(e);

		}
		
	}

	
	public void fileListReceived(JID from, FileList fileList) {
		logger.info("file list received.");
		setState(State.HOST_FILELIST_SENT);
		
		assertState(State.HOST_FILELIST_SENT);
		
		remoteFileList = fileList;
		setState(State.GUEST_FILELIST_SENT);
		
		final IOutgoingInvitationProcess process = this;
		
		/* start synchronisation .*/
		new Thread(new Runnable(){

			
			public void run() {
				logger.info("start sync.");
				process.startSynchronization();
				
			}
			
		}).start();
	}

	
	public String getProjectName() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void invitationAccepted(JID from) {
		// TODO Auto-generated method stub
		
	}

	
	public void joinReceived(JID from) {
		// TODO Auto-generated method stub
		
	}

	
	public void resourceReceived(JID from, IPath path, InputStream input) {
		logger.info("resource received.");
		
	}

	public void fileSent(IPath path) {
		// TODO Auto-generated method stub
		
	}

	
	public void fileTransferFailed(IPath path, Exception e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public TransferMode getTransferMode() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void jingleFallback() {
		// TODO Auto-generated method stub
		
	}

	
}
