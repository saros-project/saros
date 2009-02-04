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
import java.io.IOException;
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

    private final ISharedProject sharedProject;

    private int progress_done;
    private int progress_max;
    private String progress_info;

    private FileList remoteFileList;

    private List<IPath> toSend;

    /** size of project archive file */
    private final long fileSize = 100;
    private File archive;
    /** size of current transfered part of archive file. */
    private long transferedFileSize = 0;

    public int getProgressCurrent() {
        // TODO CJ: Jingle File Transfer progrss information
        if (this.tmode == TransferMode.IBB) {
            return (int) (this.transferedFileSize);
        } else {
            return this.progress_done + 1;
        }
    }

    public int getProgressMax() {
        if (this.tmode == TransferMode.IBB) {
            return (int) (this.fileSize);
        } else {
            return this.progress_max;
        }

    }

    public String getProgressInfo() {
        return this.progress_info;
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
            this.process.startSynchronization();
        }
    }

    public OutgoingInvitationProcess(ITransmitter transmitter, JID to,
            ISharedProject sharedProject, String description, boolean startNow,
            IInvitationUI inviteUI, int colorID) {

        super(transmitter, to, description, colorID);

        this.invitationUI = inviteUI;
        this.sharedProject = sharedProject;

        if (startNow) {
            transmitter.sendInviteMessage(sharedProject, to, description,
                    colorID);
            setState(State.INVITATION_SENT);
        } else {
            setState(State.INITIALIZED);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IOutgoingInvitationProcess
     */
    public void startSynchronization() {
        assertState(State.GUEST_FILELIST_SENT);

        setState(State.SYNCHRONIZING);

        if ((this.tmode == TransferMode.JINGLE)
                || (this.tmode == TransferMode.DEFAULT)
                || (this.tmode == TransferMode.IBB)) {
            try {
                FileList local = new FileList(this.sharedProject.getProject());
                FileList diff = this.remoteFileList.diff(local);

                List<IPath> added = diff.getAddedPaths();
                List<IPath> altered = diff.getAlteredPaths();
                this.toSend = new ArrayList<IPath>(added.size()
                        + altered.size());
                this.toSend.addAll(added);
                this.toSend.addAll(altered);

                this.progress_max = this.toSend.size();
                this.progress_done = 0;

                /* transfer all data with archive. */
                if (tmode == TransferMode.IBB) {
                    sendArchive();
                } else {
                    /* send separate files. */
                    sendNext();
                }

                if (!blockUntilFilesSent() || !blockUntilJoinReceived()) {
                    cancel(null, false);
                }

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
        if (this.peer.equals(from)) {
            this.peer = from;
        }

        try {
            this.transmitter.sendFileList(this.peer, this.sharedProject
                    .getFileList());
            setState(State.HOST_FILELIST_SENT);
        } catch (Exception e) {
            failed(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void fileListReceived(JID from, FileList fileList) {
        assertState(State.HOST_FILELIST_SENT);

        this.remoteFileList = fileList;
        setState(State.GUEST_FILELIST_SENT);

        this.invitationUI.runGUIAsynch(new SynchronizationRunnable(this));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void joinReceived(JID from) {
        assertState(State.SYNCHRONIZING_DONE);

        this.sharedProject.addUser(new User(from, colorID));
        setState(State.DONE);

        sendDriverEditors();

        this.transmitter.removeInvitationProcess(this); // HACK

        this.transmitter.sendUserListTo(from, this.sharedProject
                .getParticipants());
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

        if (this.tmode == TransferMode.IBB) {
            setState(State.SYNCHRONIZING_DONE);
        } else {
            this.progress_done++;
            sendNext();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.net.IFileTransferCallback#transferProgress(int)
     */
    public void transferProgress(int transfered) {
        this.transferedFileSize = transfered;
        /* update ui */
        this.invitationUI.updateInvitationProgress(this.peer);
    }

    private void sendNext() {

        if (getState() == State.CANCELED) {
            this.toSend.clear();
            return;
        }

        if (this.toSend.size() == 0) {
            setState(State.SYNCHRONIZING_DONE);
            return;
        }

        IPath path = this.toSend.remove(0);
        this.progress_info = path.toFile().getName();

        this.invitationUI.updateInvitationProgress(this.peer);

        try {
            this.transmitter.sendFileAsync(this.peer, this.sharedProject.getProject(),
                    path, -1, this);
        } catch (IOException e) {
            this.fileTransferFailed(path, e);
        }
    }

    /**
     * send all project data with archive file.
     */
    private void sendArchive() {
        if (getState() == State.CANCELED) {
            this.toSend.clear();
            return;
        }

        if (this.toSend.size() == 0) {
            setState(State.SYNCHRONIZING_DONE);
            return;
        }

        this.archive = new File("./" + getPeer().getName() + "_Project.zip");
        OutgoingInvitationProcess.logger
                .debug("Project archive file has to be send. "
                        + this.archive.getAbsolutePath() + " length: "
                        + this.archive.length());
        try {
            /* create project zip archive. */
            FileZipper.createProjectZipArchive(this.toSend, this.archive
                    .getAbsolutePath(), this.sharedProject.getProject());
            /* send data. */
            this.transmitter.sendProjectArchive(this.peer, this.sharedProject
                    .getProject(), this.archive, this);
        } catch (Exception e) {
            failed(e);
        }

        this.progress_info = "Transfer project tar file";

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
        while ((this.state != State.SYNCHRONIZING_DONE)
                && (this.state != State.DONE)) {
            if (getState() == State.CANCELED) {
                return false;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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
        this.progress_info = "Waiting for confirmation";

        while (this.state != State.DONE) {
            if (getState() == State.CANCELED) {
                return false;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.progress_info = "";

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
            filelist = this.sharedProject.getFileList();
        } catch (CoreException e) {
            filelist = null;
        }
        // HACK
        for (IPath path : driverEditors) {
            if ((filelist != null)
                    && (filelist.getPaths().contains(path) == false)) {
                continue;
            }

            this.sharedProject.getSequencer().activityCreated(
                    new EditorActivity(EditorActivity.Type.Activated, path));
        }

        if ((filelist != null)
                && (filelist.getPaths().contains(activeDriverEditor) == true)) {
            this.sharedProject.getSequencer().activityCreated(
                    new EditorActivity(EditorActivity.Type.Activated,
                            activeDriverEditor));
        }
    }

    public String getProjectName() {
        return this.sharedProject.getProject().getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.invitation.IInvitationProcess#getTransferMode()
     */
    public TransferMode getTransferMode() {
        return this.tmode;
    }

    public void setTransferMode(TransferMode mode) {
        this.tmode = mode;

    }

    @Override
    public void cancel(String errorMsg, boolean replicated) {
        super.cancel(errorMsg, replicated);
        sharedProject.returnColor(this.colorID);
    }

}
