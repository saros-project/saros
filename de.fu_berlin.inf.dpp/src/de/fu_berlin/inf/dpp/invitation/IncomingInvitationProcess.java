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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.FileListDiff;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.UncloseableInputStream;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

/**
 * @author rdjemili
 * @author sotitas
 */

public class IncomingInvitationProcess extends InvitationProcess {

    private static Logger log = Logger
        .getLogger(IncomingInvitationProcess.class);
    protected FileList remoteFileList;
    protected IProject localProject;
    protected SubMonitor monitor;
    /**
     * The project ID sent to us by the host with this invitation.
     */
    protected String projectName;
    protected SessionManager sessionManager;
    protected JoinSessionWizard inInvitationUI;
    protected VersionManager versionManager;
    protected DateTime sessionStart;
    protected ISarosSession sarosSession;
    protected String invitationID;
    protected Saros saros;

    protected boolean doStream = false;

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
        DateTime sessionStart, SarosUI sarosUI, String invitationID,
        Saros saros, boolean doStream) {

        super(transmitter, from, description, colorID, invitationProcesses);

        this.sessionManager = sessionManager;
        this.projectName = projectName;
        this.versionManager = versionManager;
        this.versionInfo = determineVersion(remoteVersionInfo);
        this.sessionStart = sessionStart;
        this.invitationID = invitationID;
        this.saros = saros;
        this.doStream = doStream;
    }

    protected VersionInfo determineVersion(VersionInfo remoteVersionInfo) {
        log.debug("Inv" + Util.prefix(peer) + ": Determining version...");
        // The host could not determine the compatibility, so we do it.
        if (remoteVersionInfo.compatibility == null) {
            remoteVersionInfo.compatibility = versionManager
                .determineCompatibility(remoteVersionInfo.version.toString());
            return remoteVersionInfo;
        }

        // Invert the compatibility information so it applies to our client.
        remoteVersionInfo.compatibility = remoteVersionInfo.compatibility
            .invert();

        return remoteVersionInfo;
    }

    public void requestRemoteFileList(SubMonitor monitor)
        throws SarosCancellationException {

        log.debug("Inv" + Util.prefix(peer)
            + ": Requesting remote file list...");
        try {
            checkCancellation();

            // The first monitor we use during the invitation.
            this.monitor = monitor;
            monitor.beginTask("Acquiring remote file list", 100);

            log.debug("Inv" + Util.prefix(peer) + ": Initializing Jingle...");
            monitor.subTask("Initializing Jingle...");
            transmitter.awaitJingleManager(peer);
            monitor.worked(5);

            checkCancellation();

            log.debug("Inv" + Util.prefix(peer)
                + ": Sending file list request...");
            monitor.subTask("Sending file list request...");

            SarosPacketCollector fileListCollector = transmitter
                .getInvitationCollector(invitationID,
                    FileTransferType.FILELIST_TRANSFER);

            transmitter.sendFileListRequest(peer, invitationID);

            remoteFileList = transmitter.receiveFileList(fileListCollector,
                monitor.newChild(95, SubMonitor.SUPPRESS_ALL_LABELS), true);

            log.debug("Inv" + Util.prefix(peer) + ": File list received.");
            checkCancellation();
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

        log.debug("Inv" + Util.prefix(peer) + ": Invitation accepted.");
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
            acceptUnsafe(baseProject, newProjectName, skipSync, monitor);
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

    private void acceptUnsafe(final IProject baseProject,
        final String newProjectName, boolean skipSync, SubMonitor monitor)
        throws SarosCancellationException, IOException {
        // If a base project is given, save it
        if (baseProject != null) {
            log.debug("Inv" + Util.prefix(peer) + ": Saving base project...");
            if (!EditorAPI.saveProject(baseProject, true)) {
                // User cancelled saving the source project
                throw new LocalCancellationException(
                    "User cancelled saving the source project.",
                    CancelOption.NOTIFY_PEER);
            }
        }

        VCSAdapter vcs = VCSAdapter.getAdapter(this.remoteFileList
            .getVcsProviderID());

        assignLocalProject(baseProject, newProjectName, vcs, monitor);

        if (vcs != null) {
            initVcState(localProject, vcs, monitor);
        }

        // TODO joining the session will already send events, which will be
        // rejected by our peers, because they don't know us yet (JoinMessage is
        // sent only later)
        sarosSession = sessionManager.joinSession(this.projectName,
            this.localProject, peer, colorID, sessionStart);
        log.debug("Inv" + Util.prefix(peer) + ": Joined the session.");

        FileList requiredFiles = computeRequiredFiles(skipSync, vcs, monitor);

        log.debug("Inv" + Util.prefix(peer) + ": Sending file list...");
        monitor.subTask("Sending file list...");

        SarosPacketCollector archiveCollector = transmitter
            .getInvitationCollector(invitationID,
                FileTransferType.ARCHIVE_TRANSFER);
        transmitter.sendFileList(peer, invitationID, requiredFiles,
            monitor.newChild(10, SubMonitor.SUPPRESS_ALL_LABELS));

        checkCancellation();

        if (doStream) {
            acceptStream();
        } else {
            monitor.subTask("Receiving archive...");

            InputStream archiveStream = transmitter.receiveArchive(
                archiveCollector,
                monitor.newChild(90, SubMonitor.SUPPRESS_ALL_LABELS), true);

            log.debug("Inv" + Util.prefix(peer) + ": Archive received.");
            checkCancellation();

            monitor.subTask("Extracting archive...");

            writeArchive(archiveStream, localProject,
                monitor.newChild(5, SubMonitor.SUPPRESS_ALL_LABELS));

            log.debug("Inv" + Util.prefix(peer)
                + ": Archive has been written to disk.");
        }

        completeInvitation();
    }

    /**
     * Recursively synchronizes the version control state (URL and revision) of
     * each resource in the project with the host by switching or updating when
     * necessary.<br>
     * <br>
     * It's very hard to predict how many resources have to be changed. In the
     * worst case, every resource has to be changed as many times as the number
     * of segments in its path. Due to these complications, the monitor is only
     * used for cancellation and the label, but not for the progress bar.
     * 
     * @throws SarosCancellationException
     */
    private void initVcState(IResource resource, VCSAdapter vcs,
        SubMonitor monitor) throws SarosCancellationException {
        if (monitor.isCanceled())
            return;

        if (!vcs.isManaged(resource))
            return;

        final VCSResourceInfo info = vcs.getResourceInfo(resource);
        final IPath path = resource.getProjectRelativePath();
        if (resource instanceof IProject) {
            /*
             * We have to revert the project first because the invitee could
             * have deleted a managed resource. Also, we don't want an update or
             * switch to cause an unresolved conflict here. The revert might
             * leave some unmanaged files, but these will get cleaned up later;
             * we're only concerned with managed files here.
             */
            vcs.revert(resource, monitor);
        }

        String url = remoteFileList.getVCSUrl(path);
        String revision = remoteFileList.getVCSRevision(path);
        if (url == null || revision == null) {
            // The resource might have been deleted.
            return;
        }
        if (!info.url.equals(url)) {
            vcs.switch_(resource, url, revision, monitor);
        } else if (!info.revision.equals(revision)) {
            vcs.update(resource, revision, monitor);
        }
        if (monitor.isCanceled())
            return;

        if (resource instanceof IContainer) {
            // Recurse.
            try {
                final IResource[] children = ((IContainer) resource).members();
                for (IResource child : children) {
                    initVcState(child, vcs, monitor);
                    if (monitor.isCanceled())
                        break;
                }
            } catch (CoreException e) {
                /*
                 * We shouldn't ever get here. CoreExceptions are thrown e.g. if
                 * the project is closed or the resource doesn't exist, both of
                 * which are impossible at this point.
                 */
                log.error("Unknown error while trying to initialize the "
                    + "children of " + resource.toString() + ".", e);
                localCancel(
                    "Could not initialize the project's version control state, "
                        + "please try again without VCS support.",
                    CancelOption.NOTIFY_PEER);
                executeCancellation();
            }
        }
    }

    private void acceptStream() throws SarosCancellationException {
        try {
            archiveStreamService.startLock.lock();
            archiveStreamService.sessionReceived.await();
        } catch (InterruptedException e) {
            log.debug("Method interrupted waiting for archive stream lock.");
        } finally {
            archiveStreamService.startLock.unlock();
        }

        StreamSession newSession = archiveStreamService.streamSession;
        int numOfFiles = archiveStreamService.getFileNum();
        IFile currentFile = null;

        int worked = 0;
        int lastWorked = 0;
        int filesReceived = 0;
        double increment = 0.0;

        InputStream in = newSession.getInputStream(0);
        ZipInputStream zin = new ZipInputStream(in);
        try {

            ZipEntry ze = null;
            monitor.beginTask("Receiving project files...", 100);

            if (numOfFiles >= 1)
                increment = (double) 100 / numOfFiles;
            else
                monitor.worked(100);

            while ((ze = zin.getNextEntry()) != null) {
                currentFile = this.localProject.getFile(ze.getName());
                monitor.setTaskName("Receiving " + ze.getName());

                if (currentFile.exists()) {
                    log.debug(currentFile
                        + " already exists on invitee. Replacing this file.");
                    currentFile.delete(true, null);
                }

                currentFile.create(new UncloseableInputStream(zin), true, null);

                worked = (int) Math.round(increment * filesReceived);

                if ((worked - lastWorked) > 0) {
                    monitor.worked((worked - lastWorked));
                    lastWorked = worked;
                }

                filesReceived++;

                checkCancellation();
            }

        } catch (SarosCancellationException e) {
            log.debug("Invitation process was cancelled.");
            localCancel("An invitee cancelled the invitation.",
                CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (CoreException e) {
            log.error("Exception while creating file. Message: ", e);
            localCancel(
                "A problem occurred while the project's files were being received: \""
                    + e.getMessage() + "\" The invitation was cancelled.",
                CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (EOFException e) {
            log.error("Error while receiving files: " + e.getMessage());
            localCancel(
                "A problem occured when receiving the project files. It is possible that the files were corrupted in transit.\n\nPlease attempt invitation again.",
                CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (Exception e) {
            log.error("Unknown exception: ", e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(zin);
            newSession.stopSession();
        }

    }

    /**
     * Computes the list of files that we're going to request from the host.<br>
     * If a VCS is used, update files if needed, and remove them from the list
     * of requested files if that's possible.
     * 
     * @param skipSync
     *            Skip the initial synchronization.
     * @param vcs
     *            The VCS adapter of the local project.
     * @param monitor
     *            The SubMonitor of the dialog.
     * @return The list of files that we need from the host.
     * @throws LocalCancellationException
     *             If the user requested a cancel.
     * @throws IOException
     */
    private FileList computeRequiredFiles(boolean skipSync, VCSAdapter vcs,
        SubMonitor monitor) throws LocalCancellationException, IOException {
        monitor.beginTask("Synchronizing", 100);
        monitor.subTask("Preparing project for synchronization...");

        if (skipSync) {
            return new FileList();
        }

        FileListDiff filesToSynchronize = null;
        FileList localFileList = null;
        try {
            localFileList = new FileList(this.localProject, vcs != null);
        } catch (CoreException e) {
            e.printStackTrace();
            return new FileList();
        }
        SubMonitor childMonitor = monitor.newChild(5,
            SubMonitor.SUPPRESS_ALL_LABELS);
        filesToSynchronize = computeDiff(localFileList, this.remoteFileList,
            childMonitor);

        List<IPath> missingFiles = filesToSynchronize.getAddedPaths();
        missingFiles.addAll(filesToSynchronize.getAlteredPaths());
        if (missingFiles.isEmpty()) {
            log.debug("Inv" + Util.prefix(peer)
                + ": There are no files to synchronize.");
            /**
             * We send an empty file list to the host as a notification that we
             * do not need any files.
             */
            return new FileList();
        }

        return new FileList(missingFiles);
    }

    /**
     * Assign a value to this.localProject.<br>
     * Use baseProject if it's not null. Otherwise check out from VCS if
     * possible. Otherwise create a new project.
     * 
     * @throws LocalCancellationException
     */
    private void assignLocalProject(final IProject baseProject,
        final String newProjectName, VCSAdapter vcs, SubMonitor monitor)
        throws LocalCancellationException {
        if (newProjectName == null) {
            this.localProject = baseProject;
            // TODO the project could be managed by a different Team provider
            if (vcs != null && !vcs.isManaged(localProject)) {
                String repositoryRoot = remoteFileList.getRepositoryRoot();
                final String url = remoteFileList.getProjectInfo().url;
                String directory = url.substring(repositoryRoot.length());
                vcs.connect(localProject, repositoryRoot, directory, monitor);
            }
            return;
        }

        if (vcs != null) {
            this.localProject = vcs.checkoutProject(newProjectName,
                this.remoteFileList, monitor);
            if (this.localProject != null)
                return;
        }

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

        log.debug("Inv" + Util.prefix(peer) + ": Writing archive to disk...");
        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    try {
                        FileUtil.writeArchive(archiveStream, project,
                            subMonitor);
                    } catch (LocalCancellationException e) {
                        throw new CoreException(new Status(IStatus.CANCEL,
                            Saros.SAROS, null, e));
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
    protected IProject createNewProject(String newProjectName,
        final IProject baseProject) throws Exception {

        log.debug("Inv" + Util.prefix(peer) + ": Creating new project...");
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        final IProject project = workspaceRoot.getProject(newProjectName);

        final File projectDir = new File(
            workspaceRoot.getLocation().toString(), newProjectName);

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
                    project.refreshLocal(IResource.DEPTH_INFINITE,
                        subMonitor.newChild(100));

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
     * Determines the missing resources.
     * 
     * @param localFileList
     *            The file list of the local project.
     * @param remoteFileList
     *            The file list of the remote project.
     * @param monitor
     *            The progress monitor of the dialog.
     * @return A modified FileListDiff which doesn't contain any directories or
     *         files to remove, but just added and altered files.
     * @throws LocalCancellationException
     *             If the process is canceled by the user.
     */
    protected FileListDiff computeDiff(FileList localFileList,
        FileList remoteFileList, SubMonitor monitor)
        throws LocalCancellationException {
        log.debug("Inv" + Util.prefix(peer) + ": Computing file list diff...");
        monitor.beginTask("Preparing local project for incoming files", 100);
        try {
            monitor.subTask("Calculating Diff");
            FileListDiff diff = FileListDiff
                .diff(localFileList, remoteFileList);
            monitor.worked(20);

            monitor.subTask("Removing unneeded resources");
            diff = diff.removeUnneededResources(localProject,
                monitor.newChild(40, SubMonitor.SUPPRESS_ALL_LABELS));

            monitor.subTask("Adding Folders");
            diff = diff.addAllFolders(localProject,
                monitor.newChild(40, SubMonitor.SUPPRESS_ALL_LABELS));

            monitor.done();
            return diff;
        } catch (CoreException e) {
            throw new LocalCancellationException(
                "Could not create diff file list: " + e.getMessage(),
                CancelOption.NOTIFY_PEER);
        }
    }

    /**
     * Ends the incoming invitation process. Sends a confirmation to the host
     * and starts the shared project.
     */
    protected void completeInvitation() {
        log.debug("Inv" + Util.prefix(peer) + ": Completing invitation...");
        sarosSession.userInvitationCompleted(sarosSession.getLocalUser());
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

        sarosSession.start();

        monitor.done();
        log.debug("Inv" + Util.prefix(peer)
            + ": Invitation has completed successfully.");
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
        log.debug("Inv"
            + Util.prefix(peer)
            + ": remoteCancel "
            + (errorMsg == null ? " by user" : " because of error: " + errorMsg));
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
            } else {
                cancelMessage = "Invitation was cancelled by local user.";
                log.debug("Inv" + Util.prefix(peer) + ": " + cancelMessage);
            }

        } else if (cancellationCause instanceof RemoteCancellationException) {
            RemoteCancellationException e = (RemoteCancellationException) cancellationCause;
            errorMsg = e.getMessage();
            if (errorMsg != null) {
                cancelMessage = "Invitation was cancelled by the remote user "
                    + " because of an error on his/her side: " + errorMsg;
                log.error("Inv" + Util.prefix(peer) + ": " + cancelMessage);
            } else {
                cancelMessage = "Invitation was cancelled by the remote user.";
                log.debug("Inv" + Util.prefix(peer) + ": " + cancelMessage);
            }
        } else {
            log.error("This type of exception is not expected here: ",
                cancellationCause);
        }

        sessionManager.stopSarosSession();
        /*
         * If the sarosSession is null, stopSharedProject() does not clear the
         * sessionID, so we have to do this manually.
         */
        sessionManager.clearSessionID();
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

    protected void processException(Exception e)
        throws SarosCancellationException {
        if (e instanceof LocalCancellationException) {
            localCancel(e.getMessage(), CancelOption.NOTIFY_PEER);
        } else if (e instanceof RemoteCancellationException) {
            remoteCancel(e.getMessage());
        } else if (e instanceof SarosCancellationException) {
            /**
             * If this exception is thrown because of a local cancellation, we
             * initiate a localCancel here.
             * 
             * If this exception is thrown because of a remote cancellation, the
             * call of localCancel will be ignored.
             */
            localCancel(e.getMessage(), CancelOption.NOTIFY_PEER);
        } else if (e instanceof IOException) {
            String errorMsg = "Unknown error: " + e;
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
        } else {
            log.warn("Inv" + Util.prefix(peer)
                + ": This type of Exception is not expected: ", e);
            String errorMsg = "Unknown error: " + e;
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
        }
        executeCancellation();
    }

    public FileList getRemoteFileList() {
        return this.remoteFileList;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    public void setInvitationUI(JoinSessionWizard inInvitationUI) {
        this.inInvitationUI = inInvitationUI;
    }
}
