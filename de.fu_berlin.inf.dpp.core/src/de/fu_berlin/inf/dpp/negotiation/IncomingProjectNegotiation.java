package de.fu_berlin.inf.dpp.negotiation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationMissingFilesExtension;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingRequest;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.FileSystem;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.SubProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.util.CoreUtils;

// MAJOR TODO refactor this class !!!
public class IncomingProjectNegotiation extends ProjectNegotiation {

    private static final Logger LOG = Logger
        .getLogger(IncomingProjectNegotiation.class);

    private static int MONITOR_WORK_SCALE = 1000;

    private final Map<String, ProjectNegotiationData> projectNegotiationData;

    private final FileReplacementInProgressObservable fileReplacementInProgressObservable;

    private boolean running;

    private PacketCollector startActivityQueuingRequestCollector;

    public IncomingProjectNegotiation(
        final JID peer, //
        final String negotiationID, //
        final List<ProjectNegotiationData> projectNegotiationData, //

        final ISarosSessionManager sessionManager, //
        final ISarosSession session, //

        final FileReplacementInProgressObservable fileReplacementInProgressObservable, //
        final IWorkspace workspace, //
        final IChecksumCache checksumCache, //

        final XMPPConnectionService connectionService, //
        final ITransmitter transmitter, //
        final IReceiver receiver //
    )

    {
        super(negotiationID, peer, sessionManager, session, workspace,
            checksumCache, connectionService, transmitter, receiver);

        this.projectNegotiationData = new HashMap<String, ProjectNegotiationData>();

        for (final ProjectNegotiationData data : projectNegotiationData)
            this.projectNegotiationData.put(data.getProjectID(), data);

        this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
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
        final IProgressMonitor monitor) {

        checkProjectMapping(projectMapping);

        synchronized (this) {
            running = true;
        }

        observeMonitor(monitor);

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

            List<FileList> missingFiles = synchronizeProjectStructures(
                projectMapping,
                computeLocalVsRemoteDiff(projectMapping, monitor));

            monitor.subTask("");

            transmitter.send(ISarosSession.SESSION_CONNECTION_ID, getPeer(),
                ProjectNegotiationMissingFilesExtension.PROVIDER
                    .create(new ProjectNegotiationMissingFilesExtension(
                        getSessionID(), getID(), missingFiles)));

            awaitActivityQueueingActivation(monitor);
            monitor.subTask("");

            /*
             * the user who sends this ProjectNegotiation is now responsible for
             * the resources of the contained projects
             */
            for (Entry<String, IProject> entry : projectMapping.entrySet()) {

                final String projectID = entry.getKey();
                final IProject project = entry.getValue();
                /*
                 * TODO Move enable (and disable) queuing responsibility to
                 * SarosSession, since the second call relies on the first one,
                 * and the first one is never done without the second. (See also
                 * finally block below.)
                 */
                session.addProjectMapping(projectID, project);
                session.enableQueuing(project);
            }

            transmitter.send(ISarosSession.SESSION_CONNECTION_ID, getPeer(),
                StartActivityQueuingResponse.PROVIDER
                    .create(new StartActivityQueuingResponse(getSessionID(),
                        getID())));

            checkCancellation(CancelOption.NOTIFY_PEER);

            boolean filesMissing = false;

            for (FileList list : missingFiles)
                filesMissing |= list.getPaths().size() > 0;

            // the host do not send an archive if we do not need any files
            if (filesMissing)
                receiveAndUnpackArchive(projectMapping,
                    archiveTransferListener, monitor);

            /*
             * We are finished with the negotiation. Add all projects resources
             * to the session.
             */
            for (Entry<String, IProject> entry : projectMapping.entrySet()) {

                final String projectID = entry.getKey();
                final IProject project = entry.getValue();

                final boolean isPartialRemoteProject = getProjectNegotiationData(
                    projectID).isPartial();

                final FileList remoteFileList = getProjectNegotiationData(
                    projectID).getFileList();

                List<IResource> resources = null;

                if (isPartialRemoteProject) {

                    final List<String> paths = remoteFileList.getPaths();

                    resources = new ArrayList<IResource>(paths.size());

                    for (final String path : paths)
                        resources.add(getResource(project, path));
                }

                session.addSharedResources(project, projectID, resources);
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            /*
             * TODO Move disable queuing responsibility to SarosSession (see
             * todo above in for loop).
             */
            for (IProject project : projectMapping.values())
                session.disableQueuing(project);

            if (fileTransferManager != null)
                fileTransferManager
                    .removeFileTransferListener(archiveTransferListener);

            fileReplacementInProgressObservable.replacementDone();

            deleteCollectors();
            monitor.done();
        }

        return terminate(exception);
    }

    /**
     * Returns the {@link ProjectNegotiationData negotiation data} for all
     * projects which are part of this negotiation.
     *
     * @return negotiation data for all projects which are part of this
     *         negotiation.
     */
    public List<ProjectNegotiationData> getProjectNegotiationData() {
        return new ArrayList<ProjectNegotiationData>(
            projectNegotiationData.values());
    }

    /**
     * Returns the {@link ProjectNegotiationData negotiation data} for the given
     * project id.
     *
     * @return negotiation data for the given project id or <code>null</code> if
     *         no negotiation data exists for the given project id.
     */
    public ProjectNegotiationData getProjectNegotiationData(final String id) {
        return projectNegotiationData.get(id);
    }

    /**
     * Receives the archive with all missing files and unpacks it.
     */
    private void receiveAndUnpackArchive(
        final Map<String, IProject> localProjectMapping,
        final ArchiveTransferListener archiveTransferListener,
        final IProgressMonitor monitor) throws IOException,
        SarosCancellationException {

        // waiting for the big archive to come in

        monitor.beginTask(null, 100);

        File archiveFile = receiveArchive(archiveTransferListener, getID(),
            new SubProgressMonitor(monitor, 50));

        /*
         * FIXME at this point it makes no sense to report the cancellation to
         * the remote side, because his negotiation is already finished !
         */

        try {
            unpackArchive(localProjectMapping, archiveFile,
                new SubProgressMonitor(monitor, 50));
            monitor.done();
        } finally {
            if (archiveFile != null)
                archiveFile.delete();
        }
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
        // for (Entry<String, IProject> entry : localProjectMapping.entrySet())
        // {
        // session.removeProjectMapping(entry.getKey(), entry.getValue());
        // }

        // // The session might have been stopped already, if not we will stop
        // it.
        // if (session.getProjectResourcesMapping().keySet().isEmpty()
        // || session.getRemoteUsers().isEmpty())
        // sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);

        sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
    }

    @Override
    public synchronized boolean remoteCancel(String errorMsg) {
        if (!super.remoteCancel(errorMsg))
            return false;

        if (!running)
            terminate(null);

        return true;
    }

    @Override
    public synchronized boolean localCancel(String errorMsg,
        CancelOption cancelOption) {
        if (!super.localCancel(errorMsg, cancelOption))
            return false;

        if (!running)
            terminate(null);

        return true;
    }

    /**
     * Computes the differences (files and folders) between the local and the
     * remote side for the given project mapping.
     *
     * @param localProjectMapping
     *            the local project mapping to use
     * @param monitor
     * @return list of differences (one for each project) between the local and
     *         the remote side.
     * @throws SarosCancellationException
     * @throws IOException
     */
    private Map<String, FileListDiff> computeLocalVsRemoteDiff(
        final Map<String, IProject> localProjectMapping,
        final IProgressMonitor monitor) throws SarosCancellationException,
        IOException {

        LOG.debug(this + " : computing file and folder differences");

        monitor.beginTask("Computing project(s) difference(s)...",
            localProjectMapping.size() * MONITOR_WORK_SCALE);

        final Map<String, FileListDiff> result = new HashMap<String, FileListDiff>();

        for (final Entry<String, IProject> entry : localProjectMapping
            .entrySet()) {

            final String id = entry.getKey();
            final IProject project = entry.getValue();

            // TODO optimize for partial shared projects

            final FileList localProjectFileList = FileListFactory
                .createFileList(project, null, checksumCache,
                    new SubProgressMonitor(monitor, 1 * MONITOR_WORK_SCALE,
                        SubProgressMonitor.SUPPRESS_BEGINTASK));

            final ProjectNegotiationData data = getProjectNegotiationData(id);

            final FileListDiff diff = FileListDiff.diff(localProjectFileList,
                data.getFileList(), data.isPartial());

            checkCancellation(CancelOption.NOTIFY_PEER);

            if (data.isPartial()
                && (!diff.getRemovedFiles().isEmpty() || !diff
                    .getRemovedFolders().isEmpty()))
                throw new IllegalStateException(
                    "partial sharing cannot delete existing resources");

            result.put(id, diff);
        }

        monitor.done();

        return result;
    }

    /**
     * Synchronize the project structures, deleting files and folders that are
     * not present on the remote side and creating empty folders that do not
     * exists and the local side.
     *
     * @param localProjectMapping
     * @param diffs
     * @return list of file lists (each for every project) containing the
     *         missing files that are not present on the local side.
     * @throws IOException
     */
    private List<FileList> synchronizeProjectStructures(
        final Map<String, IProject> localProjectMapping,
        final Map<String, FileListDiff> diffs) throws IOException {

        LOG.debug(this
            + " : deleting files and folders, creating empty folders");

        final List<FileList> result = new ArrayList<FileList>();

        for (final Entry<String, IProject> entry : localProjectMapping
            .entrySet()) {

            final String id = entry.getKey();
            final IProject project = entry.getValue();

            final FileListDiff diff = diffs.get(id);

            final List<String> resourcesToDelete = new ArrayList<String>(diff
                .getRemovedFiles().size() + diff.getRemovedFolders().size());

            resourcesToDelete.addAll(diff.getRemovedFiles());
            resourcesToDelete.addAll(diff.getRemovedFolders());

            Collections.sort(resourcesToDelete, Collections.reverseOrder());

            for (final String path : resourcesToDelete) {
                final IResource resource = getResource(project, path);

                if (resource.exists()) {

                    if (LOG.isTraceEnabled())
                        LOG.trace("deleting resource: " + resource);

                    resource.delete(IResource.KEEP_HISTORY);
                }
            }

            for (final String path : diff.getAddedFolders()) {
                final IFolder folder = project.getFolder(path);

                if (!folder.exists()) {

                    if (LOG.isTraceEnabled())
                        LOG.trace("creating folder(s): " + folder);

                    FileSystem.createFolder(folder);
                }

            }

            final List<String> missingFiles = new ArrayList<String>();

            missingFiles.addAll(diff.getAddedFiles());
            missingFiles.addAll(diff.getAlteredFiles());

            LOG.debug(this + " : " + missingFiles.size()
                + " file(s) must be synchronized");

            /*
             * We send an empty file list to the host as a notification that we
             * do not need any files for the given project.
             */
            final FileList fileList = missingFiles.isEmpty() ? FileListFactory
                .createEmptyFileList() : FileListFactory
                .createFileList(missingFiles);

            fileList.setProjectID(id);

            result.add(fileList);

        }

        return result;
    }

    private void unpackArchive(final Map<String, IProject> localProjectMapping,
        final File archiveFile, final IProgressMonitor monitor)
        throws LocalCancellationException, IOException {

        final Map<String, IProject> projectMapping = new HashMap<String, IProject>();

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
            workspace.run(decompressTask,
                projectMapping.values().toArray(new IResource[0]));
        } catch (de.fu_berlin.inf.dpp.exceptions.OperationCanceledException e) {
            LocalCancellationException canceled = new LocalCancellationException(
                null, CancelOption.DO_NOT_NOTIFY_PEER);
            canceled.initCause(e);
            throw canceled;
        }

        LOG.debug(String.format("unpacked archive in %d s",
            (System.currentTimeMillis() - startTime) / 1000));

        // TODO: now add the checksums into the cache
    }

    /**
     * Waits for the activity queuing request from the remote side.
     *
     * @param monitor
     */
    private void awaitActivityQueueingActivation(IProgressMonitor monitor)
        throws SarosCancellationException {

        monitor.beginTask("Waiting for " + getPeer().getName()
            + " to continue the project negotiation...",
            IProgressMonitor.UNKNOWN);

        Packet packet = collectPacket(startActivityQueuingRequestCollector,
            PACKET_TIMEOUT);

        if (packet == null)
            throw new LocalCancellationException("received no response from "
                + getPeer()
                + " while waiting to continue the project negotiation",
                CancelOption.DO_NOT_NOTIFY_PEER);

        monitor.done();
    }

    private void createCollectors() {
        startActivityQueuingRequestCollector = receiver
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
            if (transferFailed)
                archiveFile.delete();
        }

        monitor.done();

        LOG.debug(this + " : stored archive in file "
            + archiveFile.getAbsolutePath() + ", size: "
            + CoreUtils.formatByte(archiveFile.length()));

        return archiveFile;
    }

    private void checkProjectMapping(final Map<String, IProject> mapping) {

        for (final Entry<String, IProject> entry : mapping.entrySet()) {

            final String id = entry.getKey();
            final IProject project = entry.getValue();

            final ProjectNegotiationData data = getProjectNegotiationData(id);

            if (data == null)
                throw new IllegalArgumentException("invalid project id: " + id);

            if (!project.exists())
                throw new IllegalArgumentException("project does not exist: "
                    + project);
        }
    }

    private static class ArchiveTransferListener implements
        FileTransferListener {
        private String description;
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
            return this.request != null;
        }

        public FileTransferRequest getRequest() {
            return this.request;
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
        return "IPN [remote side: " + getPeer() + "]";
    }
}
