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
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.FileZipper;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.VersionManager;

/**
 * An outgoing invitation process.
 * 
 * TODO FIXME The whole invitation procedure needs to be completely redone,
 * because it can cause race conditions. In particular cancellation is not
 * possible at arbitrary times (something like an CANCEL_ACK is needed)
 * 
 * TODO Use {@link WorkspaceModifyOperation}s to wrap the whole invitation
 * process, so that background activities such as autoBuilding do not interfere
 * with the InvitationProcess
 * 
 * @author rdjemili
 */
public class OutgoingInvitationProcess extends InvitationProcess implements
    IOutgoingInvitationProcess {

    protected final static Logger log = Logger
        .getLogger(OutgoingInvitationProcess.class);

    /* Dependencies */
    protected Saros saros;

    protected DataTransferManager dataTransferManager;

    protected ISharedProject sharedProject;

    /* Fields */
    protected FileList remoteFileList;

    protected FileList localFileList;

    protected List<IPath> toSend;

    /** size of project archive file */
    protected final long fileSize = 100;

    /** size of current transfered part of archive file. */
    protected long transferedFileSize = 0;

    protected SubMonitor monitor;

    protected VersionManager versionManager;

    public OutgoingInvitationProcess(Saros saros, ITransmitter transmitter,
        DataTransferManager dataTransferManager, JID to,
        ISharedProject sharedProject, String description,
        IInvitationUI inviteUI, int colorID, FileList localFileList,
        SubMonitor monitor, InvitationProcessObservable invitationProcesses,
        VersionManager versionManager) {

        super(transmitter, to, description, colorID, null, invitationProcesses);

        this.localFileList = localFileList;
        this.invitationUI = inviteUI;
        this.saros = saros;
        this.sharedProject = sharedProject;
        this.dataTransferManager = dataTransferManager;
        this.versionManager = versionManager;

        this.monitor = monitor;
        this.monitor.beginTask("Performing Invitation", 100);
        setState(State.INITIALIZED);
    }

    public void start() {
        setState(State.INVITATION_SENT);
        transmitter.sendInviteMessage(sharedProject, peer, description,
            colorID, versionManager.getVersion());
        this.monitor.worked(5);
    }

    public void startSynchronization() {

        assertState(State.GUEST_FILELIST_SENT);

        setState(State.SYNCHRONIZING);

        try {
            this.toSend = new LinkedList<IPath>();
            this.toSend.addAll(this.remoteFileList.getAddedPaths());
            this.toSend.addAll(this.remoteFileList.getAlteredPaths());

            monitor.subTask("Sending Archive...");
            sendArchive(monitor.newChild(70));

            monitor.subTask("Waiting for Peer to Join");
            if (!blockUntilJoinReceived(monitor.newChild(5))) {
                cancel(null, false);
            }
        } catch (RuntimeException e) {
            cancel("An internal error occurred while starting to synchronize: "
                + e.toString(), false);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void invitationAccepted(JID from) {
        assertState(State.INVITATION_SENT);

        SubMonitor pmAccept = this.monitor.newChild(15);
        this.monitor.subTask("Sending Filelist");
        pmAccept.beginTask("Invitation Accepted", 100);

        // HACK add resource specifier to jid
        if (this.peer.equals(from)) {
            this.peer = from;
        }

        try {
            // Wait for Jingle before we send the file list...
            this.transmitter.awaitJingleManager(this.peer);
            pmAccept.worked(30);

            // Could have been canceled in between:
            if (this.state == State.CANCELED)
                return;

            setState(State.HOST_FILELIST_SENT);
            this.transmitter.sendFileList(this.peer, this.localFileList,
                pmAccept.newChild(70));

            // Could have been canceled in between:
            if (this.state == State.CANCELED)
                return;

        } catch (Exception e) {
            failed(e);
        }

    }

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
     * {@inheritDoc}
     */
    public void joinReceived(JID from) {

        // HACK Necessary because an empty list of files
        // to send causes a Race condition otherwise...
        blockUntil(State.SYNCHRONIZING_DONE, EnumSet.of(
            State.GUEST_FILELIST_SENT, State.SYNCHRONIZING));

        assertState(State.SYNCHRONIZING_DONE);

        this.sharedProject.addUser(new User(sharedProject, from, colorID));
        setState(State.DONE);
        this.monitor.done();

        // TODO Find a more reliable way to remove InvitationProcess
        this.invitationProcesses.removeInvitationProcess(this);

        this.transmitter.sendUserListTo(from, this.sharedProject
            .getParticipants());
    }

    /**
     * Block until the given state 'until' has been reached, but allow only the
     * given states as valid in the meantime.
     * 
     * This method cancels if this InvitationProcess is in an invalid state.
     * 
     * Returns false if the given state 'until' was not reached, but
     * 
     * a.) an invalid state was reached
     * 
     * b.) the cancel state was reached
     */
    protected boolean blockUntil(State until, EnumSet<State> validStates) {

        while (this.state != until) {

            if (!validStates.contains(this.state)) {
                cancel("Unexpected state(" + this.state + ")", false);
                return false;
            }

            if (getState() == State.CANCELED) {
                return false;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("Code not designed to be interruptable", e);
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void resourceReceived(JID from, IPath path, InputStream in) {
        failState();
    }

    /**
     * Send all files contained in {@link #toSend} as an archive file to the
     * peer of this invitation process.
     */
    protected void sendArchive(SubMonitor monitor) {

        monitor.beginTask("Sending as archive", 100);

        File archive = null;

        try {
            if (getState() == State.CANCELED) {
                this.toSend.clear();
                return;
            }

            if (this.toSend.size() == 0) {
                setState(State.SYNCHRONIZING_DONE);
                return;
            }

            archive = File.createTempFile(getPeer().getName(), ".zip");

            monitor.subTask("Zipping archive");
            FileZipper.createProjectZipArchive(this.toSend, archive,
                this.sharedProject.getProject(), monitor.newChild(30));

            monitor.subTask("Sending archive");
            transmitter.sendProjectArchive(this.peer, this.sharedProject
                .getProject(), archive, monitor.newChild(70));

            if (getState() == State.SYNCHRONIZING) {
                setState(State.SYNCHRONIZING_DONE);
            }
        } catch (Exception e) {
            failed(e);
        } finally {
            // Delete Archive
            if (archive != null && !archive.delete()) {
                log.warn("Could not delete archive: "
                    + archive.getAbsolutePath());
            }
            monitor.done();
        }
    }

    /**
     * Blocks until the join message has been received or the user canceled.
     * 
     * @param subMonitor
     * 
     * @return <code>true</code> if the join message has been received.
     *         <code>false</code> if the user chose to cancel
     */
    private boolean blockUntilJoinReceived(SubMonitor subMonitor) {

        subMonitor.beginTask("Waiting for Peer to Join", 1000);

        try {
            while (this.state != State.DONE) {
                if (getState() == State.CANCELED) {
                    return false;
                }

                try {
                    Thread.sleep(100);
                    // Grow but never finish monitor...
                    subMonitor.setWorkRemaining(1000);
                    subMonitor.worked(1);
                } catch (InterruptedException e) {
                    log.error("Code not designed to be interruptable", e);
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            return true;
        } finally {
            subMonitor.done();
        }
    }

    public String getProjectName() {
        return this.sharedProject.getProject().getName();
    }

    @Override
    public void cancel(String errorMsg, boolean replicated) {
        super.cancel(errorMsg, replicated);

        if (!this.monitor.isCanceled())
            this.monitor.setCanceled(true);

        sharedProject.returnColor(this.colorID);
    }
}
