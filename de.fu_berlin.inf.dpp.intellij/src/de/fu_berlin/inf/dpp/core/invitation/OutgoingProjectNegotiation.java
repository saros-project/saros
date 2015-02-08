package de.fu_berlin.inf.dpp.core.invitation;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationMissingFilesExtension;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationOfferingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingRequest;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.core.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.SubProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.FileList;
import de.fu_berlin.inf.dpp.negotiation.FileListFactory;
import de.fu_berlin.inf.dpp.negotiation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.vcs.VCSProvider;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.picocontainer.annotations.Inject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CancellationException;

/**
 * TODO: Refactor when merging with Saros/E OPN.
 */

public class OutgoingProjectNegotiation extends ProjectNegotiation {

    private static final Random PROCESS_ID_GENERATOR = new Random();
    private static final Logger LOG = Logger
        .getLogger(OutgoingProjectNegotiation.class);

    private final List<IProject> projects;
    private final ISarosSession sarosSession;

    @Inject
    private IEditorManager editorManager;
    @Inject
    private IChecksumCache checksumCache;

    private PacketCollector remoteFileListResponseCollector;

    private PacketCollector startActivityQueuingResponseCollector;

    // TODO pull up, when this class is in core
    @Inject
    private ISarosSessionManager sessionManager;

    public OutgoingProjectNegotiation(JID to, ISarosSession sarosSession,
        List<IProject> projects, ISarosContext sarosContext) {
        super(String.valueOf(PROCESS_ID_GENERATOR.nextLong()), sarosSession
            .getID(), to, sarosContext);

        this.sarosSession = sarosSession;
        this.projects = projects;
    }

    public Status start(IProgressMonitor monitor) {

        createCollectors();

        File zipArchive = null;

        observeMonitor(monitor);

        Exception exception = null;

        try {
            if (fileTransferManager == null)
            // FIXME: the logic will try to send this to the remote contact
            {
                throw new IOException("not connected to a XMPP server");
            }

            sendFileList(createProjectExchangeInfoList(projects, monitor),
                monitor);

            monitor.subTask("");

            List<FileList> fileLists = getRemoteFileList(monitor);
            monitor.subTask("");

            List<StartHandle> stoppedUsers = null;
            try {
                stoppedUsers = stopUsers(monitor);
                monitor.subTask("");

                sendAndAwaitActivityQueueingActivation(monitor);
                monitor.subTask("");

                User user = sarosSession.getUser(peer);

                if (user == null) {
                    throw new LocalCancellationException(null,
                        CancelOption.DO_NOT_NOTIFY_PEER);
                }

                /*
                 * inform all listeners that the peer has started queuing and
                 * can therefore process IResourceActivities now
                 * 
                 * TODO this needs a review as this is called inside the
                 * "blocked" section and so it is not allowed to send resource
                 * activities at this time. Maybe change the description of the
                 * listener interface ?
                 */
                sarosSession.userStartedQueuing(user);

                zipArchive = createProjectArchive(fileLists, monitor);
                monitor.subTask("");
            } finally {
                if (stoppedUsers != null) {
                    startUsers(stoppedUsers);
                }
            }

            checkCancellation(CancelOption.NOTIFY_PEER);

            if (zipArchive != null) {
                sendArchive(zipArchive, peer, ARCHIVE_TRANSFER_ID + getID(),
                    monitor);
            }

            User user = sarosSession.getUser(peer);

            if (user == null) {
                throw new LocalCancellationException(null,
                    CancelOption.DO_NOT_NOTIFY_PEER);
            }

            sarosSession.userFinishedProjectNegotiation(user);

        } catch (Exception e) {
            exception = e;
        } finally {

            if (zipArchive != null && !zipArchive.delete()) {
                LOG.warn("could not delete archive file: "
                    + zipArchive.getAbsolutePath());
            }
            deleteCollectors();
            monitor.done();
        }

        return terminateProcess(exception);
    }

    private void sendFileList(
        List<ProjectNegotiationData> projectExchangeInfos,
        IProgressMonitor monitor) throws IOException,
        SarosCancellationException {

        /*
         * FIXME display the remote side something that will it receive
         * something in the near future
         */

        checkCancellation(CancelOption.NOTIFY_PEER);

        LOG.debug(this + " : sending file list");

        /*
         * file lists are normally very small so we "accept" the circumstance
         * that this step cannot be cancelled.
         */

        monitor.setTaskName("Sending file list...");

        /*
         * The Remote receives this message at the InvitationHandler which calls
         * the SarosSessionManager which creates a IncomingProjectNegotiation
         * instance and pass it to the installed callback handler (which in the
         * current implementation opens a wizard on the remote side)
         */
        ProjectNegotiationOfferingExtension offering = new ProjectNegotiationOfferingExtension(
            getSessionID(), getID(), projectExchangeInfos);

        transmitter.send(ISarosSession.SESSION_CONNECTION_ID, peer,
            ProjectNegotiationOfferingExtension.PROVIDER.create(offering));
    }

    /**
     * Retrieve the peer's partial file list and remember which files need to be
     * sent to that user
     *
     * @param monitor
     * @throws IOException
     * @throws SarosCancellationException
     */
    private List<FileList> getRemoteFileList(IProgressMonitor monitor)
        throws IOException, SarosCancellationException {

        LOG.debug(this + " : waiting for remote file list");

        monitor.beginTask("Waiting for " + peer.getName()
            + " to choose project(s) location", IProgressMonitor.UNKNOWN);

        checkCancellation(CancelOption.NOTIFY_PEER);

        Packet packet = collectPacket(remoteFileListResponseCollector,
            60 * 60 * 1000);

        if (packet == null) {
            throw new LocalCancellationException("received no response from "
                + peer + " while waiting for the file list",
                CancelOption.DO_NOT_NOTIFY_PEER);
        }

        List<FileList> remoteFileLists = ProjectNegotiationMissingFilesExtension.PROVIDER
            .getPayload(packet).getFileLists();

        LOG.debug(this + " : remote file list has been received");

        checkCancellation(CancelOption.NOTIFY_PEER);

        monitor.done();

        return remoteFileLists;
    }

    @Override
    public Map<String, String> getProjectNames() {
        Map<String, String> result = new HashMap<String, String>();
        for (IProject project : projects) {
            result.put(sarosSession.getProjectID(project), project.getName());
        }

        return result;
    }

    @Override
    protected void executeCancellation() {
        if (sarosSession.getRemoteUsers().isEmpty()) {
            sessionManager.stopSarosSession();
        }
    }

    private List<StartHandle> stopUsers(IProgressMonitor monitor)
        throws SarosCancellationException {
        Collection<User> usersToStop;

        /*
         * TODO: Make sure that all users are fully registered when stopping
         * them, otherwise failures might occur while a user is currently
         * joining and has not fully initialized yet.
         * 
         * See also OutgoingSessionNegotiation#completeInvitation
         * 
         * srossbach: This may already be the case ... just review this
         */

        usersToStop = new ArrayList<User>(sarosSession.getUsers());

        LOG.debug(this + " : stopping users " + usersToStop);

        List<StartHandle> startHandles;

        monitor.beginTask("Locking the session...", IProgressMonitor.UNKNOWN);

        /*
         * FIXME the StopManager should use a timeout as it can happen that a
         * user leaves the session during the stop request. Currently it is up
         * to the user to press the cancel button because the StopManager did
         * not check if the user already left the session.
         * 
         * srossbach: The StopManager should not check for the absence of a user
         * and so either retry again or just stop the sharing (which currently
         * would lead to a broken session because we have no proper cancellation
         * logic !
         */
        try {
            startHandles = sarosSession.getStopManager().stop(usersToStop,
                "Synchronizing invitation");
        } catch (CancellationException e) {
            checkCancellation(CancelOption.NOTIFY_PEER);
            return null;
        }

        monitor.done();
        return startHandles;
    }

    private void startUsers(List<StartHandle> startHandles) {
        for (StartHandle startHandle : startHandles) {
            LOG.debug(this + " : restarting user " + startHandle.getUser());
            startHandle.start();
        }
    }

    /**
     * @param fileLists
     *            a list of file lists containing the files to archive
     * @return zip file containing all files denoted by the file lists or
     *         <code>null</code> if the file lists do not contain any files
     */

    private File createProjectArchive(final List<FileList> fileLists,
        final IProgressMonitor monitor) throws IOException,
        SarosCancellationException {

        boolean skip = true;

        int fileCount = 0;

        for (final FileList list : fileLists) {
            skip &= list.getPaths().isEmpty();
            fileCount += list.getPaths().size();
        }

        if (skip) {
            return null;
        }

        checkCancellation(CancelOption.NOTIFY_PEER);

        final List<IFile> filesToCompress = new ArrayList<IFile>(fileCount);
        final List<String> fileAlias = new ArrayList<String>(fileCount);

        for (final FileList list : fileLists) {
            final String projectID = list.getProjectID();

            final IProject project = sarosSession.getProject(projectID);
            project.refreshLocal();

            if (project == null) {
                throw new LocalCancellationException("project with id "
                    + projectID + " was unshared during synchronization",
                    CancelOption.NOTIFY_PEER);
            }

            if (editorManager != null)
                editorManager.saveEditors(project);

            final StringBuilder aliasBuilder = new StringBuilder();

            aliasBuilder.append(projectID).append(PATH_DELIMITER);

            final int prefixLength = aliasBuilder.length();

            for (final String path : list.getPaths()) {
                // assert path is relative !
                filesToCompress.add(project.getFile(path));
                aliasBuilder.append(path);
                fileAlias.add(aliasBuilder.toString());
                aliasBuilder.setLength(prefixLength);
            }
        }

        LOG.debug(this + " : creating archive");

        File tempArchive;

        try {
            tempArchive = File.createTempFile("saros_" + getID(), ".zip");

            // TODO run inside workspace ?
            new CreateArchiveTask(tempArchive, filesToCompress, fileAlias,
                monitor).run(null);
        } catch (OperationCanceledException e) {
            throw new LocalCancellationException();
        }

        monitor.done();

        return tempArchive;
    }

    private void createCollectors() {
        remoteFileListResponseCollector = xmppReceiver
            .createCollector(ProjectNegotiationMissingFilesExtension.PROVIDER
                .getPacketFilter(getSessionID(), getID()));

        startActivityQueuingResponseCollector = xmppReceiver
            .createCollector(StartActivityQueuingResponse.PROVIDER
                .getPacketFilter(getSessionID(), getID()));
    }

    private void deleteCollectors() {
        remoteFileListResponseCollector.cancel();
        startActivityQueuingResponseCollector.cancel();
    }

    private void sendArchive(File archive, JID remoteContact,
        String transferID, IProgressMonitor monitor)
        throws SarosCancellationException, IOException {

        LOG.debug(this + " : sending archive");
        monitor.beginTask("Sending archive file...", 100);

        assert fileTransferManager != null;

        try {
            OutgoingFileTransfer transfer = fileTransferManager
                .createOutgoingFileTransfer(remoteContact.toString());

            transfer.sendFile(archive, transferID);
            monitorFileTransfer(transfer, monitor);
        } catch (XMPPException e) {
            throw new IOException(e.getMessage(), e);
        }

        monitor.done();

        LOG.debug(this + " : archive send");
    }

    /**
     * Method to create list of ProjectExchangeInfo.
     *
     * @param projectsToShare
     *            List of projects to share
     */
    private List<ProjectNegotiationData> createProjectExchangeInfoList(
        List<IProject> projectsToShare, IProgressMonitor monitor)
        throws IOException, LocalCancellationException {

        // *stretch* progress bar so it will increment smoothly
        final int scale = 1000;

        monitor
            .beginTask(
                "Creating file list and calculating file checksums. This may take a while...",
                projectsToShare.size() * scale);

        List<ProjectNegotiationData> pInfos = new ArrayList<ProjectNegotiationData>(
            projectsToShare.size());

        for (IProject project : projectsToShare) {

            if (monitor.isCanceled()) {
                throw new LocalCancellationException(null,
                    CancelOption.DO_NOT_NOTIFY_PEER);
            }
            try {

                VCSProvider vcs = null;
                FileList projectFileList = FileListFactory.createFileList(
                    project, sarosSession.getSharedResources(project),
                    checksumCache, vcs, new SubProgressMonitor(monitor, scale,
                        SubProgressMonitor.SUPPRESS_BEGINTASK
                            | SubProgressMonitor.SUPPRESS_SETTASKNAME));

                boolean partial = !sarosSession.isCompletelyShared(project);

                String projectID = sarosSession.getProjectID(project);
                projectFileList.setProjectID(projectID);

                ProjectNegotiationData negotiationData = new ProjectNegotiationData(
                    projectID, project.getName(), partial, projectFileList);

                pInfos.add(negotiationData);

            } catch (IOException e) {
                /*
                 * avoid that the error is send to remote side (which is default
                 * for IOExceptions) at this point because the remote side has
                 * no existing project negotiation yet
                 */
                localCancel(e.getMessage(), CancelOption.DO_NOT_NOTIFY_PEER);
                // throw to LOG this error in the CancelableProcess class
                throw new IOException(e.getMessage(), e);
            }
        }

        monitor.done();

        return pInfos;
    }

    /**
     * Sends an activity queuing request to the remote side and awaits the
     * confirmation of the request.
     *
     * @param monitor
     */
    private void sendAndAwaitActivityQueueingActivation(IProgressMonitor monitor)
        throws IOException, SarosCancellationException {

        monitor.beginTask("Waiting for " + peer.getName()
                + " to perform additional initialization...",
            IProgressMonitor.UNKNOWN);

        transmitter
            .send(ISarosSession.SESSION_CONNECTION_ID, peer,
                StartActivityQueuingRequest.PROVIDER
                    .create(new StartActivityQueuingRequest(getSessionID(),
                        getID())));

        Packet packet = collectPacket(startActivityQueuingResponseCollector,
            PACKET_TIMEOUT);

        if (packet == null) {
            throw new LocalCancellationException("received no response from "
                + peer + " while waiting to finish additional initialization",
                CancelOption.DO_NOT_NOTIFY_PEER);
        }

        monitor.done();
    }

    @Override
    public String toString() {
        return "OPN [remote side: " + peer + "]";
    }
}
