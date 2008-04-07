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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.FileZipper;

/**
 * An outgoing invitation process.
 * 
 * @author rdjemili
 */
public class OutgoingInvitationProcess extends InvitationProcess implements
		IOutgoingInvitationProcess {

	private static Logger logger = Logger
			.getLogger(OutgoingInvitationProcess.class);

	private ISharedProject sharedProject;

	private int progress_done;
	private int progress_max;
	private String progress_info;

	private FileList remoteFileList;

	private List<IPath> toSend;

	/** size of project archive file */
	private long fileSize = 100;
	private File archive;
	/** size of current transfered part of archive file. */
	private long transferedFileSize = 0;

	public int getProgressCurrent() {
		if (tmode == TransferMode.IBB) {
			//TODO Änderung
			return (int) (transferedFileSize);
		} else {
			return progress_done + 1;
		}
	}

	public int getProgressMax() {
		if (tmode == TransferMode.IBB) {
			//TODO Änderung
			return (int) (fileSize);
		} else {
			return progress_max;
		}

	}

	public String getProgressInfo() {
		return progress_info;
	}

	/**
	 * A simple runnable that calls
	 * {@link IOutgoingInvitationProcess#startSynchronization(IProgressMonitor)}
	 */
	private class SynchronizationRunnable implements Runnable {
		private final OutgoingInvitationProcess process;

		public SynchronizationRunnable(OutgoingInvitationProcess process) {
			this.process = process;
		}

		public void run() {
			process.startSynchronization();
		}
	}

	public OutgoingInvitationProcess(ITransmitter transmitter, JID to,
			ISharedProject sharedProject, String description, boolean startNow,
			IInvitationUI inviteUI) {

		super(transmitter, to, description);

		this.invitationUI = inviteUI;
		this.sharedProject = sharedProject;

		if (startNow) {
			transmitter.sendInviteMessage(sharedProject, to, description);
			setState(State.INVITATION_SENT);
		} else
			setState(State.INITIALIZED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.IOutgoingInvitationProcess
	 */
	public void startSynchronization() {
		assertState(State.GUEST_FILELIST_SENT);

		setState(State.SYNCHRONIZING);

		if (tmode == TransferMode.JINGLE || tmode == TransferMode.DEFAULT
				|| tmode == TransferMode.IBB) {
			try {
				FileList local = new FileList(sharedProject.getProject());
				FileList diff = remoteFileList.diff(local);

				List<IPath> added = diff.getAddedPaths();
				List<IPath> altered = diff.getAlteredPaths();
				toSend = new ArrayList<IPath>(added.size() + altered.size());
				toSend.addAll(added);
				toSend.addAll(altered);

				progress_max = toSend.size();
				progress_done = 0;

				/* transfer all data with archive. */
				if (tmode == TransferMode.IBB) {
					sendArchive();
				} else {
					/* send separate files. */
					sendNext();
				}

				if (!blockUntilFilesSent() || !blockUntilJoinReceived())
					cancel(null, false);

			} catch (CoreException e) {
				failed(e);

			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.InvitationProcess
	 */
	public void invitationAccepted(JID from) {
		assertState(State.INVITATION_SENT);

		// HACK add resource specifier to jid
		if (peer.equals(from))
			peer = from;

		try {
			transmitter.sendFileList(peer, sharedProject.getFileList());
			setState(State.HOST_FILELIST_SENT);
		} catch (Exception e) {
			failed(e);
		}
		
		//TODO: For testing only
		tmode = TransferMode.IBB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.InvitationProcess
	 */
	public void fileListReceived(JID from, FileList fileList) {
		assertState(State.HOST_FILELIST_SENT);

		remoteFileList = fileList;
		setState(State.GUEST_FILELIST_SENT);

		invitationUI.runGUIAsynch(new SynchronizationRunnable(this));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.InvitationProcess
	 */
	public void joinReceived(JID from) {
		assertState(State.SYNCHRONIZING_DONE);

		sharedProject.addUser(new User(from));
		setState(State.DONE);

		sendDriverEditors();

		transmitter.removeInvitationProcess(this); // HACK

		transmitter.sendUserListTo(from, sharedProject.getParticipants());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.InvitationProcess
	 */
	public void resourceReceived(JID from, IPath path, InputStream in) {
		failState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.IFileTransferCallback
	 */
	public void fileTransferFailed(IPath path, Exception e) {
		failed(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.IFileTrafnsferCallback
	 */
	public void fileSent(IPath path) {
		if (tmode == TransferMode.IBB) {
			//TODO Änderung
			
			setState(State.SYNCHRONIZING_DONE);
		} else {
			progress_done++;
			sendNext();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.IFileTransferCallback#transferProgress(int)
	 */
	public void transferProgress(int transfered) {
		//TODO Änderung
		transferedFileSize = transfered;
		/* update ui */
		invitationUI.updateInvitationProgress(peer);
	}

	private void sendNext() {

		if (getState() == State.CANCELED) {
			toSend.clear();
			return;
		}

		if (toSend.size() == 0) {
			setState(State.SYNCHRONIZING_DONE);
			return;
		}

		IPath path = toSend.remove(0);
		progress_info = path.toFile().getName();

		invitationUI.updateInvitationProgress(peer);

		transmitter.sendFile(peer, sharedProject.getProject(), path, this);
	}

	/**
	 * send all project data with archive file.
	 */
	private void sendArchive() {
		//TODO Änderung
		if (getState() == State.CANCELED) {
			toSend.clear();
			return;
		}

		if (toSend.size() == 0) {
			setState(State.SYNCHRONIZING_DONE);
			return;
		}

		archive = new File("./"+getPeer().getName()+"_Project.zip");
		logger.debug("Project archive file has to be send. "
				+ archive.getAbsolutePath() + " length: " + archive.length());
		try {
			/* create project zip archive. */
			FileZipper.createProjectZipArchive(toSend, archive.getAbsolutePath(), sharedProject.getProject());
			/* send data. */
			transmitter.sendProjectArchive(peer, sharedProject.getProject(),
					archive, this);
		} catch (Exception e) {
			failed(e);
		}

		progress_info = "Transfer project tar file";

		// fileSize = archive.length();
	}

	/**
	 * Blocks until all files have been sent or the operation was canceled by
	 * the user.
	 * 
	 * @param monitor
	 *            the progress monitor for the file synchronization.
	 * @return <code>true</code> if all files have been synchronized.
	 *         <code>false</code> if the user chose to cancel.
	 */
	private boolean blockUntilFilesSent() {
		while (state != State.SYNCHRONIZING_DONE && state != State.DONE) {
			if (getState() == State.CANCELED)
				return false;

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

		return true;
	}

	/**
	 * Blocks until the join message has been received or the user cancelled.
	 * 
	 * @return <code>true</code> if the join message has been received.
	 *         <code>false</code> if the user chose to cancel.
	 */
	private boolean blockUntilJoinReceived() {
		progress_info = "Waiting for confirmation";

		while (state != State.DONE) {
			if (getState() == State.CANCELED)
				return false;

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		progress_info = "";

		return true;
	}

	/**
	 * Send activities which set the active editors.
	 */
	private void sendDriverEditors() {
		EditorManager editorManager = EditorManager.getDefault();
		Set<IPath> driverEditors = editorManager.getDriverEditors();
		IPath activeDriverEditor = editorManager.getActiveDriverEditor();
		driverEditors.remove(activeDriverEditor);

		FileList filelist;
		try {
			filelist = sharedProject.getFileList();
		} catch (CoreException e) {
			filelist = null;
		}
		// HACK
		for (IPath path : driverEditors) {
			if (filelist != null && filelist.getPaths().contains(path) == false)
				continue;

			sharedProject.getSequencer().activityCreated(
					new EditorActivity(EditorActivity.Type.Activated, path));
		}

		if (filelist != null
				&& filelist.getPaths().contains(activeDriverEditor) == true)
			sharedProject.getSequencer().activityCreated(
					new EditorActivity(EditorActivity.Type.Activated,
							activeDriverEditor));
	}

	public String getProjectName() {
		return sharedProject.getProject().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.IFileTransferCallback#jingleFallback()
	 */
	public void jingleFallback() {
		System.out.println("Fallback");
		tmode = TransferMode.IBB;
		try {
			/* TODO: send file list another one. */
			transmitter.sendFileList(peer, sharedProject.getFileList());
		} catch (Exception e) {
			failed(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.invitation.IInvitationProcess#getTransferMode()
	 */
	public TransferMode getTransferMode() {
		return tmode;
	}

}
