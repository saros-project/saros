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
package de.fu_berlin.inf.dpp.invitation;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.UserCancellationException;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.util.FileZipper;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.Compatibility;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * TODO Use {@link WorkspaceModifyOperation}s to wrap the whole invitation
 * process, so that background activityDataObjects such as autoBuilding do not
 * interfere with the InvitationProcess
 * 
 */
public class OutgoingInvitationProcess extends InvitationProcess {

    private final static Logger log = Logger
        .getLogger(OutgoingInvitationProcess.class);

    protected ISharedProject sharedProject;
    protected FileList remoteFileList;
    protected FileList localFileList;
    protected File archive;
    protected List<IPath> toSend = new LinkedList<IPath>();
    protected SubMonitor monitor;
    protected VersionManager versionManager;
    protected StopManager stopManager;
    protected IOutgoingInvitationUI outInvitationUI;
    protected String invitationID;
    protected AtomicBoolean cancelled = new AtomicBoolean(false);
    protected String cancelMessage = "";
    protected SarosPacketCollector invitationCompleteCollector;

    public OutgoingInvitationProcess(ITransmitter transmitter, JID to,
        ISharedProject sharedProject, String description,
        IOutgoingInvitationUI inviteUI, int colorID, FileList localFileList,
        SubMonitor monitor, InvitationProcessObservable invitationProcesses,
        VersionManager versionManager, StopManager stopManager) {

        super(transmitter, to, description, colorID, invitationProcesses);

        this.localFileList = localFileList;
        this.invitationUI = inviteUI;
        this.outInvitationUI = inviteUI;
        this.sharedProject = sharedProject;
        this.versionManager = versionManager;
        this.stopManager = stopManager;
        this.monitor = monitor;
        setState(State.INITIALIZED);
    }

    public void start() {
        monitor.beginTask("Invitation started...", 100);
        Random myRand = new Random();
        this.invitationID = String.valueOf(myRand.nextLong());
        try {
            checkAvailability(monitor.newChild(2));

            VersionInfo versionInfo = checkVersion(monitor.newChild(3));

            sendInvitation(versionInfo, monitor.newChild(5));

            getFileListDiff(monitor.newChild(10));

            createArchive(monitor.newChild(5));

            sendArchive(monitor.newChild(70));

            done(monitor.newChild(5));
        } catch (LocalCancellationException e) {
            cancel(null, CancelLocation.LOCAL, CancelOption.NOTIFY_PEER);
        } catch (UserCancellationException e) {
            // This must be a RemoteCancellationException
            // ignore because we will a cancel extension
        } catch (IOException e) {
            cancel(null, CancelLocation.LOCAL, CancelOption.NOTIFY_PEER);
        } finally {
            // Delete Archive
            if (archive != null && !archive.delete()) {
                log.warn("Inv " + Util.prefix(peer)
                    + ": Could not delete archive: "
                    + archive.getAbsolutePath());
            }
            monitor.done();
        }
    }

    /**
     * Check whether the peer's client has Saros support.
     */
    protected void checkAvailability(SubMonitor subMonitor) {
        subMonitor.setTaskName("Checking presence...");
        JID rqPeer = outInvitationUI.hasSaros(peer);
        if (rqPeer == null) {
            /**
             * TODO: we should check whether the user is online. How can we do
             * that? hasSaros(peer) returns null if the user is offline or if
             * the user does not have Saros, but we should distinguish these two
             * cases. If the user is offline, we can cancel the invitation.
             * 
             * (!outInvitationUI.isOnline(peer)) { cancel(null,
             * CancelLocation.LOCAL, CancelOption.DO_NOT_NOTIFY_PEER); throw new
             * CancellationException(); }
             */
            if (!outInvitationUI.confirmUnsupportedSaros(peer)) {
                cancel(null, CancelLocation.LOCAL,
                    CancelOption.DO_NOT_NOTIFY_PEER);
                throw new CancellationException();
            }
        } else {
            peer = rqPeer;
        }
    }

    /**
     * Checks the compatibility of the local Saros version with the peer's one.
     * If the versions are compatible, the invitation continues, otherwise a
     * confirmation of the user is required (a {@link MessageDialog} pops up).
     */
    protected VersionInfo checkVersion(SubMonitor subMonitor) {
        log.debug("Inv " + Util.prefix(peer) + ": Checking peer's version...");
        subMonitor.setTaskName("Checking version...");
        VersionInfo versionInfo = versionManager.determineCompatibility(peer);

        if (checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER)) {
            log.debug("Inv " + Util.prefix(peer) + ": Cancellation checkpoint");
            return null;
        }

        Compatibility comp = null;

        if (versionInfo != null)
            comp = versionInfo.compatibility;

        if (comp == VersionManager.Compatibility.OK) {
            log.debug("Inv " + Util.prefix(peer)
                + ": Saros versions are compatible, proceeding...");
            return versionInfo;
        } else {
            if ((outInvitationUI).confirmVersionConflict(versionInfo, peer))
                return versionInfo;
            else {
                cancel(null, CancelLocation.LOCAL,
                    CancelOption.DO_NOT_NOTIFY_PEER);
                throw new CancellationException();
            }
        }
    }

    /**
     * 
     * @param versionInfo
     *            a {@link VersionInfo} object with ultimate
     *            {@link Compatibility} information, so the peer can use it
     *            without further compatibility check, unless
     *            {@link Compatibility} is null (the host could not determine
     *            compatibility).
     * @throws IOException
     * @throws CancellationException
     */
    protected void sendInvitation(VersionInfo versionInfo, SubMonitor subMonitor)
        throws LocalCancellationException, IOException {
        if (checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER)) {
            log.debug("Inv " + Util.prefix(peer) + ": Cancellation checkpoint");
            throw new CancellationException();
        }
        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Sending invitation...");

        VersionInfo hostVersionInfo = versionInfo;
        if (versionInfo == null) {
            hostVersionInfo = new VersionInfo();
            hostVersionInfo.compatibility = null;
            hostVersionInfo.version = versionManager.getVersion();
        } else {
            versionInfo.version = versionManager.getVersion();
        }
        setState(State.INVITATION_SENT);

        transmitter.sendInvitation(sharedProject, peer, description, colorID,
            hostVersionInfo, invitationID);
        log.debug("Inv " + Util.prefix(peer) + ": Invitation sent.");
        subMonitor.worked(25);
        subMonitor
            .setTaskName("Invitation sent. Waiting for acknowledgement...");

        transmitter.receiveFileListRequest(invitationID, subMonitor
            .newChild(75));
        log.debug("Inv " + Util.prefix(peer) + ": FileListRequest received.");
    }

    protected void getFileListDiff(SubMonitor subMonitor) throws IOException,
        UserCancellationException {
        if (checkCancellation(CancelOption.NOTIFY_PEER)) {
            log.debug("Inv " + Util.prefix(peer) + ": Cancellation checkpoint");
            throw new CancellationException();
        }
        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Sending file list...");

        assertState(State.INVITATION_SENT);

        // Wait for Jingle before we send the file list...
        transmitter.awaitJingleManager(this.peer);
        subMonitor.worked(25);

        if (checkCancellation(CancelOption.NOTIFY_PEER)) {
            log.debug("Inv " + Util.prefix(peer) + ": Cancellation checkpoint");
            throw new LocalCancellationException();
        }

        setState(State.HOST_FILELIST_SENT);
        SarosPacketCollector fileListCollector = transmitter
            .getInvitationCollector(invitationID,
                FileTransferType.FILELIST_TRANSFER);

        transmitter.sendFileList(peer, invitationID, localFileList, subMonitor
            .newChild(25, SubMonitor.SUPPRESS_ALL_LABELS));
        subMonitor
            .setTaskName("File list sent. Waiting for the peer's file list...");

        if (checkCancellation(CancelOption.NOTIFY_PEER)) {
            log.debug("Inv " + Util.prefix(peer) + ": Cancellation checkpoint");
            throw new LocalCancellationException();
        }

        remoteFileList = transmitter.receiveFileList(fileListCollector,
            subMonitor, true);
        subMonitor.setTaskName("Peer's file list received.");

        if (checkCancellation(CancelOption.NOTIFY_PEER)) {
            log.debug("Inv " + Util.prefix(peer) + ": Cancellation checkpoint");
            throw new LocalCancellationException();
        }

        log.debug("Inv " + Util.prefix(peer) + ": Received FileList.");
        assertState(State.HOST_FILELIST_SENT);
        setState(State.GUEST_FILELIST_SENT);

        assertState(State.GUEST_FILELIST_SENT);
        setState(State.SYNCHRONIZING);
        toSend.addAll(remoteFileList.getAddedPaths());
        toSend.addAll(remoteFileList.getAlteredPaths());
    }

    protected void createArchive(SubMonitor subMonitor) throws IOException,
        CancellationException {
        if (checkCancellation(CancelOption.NOTIFY_PEER)) {
            log.debug("Inv " + Util.prefix(peer) + ": Cancellation checkpoint");
            throw new CancellationException();
        }

        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Creating archive...");
        archive = null;

        // STOP all users
        log.debug("Inv " + Util.prefix(peer) + ": Stopping users: "
            + sharedProject.getParticipants());
        // TODO: startHandles outside of sync block?
        List<StartHandle> startHandles;
        synchronized (sharedProject) {
            startHandles = stopManager.stop(sharedProject.getParticipants(),
                "Synchronizing invitation", subMonitor.newChild(25,
                    SubMonitor.SUPPRESS_ALL_LABELS));
        }
        EditorAPI.saveProject(sharedProject.getProject());

        /**
         * If the filelist <code>toSend</code> is empty, the projects are
         * identical. We do not have to send anything, so we do not create an
         * archive either.
         */
        SubMonitor archiveMonitor = subMonitor.newChild(25,
            SubMonitor.SUPPRESS_ALL_LABELS);
        if (toSend.size() != 0) {
            /*
             * FIX #2836964: Prefix string too short
             * 
             * Do not delete the "SarosSyncArchive" prefix.
             */
            archive = File.createTempFile("SarosSyncArchive-"
                + getPeer().getName(), ".zip");
            FileZipper.createProjectZipArchive(toSend, archive, sharedProject
                .getProject(), archiveMonitor);
        }
        archiveMonitor.done();

        User newUser = new User(sharedProject, peer, colorID);
        this.sharedProject.addUser(newUser);
        log.debug(Util.prefix(peer) + " added to project, colorID: " + colorID);

        synchronized (sharedProject) {
            synchronizeUserList();
        }
        subMonitor.worked(25);
        // START all users
        for (StartHandle startHandle : startHandles) {
            log.debug("Inv " + Util.prefix(peer) + ": Starting user "
                + Util.prefix(startHandle.getUser().getJID()));
            startHandle.start();
        }
        subMonitor.done();
    }

    protected void sendArchive(SubMonitor subMonitor)
        throws UserCancellationException, IOException {

        invitationCompleteCollector = transmitter
        .getInvitationCompleteCollector(invitationID);

        if (archive != null) {
            subMonitor.setTaskName("Sending archive...");
            transmitter.sendProjectArchive(this.peer, invitationID,
                this.sharedProject.getProject(), archive, subMonitor);
        } else {
            log.debug("Inv " + Util.prefix(peer)
                + ": No archive to send. The projects must be 100% identical.");
        }

        if (getState() == State.SYNCHRONIZING) {
            setState(State.SYNCHRONIZING_DONE);
        }

    }

    protected void synchronizeUserList() throws CancellationException,
        IOException {
        if (checkCancellation(CancelOption.NOTIFY_PEER)) {
            log.debug("Inv " + Util.prefix(peer) + ": Cancellation checkpoint");
            throw new CancellationException();
        }
        monitor.subTask("Waiting for peer to join...");
        for (User user : sharedProject.getRemoteUsers()) {
            transmitter.sendUserList(user.getJID(), invitationID, sharedProject
                .getRemoteUsers());
        }
        transmitter.receiveUserListConfirmation(sharedProject.getRemoteUsers(),
            monitor);
        log.debug("Inv " + Util.prefix(peer)
            + ": All userListConfirmations arrived.");
    }

    protected void done(SubMonitor subMonitor) throws CancellationException,
        IOException, LocalCancellationException {
        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Completing invitation...");

        transmitter.receiveInvitationCompleteConfirmation(monitor.newChild(50),
            invitationCompleteCollector);
        log.debug("Inv " + Util.prefix(peer)
            + ": Notifying participants that the invitation is complete.");
        sharedProject.userInvitationCompleted(sharedProject.getUser(peer));
        synchronized (sharedProject) {
            synchronizeUserList();
        }
        assertState(State.SYNCHRONIZING_DONE);
        setState(State.DONE);
        // TODO Find a more reliable way to remove InvitationProcess
        // Why is this not reliable?
        invitationProcesses.removeInvitationProcess(this);
    }

    public void cancel(String errorMsg, CancelLocation cancelLocation,
        CancelOption notification) {

        if (!cancelled.compareAndSet(false, true))
            return;

        if (monitor != null)
            monitor.setCanceled(true);

        switch (cancelLocation) {
        case LOCAL:
            if (errorMsg != null) {
                cancelMessage = "Invitation was cancelled locally"
                    + " because of an error: " + errorMsg;
                log.error("Inv " + Util.prefix(peer) + ": " + cancelMessage);
            } else {
                cancelMessage = "Invitation was cancelled by local user.";
                log.debug("Inv " + Util.prefix(peer) + ": " + cancelMessage);
            }
            break;
        case REMOTE:
            if (errorMsg != null) {
                cancelMessage = "Invitation was cancelled by the remote user "
                    + " because of an error on his/her side: " + errorMsg;
                log.error("Inv " + Util.prefix(peer) + ": " + cancelMessage);
            } else {
                cancelMessage = "Invitation was cancelled by the remote user.";
                log.debug("Inv " + Util.prefix(peer) + ": " + cancelMessage);
            }
        }

        synchronized (this) {
            if (this.state == State.CANCELED) {
                return;
            }
            setState(State.CANCELED);
        }

        if (!this.monitor.isCanceled())
            this.monitor.setCanceled(true);

        // The leaveHandler solves this. Is it okay that way?
        // sharedProject.removeUser(sharedProject.getUser(peer));
        sharedProject.returnColor(this.colorID);

        switch (notification) {
        case NOTIFY_PEER:
            transmitter.sendCancelInvitationMessage(peer, errorMsg);
            break;
        case DO_NOT_NOTIFY_PEER:
        }

        invitationProcesses.removeInvitationProcess(this);

        if (outInvitationUI != null)
            outInvitationUI.cancel(peer, errorMsg, cancelLocation);
    }

    /**
     * Checks whether the invitation process or the monitor has been cancelled.
     * If the monitor has been cancelled but the invitation process has not yet,
     * it cancels the invitation process.
     * 
     * @return <code>true</code> if either the invitation process or the monitor
     *         has been cancelled, <code>false</code> otherwise.
     */
    public boolean checkCancellation(CancelOption notification) {
        if (cancelled.get())
            return true;

        if (monitor == null)
            return false;

        if (monitor.isCanceled()) {
            cancel(null, CancelLocation.LOCAL, notification);
            return true;
        }
        return false;
    }

    public String getProjectName() {
        return this.sharedProject.getProject().getName();
    }

    public String getCancelMessage() {
        return cancelMessage;
    }
}
