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
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * @author rdjemili
 * @author sotitas
 */

public class IncomingInvitationProcess extends InvitationProcess {

    private static Logger log = Logger
        .getLogger(IncomingInvitationProcess.class);
    protected FileList remoteFileList;
    protected IProject localProject;
    protected int filesLeftToSynchronize;
    protected SubMonitor monitor;
    protected String projectName;
    protected SessionManager sessionManager;
    protected IIncomingInvitationUI inInvitationUI;
    protected VersionManager versionManager;
    protected DateTime sessionStart;
    protected ISharedProject sharedProject;
    protected String invitationID;

    /**
     * {@link VersionInfo#compatibility} applies to our client and
     * {@link VersionInfo#version} is the version of the host.
     */
    public VersionInfo versionInfo;
    protected AtomicBoolean cancelled = new AtomicBoolean(false);
    protected SarosCancellationException cancellationCause;

    public IncomingInvitationProcess(SessionManager sessionManager,
        ITransmitter transmitter, JID from, String projectName,
        String description, int colorID,
        InvitationProcessObservable invitationProcesses,
        VersionManager versionManager, VersionInfo remoteVersionInfo,
        DateTime sessionStart, SarosUI sarosUI, String invitationID) {

        super(transmitter, from, description, colorID, invitationProcesses);

        this.sessionManager = sessionManager;
        this.projectName = projectName;
        this.versionManager = versionManager;
        this.versionInfo = determineVersion(remoteVersionInfo);
        this.sessionStart = sessionStart;
        this.invitationID = invitationID;
    }

    protected VersionInfo determineVersion(VersionInfo remoteVersionInfo) {
        VersionInfo ultimateVI = remoteVersionInfo;

        // The host could not determine the compatibility, so we do it.
        if (ultimateVI.compatibility == null) {
            ultimateVI.compatibility = versionManager
                .determineCompatibility(ultimateVI.version.toString());
            return ultimateVI;
        }

        // Invert the compatibility information so it applies to our client.
        ultimateVI.compatibility = ultimateVI.compatibility.invert();

        return ultimateVI;
    }

    public void requestRemoteFileList(SubMonitor monitor)
        throws LocalCancellationException, RemoteCancellationException {

        try {
            checkCancellation();

            // The first monitor we use during the invitation.
            this.monitor = monitor;
            monitor.beginTask("Acquiring remote file list", 100);

            monitor.subTask("Initializing Jingle...");
            transmitter.awaitJingleManager(peer);
            monitor.worked(5);

            checkCancellation();

            monitor.subTask("Sending request...");

            SarosPacketCollector fileListCollector = transmitter
                .getInvitationCollector(invitationID,
                    FileTransferType.FILELIST_TRANSFER);

            transmitter.sendFileListRequest(peer, invitationID);

            remoteFileList = transmitter.receiveFileList(fileListCollector,
                monitor, true);

            monitor.worked(10);

            checkCancellation();

            log.debug("Inv" + Util.prefix(peer) + ": Received FileList.");

        } catch (Exception e) {
            processException(e);
        } finally {
            /*
             * If we do not clear the subTaskName, the next wizard page shows it
             * for a second.
             */
            monitor.subTask("");
            monitor.done();
            this.monitor = null;
        }
    }

    public void accept(IProject baseProject, String newProjectName,
        boolean skipSync, SubMonitor monitor) throws SarosCancellationException {

        // The second monitor we use during the invitation.
        this.monitor = monitor;

        if ((newProjectName == null) && (baseProject == null)) {
            throw new IllegalArgumentException(
                "At least newProjectName or baseProject must not be null.");
        }

        /*
         * Disable "auto building" while we receive files, because it confuses
         * the invitation process and might cause inconsistent states with
         * regards to file lists
         */
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc = ws.getDescription();
        boolean wasAutobuilding = desc.isAutoBuilding();

        try {
            if (wasAutobuilding) {
                desc.setAutoBuilding(false);
                ws.setDescription(desc);
            }
            checkCancellation();
            acceptUnsafe(baseProject, newProjectName, skipSync);
        } catch (Exception e) {
            processException(e);
        } finally {
            // Re-enable auto-building...
            if (wasAutobuilding) {
                desc.setAutoBuilding(true);
                try {
                    ws.setDescription(desc);
                } catch (CoreException e) {
                    localCancel(
                        "An error occurred while synchronising the project",
                        CancelOption.NOTIFY_PEER);
                }
            }
            monitor.done();
        }
    }

    protected void acceptUnsafe(final IProject baseProject,
        final String newProjectName, boolean skipSync)
        throws SarosCancellationException, IOException {
        // If a base project is given, save it
        if (baseProject != null) {
            if (!EditorAPI.saveProject(baseProject, true)) {
                // User cancelled saving the source project
                throw new LocalCancellationException(
                    "User cancelled saving the source project.",
                    CancelOption.NOTIFY_PEER);
            }
        }

        if (newProjectName != null) {
            try {
                this.localProject = Util.runSWTSync(new Callable<IProject>() {
                    public IProject call() throws CoreException,
                        InterruptedException {
                        try {
                            return createNewProject(newProjectName, baseProject);
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                throw new LocalCancellationException(e.getMessage(),
                    CancelOption.NOTIFY_PEER);
            }
        } else {
            this.localProject = baseProject;
        }

        // TODO joining the session will already send events, which will be
        // rejected by our peers, because they don't know us yet (JoinMessage is
        // send only later)
        sharedProject = sessionManager.joinSession(this.localProject, peer,
            colorID, sessionStart);

        monitor.beginTask("Synchronizing", 100);
        FileList filesToSynchronize;
        if (skipSync) {
            filesToSynchronize = new FileList();
        } else {
            monitor.subTask("Preparing project for synchronisation...");
            filesToSynchronize = handleDiff(this.localProject,
                this.remoteFileList, monitor.newChild(5,
                    SubMonitor.SUPPRESS_ALL_LABELS));
        }
        filesLeftToSynchronize = filesToSynchronize.getAddedPaths().size()
            + filesToSynchronize.getAlteredPaths().size();

        if (filesLeftToSynchronize < 1) {
            log.debug("Inv" + Util.prefix(peer)
                + ": There are no files to synchronize.");
            /**
             * We send an empty file list to the host as a notification that we
             * do not need any files.
             */
        }

        monitor.subTask("Sending file list...");

        SarosPacketCollector archiveCollector = transmitter
            .getInvitationCollector(invitationID,
                FileTransferType.ARCHIVE_TRANSFER);

        transmitter.sendFileList(peer, invitationID, filesToSynchronize,
            monitor.newChild(10, SubMonitor.SUPPRESS_ALL_LABELS));

        checkCancellation();

        monitor.subTask("Receiving archive...");

        InputStream archiveStream = transmitter.receiveArchive(
            archiveCollector, monitor.newChild(75,
                SubMonitor.SUPPRESS_ALL_LABELS), true);

        checkCancellation();

        monitor.subTask("Extracting archive...");

        writeArchive(archiveStream, localProject, monitor.newChild(10,
            SubMonitor.SUPPRESS_ALL_LABELS));

        log.debug("Inv" + Util.prefix(peer)
            + ": Archive received and written to disk...");

        done();
    }

    /**
     * Have a look at the description of {@link WorkspaceModifyOperation}!
     * 
     * @throws LocalCancellationException
     * 
     * @see WorkspaceModifyOperation
     */
    protected void writeArchive(final InputStream archiveStream,
        final IProject project, final SubMonitor subMonitor)
        throws LocalCancellationException {
        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    try {
                        FileUtil.writeArchive(archiveStream, project,
                            subMonitor);
                    } catch (LocalCancellationException e) {
                        throw new CoreException(new Status(IStatus.CANCEL,
                            "pluginId", null, e));
                    }
                }
            }, subMonitor);
        } catch (CoreException e) {
            try {
                throw e.getCause();
            } catch (LocalCancellationException lc) {
                throw lc;
            } catch (Throwable t) {
                throw new LocalCancellationException(
                    "An error occurred while writing the archive: "
                        + t.getMessage(), CancelOption.NOTIFY_PEER);
            }
        }
    }

    /**
     * Creates a new project.
     * 
     * @param newProjectName
     *            the project name of the new project.
     * @param baseProject
     *            if not <code>null</code> all files of the baseProject will be
     *            copied into the new project after having created it.
     * @return the new project.
     * @throws Exception
     * 
     * @swt Needs to be run from the SWT UI Thread
     */
    protected static IProject createNewProject(String newProjectName,
        final IProject baseProject) throws Exception {

        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        final IProject project = workspaceRoot.getProject(newProjectName);

        // TODO Why do some string magic here?
        final File projectDir = new File(workspaceRoot.getLocation().toString()
            + File.separator + newProjectName);

        if (projectDir.exists()) {
            throw new CoreException(new Status(IStatus.ERROR, Saros.SAROS,
                "Project " + newProjectName + " already exists!"));
        }

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(EditorAPI
            .getAWorkbenchWindow().getShell());

        dialog.run(true, true, new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                throws InvocationTargetException {
                try {
                    SubMonitor subMonitor = SubMonitor.convert(monitor,
                        "Copy local resources... ", 300);

                    subMonitor.subTask("Clearing History...");
                    project.clearHistory(subMonitor.newChild(100));

                    subMonitor.subTask("Refreshing Project");
                    project.refreshLocal(IResource.DEPTH_INFINITE, subMonitor
                        .newChild(100));

                    if (baseProject == null) {
                        subMonitor.subTask("Creating Project...");
                        project.create(subMonitor.newChild(50));

                        subMonitor.subTask("Opening Project...");
                        project.open(subMonitor.newChild(50));
                    } else {
                        subMonitor.subTask("Copying Project...");
                        baseProject.copy(project.getFullPath(), true,
                            subMonitor.newChild(100));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e.getCause());
                }
            }
        });
        return project;
    }

    /**
     * Prepares for receiving the missing resources.
     * 
     * @param localProject
     *            the project that is used for the base of the replication.
     * @param remoteFileList
     *            the file list of the remote project.
     * @return a FileList to request from the host. This list does not contain
     *         any directories or files to remove, but just added and altered
     *         files.
     * @throws LocalCancellationException
     */
    protected FileList handleDiff(IProject localProject,
        FileList remoteFileList, SubMonitor monitor)
        throws LocalCancellationException {

        monitor.beginTask("Preparing local project for incoming files", 100);
        try {
            monitor.subTask("Calculating Diff");
            FileList diff = new FileList(localProject).diff(remoteFileList);
            monitor.worked(20);

            monitor.subTask("Removing unneeded resources");
            diff = diff.removeUnneededResources(localProject, monitor.newChild(
                40, SubMonitor.SUPPRESS_ALL_LABELS));

            monitor.subTask("Adding Folders");
            diff = diff.addAllFolders(localProject, monitor.newChild(40,
                SubMonitor.SUPPRESS_ALL_LABELS));

            monitor.done();
            return diff;
        } catch (CoreException e) {
            throw new LocalCancellationException(
                "Could not create diff file list: " + e.getMessage(),
                CancelOption.NOTIFY_PEER);
        }
    }

    /**
     * Ends the incoming invitation process.
     */
    protected void done() {
        sharedProject.userInvitationCompleted(sharedProject.getLocalUser());
        log.debug("Inv" + Util.prefix(peer)
            + ": isInvitationComplete has been set to true.");

        /*
         * TODO: Wait until all of the activities in the queue (which arrived
         * during the invitation) are processed and notify the host only after
         * that.
         */

        transmitter.sendInvitationCompleteConfirmation(peer, invitationID);
        log.debug("Inv" + Util.prefix(peer)
            + ": Invitation complete confirmation sent.");

        invitationProcesses.removeInvitationProcess(this);

        sharedProject.start();

        monitor.done();
        log.debug("Inv" + Util.prefix(peer) + ": Invitation complete.");
    }

    /**
     * This method does <strong>not</strong> execute the cancellation but only
     * sets the {@link #cancellationCause}. It should be called if the
     * cancellation was initated by the <strong>local</strong> user. The
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
        if (monitor == null) {
            log.debug("Inv" + Util.prefix(peer)
                + ": Closing JoinSessionWizard manually.");
            try {
                executeCancellation();
            } catch (SarosCancellationException e) {
                /**
                 * This happens if the JoinSessionWizard is currently waiting
                 * for user input.
                 */
                if (inInvitationUI != null)
                    inInvitationUI.cancelWizard(peer, e.getMessage(),
                        CancelLocation.LOCAL);
                else
                    log.error("The inInvitationUI is null, could not"
                        + " close the JoinSessionWizard.");
            }
        }
    }

    @Override
    public void remoteCancel(String errorMsg) {
        if (!cancelled.compareAndSet(false, true))
            return;
        log.debug("Inv" + Util.prefix(peer) + ": remoteCancel: " + errorMsg);
        if (monitor != null)
            monitor.setCanceled(true);
        cancellationCause = new RemoteCancellationException(errorMsg);
        if (monitor == null) {
            log.debug("Inv" + Util.prefix(peer)
                + ": Closing JoinSessionWizard manually.");
            try {
                executeCancellation();
            } catch (SarosCancellationException e) {
                /**
                 * This happens if the JoinSessionWizard is currently waiting
                 * for user input.
                 */
                if (inInvitationUI != null)
                    inInvitationUI.cancelWizard(peer, e.getMessage(),
                        CancelLocation.REMOTE);
                else
                    log.error("The inInvitationUI is null, could not"
                        + " close the JoinSessionWizard.");
            }
        }
    }

    protected void executeCancellation() throws LocalCancellationException,
        RemoteCancellationException {

        log.debug("Inv" + Util.prefix(peer) + ": executeCancellation");
        if (!cancelled.get())
            throw new IllegalStateException(
                "executeCancellation should only be called after localCancel or remoteCancel!");

        String errorMsg;
        String cancelMessage;
        try {
            throw cancellationCause;
        } catch (LocalCancellationException e) {
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
                throw new LocalCancellationException(errorMsg, e
                    .getCancelOption());
            } else {
                cancelMessage = "Invitation was cancelled by local user.";
                log.debug("Inv" + Util.prefix(peer) + ": " + cancelMessage);
                throw new LocalCancellationException(null, e.getCancelOption());
            }

        } catch (RemoteCancellationException e) {
            errorMsg = e.getMessage();
            if (errorMsg != null) {
                cancelMessage = "Invitation was cancelled by the remote user "
                    + " because of an error on his/her side: " + errorMsg;
                log.error("Inv" + Util.prefix(peer) + ": " + cancelMessage);
                throw new RemoteCancellationException(errorMsg);
            } else {
                cancelMessage = "Invitation was cancelled by the remote user.";
                log.debug("Inv" + Util.prefix(peer) + ": " + cancelMessage);
                throw new RemoteCancellationException(null);
            }
        } catch (Exception e) {
            log.error("This type of exception is not expected here: " + e);
        } finally {
            sessionManager.stopSharedProject();
            /*
             * If the sharedProject is null, stopSharedProject() does not clear
             * the sessionID.
             */
            sessionManager.clearSessionID();
            invitationProcesses.removeInvitationProcess(this);
        }
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
    protected void checkCancellation() throws SarosCancellationException {
        if (cancelled.get()) {
            log.debug("Inv" + Util.prefix(peer) + ": Cancellation checkpoint");
            throw new SarosCancellationException();
        }

        if (monitor == null)
            return;

        if (monitor.isCanceled()) {
            log.debug("Inv" + Util.prefix(peer) + ": Cancellation checkpoint");
            localCancel(null, CancelOption.NOTIFY_PEER);
            throw new SarosCancellationException();
        }

        return;
    }

    protected void processException(Exception ex)
        throws LocalCancellationException, RemoteCancellationException {
        try {
            throw ex;
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
            String errorMsg = "Unknown error (IOException).";
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (Exception e) {
            log.warn("Inv" + Util.prefix(peer)
                + ": This type of Exception is not expected: " + e);
            String errorMsg = "Unknown error (Exception).";
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
            executeCancellation();
        }
    }

    public FileList getRemoteFileList() {
        return this.remoteFileList;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    public void setInvitationUI(IIncomingInvitationUI inInvitationUI) {
        this.inInvitationUI = inInvitationUI;
    }
}
