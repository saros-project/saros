package de.fu_berlin.inf.dpp.core.invitation;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationMissingFilesExtension;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingRequest;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.FileSystem;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.SubProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.DecompressArchiveTask;
import de.fu_berlin.inf.dpp.negotiation.FileList;
import de.fu_berlin.inf.dpp.negotiation.FileListDiff;
import de.fu_berlin.inf.dpp.negotiation.FileListFactory;
import de.fu_berlin.inf.dpp.negotiation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
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
import java.util.Collections;
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

    private static int MONITOR_WORK_SCALE = 1000;

    private final ISarosSession session;

    private final List<ProjectNegotiationData> projectInfos;

    @Inject
    private IChecksumCache checksumCache;
    @Inject
    private IWorkspace workspace;
    @Inject
    private FileReplacementInProgressObservable fileReplacementInProgressObservable;
    /**
     * Maps the projectID to the project in workspace
     */
    private final Map<String, IProject> localProjectMapping;
    private boolean running;

    private PacketCollector startActivityQueuingRequestCollector;

    // TODO pull up, when this class is in core
    @Inject
    private ISarosSessionManager sessionManager;

    public IncomingProjectNegotiation(ISarosSession session, JID peer,
        String negotiationID, List<ProjectNegotiationData> projectInfos,
        ISarosContext sarosContext) {
        super(negotiationID, session.getID(), peer, sarosContext);

        this.session = session;
        this.projectInfos = projectInfos;
        localProjectMapping = new HashMap<String, IProject>();
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

    /**
     * Starts the negotiation. The negotiation can be aborted by canceling the
     * given monitor. The execution of this method perform changes to the file
     * system! It is the responsibility of the caller to ensure that appropriate
     * actions are performed to avoid unintended data loss, i.e this method will
     * do a best effort to backup altered data but no guarantee can be made in
     * doing so!
     *
     * @param projectMapping
     *            mapping from remote project ids to the target local projects
     *
     * @throws IllegalArgumentException
     *             if either a project id is not valid or the referenced project
     *             for that id does not exist
     */
    public Status run(Map<String, IProject> projectMapping,
        IProgressMonitor monitor, boolean useVersionControl) {

        checkProjectMapping(projectMapping);

        synchronized (this) {
            running = true;
        }

        observeMonitor(monitor);

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

            List<FileList> missingFiles = calculateMissingFiles(projectMapping,
                useVersionControl, monitor);

            transmitter.send(ISarosSession.SESSION_CONNECTION_ID, peer,
                ProjectNegotiationMissingFilesExtension.PROVIDER
                    .create(new ProjectNegotiationMissingFilesExtension(
                        getSessionID(), getID(), missingFiles)));

            awaitActivityQueueingActivation(new SubProgressMonitor(monitor, 10));

            /*
             * the user who sends this ProjectNegotiation is now responsible for
             * the resources of the contained projects
             */
            for (Entry<String, IProject> entry : localProjectMapping
                .entrySet()) {
                final String projectID = entry.getKey();
                IProject project = entry.getValue();

                /*
                 * TODO Move enable (and disable) queuing responsibility to
                 * SarosSession, since the second call relies on the first one,
                 * and the first one is never done without the second. (See also
                 * finally block below.)
                 */
                session.addProjectMapping(projectID, project, peer);
                session.enableQueuing(project);
            }

            transmitter.send(ISarosSession.SESSION_CONNECTION_ID, peer,
                StartActivityQueuingResponse.PROVIDER.create(
                    new StartActivityQueuingResponse(getSessionID(), getID()))
            );

            checkCancellation(CancelOption.NOTIFY_PEER);

            boolean filesMissing = false;

            for (FileList list : missingFiles)
                filesMissing |= !list.getPaths().isEmpty();

            // Host/Inviter decided to transmit files with one big archive
            if (filesMissing)
                acceptArchive(archiveTransferListener, monitor);

            /*
             * We are finished with the exchanging process. Add all projects
             * resources to the session.
             */
            for (Entry<String, IProject> entry : localProjectMapping
                .entrySet()) {

                final String projectID = entry.getKey();
                final IProject project = entry.getValue();

                List<IResource> resources = null;

                if (isPartialRemoteProject(projectID)) {

                    final List<String> paths = getRemoteFileList(projectID)
                        .getPaths();

                    resources = new ArrayList<IResource>(paths.size());

                    for (final String path : paths)
                        resources.add(getResource(project, path));
                }

                session.addSharedResources(project, projectID, resources);
                sessionManager.projectAdded(projectID);
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            /*
             * TODO Move disable queuing responsibility to SarosSession (see
             * todo above in for loop).
             */
            session.disableQueuing();

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
     * @param projectMapping
     *            projectID => projectName (in local workspace)
     */
    private List<FileList> calculateMissingFiles(
        Map<String, IProject> projectMapping, boolean useVersionControl,
        IProgressMonitor monitor) throws SarosCancellationException,
        IOException {

        monitor.beginTask(null, 100);
        int numberOfLoops = projectMapping.size();
        List<FileList> missingFiles = new ArrayList<FileList>();

  /*
         * this for loop sets up all the projects needed for the session and
         * computes the missing files.
         */
        for (Entry<String, IProject> entry : projectMapping.entrySet()) {
            SubProgressMonitor lMonitor = new SubProgressMonitor(monitor,
                100 / numberOfLoops);

            checkCancellation(CancelOption.NOTIFY_PEER);

            final String projectID = entry.getKey();
            final IProject project = entry.getValue();

            ProjectNegotiationData projectInfo = null;

            for (ProjectNegotiationData info : projectInfos) {
                if (info.getProjectID().equals(projectID)) {
                    projectInfo = info;
                }
            }

            if (projectInfo == null)
            // this should never happen
            {
                throw new RuntimeException(
                    "cannot add project with id " + projectID
                        + ", this id is unknown"
                );
            }

            VCSProvider vcs = null;

            localProjectMapping.put(projectID, project);

            checkCancellation(CancelOption.NOTIFY_PEER);

            LOG.debug("compute required Files for project " + project.getName()
                + " with ID: " + projectID);

            FileList requiredFiles = computeRequiredFiles(project,
                projectInfo.getFileList(), projectID, vcs,
                new SubProgressMonitor(lMonitor, MONITOR_WORK_SCALE));

            requiredFiles.setProjectID(projectID);
            checkCancellation(CancelOption.NOTIFY_PEER);
            missingFiles.add(requiredFiles);

            lMonitor.done();
        }

        monitor.done();
        return missingFiles;
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
        for (Entry<String, IProject> entry : localProjectMapping.entrySet())
            session.removeProjectMapping(entry.getKey(), entry.getValue(),
                peer);

        // The session might have been stopped already, if not we will stop it.
        if (session.getProjectResourcesMapping().keySet().isEmpty() || session
            .getRemoteUsers().isEmpty())
            sessionManager.stopSarosSession();
    }

    @Override
    public synchronized boolean remoteCancel(String errorMsg) {
        if (!super.remoteCancel(errorMsg))
            return false;

        if (!running)
            terminateProcess(null);

        return true;
    }

    @Override
    public synchronized boolean localCancel(String errorMsg,
        CancelOption cancelOption) {
        if (!super.localCancel(errorMsg, cancelOption))
            return false;

        if (!running)
            terminateProcess(null);

        return true;
    }

    /**
     * Computes the list of files that we're going to request from the host.<br>
     * If a VCS is used, update files if needed, and remove them from the list
     * of requested files if that's possible.
     *
     * @param project
     * @param remoteFileList
     * @param monitor
     * @return The list of files that we need from the host.
     * @throws LocalCancellationException If the user requested a cancel.
     * @throws IOException
     */
    private FileList computeRequiredFiles(IProject project,
        FileList remoteFileList, String projectID, VCSProvider provider,
        IProgressMonitor monitor)
        throws LocalCancellationException, IOException {

        monitor.beginTask("Compute required Files...", 1 * MONITOR_WORK_SCALE);

        FileList localFileList = FileListFactory
            .createFileList(project, null, checksumCache, provider,
                new SubProgressMonitor(monitor, 1 * MONITOR_WORK_SCALE,
                    SubProgressMonitor.SUPPRESS_BEGINTASK)
            );

        FileListDiff filesToSynchronize = computeDiff(localFileList,
            remoteFileList, project, projectID);

        List<String> missingFiles = new ArrayList<String>();
        missingFiles.addAll(filesToSynchronize.getAddedPaths());
        missingFiles.addAll(filesToSynchronize.getAlteredPaths());

        /*
         * We send an empty file list to the host as a notification that we do
         * not need any files.
         */

        monitor.done();

        if (missingFiles.isEmpty()) {
            LOG.debug(this + " : there are no files to synchronize.");
            return FileListFactory.createEmptyFileList();
        }

        return FileListFactory.createFileList(missingFiles);
    }

    /**
     * Determines the missing resources.
     *
     * @param localFileList       The file list of the local project.
     * @param remoteFileList      The file list of the remote project.
     * @param currentLocalProject The project in workspace. Every file we need to add/replace is
     *                            added to the {@link FileListDiff}
     * @param projectID
     * @return A modified FileListDiff which doesn't contain any directories or
     * files to remove, but just added and altered files.
     */
     /*
     * FIXME it is not very obviously that a computeDiff method also
     * manipulates/DELETES files !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */
    private FileListDiff computeDiff(FileList localFileList,
        FileList remoteFileList, final IProject currentLocalProject,
        String projectID) throws IOException {
        LOG.debug(this + " : computing file list difference");

        FileListDiff diff = FileListDiff.diff(localFileList, remoteFileList);

        if (!isPartialRemoteProject(projectID)) {

            deleteResources(currentLocalProject,
                diff.getRemovedPathsSanitized());

            diff.clearRemovedPaths();
        }

        for (String path : diff.getAddedFolders()) {
            IFolder folder = currentLocalProject.getFolder(path);
            if (!folder.exists()) {
                FileSystem.createFolder(folder);
            }
        }

        diff.clearAddedFolders();

        return diff;
    }

    /**
     * Deletes the resources denoted by the given paths for the given project.
     * This method manipulates the order of the list!
     */
    private void deleteResources(final IProject project,
        final List<String> paths) throws IOException {

        Collections.sort(paths, Collections.reverseOrder());

        for (final String path : paths) {
            final IResource resource = getResource(project, path);

            if (resource.exists())
                resource.delete(IResource.KEEP_HISTORY);
        }
    }

    private void unpackArchive(final File archiveFile,
        final IProgressMonitor monitor) throws LocalCancellationException,
        IOException {

        final Map<String, de.fu_berlin.inf.dpp.filesystem.IProject> projectMapping = new HashMap<String, de.fu_berlin.inf.dpp.filesystem.IProject>();

        for (Entry<String, IProject> entry : localProjectMapping.entrySet())
            projectMapping.put(entry.getKey(), entry.getValue());


        final DecompressArchiveTask decompressTask = new DecompressArchiveTask(
            archiveFile, projectMapping, PATH_DELIMITER, monitor);

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

    private void checkProjectMapping(final Map<String, IProject> mapping) {
        for (final Entry<String, IProject> entry : mapping.entrySet()) {

            if (getRemoteFileList(entry.getKey()) == null)
                throw new IllegalArgumentException(
                    "invalid project id: " + entry.getKey());

            if (!entry.getValue().exists())
                throw new IllegalArgumentException(
                    "project does not exist: " + entry.getValue());
        }
    }

    private IResource getResource(IProject project, String path) {
        if (path.endsWith(FileList.DIR_SEPARATOR))
            return project.getFolder(path);
        else
            return project.getFile(path);
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
