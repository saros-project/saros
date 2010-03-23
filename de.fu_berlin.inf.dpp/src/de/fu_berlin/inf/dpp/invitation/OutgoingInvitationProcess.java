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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.StreamService;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.ui.wizards.InvitationWizard;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.Compatibility;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * @author rdjemili
 * @author sotitas
 */

public class OutgoingInvitationProcess extends InvitationProcess {

    private final static Logger log = Logger
        .getLogger(OutgoingInvitationProcess.class);

    protected final static Random INVITATION_RAND = new Random();

    protected ISharedProject sharedProject;

    /**
     * This OutgoingInvitation is about the following IProject
     */
    protected IProject project;

    protected List<IResource> partialProjectResources;
    protected FileList remoteFileList;
    protected File archive;
    /**
     * A {@link VersionInfo} object with ultimate {@link Compatibility}
     * information, so the peer can use it without further compatibility check,
     * unless {@link Compatibility} is null (the host could not determine
     * compatibility).
     */
    protected VersionInfo versionInfo;
    protected List<IPath> toSend = new LinkedList<IPath>();

    protected SubMonitor monitor;
    protected VersionManager versionManager;
    protected StopManager stopManager;
    protected DiscoveryManager discoveryManager;
    protected String invitationID;
    protected AtomicBoolean cancelled = new AtomicBoolean(false);
    protected SarosCancellationException cancellationCause;
    protected SarosPacketCollector invitationCompleteCollector;

    @Inject
    protected SessionManager sessionManager;

    public OutgoingInvitationProcess(ITransmitter transmitter, JID to,
        ISharedProject sharedProject, List<IResource> partialProjectResources,
        IProject project, String description, int colorID,
        InvitationProcessObservable invitationProcesses,
        VersionManager versionManager, StopManager stopManager,
        DiscoveryManager discoveryManager) {

        super(transmitter, to, description, colorID, invitationProcesses);

        this.sharedProject = sharedProject;
        this.partialProjectResources = partialProjectResources;
        this.versionManager = versionManager;
        this.stopManager = stopManager;
        this.discoveryManager = discoveryManager;
        this.project = project;
    }

    public void start(SubMonitor monitor) throws SarosCancellationException {

        log.debug("Inv" + Util.prefix(peer) + ": Invitation has started.");

        this.monitor = monitor;
        monitor.beginTask("Invitation has started.", 100);
        this.invitationID = String.valueOf(INVITATION_RAND.nextLong());
        try {
            checkAvailability(monitor.newChild(1));

            checkVersion(monitor.newChild(1));

            sendInvitation(monitor.newChild(1));

            getFileListDiff(monitor.newChild(1));

            streamArchive(monitor.newChild(93));

            completeInvitation(monitor.newChild(3));
        } catch (LocalCancellationException e) {
            localCancel(e.getMessage(), CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (RemoteCancellationException e) {
            remoteCancel(e.getMessage());
            executeCancellation();
        } catch (SarosCancellationException e) {
            /**
             * If this exception is thrown because of a local cancellation, we
             * initiate a localCancel here.
             * 
             * If this exception is thrown because of a remote cancellation, the
             * call of localCancel will be ignored.
             */
            localCancel(e.getMessage(), CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (IOException e) {
            String errorMsg = "Unknown error: " + e;
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (Exception e) {
            log.warn("Inv" + Util.prefix(peer)
                + ": This type of Exception is not expected: ", e);
            String errorMsg = "Unknown error: " + e;
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
            executeCancellation();
        } finally {
            // Delete Archive
            if (archive != null && !archive.delete()) {
                log.warn("Inv" + Util.prefix(peer)
                    + ": Could not delete archive: "
                    + archive.getAbsolutePath());
            }
            monitor.done();
        }
    }

    /**
     * Check whether the peer's client has Saros support.
     */
    protected void checkAvailability(SubMonitor subMonitor)
        throws LocalCancellationException {

        log.debug("Inv" + Util.prefix(peer) + ": Checking Saros support...");
        subMonitor.setTaskName("Checking Saros support...");

        JID rqPeer = discoveryManager.getSupportingPresence(peer,
            Saros.NAMESPACE);
        if (rqPeer == null) {
            log.debug("Inv" + Util.prefix(peer) + ": Saros is not supported.");
            if (!InvitationWizard.confirmUnsupportedSaros(peer)) {
                localCancel(null, CancelOption.DO_NOT_NOTIFY_PEER);
                throw new LocalCancellationException();
            }
            /**
             * In order to avoid inviting other XMPP clients, we construct an
             * RQ-JID.
             */
            rqPeer = new JID(peer.getBareJID() + "/" + Saros.RESOURCE);
        } else {
            log.debug("Inv" + Util.prefix(peer) + ": Saros is supported.");
        }
        peer = rqPeer;
    }

    /**
     * Checks the compatibility of the local Saros version with the peer's one.
     * If the versions are compatible, the invitation continues, otherwise a
     * confirmation of the user is required (a {@link MessageDialog} pops up).
     */
    protected void checkVersion(SubMonitor subMonitor)
        throws SarosCancellationException {

        log.debug("Inv" + Util.prefix(peer) + ": Checking peer's version...");
        subMonitor.setTaskName("Checking version...");
        VersionInfo versionInfo = versionManager.determineCompatibility(peer);

        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        Compatibility comp = null;

        if (versionInfo != null)
            comp = versionInfo.compatibility;

        if (comp == VersionManager.Compatibility.OK) {
            log.debug("Inv" + Util.prefix(peer)
                + ": Saros versions are compatible, proceeding...");
            this.versionInfo = versionInfo;
        } else {
            log.debug("Inv" + Util.prefix(peer)
                + ": Saros versions are not compatible.");
            if (InvitationWizard.confirmVersionConflict(versionInfo, peer,
                versionManager.getVersion()))
                this.versionInfo = versionInfo;
            else {
                localCancel(null, CancelOption.DO_NOT_NOTIFY_PEER);
                throw new LocalCancellationException();
            }
        }
    }

    protected void sendInvitation(SubMonitor subMonitor)
        throws SarosCancellationException, IOException {

        log.debug("Inv" + Util.prefix(peer) + ": Sending invitation...");
        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);
        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Sending invitation...");

        /*
         * TODO: this method should get a complete VersionInfo object from the
         * checkVersion() method.
         */
        VersionInfo hostVersionInfo = versionInfo;
        if (hostVersionInfo == null) {
            hostVersionInfo = new VersionInfo();
            hostVersionInfo.compatibility = null;
        }

        hostVersionInfo.version = versionManager.getVersion();

        SarosPacketCollector fileListRequestCollector = transmitter
            .getFileListRequestCollector(invitationID);

        transmitter.sendInvitation(sharedProject.getProjectMapper().getID(
            this.project), peer, description, colorID, hostVersionInfo,
            invitationID, sharedProject.getSessionStart());

        subMonitor.worked(25);
        subMonitor
            .setTaskName("Invitation sent. Waiting for acknowledgement...");

        transmitter.receiveFileListRequest(fileListRequestCollector,
            invitationID, subMonitor.newChild(75,
                SubMonitor.SUPPRESS_ALL_LABELS));
        log.debug("Inv" + Util.prefix(peer)
            + ": File list request has received.");
    }

    protected void getFileListDiff(SubMonitor subMonitor) throws IOException,
        SarosCancellationException {

        checkCancellation(CancelOption.NOTIFY_PEER);

        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Initializing Jingle...");

        // Wait for Jingle before we send the file list...
        transmitter.awaitJingleManager(this.peer);
        subMonitor.worked(25);

        checkCancellation(CancelOption.NOTIFY_PEER);

        SarosPacketCollector fileListCollector = transmitter
            .getInvitationCollector(invitationID,
                FileTransferType.FILELIST_TRANSFER);
        log.debug("Inv" + Util.prefix(peer)
            + ": Waiting for remote file list...");

        subMonitor.setTaskName("Creating file list...");
        FileList localFileList;
        try {
            if (partialProjectResources != null) {
                localFileList = new FileList(partialProjectResources
                    .toArray(new IResource[partialProjectResources.size()]));
            } else {
                localFileList = new FileList(project);
            }

        } catch (CoreException e) {
            throw new LocalCancellationException(e.getMessage(),
                CancelOption.NOTIFY_PEER);
        }

        log.debug("Inv" + Util.prefix(peer) + ": Sending file list...");
        subMonitor.setTaskName("Sending file list...");
        transmitter.sendFileList(peer, invitationID, localFileList, subMonitor
            .newChild(50, SubMonitor.SUPPRESS_ALL_LABELS));
        subMonitor.setTaskName("Waiting for the peer's file list...");

        checkCancellation(CancelOption.NOTIFY_PEER);

        remoteFileList = transmitter.receiveFileList(fileListCollector,
            subMonitor, true);
        log.debug("Inv" + Util.prefix(peer)
            + ": Remote file list has been received.");

        checkCancellation(CancelOption.NOTIFY_PEER);

        toSend.addAll(remoteFileList.getAddedPaths());
        toSend.addAll(remoteFileList.getAlteredPaths());
    }

    protected void streamArchive(SubMonitor subMonitor)
        throws SarosCancellationException {

        checkCancellation(CancelOption.NOTIFY_PEER);

        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Creating archive...");

        /*
         * STOP users
         * 
         * TODO: stop all users. If we stop users which are currently joining,
         * it can cause a deadlock, because the StopManager does not answer if
         * someone is already stopped.
         */
        Collection<User> usersToStop = new ArrayList<User>();
        for (User user : sharedProject.getParticipants()) {
            if (user.isInvitationComplete())
                usersToStop.add(user);
        }
        log.debug("Inv" + Util.prefix(peer) + ": Stopping users: "
            + usersToStop);
        // TODO: startHandles outside of sync block?
        List<StartHandle> startHandles;
        synchronized (sharedProject) {
            startHandles = stopManager.stop(usersToStop,
                "Synchronizing invitation", subMonitor.newChild(25,
                    SubMonitor.SUPPRESS_ALL_LABELS));
        }

        try {
            // TODO: Ask the user whether to save the resources, but only if
            // they have changed. How to ask Eclipse whether there are resource
            // changes?
            // if (outInvitationUI.confirmProjectSave(peer))
            EditorAPI.saveProject(this.project, false);
            // else
            // throw new LocalCancellationException();

            User newUser = null;
            synchronized (sharedProject) {
                newUser = new User(sharedProject, peer, colorID);
                this.sharedProject.addUser(newUser);
                log.debug(Util.prefix(peer) + " added to project, colorID: "
                    + colorID);
                synchronizeUserList();
            }

            if (toSend.size() != 0) {

                streamSession = streamServiceManager.createSession(
                    archiveStreamService, newUser, null, sessionListener);

                archiveStreamService.setFileNumber(toSend.size());

                streamSession.setListener(sessionListener);

                subMonitor.setTaskName("Streaming archive...");

                performFileStream(archiveStreamService, streamSession,
                    subMonitor.newChild(75));
            }

        } catch (Exception e) {
            log.error("Error while executing archive stream: ", e);
        } finally {
            // START all users
            for (StartHandle startHandle : startHandles) {
                log.debug("Inv" + Util.prefix(peer) + ": Starting user "
                    + Util.prefix(startHandle.getUser().getJID()));
                startHandle.start();
            }
        }
    }

    private void performFileStream(StreamService streamService,
        final StreamSession session, SubMonitor subMonitor)
        throws SarosCancellationException {

        OutputStream output = session.getOutputStream(0);
        ZipOutputStream zout = new ZipOutputStream(output);
        int worked = 0;
        int lastWorked = 0;
        int filesSent = 0;
        double increment = (double) 100 / toSend.size();
        subMonitor.beginTask("Streaming files...", 100);
        try {
            for (IPath ip : toSend) {
                IFile file = this.project.getFile(ip);
                String absPath = file.getLocation().toPortableString();

                byte[] buffer = new byte[streamService.getChunkSize()[0]];
                InputStream input = new FileInputStream(absPath);

                ZipEntry ze = new ZipEntry(ip.toPortableString());
                zout.putNextEntry(ze);

                int numRead = 0;
                while ((numRead = input.read(buffer)) > 0) {
                    zout.write(buffer, 0, numRead);
                }
                zout.flush();
                zout.closeEntry();
                input.close();

                // Progress monitor
                worked = (int) Math.round(increment * filesSent);

                if ((worked - lastWorked) > 0) {
                    subMonitor.worked((worked - lastWorked));
                    lastWorked = worked;
                }

                filesSent++;

                checkCancellation(CancelOption.NOTIFY_PEER);
            }
        } catch (IOException e) {
            error = true;
            log.error("Error while sending file: ", e);
            localCancel(
                "An I/O problem occurred while the project's files were being sent: \""
                    + e.getMessage() + "\" The invitation was cancelled.",
                CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (SarosCancellationException e) {
            log.debug("Invitation process was cancelled.");
        } catch (Exception e) {
            log.error("Unknown exception: ", e);
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(zout);
        }
        subMonitor.done();
    }

    protected void completeInvitation(SubMonitor subMonitor)
        throws IOException, SarosCancellationException {

        log.debug("Inv" + Util.prefix(peer)
            + ": Waiting for invitation complete confirmation...");
        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Waiting for peer to complete invitation...");

        invitationCompleteCollector = transmitter
            .getInvitationCompleteCollector(invitationID);

        transmitter.receiveInvitationCompleteConfirmation(monitor.newChild(50,
            SubMonitor.SUPPRESS_ALL_LABELS), invitationCompleteCollector);
        log.debug("Inv" + Util.prefix(peer)
            + ": Notifying participants that the invitation is complete.");

        subMonitor.setTaskName("Completing invitation...");
        synchronized (sharedProject) {
            sharedProject.userInvitationCompleted(sharedProject.getUser(peer));
            synchronizeUserList();
        }
        subMonitor.setTaskName("Invitation has completed successfully.");

        invitationProcesses.removeInvitationProcess(this);
        log.debug("Inv" + Util.prefix(peer)
            + ": Invitation has completed successfully.");
    }

    protected void synchronizeUserList() throws SarosCancellationException {
        checkCancellation(CancelOption.NOTIFY_PEER);

        log.debug("Inv" + Util.prefix(peer) + ": Synchronizing userlist "
            + sharedProject.getParticipants());

        SarosPacketCollector userListConfirmationCollector = transmitter
            .getUserListConfirmationCollector();

        for (User user : sharedProject.getRemoteUsers()) {
            transmitter.sendUserList(user.getJID(), invitationID, sharedProject
                .getParticipants());
        }

        log.debug("Inv" + Util.prefix(peer)
            + ": Waiting for user list confirmations...");
        transmitter.receiveUserListConfirmation(userListConfirmationCollector,
            sharedProject.getRemoteUsers(), monitor);
        log.debug("Inv" + Util.prefix(peer)
            + ": All user list confirmations have arrived.");
    }

    /**
     * This method does <strong>not</strong> execute the cancellation but only
     * sets the {@link #cancellationCause}. It should be called if the
     * cancellation was initiated by the <strong>local</strong> user. The
     * cancellation will be ignored if the invitation has already been canceled
     * before. <br>
     * In order to cancel the invitation process {@link #executeCancellation()}
     * should be called.
     * 
     * @param errorMsg
     *            the error that caused the cancellation. This should be some
     *            user-friendly text as it might be presented to the user.
     *            <code>null</code> if the cancellation was caused by the user's
     *            request and not by some error.
     * 
     * @param cancelOption
     *            If <code>NOTIFY_PEER</code> we send a cancellation message to
     *            our peer.
     */
    public void localCancel(String errorMsg, CancelOption cancelOption) {
        if (!cancelled.compareAndSet(false, true))
            return;
        log.debug("Inv" + Util.prefix(peer) + ": localCancel: " + errorMsg);
        if (monitor != null)
            monitor.setCanceled(true);
        cancellationCause = new LocalCancellationException(errorMsg,
            cancelOption);
    }

    /**
     * This method does <strong>not</strong> execute the cancellation but only
     * sets the {@link #cancellationCause}. It should be called if the
     * cancellation was initated by the <strong>remote</strong> user. The
     * cancellation will be ignored if the invitation has already been cancelled
     * before. <br>
     * In order to cancel the invitation process {@link #executeCancellation()}
     * should be called.
     * 
     * @param errorMsg
     *            the error that caused the cancellation. This should be some
     *            user-friendly text as it might be presented to the user.
     *            <code>null</code> if the cancellation was caused by the user's
     *            request and not by some error.
     */
    @Override
    public void remoteCancel(String errorMsg) {
        if (!cancelled.compareAndSet(false, true))
            return;
        log.debug("Inv" + Util.prefix(peer) + ": remoteCancel: " + errorMsg);
        if (monitor != null)
            monitor.setCanceled(true);
        cancellationCause = new RemoteCancellationException(errorMsg);
    }

    /**
     * Cancels the invitation process based on the exception stored in
     * {@link #cancellationCause}. This method is always called by this local
     * object, so even if an another object "cancels" the invitation (
     * {@link #localCancel(String, CancelOption)}, {@link #remoteCancel(String)}
     * ), the exceptions will be thrown up on the stack to the caller of
     * {@link #start(SubMonitor)}, and not to the object which has "cancelled"
     * the process. The cancel methods (
     * {@link #localCancel(String, CancelOption)}, {@link #remoteCancel(String)}
     * ) do not cancel the invitation alone, but they set the
     * {@link #cancellationCause} and cancel the {@link #monitor}. Now it is the
     * responsibility of the objects which use the {@link #monitor} to throw a
     * {@link SarosCancellationException} (or it's subclasses), which will be
     * caught by this object causing a call to this method. If this does not
     * happen, the next {@link #checkCancellation(CancelOption)} cancels the
     * invitation.
     */
    protected void executeCancellation() throws SarosCancellationException {

        log.debug("Inv" + Util.prefix(peer) + ": executeCancellation");
        if (!cancelled.get())
            throw new IllegalStateException(
                "executeCancellation should only be called after localCancel or remoteCancel!");

        String errorMsg;
        String cancelMessage;
        if (cancellationCause instanceof LocalCancellationException) {
            LocalCancellationException e = (LocalCancellationException) cancellationCause;
            errorMsg = e.getMessage();

            switch (e.getCancelOption()) {
            case NOTIFY_PEER:
                transmitter.sendCancelInvitationMessage(peer, errorMsg);
                break;
            case DO_NOT_NOTIFY_PEER:
                break;
            default:
                log.warn("Inv" + Util.prefix(peer)
                    + ": This case is not expected here.");
            }

            if (errorMsg != null) {
                cancelMessage = "Invitation was cancelled locally"
                    + " because of an error: " + errorMsg;
                log.error("Inv" + Util.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation failed.");
            } else {
                cancelMessage = "Invitation was cancelled by local user.";
                log.debug("Inv" + Util.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation has been cancelled.");
            }

        } else if (cancellationCause instanceof RemoteCancellationException) {
            RemoteCancellationException e = (RemoteCancellationException) cancellationCause;

            errorMsg = e.getMessage();
            if (errorMsg != null) {
                cancelMessage = "Invitation was cancelled by the remote user "
                    + " because of an error on his/her side: " + errorMsg;
                log.error("Inv" + Util.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation failed.");
            } else {
                cancelMessage = "Invitation was cancelled by the remote user.";
                log.debug("Inv" + Util.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation has been cancelled.");
            }
        } else {
            log.error("This type of exception is not expected here: ",
                cancellationCause);
            monitor.setTaskName("Invitation failed.");
        }
        sharedProject.returnColor(this.colorID);
        invitationProcesses.removeInvitationProcess(this);
        throw cancellationCause;
    }

    /**
     * Checks whether the invitation process or the monitor has been cancelled.
     * If the monitor has been cancelled but the invitation process has not yet,
     * it cancels the invitation process.
     * 
     * @throws SarosCancellationException
     *             if the invitation process or the monitor has already been
     *             cancelled.
     */
    protected void checkCancellation(CancelOption cancelOption)
        throws SarosCancellationException {
        if (cancelled.get()) {
            log.debug("Inv" + Util.prefix(peer) + ": Cancellation checkpoint");
            throw new SarosCancellationException();
        }

        if (monitor == null) {
            log.warn("Inv" + Util.prefix(peer) + ": The monitor is null.");
            return;
        }

        if (monitor.isCanceled()) {
            log.debug("Inv" + Util.prefix(peer) + ": Cancellation checkpoint");
            localCancel(null, cancelOption);
            throw new SarosCancellationException();
        }
    }

    @Override
    public String getProjectName() {
        return project.getName();
    }

}
