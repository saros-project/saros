package de.fu_berlin.inf.dpp.negotiation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationMissingFilesExtension;
import de.fu_berlin.inf.dpp.communication.extensions.ProjectNegotiationOfferingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingRequest;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IFile;
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
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;

public class OutgoingProjectNegotiation extends ProjectNegotiation {

    private static Logger LOG = Logger
        .getLogger(OutgoingProjectNegotiation.class);

    private List<IProject> projects;

    private static final Random NEGOTIATION_ID_GENERATOR = new Random();

    private final IEditorManager editorManager;

    private PacketCollector remoteFileListResponseCollector;

    private PacketCollector startActivityQueuingResponseCollector;

    public OutgoingProjectNegotiation( //
        final JID peer, //
        final List<IProject> projects, //

        final ISarosSessionManager sessionManager, //
        final ISarosSession session, //

        final IEditorManager editorManager, //

        final IWorkspace workspace, //
        final IChecksumCache checksumCache, //

        final XMPPConnectionService connectionService, //
        final ITransmitter transmitter, //
        final IReceiver receiver//
    )

    {
        super(String.valueOf(NEGOTIATION_ID_GENERATOR.nextLong()), peer,
            sessionManager, session, workspace, checksumCache,
            connectionService, transmitter, receiver);

        this.projects = projects;

        this.editorManager = editorManager;
    }

    public Status run(IProgressMonitor monitor) {

        createCollectors();

        File zipArchive = null;

        observeMonitor(monitor);

        Exception exception = null;

        try {
            if (fileTransferManager == null)
                // FIXME: the logic will try to send this to the remote contact
                throw new IOException("not connected to a XMPP server");

            /*
             * FIXME save editors first, then do the file list and zip stuff
             * inside a Workspace Runnable with file locks !. There is a small
             * gap between saving editors and entering the file lock but it will
             * almost never matter in a real execution environment.
             * 
             * Do not save the editors inside the runnable as this may not work
             * depending on the IEditorManager implementation, i.e this thread
             * holds the lock, but saving editors is performed in another thread
             * !
             */
            sendFileList(createProjectNegotiationDataList(projects, monitor),
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

                User user = session.getUser(getPeer());

                if (user == null)
                    throw new LocalCancellationException(null,
                        CancelOption.DO_NOT_NOTIFY_PEER);

                /*
                 * inform all listeners that the peer has started queuing and
                 * can therefore process IResourceActivities now
                 * 
                 * TODO this needs a review as this is called inside the
                 * "blocked" section and so it is not allowed to send resource
                 * activities at this time. Maybe change the description of the
                 * listener interface ?
                 */
                session.userStartedQueuing(user);

                zipArchive = createProjectArchive(fileLists, monitor);
                monitor.subTask("");
            } finally {
                if (stoppedUsers != null)
                    startUsers(stoppedUsers);
            }

            checkCancellation(CancelOption.NOTIFY_PEER);

            if (zipArchive != null)
                sendArchive(zipArchive, getPeer(), ARCHIVE_TRANSFER_ID
                    + getID(), monitor);

            User user = session.getUser(getPeer());

            if (user == null)
                throw new LocalCancellationException(null,
                    CancelOption.DO_NOT_NOTIFY_PEER);

            session.userFinishedProjectNegotiation(user);

        } catch (Exception e) {
            exception = e;
        } finally {

            if (zipArchive != null && !zipArchive.delete())
                LOG.warn("could not delete archive file: "
                    + zipArchive.getAbsolutePath());
            deleteCollectors();
            monitor.done();
        }

        return terminate(exception);
    }

    private void sendFileList(List<ProjectNegotiationData> projectInfos,
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
         * that this step cannot be canceled.
         */

        monitor.setTaskName("Sending file list...");

        /*
         * The Remote receives this message at the InvitationHandler which calls
         * the SarosSessionManager which creates a IncomingProjectNegotiation
         * instance and pass it to the installed callback handler (which in the
         * current implementation opens a wizard on the remote side)
         */
        ProjectNegotiationOfferingExtension offering = new ProjectNegotiationOfferingExtension(
            getSessionID(), getID(), projectInfos);

        transmitter.send(ISarosSession.SESSION_CONNECTION_ID, getPeer(),
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

        monitor.beginTask("Waiting for " + getPeer().getName()
            + " to choose project(s) location", IProgressMonitor.UNKNOWN);

        checkCancellation(CancelOption.NOTIFY_PEER);

        Packet packet = collectPacket(remoteFileListResponseCollector,
            60 * 60 * 1000);

        if (packet == null)
            throw new LocalCancellationException("received no response from "
                + getPeer() + " while waiting for the file list",
                CancelOption.DO_NOT_NOTIFY_PEER);

        List<FileList> remoteFileLists = ProjectNegotiationMissingFilesExtension.PROVIDER
            .getPayload(packet).getFileLists();

        LOG.debug(this + " : remote file list has been received");

        checkCancellation(CancelOption.NOTIFY_PEER);

        monitor.done();

        return remoteFileLists;
    }

    @Override
    protected void executeCancellation() {
        if (session.getRemoteUsers().isEmpty())
            sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
    }

    private List<StartHandle> stopUsers(IProgressMonitor monitor) {

        /*
         * TODO: Make sure that all users are fully registered when stopping
         * them, otherwise failures might occur while a user is currently
         * joining and has not fully initialized yet.
         * 
         * See also OutgoingSessionNegotiation#completeInvitation
         * 
         * srossbach: This may already be the case ... just review this
         */

        final List<User> usersToStop = new ArrayList<User>(session.getUsers());

        LOG.debug(this + " : stopping users " + usersToStop);

        monitor.beginTask("Locking the session...", IProgressMonitor.UNKNOWN);

        // FIXME better handling of users that do not reply !!!
        try {
            return session.getStopManager().stop(usersToStop,
                "archive creation for OPN [id=" + getID() + "]");
        } catch (CancellationException e) {
            LOG.warn("failed to stop users", e);
            return null;
        } finally {
            monitor.done();
        }
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

        if (skip)
            return null;

        checkCancellation(CancelOption.NOTIFY_PEER);

        final List<IFile> filesToCompress = new ArrayList<IFile>(fileCount);
        final List<String> fileAlias = new ArrayList<String>(fileCount);

        final List<IResource> projectsToLock = new ArrayList<IResource>();

        for (final FileList list : fileLists) {
            final String projectID = list.getProjectID();

            final IProject project = session.getProject(projectID);

            if (project == null)
                throw new LocalCancellationException("project with id "
                    + projectID + " was unshared during synchronization",
                    CancelOption.NOTIFY_PEER);

            projectsToLock.add(project);

            /*
             * force editor buffer flush because we read the files from the
             * underlying storage
             */
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

        File tempArchive = null;

        try {
            tempArchive = File.createTempFile("saros_" + getID(), ".zip");
            workspace.run(new CreateArchiveTask(tempArchive, filesToCompress,
                fileAlias, monitor), projectsToLock.toArray(new IResource[0]));
        } catch (OperationCanceledException e) {
            LocalCancellationException canceled = new LocalCancellationException();
            canceled.initCause(e);
            throw canceled;
        }

        monitor.done();

        return tempArchive;
    }

    private void createCollectors() {
        remoteFileListResponseCollector = receiver
            .createCollector(ProjectNegotiationMissingFilesExtension.PROVIDER
                .getPacketFilter(getSessionID(), getID()));

        startActivityQueuingResponseCollector = receiver
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

    private List<ProjectNegotiationData> createProjectNegotiationDataList(
        final List<IProject> projectsToShare, final IProgressMonitor monitor)
        throws IOException, LocalCancellationException {

        // *stretch* progress bar so it will increment smoothly
        final int scale = 1000;

        monitor
            .beginTask(
                "Creating file list and calculating file checksums. This may take a while...",
                projectsToShare.size() * scale);

        List<ProjectNegotiationData> negData = new ArrayList<ProjectNegotiationData>(
            projectsToShare.size());

        for (IProject project : projectsToShare) {

            if (monitor.isCanceled())
                throw new LocalCancellationException(null,
                    CancelOption.DO_NOT_NOTIFY_PEER);
            try {

                /*
                 * force editor buffer flush because we read the files from the
                 * underlying storage
                 */
                if (editorManager != null)
                    editorManager.saveEditors(project);

                FileList projectFileList = FileListFactory.createFileList(
                    project, session.getSharedResources(project),
                    checksumCache, new SubProgressMonitor(monitor, 1 * scale,
                        SubProgressMonitor.SUPPRESS_BEGINTASK
                            | SubProgressMonitor.SUPPRESS_SETTASKNAME));

                boolean partial = !session.isCompletelyShared(project);

                String projectID = session.getProjectID(project);
                projectFileList.setProjectID(projectID);

                ProjectNegotiationData data = new ProjectNegotiationData(
                    projectID, project.getName(), partial, projectFileList);

                negData.add(data);

            } catch (IOException e) {
                /*
                 * avoid that the error is send to remote side (which is default
                 * for IOExceptions) at this point because the remote side has
                 * no existing project negotiation yet
                 */
                localCancel(e.getMessage(), CancelOption.DO_NOT_NOTIFY_PEER);
                // throw to LOG this error in the Negotiation class
                throw new IOException(e.getMessage(), e);
            }
        }

        monitor.done();

        return negData;
    }

    /**
     * Sends an activity queuing request to the remote side and awaits the
     * confirmation of the request.
     *
     * @param monitor
     */
    private void sendAndAwaitActivityQueueingActivation(IProgressMonitor monitor)
        throws IOException, SarosCancellationException {

        monitor.beginTask("Waiting for " + getPeer().getName()
            + " to perform additional initialization...",
            IProgressMonitor.UNKNOWN);

        transmitter
            .send(ISarosSession.SESSION_CONNECTION_ID, getPeer(),
                StartActivityQueuingRequest.PROVIDER
                    .create(new StartActivityQueuingRequest(getSessionID(),
                        getID())));

        Packet packet = collectPacket(startActivityQueuingResponseCollector,
            PACKET_TIMEOUT);

        if (packet == null)
            throw new LocalCancellationException("received no response from "
                + getPeer()
                + " while waiting to finish additional initialization",
                CancelOption.DO_NOT_NOTIFY_PEER);

        monitor.done();
    }

    @Override
    public String toString() {
        return "OPN [remote side: " + getPeer() + "]";
    }
}
