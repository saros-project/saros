package de.fu_berlin.inf.dpp.test.net.mock;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.State;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.util.ResourceHelper;

public class MockInvitationProcess implements IInvitationProcess{

	private static Logger logger = Logger.getLogger(MockInvitationProcess.class.toString());
	
	protected final ITransmitter transmitter;

	protected State state;

	private Exception exception;

	protected JID peer;

	protected IInvitationUI invitationUI=null;

	protected String description;
	
	public MockInvitationProcess(ITransmitter transmitter, JID peer, String description) {
		this.transmitter = transmitter;
		this.peer = peer;
		this.description = description;

		transmitter.addInvitationProcess(this);
	}
	

	public void cancel(String errorMsg, boolean replicated) {
		// TODO Auto-generated method stub
		
	}


	public void fileListReceived(JID from, FileList fileList) {
		// TODO Auto-generated method stub
		
	}


	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}


	public Exception getException() {
		// TODO Auto-generated method stub
		return null;
	}


	public JID getPeer() {
		// TODO Auto-generated method stub
		return null;
	}


	public String getProjectName() {
		// TODO Auto-generated method stub
		return null;
	}


	public State getState() {
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
		logger.log(Level.FINE,"received method");
		try {
			IProject localProject = null;
			// Then check with all the projects
//			File f = new File("/home/troll/testfile.txt");
			
			localProject = ResourceHelper.createDefaultProject();
//			IWorkspace workspace = ResourcesPlugin.getWorkspace();
//			IProject[] projects = workspace.getRoot().getProjects();
//			localProject = workspace.getRoot().getProject("testProject");
//			for(IProject p : projects){
//				if(p.getName().equals("Saros Test")){
//					localProject = p;
//				}
//			}
			if(localProject == null){
				logger.log(Level.WARNING,"no project founded.");
				return;
			}
			
			IFile file = localProject.getFile(path);
			if (file.exists()) {
//				file.setReadOnly(false);
				logger.info("file exist");
				file.setContents(input, IResource.FORCE, new NullProgressMonitor());
			} else {
				logger.info("file will be created");
				file.create(input, true, new NullProgressMonitor());
			}
		} catch (Exception e) {
			logger.log(Level.WARNING,e.getMessage());
		}
		
	}


	public TransferMode getTransferMode() {
		// TODO Auto-generated method stub
		return null;
	}

	public void jingleFallback() {
		// TODO Auto-generated method stub
		
	}


	public void fileSent(IPath path) {
		// TODO Auto-generated method stub
		
	}


	public void fileTransferFailed(IPath path, Exception e) {
		// TODO Auto-generated method stub
		
	}


	public void transferProgress(int transfered) {
		// TODO Auto-generated method stub
		
	}


	public void setTransferMode(TransferMode mode) {
		// TODO Auto-generated method stub
		
	}

}
