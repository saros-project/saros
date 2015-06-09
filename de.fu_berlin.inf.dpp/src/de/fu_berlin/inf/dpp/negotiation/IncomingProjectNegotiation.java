package de.fu_berlin.inf.dpp.negotiation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationMissingFilesExtension;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingRequest;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.FileSystem;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.SubProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.remote.RemoteProgressManager;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.preferences.Preferences;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.util.CoreUtils;
import de.fu_berlin.inf.dpp.vcs.VCSProvider;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

// MAJOR TODO refactor this class !!!
public class IncomingProjectNegotiation extends ProjectNegotiation {

    private static final Logger LOG = Logger
        .getLogger(IncomingProjectNegotiation.class);

    private static int MONITOR_WORK_SCALE = 1000;

    private final List<ProjectNegotiationData> projectNegotiationData;

    @Inject
    private IWorkspace workspace;

    @Inject
    private Preferences preferences;

    @Inject
    private RemoteProgressManager rpm;

    @Inject
    private IChecksumCache checksumCache;

    @Inject
    private FileReplacementInProgressObservable fileReplacementInProgressObservable;

    /*
     * FIXME remove this field, it is used as global access variable throughout
     * multiple methods in this class which is error prone !
     */
    private Map<String, IProject> localProjectMapping;

    private final ISarosSession session;

    private boolean running;

    private PacketCollector startActivityQueuingRequestCollector;

    /**
     * Initializes an IncomingProjectNegotiation.
     *
     * @param negotiationID
     *            unique ID of the negotiation
     * @param peer
     *            JID of the peer to negotiate with
     * @param session
     *            current Saros session
     * @param projectInfos
     *            information about the projects shared by the peer
     * @param sarosContext
     *            Saros dependency injection context
     */
    public IncomingProjectNegotiation(String negotiationID, JID peer,
        ISarosSession session, List<ProjectNegotiationData> projectInfos,
        ISarosContext sarosContext) {
        super(negotiationID, peer, session.getID(), sarosContext);

        this.session = session;
        this.projectNegotiationData = projectInfos;
        this.localProjectMapping = new HashMap<String, IProject>();
    }

    @Override
    public Map<String, String> getProjectNames() {
        Map<String, String> result = new HashMap<String, String>();
        for (ProjectNegotiationData data : projectNegotiationData) {
            result.put(data.getProjectID(), data.getProjectName());
        }
        return result;
    }

    /**
     *
     * @param projectID
     * @return The {@link FileList fileList} which belongs to the project with
     *         the ID <code>projectID</code> from inviter <br />
     *         <code><b>null<b></code> if there isn't such a {@link FileList
     *         fileList}
     */
    public FileList getRemoteFileList(String projectID) {
        for (ProjectNegotiationData data : projectNegotiationData) {
            if (data.getProjectID().equals(projectID))
                return data.getFileList();
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
        final IProgressMonitor monitor, boolean useVersionControl) {

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

            if (useVersionControl) {
                switchToVCS(projectNegotiationData, projectMapping, monitor);

                monitor.subTask("");
            }

            List<FileList> missingFiles = calculateMissingFiles(projectMapping,
                useVersionControl, monitor);

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
            for (Entry<String, IProject> entry : localProjectMapping.entrySet()) {

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

            // Host/Inviter decided to transmit files with one big archive
            if (filesMissing)
                acceptArchive(archiveTransferListener, monitor);

            /*
             * We are finished with the negotiation. Add all projects resources
             * to the session.
             */
            for (Entry<String, IProject> entry : localProjectMapping.entrySet()) {

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

        return terminate(exception);
    }

    public boolean isPartialRemoteProject(String projectID) {
        for (ProjectNegotiationData data : projectNegotiationData) {
            if (data.getProjectID().equals(projectID))
                return data.isPartial();
        }
        return false;
    }

    /**
     * Accepts the archive with all missing files and decompress it.
     */
    private void acceptArchive(
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
            unpackArchive(archiveFile, new SubProgressMonitor(monitor, 50));
            monitor.done();
        } finally {
            if (archiveFile != null)
                archiveFile.delete();
        }
    }

    /**
     * calculates all the files the host/inviter has to send for synchronization
     *
     * @param projectMapping
     *            projectID => projectName (in local workspace)
     */
    // TODO should be renamed to something like synchronizeProject(s)...
    private List<FileList> calculateMissingFiles(
        Map<String, IProject> projectMapping, boolean useVersionControl,
        IProgressMonitor monitor) throws SarosCancellationException,
        IOException {

        monitor.beginTask(null, projectMapping.size() * MONITOR_WORK_SCALE);

        List<FileList> missingFiles = new ArrayList<FileList>();

        /*
         * this for loop sets up all the projects needed for the session and
         * computes the missing files.
         */
        for (Entry<String, IProject> entry : projectMapping.entrySet()) {

            checkCancellation(CancelOption.NOTIFY_PEER);

            final String projectID = entry.getKey();

            IProject project = entry.getValue();

            ProjectNegotiationData projectInfo = null;

            for (ProjectNegotiationData data : projectNegotiationData) {
                if (data.getProjectID().equals(projectID))
                    projectInfo = data;
            }

            if (projectInfo == null)
                // this should never happen
                throw new RuntimeException("cannot add project with id "
                    + projectID + ", this id is unknown");

            if (!project.exists())
                throw new IllegalStateException("project " + project
                    + " does not exists");

            localProjectMapping.put(projectID, project);

            checkCancellation(CancelOption.NOTIFY_PEER);

            LOG.debug("compute required files for project " + project
                + " with ID: " + projectID);

            FileList requiredFiles = computeRequiredFiles(project,
                projectInfo.getFileList(), projectID, new SubProgressMonitor(
                    monitor, 1 * MONITOR_WORK_SCALE));

            requiredFiles.setProjectID(projectID);
            checkCancellation(CancelOption.NOTIFY_PEER);
            missingFiles.add(requiredFiles);
        }

        monitor.done();

        return missingFiles;
    }

    /**
     * Adds VCS support to the given projects denoted by the negotiation data.
     * When this method returns all projects should be managed by a VCS provider
     * and are in the same VCS state as described in the file list that is
     * stored in the project negotiation data for each project.
     *
     * @param negotiationData
     *            negotiation data describing the project states
     * @param projectMapping
     *            mapping to final projects destinations
     * @param monitor
     * @throws SarosCancellationException
     */
    /*
     * TODO maybe make this method only takes a single project so that is can be
     * called from calculateMissingFiles(...) again after the issue with the VCS
     * provider is resolved when creating the file list DIFF
     */
    private void switchToVCS(
        final List<ProjectNegotiationData> negotiationData,
        Map<String, IProject> projectMapping, final IProgressMonitor monitor)
        throws SarosCancellationException {

        monitor.beginTask(null, negotiationData.size() * MONITOR_WORK_SCALE);

        if (!preferences.useVersionControl()) {
            LOG.warn("aborting VCS synchronization: local VCS support is disabled");
            monitor.done();
            return;
        }

        for (final ProjectNegotiationData data : negotiationData) {

            checkCancellation(CancelOption.NOTIFY_PEER);

            // VCS with partial sharing is currently not supported
            if (data.isPartial()) {
                monitor.worked(1 * MONITOR_WORK_SCALE);
                continue;
            }

            final String projectID = data.getProjectID();
            IProject project = projectMapping.get(projectID);

            if (project == null) {
                LOG.warn("unable to synchronize VCS state for project id "
                    + projectID + ", no local project found for this id");

                monitor.worked(1 * MONITOR_WORK_SCALE);
                continue;
            }

            if (!project.exists()) {
                throw new IllegalStateException("project " + project
                    + " does not exists");
            }

            final String vcsProviderID = data.getFileList().getVcsProviderID();
            final VCSProvider vcsProvider = vcsProviderFactory
                .getProvider(vcsProviderID);

            if (vcsProvider == null) {
                LOG.warn("unable to synchronize VCS state for project id "
                    + projectID + ", no VCS adapter found for provider id "
                    + vcsProviderID);

                monitor.worked(1 * MONITOR_WORK_SCALE);
                continue;
            }

            project = checkoutVCSProject(vcsProvider, project,
                data.getFileList(), new SubProgressMonitor(monitor,
                    1 * MONITOR_WORK_SCALE));

            if (monitor.isCanceled()) {
                checkCancellation(CancelOption.NOTIFY_PEER);
            }

            if (project == null) {
                throw new LocalCancellationException("VCS checkout failed",
                    CancelOption.NOTIFY_PEER);
            }

            synchronizeVCSState(project, vcsProvider, data.getFileList(),
                new SubProgressMonitor(monitor, 0));
        }

        monitor.done();
    }

    /**
     * Checks out a project using the provided VCS adapter. If the project does
     * not exists it will be created, otherwise it will be updated.
     *
     * @param vcs
     *            the VCS adapter to use for checkout
     * @param project
     *            the project to use for checkout
     * @param fileList
     * @return the checked out project or <code>null</code> if it could not
     *         checked out
     * @throws LocalCancellationException
     *             if the negotiation is canceled locally
     */
    private IProject checkoutVCSProject(final VCSProvider vcs,
        IProject project, final FileList fileList,
        final IProgressMonitor monitor) throws LocalCancellationException {

        int ticksToConsume = 0;

        if (monitor instanceof SubProgressMonitor)
            ticksToConsume = ((SubProgressMonitor) monitor).getTotalTicks();

        if (isPartialRemoteProject(fileList.getProjectID()))
            throw new IllegalStateException(
                "VCS operations on partial shared projects are not supported");

        // FIXME check if the project is managed by another provider first
        if (vcs.isManaged(project)) {

            final String repositoryRoot = fileList.getRepositoryRoot();
            final String directory = fileList.getProjectInfo().getURL()
                .substring(repositoryRoot.length());

            // FIXME this should at least throw a OperationCanceledException
            vcs.connect(project, repositoryRoot, directory,
                new SubProgressMonitor(monitor, ticksToConsume / 2));

            return project;
        }

        /*
         * Inform the host of the session that the current (local) user has
         * started the possibly time consuming SVN checkout via a
         * remoteProgressMonitor
         * 
         * The monitor that is created here is shown both locally and remote and
         * is handled like a regular progress monitor.
         */
        IProgressMonitor remoteMonitor = rpm.createRemoteProgress(
            Collections.singletonList(session.getHost()),
            new SubProgressMonitor(monitor, ticksToConsume / 2));

        remoteMonitor.setTaskName("Project checkout via subversion");

        project = vcs.checkoutProject(project.getName(), fileList,
            remoteMonitor);

        /*
         * HACK: After checking out a project, give Eclipse/the Team provider
         * time to realize that the project is now managed. The problem was that
         * when checking later to see if we have to switch/update individual
         * resources in initVcState, the project appeared as unmanaged. It might
         * work to wrap initVcState in a job, such that it is scheduled after
         * the project is marked as managed.
         */
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // do nothing
        }

        return project;
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
        for (Entry<String, IProject> entry : localProjectMapping.entrySet()) {
            session.removeProjectMapping(entry.getKey(), entry.getValue());
        }

        // The session might have been stopped already, if not we will stop it.
        if (session.getProjectResourcesMapping().keySet().isEmpty()
            || session.getRemoteUsers().isEmpty())
            sessionManager.stopSarosSession();
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
     * Computes the list of files that should be requested from the host because
     * they are either missing in the target project or are containing different
     * data.
     *
     * @param project
     * @param remoteFileList
     * @param monitor
     *
     * @return The list of files that we need from the host.
     * @throws LocalCancellationException
     *             If the user requested a cancel.
     * @throws IOException
     */
    private FileList computeRequiredFiles(IProject project,
        FileList remoteFileList, String projectID, IProgressMonitor monitor)
        throws LocalCancellationException, IOException {

        monitor.beginTask("Compute required Files...", 1 * MONITOR_WORK_SCALE);

        /*
         * FIXME VCS data is completely ignored during a diff ! It also makes no
         * sense any longer. Assume we synchronized VCS state properly but in
         * the meantime someone switched some file reversions. We will never get
         * them as we are queuing just before the host starts compressing the
         * files (and so never "seen" them). We might get the correct binary
         * file content but the meta data (reversions does NOT match) !
         */
        FileList localFileList = FileListFactory.createFileList(project, null,
            checksumCache, null, new SubProgressMonitor(monitor,
                1 * MONITOR_WORK_SCALE, SubProgressMonitor.SUPPRESS_BEGINTASK));

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

        LOG.debug(this + " : " + missingFiles.size()
            + " file(s) must be synchronized");

        return missingFiles.isEmpty() ? FileListFactory.createEmptyFileList()
            : FileListFactory.createFileList(missingFiles);
    }

    /**
     * Determines the missing resources.
     *
     * @param localFileList
     *            The file list of the local project.
     * @param remoteFileList
     *            The file list of the remote project.
     * @param project
     *            The project in workspace. Every file we need to add/replace is
     *            added to the {@link FileListDiff}
     * @param projectID
     * @return A modified FileListDiff which doesn't contain any directories or
     *         files to remove, but just added and altered files.
     */
    /*
     * FIXME it is not very obviously that a computeDiff method also
     * manipulates/DELETES files !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */
    private FileListDiff computeDiff(final FileList localFileList,
        final FileList remoteFileList, final IProject project,
        final String projectID) throws IOException {

        LOG.debug(this + " : computing file list difference");

        final FileListDiff diff = FileListDiff.diff(localFileList,
            remoteFileList);

        if (!isPartialRemoteProject(projectID)) {

            /*
             * FIXME run inside Workspace and
             */

            deleteResources(project, diff.getRemovedPathsSanitized());

            diff.clearRemovedPaths();
        }

        for (final String path : diff.getAddedFolders()) {
            final IFolder folder = project.getFolder(path);

            if (!folder.exists())
                FileSystem.createFolder(folder);

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
     * Recursively synchronizes the version control state (URL and revision) of
     * each resource in the project with the host by switching or updating when
     * necessary.
     * <p>
     * It's very hard to predict how many resources have to be changed. In the
     * worst case, every resource has to be changed as many times as the number
     * of segments in its path. Due to these complications, the monitor is only
     * used for cancellation and the label, but not for the progress bar.
     *
     * @param remoteFileList
     *
     * @throws SarosCancellationException
     */
    private void synchronizeVCSState(IResource resource, VCSProvider vcs,
        FileList remoteFileList, IProgressMonitor monitor)
        throws SarosCancellationException {

        /*
         * as this is called recursively the monitor can only used for
         * cancellation
         */

        if (monitor.isCanceled())
            return;

        if (!vcs.isManaged(resource)) {
            LOG.trace("resource is not managed: " + resource);
            return;

        }

        final VCSResourceInfo info = vcs.getCurrentResourceInfo(resource);
        final String path = resource.getProjectRelativePath()
            .toPortableString();

        if (resource.getType() == IResource.PROJECT) {
            LOG.debug("reverting project: " + resource);
            /*
             * We have to revert the project first because the invitee could
             * have deleted a managed resource. Also, we don't want an update or
             * switch to cause an unresolved conflict here. The revert might
             * leave some unmanaged files, but these will get cleaned up later;
             * we're only concerned with managed files here.
             */
            vcs.revert(resource);
        }

        // FIXME both calls may return null
        final String localURL = info.getURL();
        final String localRevision = info.getRevision();

        final String remoteURL = remoteFileList.getVCSUrl(path);
        final String remoteRevision = remoteFileList.getVCSRevision(path);

        if (remoteURL == null || remoteRevision == null) {
            // The resource might have been deleted.
            return;
        }

        if (!remoteURL.equals(localURL)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("switching " + resource.getName() + " from "
                    + localURL + " to " + remoteURL);
            }
            vcs.switch_(resource, remoteURL, remoteRevision,
                new SubProgressMonitor(monitor, 0));
        } else if (!remoteRevision.equals(localRevision)
            && remoteFileList.getPaths().contains(path)) {

            if (LOG.isTraceEnabled()) {
                LOG.trace("updating " + resource.getName() + " from "
                    + localRevision + " to " + remoteRevision);
            }

            vcs.update(resource, remoteRevision, new SubProgressMonitor(
                monitor, 0));
        } else if (LOG.isTraceEnabled()) {
            LOG.trace("local revision of " + resource.getName()
                + " matches remote revision: " + localRevision);
        }

        if (monitor.isCanceled())
            return;

        if (resource instanceof IContainer) {
            try {
                List<IResource> children = Arrays
                    .asList(((IContainer) resource).members());
                for (IResource child : children) {

                    final String childPath = child.getProjectRelativePath()
                        .toString();

                    if (remoteFileList.getVCSUrl(childPath) != null
                        && remoteFileList.getVCSRevision(childPath) != null) {
                        synchronizeVCSState(child, vcs, remoteFileList, monitor);
                    }

                    if (monitor.isCanceled())
                        break;
                }
            } catch (IOException e) {
                /*
                 * We shouldn't ever get here. CoreExceptions are thrown e.g. if
                 * the project is closed or the resource doesn't exist, both of
                 * which are impossible at this point.
                 */
                LOG.error("Unknown error while trying to initialize the "
                    + "children of " + resource.toString() + ".", e);
                localCancel(
                    "Could not initialize the project's version control state, "
                        + "please try again without VCS support.",
                    CancelOption.NOTIFY_PEER);
                executeCancellation();
            }
        }

        monitor.done();
    }

    public List<ProjectNegotiationData> getProjectInfos() {
        return projectNegotiationData;
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

            if (getRemoteFileList(entry.getKey()) == null)
                throw new IllegalArgumentException("invalid project id: "
                    + entry.getKey());

            if (!entry.getValue().exists())
                throw new IllegalArgumentException("project does not exist: "
                    + entry.getValue());
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
