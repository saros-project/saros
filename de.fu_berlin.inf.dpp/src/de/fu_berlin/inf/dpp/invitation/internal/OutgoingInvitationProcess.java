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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager.JingleConnectionState;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.FileZipper;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * An outgoing invitation process.
 * 
 * @author rdjemili
 */
public class OutgoingInvitationProcess extends InvitationProcess implements
    IOutgoingInvitationProcess {

    protected final static Logger log = Logger
        .getLogger(OutgoingInvitationProcess.class);

    protected final ISharedProject sharedProject;

    protected int progress_done;
    protected int progress_max;
    protected String progress_info = "";

    protected boolean isP2P = false;

    protected FileList remoteFileList;

    protected List<IPath> toSend;

    /** size of project archive file */
    protected final long fileSize = 100;

    /** size of current transfered part of archive file. */
    protected long transferedFileSize = 0;

    public int getProgressCurrent() {
        // TODO CJ: Jingle File Transfer progress information
        if (isP2P) {
            return this.progress_done + 1;
        } else {
            return (int) (this.transferedFileSize);
        }
    }

    public int getProgressMax() {
        if (isP2P) {
            return this.progress_max;
        } else {
            return (int) (this.fileSize);
        }
    }

    public String getProgressInfo() {
        return this.progress_info;
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

    public void startSynchronization() {
        assertState(State.GUEST_FILELIST_SENT);

        setState(State.SYNCHRONIZING);

        try {
            FileList local = new FileList(this.sharedProject.getProject());
            FileList diff = this.remoteFileList.diff(local);

            List<IPath> added = diff.getAddedPaths();
            List<IPath> altered = diff.getAlteredPaths();
            // TODO A linked list might be more efficient. See #sendNext().
            this.toSend = new ArrayList<IPath>(added.size() + altered.size());
            this.toSend.addAll(added);
            this.toSend.addAll(altered);

            this.progress_max = this.toSend.size();
            this.progress_done = 0;

            DataTransferManager manager = Saros.getDefault().getContainer()
                .getComponent(DataTransferManager.class);

            JingleFileTransferManager jingleManager = manager
                .getJingleManager();

            // If fast p2p connection send individual files, otherwise archive
            if (jingleManager.getState(getPeer()) == JingleConnectionState.ESTABLISHED) {
                isP2P = true;
                sendNext();
            } else {
                isP2P = false;
                sendArchive();
            }

            if (!blockUntilFilesSent() || !blockUntilJoinReceived()) {
                cancel(null, false);
            }

        } catch (CoreException e) {
            failed(e);
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
                .getFileList(), this);
            setState(State.HOST_FILELIST_SENT);
        } catch (Exception e) {
            failed(e);
        }

    }

    /**
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void fileListReceived(JID from, FileList fileList) {
        assertState(State.HOST_FILELIST_SENT);

        this.remoteFileList = fileList;
        setState(State.GUEST_FILELIST_SENT);

        // Run asynchronously
        Util.runSafeAsync("OutgoingInvitationProcess-synchronisation-", log,
            new Runnable() {
                public void run() {
                    startSynchronization();
                }
            });
    }

    /**
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void joinReceived(JID from) {
        assertState(State.SYNCHRONIZING_DONE);

        this.sharedProject.addUser(new User(from, colorID));
        setState(State.DONE);

        sendDriverEditors();

        // TODO Find a more reliable way to remove InvitationProcess
        this.transmitter.removeInvitationProcess(this);

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
     * Methods for implementing IFileTransferCallback
     */
    public void fileTransferFailed(IPath path, Exception e) {
        failed(e);
    }

    public void fileSent(IPath path) {

        if (isP2P) {
            progress_done++;
            sendNext();
        } else {
            setState(State.SYNCHRONIZING_DONE);
        }
    }

    public void transferProgress(int transfered) {
        transferedFileSize = transfered;

        // Tell the UI to update itself
        invitationUI.updateInvitationProgress(peer);
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
        this.setProgressInfo(path.toFile().getName());

        try {
            this.transmitter.sendFileAsync(this.peer, this.sharedProject
                .getProject(), path, TimedActivity.NO_SEQUENCE_NR, this);
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

        // TODO The "./" prefix should be unnecessary and is not cross platform.
        // TODO Use a $TEMP directory here.
        File archive = new File("./" + getPeer().getName() + "_Project.zip");

        try {
            this.setProgressInfo("Creating Archive");

            long time = System.currentTimeMillis();

            // Create project archive.
            // TODO Track Progress and provide possibility to cancel
            FileZipper.createProjectZipArchive(this.toSend, archive
                .getAbsolutePath(), this.sharedProject.getProject());

            log.debug(String.format(
                "Created project archive in %d s (%d KB): %s", (System
                    .currentTimeMillis() - time) / 1000,
                archive.length() / 1024, archive.getAbsolutePath()));

            this.setProgressInfo("Sending project archive");

            // Send data.
            // TODO Track Progress and provide possibility to cancel
            this.transmitter.sendProjectArchive(this.peer, this.sharedProject
                .getProject(), archive, this);
        } catch (Exception e) {
            failed(e);
        } finally {
            if (!archive.delete()) {
                log.warn("Could not delete archive: "
                    + archive.getAbsolutePath());
            }
        }
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
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
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

        this.setProgressInfo("Waiting for confirmation");

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
        this.setProgressInfo("");

        return true;
    }

    /**
     * Send activities which set the active editors and their viewports.
     */
    private void sendDriverEditors() {

        /*
         * TODO Instead of sending this information to everybody, we should
         * rather use a subscription mechanism so that everybody can ask us
         * where we are
         */

        // TODO The following code is broken!
        // final EditorManager editorManager = EditorManager.getDefault();
        // ArrayList<IPath> driverEditors = new ArrayList<IPath>(editorManager
        // .getDriverEditors());
        //
        // // Make sure the active editor is the last in this list.
        // IPath activeDriverEditor = editorManager.getActiveDriverEditor();
        // if (activeDriverEditor != null) {
        // driverEditors.remove(activeDriverEditor);
        // driverEditors.add(activeDriverEditor);
        // }
        //
        // // Create editor activated activities and viewport information for
        // all
        // // the driver's editors.
        // final ActivitySequencer sequencer =
        // this.sharedProject.getSequencer();
        // for (final IPath path : driverEditors) {
        //
        // if (this.sharedProject.getProject().findMember(path) == null) {
        // log.warn("Editor " + path + " is not a driver's editor!");
        // return;
        // }
        // sequencer.activityCreated(new EditorActivity(
        // EditorActivity.Type.Activated, path));
        //
        // Util.runSafeSWTSync(log, new Runnable() {
        // public void run() {
        // ILineRange range = editorManager.getCurrentViewport(path);
        //
        // if (range != null) {
        // // TODO Investigate whether it is okay to dispatch
        // // directly
        // sequencer.activityCreated(new ViewportActivity(range,
        // path));
        // }
        // }
        // });
        // }
    }

    public String getProjectName() {
        return this.sharedProject.getProject().getName();
    }

    @Override
    public void cancel(String errorMsg, boolean replicated) {
        super.cancel(errorMsg, replicated);
        sharedProject.returnColor(this.colorID);
    }

    public void setProgressInfo(String progress_info) {
        this.progress_info = progress_info;
        this.invitationUI.updateInvitationProgress(this.peer);
    }

}
