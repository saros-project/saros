package de.fu_berlin.inf.dpp.invitation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelLocation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.extensions.ProjectNegotiationMissingFilesExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.StartActivityQueuingRequest;
import de.fu_berlin.inf.dpp.net.internal.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.IChecksumCache;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.ui.RemoteProgressManager;
import de.fu_berlin.inf.dpp.ui.wizards.AddProjectToSessionWizard;
import de.fu_berlin.inf.dpp.ui.wizards.pages.EnterProjectNamePage;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

// MAJOR TODO refactor this class !!!
public class IncomingProjectNegotiation extends ProjectNegotiation {

    private static Logger log = Logger
        .getLogger(IncomingProjectNegotiation.class);

    private SubMonitor monitor;
    private AddProjectToSessionWizard addIncomingProjectUI;

    private List<ProjectNegotiationData> projectInfos;

    @Inject
    private PreferenceUtils preferenceUtils;

    @Inject
    private SarosSessionObservable sarosSessionObservable;

    @Inject
    private RemoteProgressManager rpm;

    @Inject
    private IChecksumCache checksumCache;

    @Inject
    private FileReplacementInProgressObservable fileReplacementInProgressObservable;
    /**
     * maps the projectID to the project in workspace
     */
    private Map<String, IProject> localProjects;

    private JID jid;

    private final ISarosSession sarosSession;

    private boolean running;

    private SarosPacketCollector startActivityQueuingRequestCollector;

    public IncomingProjectNegotiation(ISarosSession sarosSession, JID peer,
        String processID, List<ProjectNegotiationData> projectInfos,
        ISarosContext sarosContext) {
        super(peer, sarosSession.getID(), sarosContext);

        this.sarosSession = sarosSession;
        this.processID = processID;
        this.projectInfos = projectInfos;
        this.localProjects = new HashMap<String, IProject>();
        this.jid = peer;
    }

    @Override
    public Map<String, String> getProjectNames() {
        Map<String, String> result = new HashMap<String, String>();
        for (ProjectNegotiationData info : this.projectInfos) {
            result.put(info.getProjectID(), info.getProjectName());
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
        for (ProjectNegotiationData info : this.projectInfos) {
            if (info.getProjectID().equals(projectID))
                return info.getFileList();
        }
        return null;
    }

    public synchronized void setProjectInvitationUI(
        AddProjectToSessionWizard addIncomingProjectUI) {
        this.addIncomingProjectUI = addIncomingProjectUI;
    }

    /**
     * 
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

        this.monitor = SubMonitor.convert(monitor,
            "Initializing shared project", 100);

        observeMonitor(monitor);

        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc = ws.getDescription();
        boolean wasAutobuilding = desc.isAutoBuilding();

        fileReplacementInProgressObservable.startReplacement();

        ArchiveTransferListener archiveTransferListener = new ArchiveTransferListener(
            ARCHIVE_TRANSFER_ID + processID);

        Exception exception = null;

        createCollectors();

        try {
            checkCancellation(CancelOption.NOTIFY_PEER);

            if (wasAutobuilding) {
                desc.setAutoBuilding(false);
                ws.setDescription(desc);
            }

            if (fileTransferManager == null)
                // FIXME: the logic will try to send this to the remote contact
                throw new IOException("not connected to a XMPP server");

            fileTransferManager
                .addFileTransferListener(archiveTransferListener);

            List<FileList> missingFiles = calculateMissingFiles(projectNames,
                useVersionControl, this.monitor.newChild(10));

            transmitter.sendToSessionUser(ISarosSession.SESSION_CONNECTION_ID,
                peer, ProjectNegotiationMissingFilesExtension.PROVIDER
                    .create(new ProjectNegotiationMissingFilesExtension(
                        sessionID, processID, missingFiles)));

            awaitActivityQueueingActivation(this.monitor.newChild(0));

            // the user who sends this ProjectNegotiation is now responsible for
            // all resources from that project
            for (Entry<String, IProject> entry : localProjects.entrySet()) {
                sarosSession.addProjectOwnership(entry.getKey(),
                    ResourceAdapterFactory.create(entry.getValue()), jid);
                sarosSession.enableQueuing(entry.getKey());
            }

            transmitter.sendToSessionUser(ISarosSession.SESSION_CONNECTION_ID,
                getPeer(), StartActivityQueuingResponse.PROVIDER
                    .create(new StartActivityQueuingResponse(sessionID,
                        processID)));

            checkCancellation(CancelOption.NOTIFY_PEER);

            boolean filesMissing = false;

            for (FileList list : missingFiles)
                filesMissing |= list.getPaths().size() > 0;

            // Host/Inviter decided to transmit files with one big archive
            if (filesMissing)
                acceptArchive(archiveTransferListener, localProjects.size(),
                    this.monitor.newChild(80));

            // We are finished with the exchanging process. Add all projects
            // resources to the session.
            for (String projectID : localProjects.keySet()) {
                IProject iProject = localProjects.get(projectID);
                if (isPartialRemoteProject(projectID)) {
                    List<IPath> paths = getRemoteFileList(projectID).getPaths();
                    List<IResource> dependentResources = new ArrayList<IResource>();
                    for (IPath iPath : paths) {
                        dependentResources.add(iProject.findMember(iPath));
                    }

                    sarosSession.addSharedResources(
                        ResourceAdapterFactory.create(iProject), projectID,
                        ResourceAdapterFactory.convertTo(dependentResources));
                } else {
                    sarosSession.addSharedResources(
                        ResourceAdapterFactory.create(iProject), projectID,
                        null);
                }

                sessionManager.projectAdded(projectID);
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            sarosSession.disableQueuing();

            if (fileTransferManager != null)
                fileTransferManager
                    .removeFileTransferListener(archiveTransferListener);

            fileReplacementInProgressObservable.replacementDone();

            deleteCollectors();
            monitor.done();

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
        }

        return terminateProcess(exception);
    }

    public boolean isPartialRemoteProject(String projectID) {
        for (ProjectNegotiationData info : this.projectInfos) {
            if (info.getProjectID().equals(projectID))
                return info.isPartial();
        }
        return false;
    }

    /**
     * The archive with all missing files sorted by project will be received and
     * unpacked project by project.
     * 
     * @param projectCount
     *            how many projects will be in the big archive
     */
    private void acceptArchive(ArchiveTransferListener archiveTransferListener,
        int projectCount, SubMonitor monitor) throws IOException,
        SarosCancellationException {

        // waiting for the big archive to come in

        monitor.beginTask(null, 100);

        File archiveFile = receiveArchive(archiveTransferListener, processID,
            monitor.newChild(50, SubMonitor.SUPPRESS_NONE));

        /*
         * FIXME at this point it makes no sense to report the cancellation to
         * the remote side, because his negotiation is already finished !
         */

        ZipInputStream zipInputStream = null;
        ZipEntry zipEntry;

        SubMonitor zipStreamLoopMonitor = monitor.newChild(50,
            SubMonitor.SUPPRESS_NONE);

        zipStreamLoopMonitor.beginTask(null, 100);

        try {

            zipInputStream = new ZipInputStream(new BufferedInputStream(
                new FileInputStream(archiveFile)));

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                // Every zipEntry is (again) a ZipArchive, which contains all
                // missing files for one project.
                SubMonitor currentArchiveMonitor = zipStreamLoopMonitor
                    .newChild(100 / projectCount, SubMonitor.SUPPRESS_NONE);
                /*
                 * For every entry (which is a zipArchive for a single project)
                 * we have to find out which project it is meant for. So we need
                 * the projectID.
                 * 
                 * The archive name contains the projectID.
                 * 
                 * archiveName = projectID + this.projectIDDelimiter +
                 * randomNumber + '.zip'
                 */
                String projectID = zipEntry.getName().substring(0,
                    zipEntry.getName().indexOf(this.projectIDDelimiter));

                IProject project = localProjects.get(projectID);

                /*
                 * see FileUtils.writeArchive ... do not wrap the zip input
                 * stream here
                 */
                writeArchive(new FilterInputStream(zipInputStream) {
                    @Override
                    public void close() throws IOException {
                        // prevent the ZipInputStream from being closed
                    }
                }, project, currentArchiveMonitor);

                zipInputStream.closeEntry();
                currentArchiveMonitor.done();
            }
        } catch (OperationCanceledException e) {
            throw new LocalCancellationException(null,
                CancelOption.DO_NOT_NOTIFY_PEER);
        } finally {
            IOUtils.closeQuietly(zipInputStream);

            if (archiveFile != null)
                archiveFile.delete();

            monitor.done();
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
        SubMonitor subMonitor) throws SarosCancellationException, IOException {

        subMonitor.beginTask(null, 100);
        int numberOfLoops = projectNames.size();
        List<FileList> missingFiles = new ArrayList<FileList>();

        /*
         * this for loop sets up all the projects needed for the session and
         * computes the missing files.
         */
        for (Entry<String, String> entry : projectNames.entrySet()) {
            SubMonitor lMonitor = subMonitor.newChild(100 / numberOfLoops);
            String projectID = entry.getKey();
            String projectName = entry.getValue();
            checkCancellation(CancelOption.NOTIFY_PEER);
            ProjectNegotiationData projectInfo = null;
            for (ProjectNegotiationData pInfo : this.projectInfos) {
                if (pInfo.getProjectID().equals(projectID))
                    projectInfo = pInfo;
            }
            if (projectInfo == null) {
                log.error("tried to add a project that wasn't shared");
                // this should never happen
                continue;
            }

            VCSAdapter vcs = null;
            if (preferenceUtils.useVersionControl() && useVersionControl) {
                vcs = VCSAdapter.getAdapter(projectInfo.getFileList()
                    .getVcsProviderID());
            }

            IProject iProject = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(projectName);
            if (iProject.exists()) {
                /*
                 * Saving unsaved files is supposed to be done in
                 * JoinSessionWizard#performFinish().
                 */
                if (EditorAPI.existUnsavedFiles(iProject)) {
                    log.error("Unsaved files detected.");
                }
            } else {
                iProject = null;
            }
            IProject localProject = assignLocalProject(iProject, projectName,
                projectID, vcs, lMonitor.newChild(30), projectInfo);
            localProjects.put(projectID, localProject);

            checkCancellation(CancelOption.NOTIFY_PEER);
            if (vcs != null && !isPartialRemoteProject(projectID)) {
                log.debug("initVcState");
                initVcState(localProject, vcs, lMonitor.newChild(40),
                    projectInfo.getFileList());
            }
            checkCancellation(CancelOption.NOTIFY_PEER);

            log.debug("compute required Files for project " + projectName
                + " with ID: " + projectID);
            FileList requiredFiles = computeRequiredFiles(localProject,
                projectInfo.getFileList(), projectID, vcs,
                lMonitor.newChild(30));
            requiredFiles.setProjectID(projectID);
            checkCancellation(CancelOption.NOTIFY_PEER);
            missingFiles.add(requiredFiles);
            lMonitor.done();
        }
        return missingFiles;
    }

    /**
     * In this method the project we want to have in the session is initialized.
     * If the baseProject is not null we use it as the base to create the
     * 
     * @param projectID
     * 
     * @param projectInfo
     * 
     * @throws LocalCancellationException
     */
    private IProject assignLocalProject(final IProject baseProject,
        final String newProjectName, String projectID, final VCSAdapter vcs,
        final IProgressMonitor monitor, ProjectNegotiationData projectInfo)
        throws IOException, LocalCancellationException {
        IProject newLocalProject = baseProject;
        FileList remoteFileList = projectInfo.getFileList();
        // if the baseProject already exists
        if (newLocalProject != null) {
            if (newLocalProject.getName().equals(newProjectName)) {
                // TODO the project could be managed by a different Team
                // provider
                if (vcs != null && !vcs.isManaged(newLocalProject)
                    && !projectInfo.isPartial()) {
                    String repositoryRoot = remoteFileList.getRepositoryRoot();
                    final String url = remoteFileList.getProjectInfo().url;
                    String directory = url.substring(repositoryRoot.length());
                    vcs.connect(newLocalProject, repositoryRoot, directory,
                        monitor);
                }
            }
            return newLocalProject;
        }

        if (vcs != null) {
            if (!isPartialRemoteProject(projectID)) {
                try {
                    /*
                     * Inform the host of the session that the current (local)
                     * user has started the possibly time consuming SVN checkout
                     * via a remoteProgressMonitor
                     */
                    ISarosSession sarosSession = sarosSessionObservable
                        .getValue();
                    if (sarosSession != null) {
                        /*
                         * The monitor that is created here is shown both
                         * locally and remote and is handled like a regular
                         * progress monitor.
                         */
                        IProgressMonitor remoteMonitor = rpm
                            .mirrorLocalProgressMonitorToRemote(sarosSession,
                                sarosSession.getHost(), monitor);
                        remoteMonitor
                            .setTaskName("Project checkout via subversion");
                        newLocalProject = vcs.checkoutProject(newProjectName,
                            remoteFileList, remoteMonitor);
                    } else {
                        log.error("No Saros session!");
                    }
                } catch (OperationCanceledException e) {
                    /*
                     * The exception is thrown if the user canceled the svn
                     * checkout process. We send the remote user a sophisticated
                     * message here.
                     */
                    throw new LocalCancellationException(
                        "The CVS checkout process was canceled",
                        CancelOption.NOTIFY_PEER);
                }
            }

            /*
             * HACK: After checking out a project, give Eclipse/the Team
             * provider time to realize that the project is now managed. The
             * problem was that when checking later to see if we have to
             * switch/update individual resources in initVcState, the project
             * appeared as unmanaged. It might work to wrap initVcState in a
             * job, such that it is scheduled after the project is marked as
             * managed.
             */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // do nothing
            }
            if (newLocalProject != null)
                return newLocalProject;
        }

        final CreateProjectTask createProjectTask = new CreateProjectTask(
            newProjectName, baseProject, monitor);

        try {
            ResourcesPlugin.getWorkspace().run(createProjectTask, monitor);
        } catch (OperationCanceledException e) {
            throw new LocalCancellationException();
        } catch (CoreException e) {
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
            sarosSession.removeProjectOwnership(entry.getKey(),
                ResourceAdapterFactory.create(entry.getValue()), jid);

        // The session might have been stopped already, if not we will stop it.
        if (sarosSession.getProjectResourcesMapping().keySet().isEmpty()
            || sarosSession.getRemoteUsers().isEmpty())
            sessionManager.stopSarosSession();
    }

    @Override
    public synchronized boolean remoteCancel(String errorMsg) {
        if (!super.remoteCancel(errorMsg))
            return false;

        if (addIncomingProjectUI != null)
            addIncomingProjectUI.cancelWizard(peer, errorMsg,
                CancelLocation.REMOTE);

        if (!running)
            terminateProcess(null);

        return true;
    }

    @Override
    public synchronized boolean localCancel(String errorMsg,
        CancelOption cancelOption) {
        if (!super.localCancel(errorMsg, cancelOption))
            return false;

        if (addIncomingProjectUI != null)
            addIncomingProjectUI.cancelWizard(peer, errorMsg,
                CancelLocation.LOCAL);

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
     * 
     * @return The list of files that we need from the host.
     * @throws LocalCancellationException
     *             If the user requested a cancel.
     * @throws IOException
     */
    private FileList computeRequiredFiles(IProject currentLocalProject,
        FileList remoteFileList, String projectID, VCSAdapter vcs,
        IProgressMonitor monitor) throws LocalCancellationException,
        IOException {

        SubMonitor subMonitor = SubMonitor.convert(monitor,
            "Compute required Files...", 1);

        FileListDiff filesToSynchronize = null;
        FileList localFileList = null;

        try {
            localFileList = FileListFactory.createFileList(currentLocalProject,
                null, checksumCache, vcs != null, subMonitor.newChild(1));
        } catch (CoreException e) {
            throw new IOException(e.getMessage(), e.getCause());
        }

        filesToSynchronize = computeDiff(localFileList, remoteFileList,
            currentLocalProject, projectID);

        List<IPath> missingFiles = filesToSynchronize.getAddedPaths();
        missingFiles.addAll(filesToSynchronize.getAlteredPaths());

        /*
         * We send an empty file list to the host as a notification that we do
         * not need any files.
         */

        if (missingFiles.isEmpty()) {
            log.debug(this + " : there are no files to synchronize.");
            subMonitor.done();
            return FileListFactory.createEmptyFileList();
        }

        subMonitor.done();
        return FileListFactory.createPathFileList(missingFiles);
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
    protected FileListDiff computeDiff(FileList localFileList,
        FileList remoteFileList, IProject currentLocalProject, String projectID)
        throws IOException {
        log.debug(this + " : computing file list difference");

        try {
            FileListDiff diff = FileListDiff
                .diff(localFileList, remoteFileList);

            if (!isPartialRemoteProject(projectID)) {
                /*
                 * WTF !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! THIS IS DELETING
                 * FILES !!!!!!!
                 */
                diff = diff.removeUnneededResources(currentLocalProject,
                    new NullProgressMonitor());
            }

            diff = diff.addAllFolders(currentLocalProject,
                new NullProgressMonitor());

            return diff;
        } catch (CoreException e) {
            throw new IOException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Have a look at the description of {@link WorkspaceModifyOperation}!
     * 
     * @see WorkspaceModifyOperation
     */
    private void writeArchive(final InputStream archiveStream,
        final IProject project, final IProgressMonitor monitor)
        throws LocalCancellationException, IOException {

        final DecompressTask decompressTask = new DecompressTask(
            new ZipInputStream(archiveStream), project, monitor);

        long startTime = System.currentTimeMillis();

        log.debug(this + " : Writing archive to disk...");
        /*
         * TODO: calculate the ADLER32 checksums during decompression and add
         * them into the ChecksumCache. The insertion must be done after the
         * WorkspaceRunnable has run or all checksums will be invalidated during
         * the IResourceChangeListener updates inside the WorkspaceRunnable or
         * after it finished!
         */

        try {
            ResourcesPlugin.getWorkspace().run(decompressTask, monitor);
        } catch (OperationCanceledException e) {
            throw new LocalCancellationException(null,
                CancelOption.DO_NOT_NOTIFY_PEER);
        } catch (CoreException e) {
            throw new IOException(e.getMessage(), e.getCause());
        }

        log.debug(String.format("Unpacked archive in %d s",
            (System.currentTimeMillis() - startTime) / 1000));

        // TODO: now add the checksums into the cache
    }

    @Override
    public String getProcessID() {
        return processID;
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
     * @param remoteFileList
     * 
     * @throws SarosCancellationException
     */
    private void initVcState(IResource resource, VCSAdapter vcs,
        SubMonitor monitor, FileList remoteFileList)
        throws SarosCancellationException {
        if (monitor.isCanceled())
            return;

        if (!vcs.isManaged(resource))
            return;

        final VCSResourceInfo info = vcs.getCurrentResourceInfo(resource);
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
        List<IPath> paths = remoteFileList.getPaths();
        if (url == null || revision == null) {
            // The resource might have been deleted.
            return;
        }
        if (!info.url.equals(url)) {
            log.trace("Switching " + resource.getName() + " from " + info.url
                + " to " + url);
            vcs.switch_(resource, url, revision, monitor);
        } else if (!info.revision.equals(revision) && paths.contains(path)) {
            log.trace("Updating " + resource.getName() + " from "
                + info.revision + " to " + revision);
            vcs.update(resource, revision, monitor);
        }
        if (monitor.isCanceled())
            return;

        if (resource instanceof IContainer) {
            // Recurse.
            try {
                List<IResource> children = Arrays
                    .asList(((IContainer) resource).members());
                for (IResource child : children) {
                    if (remoteFileList.getPaths().contains(child.getFullPath()))
                        initVcState(child, vcs, monitor, remoteFileList);
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
                .getPacketFilter(sessionID, processID));
    }

    private void deleteCollectors() {
        startActivityQueuingRequestCollector.cancel();
    }

    private File receiveArchive(
        ArchiveTransferListener archiveTransferListener, String transferID,
        IProgressMonitor monitor) throws IOException,
        SarosCancellationException {

        monitor.beginTask("Receiving archive file...", 100);
        log.debug("waiting for incoming archive stream request");

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

        log.debug(this + " : receiving archive");

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

            monitor.done();
        }

        log.debug(this + " : stored archive in file "
            + archiveFile.getAbsolutePath() + ", size: "
            + Utils.formatByte(archiveFile.length()));

        return archiveFile;
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

    @Override
    public String toString() {
        return "IPN [remote side: " + peer + "]";
    }
}
