package de.fu_berlin.inf.dpp.invitation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.FileListFactory;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.ProjectsAddedActivity;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.extensions.FileListExtension;
import de.fu_berlin.inf.dpp.project.IChecksumCache;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.Messages;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.util.FileZipper;
import de.fu_berlin.inf.dpp.util.MappedList;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.ZipProgressMonitor;

public class OutgoingProjectNegotiation extends ProjectNegotiation {

    private static Logger log = Logger
        .getLogger(OutgoingProjectNegotiation.class);

    /**
     * this maps the currently exchanging projects. projectID => project in
     * workspace
     */
    private List<IProject> projects;

    private ISarosSession sarosSession;

    /**
     * projectID => List of {@link IPath files} that will be send to peer
     */
    private MappedList<String, IPath> projectFilesToSend = new MappedList<String, IPath>();

    private final static Random INVITATION_RAND = new Random();

    @Inject
    private EditorManager editorManager;

    @Inject
    private IReceiver xmppReceiver;

    @Inject
    private IChecksumCache checksumCache;

    private SarosPacketCollector remoteFileListResponseCollector;

    public OutgoingProjectNegotiation(JID to, ISarosSession sarosSession,
        List<IProject> projects, SarosContext sarosContext) {
        super(to, sarosContext);

        this.processID = String.valueOf(INVITATION_RAND.nextLong());
        this.sarosSession = sarosSession;
        this.projects = projects;
        // FIMXE move to SarosSessionManager
        this.projectExchangeProcesses.addProjectExchangeProcess(this);
    }

    public Status start(IProgressMonitor monitor) {

        createCollectors();
        File zipArchive = null;

        List<File> zipArchives = new ArrayList<File>();

        observeMonitor(monitor);

        Exception exception = null;

        try {
            if (fileTransferManager == null)
                // FIXME: the logic will try to send this to the remote contact
                throw new IOException("not connected to a XMPP server");

            sendFileList(createProjectExchangeInfoList(projects, monitor),
                monitor);

            monitor.subTask("");

            getRemoteFileList(monitor);
            monitor.subTask("");

            editorManager.setAllLocalOpenedEditorsLocked(false);
            zipArchives = createProjectArchives(projectFilesToSend, monitor);
            monitor.subTask("");

            checkCancellation(CancelOption.NOTIFY_PEER);

            if (zipArchives.size() > 0) {

                // pack all archive files into one big archive
                zipArchive = File.createTempFile("SarosSyncArchive", ".zip");
                try {
                    FileZipper.zipFiles(zipArchives, zipArchive, false,
                        new ZipProgressMonitor(monitor, zipArchives.size(),
                            false));

                    monitor.subTask("");
                    monitor.done();

                } catch (OperationCanceledException e) {
                    throw new LocalCancellationException();
                }
                zipArchives.add(zipArchive);

                sendArchive(zipArchive, peer, processID, monitor);
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            projectExchangeProcesses.removeProjectExchangeProcess(this);

            for (File archive : zipArchives) {
                if (!archive.delete())
                    log.warn("could not archive file: "
                        + archive.getAbsolutePath());
            }
            deleteCollectors();
            monitor.done();
        }

        return terminateProcess(exception);
    }

    private void sendFileList(List<ProjectExchangeInfo> projectExchangeInfos,
        IProgressMonitor monitor) throws SarosCancellationException {

        /*
         * FIXME must be calculated while the session is blocked !
         * 
         * FIXME display the remote side something that will it receive
         * something in the near future
         */

        checkCancellation(CancelOption.NOTIFY_PEER);

        log.debug(this + " : sending file list");

        /*
         * sending an activity takes 0 ms because the activity will be buffered
         * and send by another thread
         */
        // monitor.setTaskName("Sending file list...");

        /*
         * For those who do not get the "magic". This activity is executed in
         * the remote SarosSession and handled by the ProjectAddedManager which
         * calls the SarosSessionManager which creates a
         * IncomingProjectNegotiation instance and pass it to the SarosUI which
         * finally opens the Wizard on the remote side
         */
        sarosSession.sendActivity(sarosSession.getUser(peer),
            new ProjectsAddedActivity(sarosSession.getLocalUser(),
                projectExchangeInfos, processID));
    }

    /**
     * Retrieve the peer's partial file list and remember which files need to be
     * sent to that user
     * 
     * @param monitor
     * @throws IOException
     * @throws SarosCancellationException
     */
    protected void getRemoteFileList(IProgressMonitor monitor)
        throws IOException, SarosCancellationException {

        log.debug(this + " : waiting for remote file list");

        monitor.beginTask("Waiting for " + peer.getName()
            + " to choose project(s) location", IProgressMonitor.UNKNOWN);

        checkCancellation(CancelOption.NOTIFY_PEER);

        Packet packet = collectPacket(remoteFileListResponseCollector,
            60 * 60 * 1000, monitor);

        if (packet == null)
            throw new LocalCancellationException("received no response from "
                + peer + " while waiting for the file list",
                CancelOption.DO_NOT_NOTIFY_PEER);

        List<FileList> remoteFileLists = FileListExtension.PROVIDER.getPayload(
            packet).getFileLists();

        log.debug(this + " : remote file list has been received");

        checkCancellation(CancelOption.NOTIFY_PEER);

        for (FileList fileList : remoteFileLists) {
            projectFilesToSend
                .put(fileList.getProjectID(), fileList.getPaths());
        }

        monitor.done();
    }

    @Override
    public Map<String, String> getProjectNames() {
        Map<String, String> result = new HashMap<String, String>();
        for (IProject project : projects)
            result.put(sarosSession.getProjectID(project), project.getName());

        return result;
    }

    @Override
    public String getProcessID() {
        return this.processID;
    }

    @Override
    protected void executeCancellation() {
        if (sarosSession.getRemoteUsers().isEmpty())
            sessionManager.stopSarosSession();
    }

    /**
     * 
     * @param projectFilesToSend
     *            projectID => List of {@link IPath files} that will be sent to
     *            peer
     * @return List of project archives
     */
    private List<File> createProjectArchives(
        MappedList<String, IPath> projectFilesToSend, IProgressMonitor monitor)
        throws IOException, SarosCancellationException {

        Collection<User> usersToStop;

        /*
         * Make sure that all users are fully registered, otherwise failures
         * might occur while a user is currently joining and has not fully
         * initialized yet.
         * 
         * See also OutgoingSessionNegotiation#completeInvitation
         */

        synchronized (CancelableProcess.SHARED_LOCK) {
            usersToStop = new ArrayList<User>(sarosSession.getUsers());
        }

        log.debug(this + " : stopping users " + usersToStop);

        List<StartHandle> startHandles;

        monitor.beginTask("Locking the session...", IProgressMonitor.UNKNOWN);

        /*
         * FIMXE the StopManager should use a timeout as it can happen that a
         * user leaves the session during the stop request. Currently it is up
         * to the user to press the cancel button because the StopManager did
         * not check if the user already left the session.
         * 
         * Stefan Rossbach: The StopManager should not check for the absence of
         * a user and so either retry again or just stop the sharing (which
         * currently would lead to a broken session because we have no proper
         * cancellation logic !
         */
        try {
            startHandles = sarosSession.getStopManager().stop(usersToStop,
                "Synchronizing invitation", monitor);
        } catch (CancellationException e) {
            checkCancellation(CancelOption.NOTIFY_PEER);
            return null;
        }

        monitor.done();

        log.debug(this + " : creating archive(s)");

        SubMonitor subMonitor = SubMonitor.convert(monitor,
            "Creating project archives...", projectFilesToSend.keySet().size());

        /*
         * Use editorManager.saveText() because the EditorAPI.saveProject() will
         * not save files which were modified in the background. This is what
         * happens for example if a user edits a file which is not opened by the
         * local user.
         * 
         * Stefan Rossbach: this will still fail if a user edited a file and
         * then closes the editor without saving it.
         */

        try {

            // FIXME this throws a NPE if the session has already been stopped
            for (SPath path : editorManager.getOpenEditorsOfAllParticipants())
                editorManager.saveText(path);

            checkCancellation(CancelOption.NOTIFY_PEER);

            List<File> archivesToSend = new LinkedList<File>();

            for (Map.Entry<String, List<IPath>> entry : projectFilesToSend
                .entrySet()) {

                String projectID = entry.getKey();
                List<IPath> filesToCompress = entry.getValue();

                File projectArchive = createProjectArchive(
                    subMonitor.newChild(1), filesToCompress, projectID);

                if (projectArchive != null)
                    archivesToSend.add(projectArchive);

            }

            subMonitor.done();

            return archivesToSend;

        } finally {
            for (StartHandle startHandle : startHandles) {
                log.debug(this + " : restarting users "
                    + Utils.prefix(startHandle.getUser().getJID()));
                startHandle.start();
            }
        }
    }

    private File createProjectArchive(IProgressMonitor monitor,
        List<IPath> toSend, String projectID) throws IOException,
        SarosCancellationException {

        IProject project = sarosSession.getProject(projectID);
        /*
         * TODO: Ask the user whether to save the resources, but only if they
         * have changed. How to ask Eclipse whether there are resource changes?
         * if (outInvitationUI.confirmProjectSave(peer)) getOpenEditors =>
         * filter per Project => if dirty ask to save
         */
        EditorAPI.saveProject(project, false);

        String prefix = projectID + projectIDDelimiter;

        File tempArchive = null;

        try {
            if (toSend.size() > 0) {
                tempArchive = File.createTempFile(prefix, ".zip");

                FileZipper.createProjectZipArchive(project, toSend,
                    tempArchive, new ZipProgressMonitor(monitor, toSend.size(),
                        true));
            }
        } catch (OperationCanceledException e) {
            throw new LocalCancellationException();
        }

        monitor.done();

        return tempArchive;
    }

    private Packet collectPacket(SarosPacketCollector collector, long timeout,
        IProgressMonitor monitor) throws SarosCancellationException {

        Packet packet = null;

        while (timeout > 0) {
            if (monitor != null && monitor.isCanceled())
                checkCancellation(CancelOption.NOTIFY_PEER);

            packet = collector.nextResult(1000);

            if (packet != null)
                break;

            timeout -= 1000;
        }

        return packet;
    }

    private void createCollectors() {
        remoteFileListResponseCollector = xmppReceiver
            .createCollector(FileListExtension.PROVIDER.getPacketFilter(
                sessionID, processID));
    }

    private void deleteCollectors() {
        remoteFileListResponseCollector.cancel();
    }

    private void sendArchive(File archive, JID remoteContact,
        String transferID, IProgressMonitor monitor)
        throws SarosCancellationException, IOException {

        log.debug(this + " : sending archive");
        monitor.beginTask("Sending archive file...", 100);

        assert fileTransferManager != null;

        try {
            OutgoingFileTransfer transfer = fileTransferManager
                .createOutgoingFileTransfer(remoteContact.toString());

            transfer.sendFile(archive, transferID);
            monitorFileTransfer(transfer, monitor);
        } catch (XMPPException e) {
            throw new IOException(e.getMessage(), e.getCause());
        }

        monitor.done();

        log.debug(this + " : archive send");
    }

    /**
     * Method to create list of ProjectExchangeInfo.
     * 
     * @param projectsToShare
     *            List of projects initially to share
     * @param monitor
     *            Show progress
     * @return
     * @throws LocalCancellationException
     */
    private List<ProjectExchangeInfo> createProjectExchangeInfoList(
        List<IProject> projectsToShare, IProgressMonitor monitor)
        throws LocalCancellationException {

        SubMonitor subMonitor = SubMonitor.convert(monitor,
            Messages.SarosSessionManager_creating_file_list,
            projectsToShare.size());

        List<ProjectExchangeInfo> pInfos = new ArrayList<ProjectExchangeInfo>(
            projectsToShare.size());

        for (IProject project : projectsToShare) {

            if (monitor.isCanceled())
                throw new LocalCancellationException(null,
                    CancelOption.DO_NOT_NOTIFY_PEER);
            try {
                String projectID = sarosSession.getProjectID(project);
                String projectName = project.getName();

                FileList projectFileList = FileListFactory.createFileList(
                    project, sarosSession.getSharedResources(project),
                    checksumCache, sarosSession.useVersionControl(),
                    subMonitor.newChild(1));

                projectFileList.setProjectID(projectID);
                boolean partial = !sarosSession.isCompletelyShared(project);

                ProjectExchangeInfo pInfo = new ProjectExchangeInfo(projectID,
                    project.getName(), projectName, partial, projectFileList);

                pInfos.add(pInfo);

            } catch (CoreException e) {
                throw new LocalCancellationException(e.getMessage(),
                    CancelOption.DO_NOT_NOTIFY_PEER);
            }
        }

        subMonitor.done();

        return pInfos;
    }

    @Override
    public String toString() {
        return "OPN [remote side: " + peer + "]";
    }
}
