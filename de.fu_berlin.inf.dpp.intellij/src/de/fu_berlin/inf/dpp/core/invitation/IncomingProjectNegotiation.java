package de.fu_berlin.inf.dpp.core.invitation;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationMissingFilesExtension;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingRequest;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.core.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.core.monitoring.remote.RemoteProgressManager;
import de.fu_berlin.inf.dpp.core.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.intellij.project.fs.PathImp;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.SubProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.FileList;
import de.fu_berlin.inf.dpp.negotiation.FileListDiff;
import de.fu_berlin.inf.dpp.negotiation.FileListFactory;
import de.fu_berlin.inf.dpp.negotiation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.util.CoreUtils;
import de.fu_berlin.inf.dpp.vcs.VCSProvider;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.picocontainer.annotations.Inject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * TODO: Refactor when merging with Saros/E IPN.
 */
public class IncomingProjectNegotiation extends ProjectNegotiation {

    private static final Logger LOG = Logger
        .getLogger(IncomingProjectNegotiation.class);
    private final ISarosSession sarosSession;
    private IProgressMonitor monitor;
    // TODO: uncomment when AddProjectToSessionWizard was added
    // private AddProjectToSessionWizard addIncomingProjectUI;
    private final List<ProjectNegotiationData> projectInfos;

    @Inject
    private PreferenceUtils preferenceUtils;
    @Inject
    private SarosSessionObservable sarosSessionObservable;
    @Inject
    private RemoteProgressManager rpm;
    @Inject
    private IChecksumCache checksumCache;
    @Inject
    private IWorkspace workspace;
    @Inject
    private FileReplacementInProgressObservable fileReplacementInProgressObservable;
    /**
     * Maps the projectID to the project in workspace
     */
    private final Map<String, IProject> localProjects;
    private boolean running;

    private PacketCollector startActivityQueuingRequestCollector;

    // TODO pull up, when this class is in core
    @Inject
    private ISarosSessionManager sessionManager;

    public IncomingProjectNegotiation(ISarosSession sarosSession, JID peer,
        String negotiationID, List<ProjectNegotiationData> projectInfos,
        ISarosContext sarosContext) {
        super(negotiationID, sarosSession.getID(), peer, sarosContext);

        this.sarosSession = sarosSession;
        this.projectInfos = projectInfos;
        localProjects = new HashMap<String, IProject>();
    }

    @Override
    public Map<String, String> getProjectNames() {
        Map<String, String> result = new HashMap<String, String>();
        for (ProjectNegotiationData info : projectInfos) {
            result.put(info.getProjectID(), info.getProjectName());
        }
        return result;
    }

    /**
     * @param projectID
     * @return The {@link FileList fileList} which belongs to the project with
     *         the ID <code>projectID</code> from inviter <br />
     *         <code><b>null<b></code> if there isn't such a {@link FileList
     *         fileList}
     */
    public FileList getRemoteFileList(String projectID) {
        for (ProjectNegotiationData info : projectInfos) {
            if (info.getProjectID().equals(projectID))
                return info.getFileList();
        }
        return null;
    }

    // TODO: uncomment when AddProjectToSessionWizard was added
    /*
     * public synchronized void setProjectInvitationUI(
     * AddProjectToSessionWizard addIncomingProjectUI) {
     * this.addIncomingProjectUI = addIncomingProjectUI; }
     */

    /**
     * @param projectNames
     *            In this parameter the names of the projects are stored. They
     *            key is the session wide <code><b>projectID</b></code> and the
     *            value is the name of the project in the workspace of the local
     *            user (given from the {@link EnterProjectNamePage})
     */
    public Status accept(Map<String, String> projectNames,
        IProgressMonitor monitor, boolean useVersionControl) {

        synchronized (this) {
            running = true;
        }

        this.monitor = monitor;
        monitor.beginTask("Initializing shared project", 100);

        observeMonitor(monitor);

        IWorkspace ws = workspace;

        // TODO: By default IDEA does not autobuild, but we should add a check
        // for that

        fileReplacementInProgressObservable.startReplacement();

        ArchiveTransferListener archiveTransferListener = new ArchiveTransferListener(
            ARCHIVE_TRANSFER_ID + getID());

        Exception exception = null;

        createCollectors();

        try {
            checkCancellation(CancelOption.NOTIFY_PEER);

            if (fileTransferManager == null)
                // FIXME: the logic will try to send this to the remote contact
                throw new IOException("not connected to a XMPP server");

            fileTransferManager
                .addFileTransferListener(archiveTransferListener);

            List<FileList> missingFiles = calculateMissingFiles(projectNames,
                useVersionControl, new SubProgressMonitor(monitor, 10));

            transmitter.send(ISarosSession.SESSION_CONNECTION_ID, peer,
                ProjectNegotiationMissingFilesExtension.PROVIDER
                    .create(new ProjectNegotiationMissingFilesExtension(
                        getSessionID(), getID(), missingFiles)));

            awaitActivityQueueingActivation(new SubProgressMonitor(monitor, 10));

            /*
             * the user who sends this ProjectNegotiation is now responsible for
             * the resources of the contained projects
             */
            for (Entry<String, IProject> entry : localProjects.entrySet()) {
                IProject project = entry.getValue();

                /*
                 * TODO Move enable (and disable) queuing responsibility to
                 * SarosSession, since the second call relies on the first one,
                 * and the first one is never done without the second. (See also
                 * finally block below.)
                 */
                sarosSession.addProjectMapping(entry.getKey(), project, peer);
                sarosSession.enableQueuing(project);
            }

            transmitter.send(ISarosSession.SESSION_CONNECTION_ID, peer,
                StartActivityQueuingResponse.PROVIDER
                    .create(new StartActivityQueuingResponse(getSessionID(),
                        getID())));

            checkCancellation(CancelOption.NOTIFY_PEER);

            boolean filesMissing = false;

            for (FileList list : missingFiles)
                filesMissing |= !list.getPaths().isEmpty();

            // Host/Inviter decided to transmit files with one big archive
            if (filesMissing)
                acceptArchive(archiveTransferListener, new SubProgressMonitor(
                    monitor, 80));

            // We are finished with the exchanging process. Add all projects
            // resources to the session.
            for (String projectID : localProjects.keySet()) {
                IProject iProject = localProjects.get(projectID);
                if (isPartialRemoteProject(projectID)) {
                    List<String> paths = getRemoteFileList(projectID)
                        .getPaths();
                    List<IResource> dependentResources = new ArrayList<IResource>();

                    for (String path : paths) {

                        dependentResources.add(iProject.getFile(path));
                    }

                    sarosSession.addSharedResources(iProject, projectID,
                        dependentResources);
                } else {
                    sarosSession.addSharedResources(iProject, projectID, null);
                }

                sessionManager.projectAdded(projectID);
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            /*
             * TODO Move disable queuing responsibility to SarosSession (see
             * todo above in for loop).
             */
            sarosSession.disableQueuing();

            if (fileTransferManager != null)
                fileTransferManager
                    .removeFileTransferListener(archiveTransferListener);

            fileReplacementInProgressObservable.replacementDone();

            deleteCollectors();
            monitor.done();
        }

        return terminateProcess(exception);
    }

    public boolean isPartialRemoteProject(String projectID) {
        for (ProjectNegotiationData info : projectInfos) {
            if (info.getProjectID().equals(projectID))
                return info.isPartial();
        }
        return false;
    }

    /**
     * Accepts the archive with all missing files and decompress it.
     */
    private void acceptArchive(ArchiveTransferListener archiveTransferListener,
        IProgressMonitor monitor) throws IOException,
        SarosCancellationException {

        // waiting for the big archive to come in

        monitor.beginTask("Receiving project files...", 100);

        File archiveFile = receiveArchive(archiveTransferListener, getID(),
            new SubProgressMonitor(monitor, 50));

        /*
         * FIXME at this point it makes no sense to report the cancellation to
         * the remote side, because his negotiation is already finished !
         */

        try {
            unpackArchive(archiveFile, new SubProgressMonitor(monitor, 50));
            monitor.done();
        } finally {
            if (archiveFile != null) {
                boolean result = archiveFile.delete();
                if (!result) {
                    LOG.warn("could not delete archive File "
                        + archiveFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * calculates all the files the host/inviter has to send for synchronization
     * 
     * @param projectNames
     *            projectID => projectName (in local workspace)
     */
    private List<FileList> calculateMissingFiles(
        Map<String, String> projectNames, boolean useVersionControl,
        IProgressMonitor subMonitor) throws SarosCancellationException,
        IOException {

        subMonitor.beginTask(null, 100);
        int numberOfLoops = projectNames.size();
        List<FileList> missingFiles = new ArrayList<FileList>();

        /*
         * this for loop sets up all the projects needed for the session and
         * computes the missing files.
         */
        for (Entry<String, String> entry : projectNames.entrySet()) {
            SubProgressMonitor lMonitor = new SubProgressMonitor(subMonitor,
                100 / numberOfLoops);

            checkCancellation(CancelOption.NOTIFY_PEER);

            final String projectID = entry.getKey();
            final String projectName = entry.getValue();

            ProjectNegotiationData projectInfo = null;

            for (ProjectNegotiationData info : projectInfos) {
                if (info.getProjectID().equals(projectID))
                    projectInfo = info;
            }

            if (projectInfo == null)
                // this should never happen
                throw new RuntimeException("cannot add project with id "
                    + projectID + ", this id is unknown");

            VCSProvider vcs = null;

            IProject project = workspace.getProject(projectName);

            if (!project.exists()) {
                project = createProject(project, null);
            }

            localProjects.put(projectID, project);

            checkCancellation(CancelOption.NOTIFY_PEER);

            LOG.debug("compute required Files for project " + projectName
                + " with ID: " + projectID);

            FileList requiredFiles = computeRequiredFiles(project,
                projectInfo.getFileList(), projectID, vcs,
                new SubProgressMonitor(lMonitor, 30));

            requiredFiles.setProjectID(projectID);
            checkCancellation(CancelOption.NOTIFY_PEER);
            missingFiles.add(requiredFiles);

            lMonitor.done();
        }

        return missingFiles;
    }

    /**
     * Creates a new project. If a base project is given those files are copied
     * into the new project.
     * 
     * @param project
     *            the project to create
     * @param base
     *            the project to copy resources from
     * @return the created project
     * @throws LocalCancellationException
     *             if the process is canceled locally
     * @throws IOException
     *             if the project already exists or could not created
     */
    private IProject createProject(final IProject project, final IProject base)
        throws LocalCancellationException, IOException {

        final CreateProjectTask createProjectTask = new CreateProjectTask(
            project.getName(), base, monitor, workspace);

        try {
            workspace.run(createProjectTask);
        } catch (OperationCanceledException e) {
            throw new LocalCancellationException();
        } catch (IOException e) {
            throw new IOException(e.getMessage(), e.getCause());
        }

        return createProjectTask.getProject();
    }

    @Override
    protected void executeCancellation() {

        /*
         * Remove the entries from the mapping in the SarosSession.
         * 
         * Stefan Rossbach 28.12.2012: This will not gain you anything because
         * the project is marked as shared on the remote side and so will never
         * be able to be shared again to us. Again the whole architecture does
         * currently NOT support cancellation of the project negotiation
         * properly !
         */
        for (Entry<String, IProject> entry : localProjects.entrySet())
            sarosSession.removeProjectMapping(entry.getKey(), entry.getValue(),
                peer);

        // The session might have been stopped already, if not we will stop it.
        if (sarosSession.getProjectResourcesMapping().keySet().isEmpty()
            || sarosSession.getRemoteUsers().isEmpty())
            sessionManager.stopSarosSession();
    }

    @Override
    public synchronized boolean remoteCancel(String errorMsg) {
        if (!super.remoteCancel(errorMsg))
            return false;
        /*
         * TODO: uncomment when AddProjectToSessionWizard was added if
         * (addIncomingProjectUI != null) addIncomingProjectUI
         * .cancelWizard(peer, errorMsg, CancelLocation.REMOTE);
         */
        if (!running)
            terminateProcess(null);

        return true;
    }

    @Override
    public synchronized boolean localCancel(String errorMsg,
        CancelOption cancelOption) {
        if (!super.localCancel(errorMsg, cancelOption))
            return false;
        /*
         * TODO: uncomment when AddProjectToSessionWizard was added if
         * (addIncomingProjectUI != null) addIncomingProjectUI
         * .cancelWizard(peer, errorMsg, CancelLocation.LOCAL);
         */
        if (!running)
            terminateProcess(null);

        return true;
    }

    /**
     * Computes the list of files that we're going to request from the host.<br>
     * If a VCS is used, update files if needed, and remove them from the list
     * of requested files if that's possible.
     * 
     * @param currentLocalProject
     * @param remoteFileList
     * @param vcs
     *            The VCS adapter of the local project.
     * @param monitor
     * @return The list of files that we need from the host.
     * @throws LocalCancellationException
     *             If the user requested a cancel.
     * @throws IOException
     */
    private FileList computeRequiredFiles(IProject currentLocalProject,
        FileList remoteFileList, String projectID, VCSProvider provider,
        IProgressMonitor monitor) throws LocalCancellationException,
        IOException {

        // Compute required Files
        IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);

        FileList localFileList = FileListFactory.createFileList(
            currentLocalProject, null, checksumCache, provider,
            new SubProgressMonitor(monitor, 1));

        FileListDiff filesToSynchronize = computeDiff(localFileList,
            remoteFileList, currentLocalProject, projectID);

        List<String> missingFiles = new ArrayList<String>();
        missingFiles.addAll(filesToSynchronize.getAddedPaths());
        missingFiles.addAll(filesToSynchronize.getAlteredPaths());

        /*
         * We send an empty file list to the host as a notification that we do
         * not need any files.
         */

        if (missingFiles.isEmpty()) {
            LOG.debug(this + " : there are no files to synchronize.");
            subMonitor.done();
            return FileListFactory.createEmptyFileList();
        }

        subMonitor.done();
        return FileListFactory.createFileList(missingFiles);
    }

    /**
     * Determines the missing resources.
     * 
     * @param localFileList
     *            The file list of the local project.
     * @param remoteFileList
     *            The file list of the remote project.
     * @param currentLocalProject
     *            The project in workspace. Every file we need to add/replace is
     *            added to the {@link FileListDiff}
     * @param projectID
     * @return A modified FileListDiff which doesn't contain any directories or
     *         files to remove, but just added and altered files.
     */
    private FileListDiff computeDiff(FileList localFileList,
        FileList remoteFileList, final IProject currentLocalProject,
        String projectID) throws IOException {
        LOG.debug(this + " : computing file list difference");

        FileListDiff diff = FileListDiff.diff(localFileList, remoteFileList);

        try {
            if (!isPartialRemoteProject(projectID)) {
                final List<String> toDelete = diff.getRemovedPathsSanitized();

                /*
                 * WTF !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! THIS IS DELETING
                 * FILES !!!!!!!
                 */

                workspace.run(new IWorkspaceRunnable() {
                    @Override
                    public void run(IProgressMonitor progress)
                        throws IOException {
                        for (String path : toDelete) {
                            IResource resource = path
                                .endsWith(PathImp.FILE_SEPARATOR) ? currentLocalProject
                                .getFolder(path) : currentLocalProject
                                .getFile(path);

                            /*
                             * Check if resource exists because it might have
                             * already been deleted when deleting its folder
                             */
                            if (resource.exists()) {
                                resource.delete(IResource.FORCE
                                    | IResource.KEEP_HISTORY);
                            }
                        }
                    }
                });

                diff.clearRemovedPaths();
            }

            for (String path : diff.getAddedFolders()) {
                IFolder folder = currentLocalProject.getFolder(path);
                if (!folder.exists()) {
                    FileUtils.create(folder);
                }
            }

            diff.clearAddedFolders();

            return diff;
        } catch (IOException e) {
            throw new IOException(e.getMessage(), e.getCause());
        }
    }

    private void unpackArchive(final File archiveFile,
        final IProgressMonitor monitor) throws LocalCancellationException,
        IOException {

        final DecompressArchiveTask decompressTask = new DecompressArchiveTask(
            archiveFile, localProjects, PATH_DELIMITER, monitor);

        long startTime = System.currentTimeMillis();

        LOG.debug(this + " : unpacking archive file...");

        /*
         * TODO: calculate the ADLER32 checksums during decompression and add
         * them into the ChecksumCache. The insertion must be done after the
         * WorkspaceRunnable has run or all checksums will be invalidated during
         * the IResourceChangeListener updates inside the WorkspaceRunnable or
         * after it finished!
         */

        try {
            workspace.run(decompressTask);
        } catch (OperationCanceledException e) {
            throw new LocalCancellationException(null,
                CancelOption.DO_NOT_NOTIFY_PEER);
        }

        LOG.debug(String.format("unpacked archive in %d s",
            (System.currentTimeMillis() - startTime) / 1000));

        // TODO: now add the checksums into the cache
    }

    public List<ProjectNegotiationData> getProjectInfos() {
        return projectInfos;
    }

    /**
     * Waits for the activity queuing request from the remote side.
     * 
     * @param monitor
     */
    private void awaitActivityQueueingActivation(IProgressMonitor monitor)
        throws SarosCancellationException {

        monitor.beginTask("Waiting for " + peer.getName()
            + " to continue the project negotiation...",
            IProgressMonitor.UNKNOWN);

        Packet packet = collectPacket(startActivityQueuingRequestCollector,
            PACKET_TIMEOUT);

        if (packet == null)
            throw new LocalCancellationException("received no response from "
                + peer + " while waiting to continue the project negotiation",
                CancelOption.DO_NOT_NOTIFY_PEER);

        monitor.done();
    }

    private void createCollectors() {
        startActivityQueuingRequestCollector = xmppReceiver
            .createCollector(StartActivityQueuingRequest.PROVIDER
                .getPacketFilter(getSessionID(), getID()));
    }

    private void deleteCollectors() {
        startActivityQueuingRequestCollector.cancel();
    }

    private File receiveArchive(
        ArchiveTransferListener archiveTransferListener, String transferID,
        IProgressMonitor monitor) throws IOException,
        SarosCancellationException {

        monitor.beginTask("Receiving archive file...", 100);
        LOG.debug("waiting for incoming archive stream request");

        monitor
            .subTask("Host is compressing project files. Waiting for the archive file...");

        try {
            while (!archiveTransferListener.hasReceived()) {
                checkCancellation(CancelOption.NOTIFY_PEER);
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            monitor.setCanceled(true);
            monitor.done();
            Thread.currentThread().interrupt();
            throw new LocalCancellationException();
        }

        monitor.subTask("Receiving archive file...");

        LOG.debug(this + " : receiving archive");

        IncomingFileTransfer transfer = archiveTransferListener.getRequest()
            .accept();

        File archiveFile = File.createTempFile(
            "saros_archive_" + System.currentTimeMillis(), null);

        boolean transferFailed = true;

        try {
            transfer.recieveFile(archiveFile);

            monitorFileTransfer(transfer, monitor);
            transferFailed = false;
        } catch (XMPPException e) {
            throw new IOException(e.getMessage(), e.getCause());
        } finally {
            if (transferFailed) {
                boolean result = archiveFile.delete();
                if (!result) {
                    LOG.warn("could not delete archive File "
                        + archiveFile.getAbsolutePath());
                }
            }
            monitor.done();
        }

        LOG.debug(this + " : stored archive in file "
            + archiveFile.getAbsolutePath() + ", size: "
            + CoreUtils.formatByte(archiveFile.length()));

        return archiveFile;
    }

    @Override
    public String toString() {
        return "IPN [remote side: " + peer + "]";
    }

    private static class ArchiveTransferListener implements
        FileTransferListener {
        private final String description;
        private volatile FileTransferRequest request;

        public ArchiveTransferListener(String description) {
            this.description = description;
        }

        @Override
        public void fileTransferRequest(FileTransferRequest request) {
            if (request.getDescription().equals(description)) {
                this.request = request;
            }
        }

        public boolean hasReceived() {
            return request != null;
        }

        public FileTransferRequest getRequest() {
            return request;
        }
    }
}
