/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.invitation.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.ErrorMessageDialog;

/**
 * An incoming invitation process.
 * 
 * @author rdjemili
 */
public class IncomingInvitationProcess extends InvitationProcess implements
	IIncomingInvitationProcess {

	private static Logger logger = Logger.getLogger(IncomingInvitationProcess.class);
	
	private FileList remoteFileList;

	private IProject localProject;

	private int filesLeftToSynchronize;

	private IProgressMonitor progressMonitor;
	
	protected String projectName;
	
	public IncomingInvitationProcess(ITransmitter transmitter, JID from, String projectName,
		String description) {

		super(transmitter, from, description);

		this.projectName = projectName;
		this.setState(State.INVITATION_SENT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IInvitationProcess
	 */
	public void fileListReceived(JID from, FileList fileList) {
		assertState(State.HOST_FILELIST_REQUESTED);
		
		if (fileList == null)
			cancel("Failed to receive remote file list.", false);
		else {
			remoteFileList = fileList;
			setState(State.HOST_FILELIST_SENT);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IIncomingInvitationProcess
	 */
	public FileList requestRemoteFileList(IProgressMonitor monitor) {
		assertState(State.INVITATION_SENT);

		monitor.beginTask("Requesting remote file list", IProgressMonitor.UNKNOWN);

		transmitter.sendRequestForFileListMessage(peer);
		setState(State.HOST_FILELIST_REQUESTED);

		while (remoteFileList == null && state != State.CANCELED) {
			if (monitor.isCanceled()) {
				cancel(null, false);
			}
			
			try {
				Thread.sleep(500);
				monitor.worked(1);
			} catch (InterruptedException e) {
			}
		}

		monitor.done();

		return remoteFileList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IIncomingInvitationProcess
	 */
	public void accept(IProject baseProject, String newProjectName, IProgressMonitor monitor) {

		if (newProjectName == null && baseProject == null)
			throw new IllegalArgumentException(
				"At least newProjectName or baseProject have to be not null.");

		try {
			assertState(State.HOST_FILELIST_SENT);

			if (newProjectName != null) {
				localProject = createNewProject(newProjectName, baseProject);
			} else {
				localProject = baseProject;
			}

			filesLeftToSynchronize = handleDiff(localProject, remoteFileList);

			progressMonitor = monitor;
			progressMonitor.beginTask("Synchronizing...", filesLeftToSynchronize);
			setState(State.SYNCHRONIZING);

			transmitter.sendFileList(peer, new FileList(localProject));

			if (blockUntilAllFilesSynchronized(monitor))
				done();
			else
				cancel(null, false);

		} catch (Exception e) {
			ErrorMessageDialog.showErrorMessage(new Exception("Exception during create project."));
			failed(e);

		} finally {
			monitor.done();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.InvitationProcess
	 */
	public void invitationAccepted(JID from) {
		failState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.InvitationProcess
	 */
	public void joinReceived(JID from) {
		failState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.InvitationProcess
	 */
	public void resourceReceived(JID from, IPath path, InputStream in) {
		logger.debug("new file received: "+path);
		if (localProject==null)
			return; // we dont have started the new project yet, so received ressources are not welcomed
		
		try {
			IFile file = localProject.getFile(path);
			if (file.exists()) {
				file.setReadOnly(false);
				file.setContents(in, IResource.FORCE, new NullProgressMonitor());
			} else {
				file.create(in, true, new NullProgressMonitor());
				logger.debug("New File created: "+file.getName());
			}
		} catch (Exception e) {
			failed(e);
		}

		progressMonitor.worked(1);
		progressMonitor.subTask("Files left: " + filesLeftToSynchronize);
		
		filesLeftToSynchronize--;
		logger.debug("file counter: "+filesLeftToSynchronize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IIncomingInvitationProcess
	 */
	public FileList getRemoteFileList() {
		return remoteFileList;
	}

	/**
	 * Blocks until all files have been synchronized or cancel has been
	 * selected.
	 * 
	 * @return <code>true</code> if all files were synchronized.
	 *         <code>false</code> if operation was canceled by user.
	 */
	private boolean blockUntilAllFilesSynchronized(IProgressMonitor monitor) {
		//TODO: deadlock abfangen.
		while (filesLeftToSynchronize > 0) {
			if (monitor.isCanceled() || getState() == State.CANCELED) {
				return false;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

		return true;
	}

	/**
	 * Creates a new project.
	 * 
	 * @param newProjectName
	 *            the project name of the new project.
	 * @param baseProject
	 *            if not <code>null</code> all files of the baseProject will
	 *            be copied into the new project after having created it.
	 * @return the new project.
	 * @throws CoreException
	 *             if something goes wrong while creating the new project.
	 */
	private IProject createNewProject(String newProjectName, IProject baseProject)
		throws CoreException {

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspaceRoot.getProject(newProjectName);
		
		project.clearHistory(null);
		project.refreshLocal(IProject.DEPTH_INFINITE, null);

		if (baseProject == null) {
			project.create(new NullProgressMonitor());
			project.open(new NullProgressMonitor());
		} else {
			baseProject.copy(project.getFullPath(), true, new NullProgressMonitor());
		}

		return project;
	}

	/**
	 * Prepares for receiving the missing resources.
	 * 
	 * @param localProject
	 *            the project that is used for the base of the replication.
	 * @param remoteFileList
	 *            the file list of the remote project.
	 * @return the number of files that we need to receive to end the
	 *         synchronization.
	 * @throws CoreException
	 *             is thrown when getting all files of the local project.
	 */
	private int handleDiff(IProject localProject, FileList remoteFileList) throws CoreException {

		FileList diff = new FileList(localProject).diff(remoteFileList);

		removeUnneededResources(localProject, diff);
		int addedPaths = addAllFolders(localProject, diff);

		return diff.getAddedPaths().size() - addedPaths + diff.getAlteredPaths().size();
	}

	/**
	 * Removes all local resources that aren't part of the shared project we're
	 * currently joining. This includes files and folders.
	 * 
	 * @param localProject
	 *            the local project were the shared project will be replicated.
	 * @param diff
	 *            the fileList which contains the diff information.
	 * @throws CoreException
	 */
	private void removeUnneededResources(IProject localProject, FileList diff) throws CoreException {

		// TODO dont throw CoreException
		// TODO check if this triggers the resource listener
		for (IPath path : diff.getRemovedPaths()) {
			if (path.hasTrailingSeparator()) {
				IFolder folder = localProject.getFolder(path);

				if (folder.exists())
					folder.delete(true, new NullProgressMonitor());

			} else {
				IFile file = localProject.getFile(path);

				// check if file exists because it might have already been
				// deleted when deleting its folder
				if (file.exists())
					file.delete(true, new NullProgressMonitor());
			}
		}
	}

	private int addAllFolders(IProject localProject, FileList diff) throws CoreException {

		int addedFolders = 0;

		for (IPath path : diff.getAddedPaths()) {
			if (path.hasTrailingSeparator()) {
				IFolder folder = localProject.getFolder(path);
				if (!folder.exists())
					folder.create(true, true, new NullProgressMonitor());

				addedFolders++;
			}
		}

		return addedFolders;
	}

	/**
	 * Ends the incoming invitiation process.
	 */
	private void done() {
		JID host = peer;
		JID driver = peer;

		// HACK
		List<JID> users = new ArrayList<JID>();
		users.add(host);
		users.add(Saros.getDefault().getMyJID());

		SessionManager sessionManager = Saros.getDefault().getSessionManager();
		ISharedProject sharedProject = sessionManager
			.joinSession(localProject, host, driver, users);

		transmitter.sendJoinMessage(sharedProject);
		transmitter.removeInvitationProcess(this); // HACK
		
		sharedProject.setProjectReadonly(true);

		setState(State.DONE);
	}

	public String getProjectName() {
		return this.projectName;
	}

	public void updateInvitationProgress(JID jid) {
		// ignored, not needed atm		
	}

	/*
	 * (non-Javadoc)
	 * @see de.fu_berlin.inf.dpp.invitation.IInvitationProcess#getTransferMode()
	 */
	public TransferMode getTransferMode() {
		return tmode;
	}

}
