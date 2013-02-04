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

    private SubMonitor monitor;

    /**
     * projectID => List of {@link IPath files} that will be send to peer
     */
    private MappedList<String, IPath> projectFilesToSend = new MappedList<String, IPath>();

    private final static Random INVITATION_RAND = new Random();

    private boolean useVersionControl;

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
    }

    public Status start(IProgressMonitor progressMonitor) {

        createCollectors();
        File zipArchive = null;

        List<File> zipArchives = new ArrayList<File>();

        monitor = SubMonitor.convert(progressMonitor,
            "Retrieving list of files needed for synchronization...", 100);

        observeMonitor(monitor);

        Exception exception = null;

        try {
            if (fileTransferManager == null)
                // FIXME: the logic will try to send this to the remote contact
                throw new IOException("not connected to a XMPP server");

            sendFileList(monitor.newChild(10));
            monitor.subTask("");
            getRemoteFileList(monitor.newChild(0));
            monitor.subTask("");
            editorManager.setAllLocalOpenedEditorsLocked(false);
            // pack each project into one archive and check if it was
            // cancelled.
            zipArchives = createProjectArchives(projectFilesToSend,
                monitor.newChild(20));

            checkCancellation(CancelOption.NOTIFY_PEER);

            if (zipArchives.size() > 0) {

                // pack all archive files into one big archive
                zipArchive = File.createTempFile("SarosSyncArchive", ".zip");
                try {
                    FileZipper.zipFiles(zipArchives, zipArchive, false,
                        new ZipProgressMonitor(monitor.newChild(20),
                            zipArchives.size(), false));

                } catch (OperationCanceledException e) {
                    throw new LocalCancellationException();
                }
                zipArchives.add(zipArchive);
            }
            // send the big archive
            monitor.subTask("");

            sendArchive(zipArchive, peer, processID,
                monitor.newChild(50, SubMonitor.SUPPRESS_NONE));

            projectExchangeProcesses.removeProjectExchangeProcess(this);
        } catch (Exception e) {
            exception = e;
        } finally {
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

    /**
     * Build the list of files (with checksums, so it might take a while) that
     * should be present on the remote users computer to participate in the
     * session. The file lists are contained in the {@link ProjectExchangeInfo}
     * Objects for each Project, and wrapped in a {@link ProjectsAddedActivity}
     * that is finally sent to the user
     * 
     * @param monitor
     * @throws LocalCancellationException
     */
    private void sendFileList(SubMonitor monitor)
        throws LocalCancellationException {

        try {

            /*
             * FIXME must be calculated while the session is blocked !
             * 
             * FIXME display the remote side something that will it receive
             * something in the near future
             */
            List<ProjectExchangeInfo> projectExchangeInfos = createProjectExchangeInfoList(
                projects, monitor);

            if (monitor.isCanceled())
                throw new LocalCancellationException(null,
                    CancelOption.NOTIFY_PEER);

            monitor.subTask("");
            log.debug(this + " : Sending file list");
            monitor.setTaskName("Sending file list...");

            /*
             * For those who do not get the "magic". This activity is executed
             * in the remote SarosSession and handled by the ProjectAddedManager
             * which calls the SarosSessionManager which creates a
             * IncomingProjectNegotiation instance and pass it to the SarosUI
             * which finally opens the Wizard on the remote side
             */
            sarosSession.sendActivity(sarosSession.getUser(peer),
                new ProjectsAddedActivity(sarosSession.getLocalUser(),
                    projectExchangeInfos, processID));

        } finally {
            monitor.done();
        }

    }

    /**
     * Retrieve the peer's partial file list and remember which files need to be
     * sent to that user
     * 
     * @param monitor
     * @throws IOException
     * @throws SarosCancellationException
     */
    protected void getRemoteFileList(SubMonitor monitor) throws IOException,
        SarosCancellationException {

        log.debug(this + " : Waiting for remote file list");

        try {
            monitor.setTaskName("Waiting for " + peer.getName()
                + " to choose project(s) location");
            monitor.beginTask("Waiting for " + peer.getName()
                + " to choose project(s) location", 1);

            checkCancellation(CancelOption.NOTIFY_PEER);

            Packet packet = collectPacket(remoteFileListResponseCollector,
                60 * 60 * 1000, monitor);

            if (packet == null)
                throw new LocalCancellationException(
                    "received no response from " + peer
                        + " while waiting for the file list",
                    CancelOption.DO_NOT_NOTIFY_PEER);

            List<FileList> remoteFileLists = FileListExtension.PROVIDER
                .getPayload(packet).getFileLists();

            log.debug(this + " : Remote file list has been received");

            checkCancellation(CancelOption.NOTIFY_PEER);

            for (FileList fileList : remoteFileLists) {
                this.projectFilesToSend.put(fileList.getProjectID(),
                    fileList.getPaths());
                log.debug(fileList.toString());
            }
        } finally {
            monitor.done();
        }
    }

    @Override
    public Map<String, String> getProjectNames() {
        Map<String, String> result = new HashMap<String, String>();
        for (IProject project : this.projects) {
            result.put(sarosSession.getProjectID(project), project.getName());
        }
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

        projectExchangeProcesses.removeProjectExchangeProcess(this);
    }

    /**
     * 
     * @param projectFilesToSend
     *            projectID => List of {@link IPath files} that will be sent to
     *            peer
     * @return List of project archives
     */
    private List<File> createProjectArchives(
        MappedList<String, IPath> projectFilesToSend, SubMonitor monitor)
        throws IOException, SarosCancellationException {

        log.debug(this + " : Creating archive");
        checkCancellation(CancelOption.NOTIFY_PEER);

        monitor.beginTask("Creating archives...", projectFilesToSend.size());
        /*
         * STOP users
         * 
         * TODO: stop all users. If we stop users which are currently joining,
         * it can cause a deadlock, because the StopManager does not answer if
         * someone is already stopped.
         */
        Collection<User> usersToStop = new ArrayList<User>();
        for (User user : sarosSession.getParticipants()) {
            if (user.isInvitationComplete())
                usersToStop.add(user);
        }
        log.debug(this + " : Stopping users : " + usersToStop);
        // TODO: startHandles outside of sync block?
        List<StartHandle> startHandles;

        synchronized (sarosSession) {
            try {
                startHandles = sarosSession.getStopManager().stop(usersToStop,
                    "Synchronizing invitation",
                    monitor.newChild(0, SubMonitor.SUPPRESS_ALL_LABELS));
            } catch (CancellationException e) {
                checkCancellation(CancelOption.NOTIFY_PEER);
                return null;
            }
        }

        /*
         * Use editorManager.saveText() because the EditorAPI.saveProject() will
         * not save files which were modified in the background. This is what
         * happens for example if a user edits a file which is not opened by the
         * local user.
         * 
         * Stefan Rossbach: this will still fail if a user edited a file and
         * then closes the editor without saving it.
         */
        for (SPath path : editorManager.getOpenEditorsOfAllParticipants())
            editorManager.saveText(path);

        try {
            List<File> archivesToSend = new LinkedList<File>();

            for (Map.Entry<String, List<IPath>> entry : projectFilesToSend
                .entrySet()) {

                String projectID = entry.getKey();
                List<IPath> toSend = entry.getValue();

                File projectArchive = createProjectArchive(
                    monitor.newChild(1, SubMonitor.SUPPRESS_NONE), toSend,
                    projectID);

                if (projectArchive != null)
                    archivesToSend.add(projectArchive);

            }

            return archivesToSend;

        } finally {

            monitor.done();

            // START all users
            for (StartHandle startHandle : startHandles) {
                log.debug(this + " : Starting user "
                    + Utils.prefix(startHandle.getUser().getJID()));
                startHandle.start();
            }
        }
    }

    private File createProjectArchive(SubMonitor monitor, List<IPath> toSend,
        String projectID) throws IOException, SarosCancellationException {

        IProject project = sarosSession.getProject(projectID);
        log.debug("Got project from session");
        /*
         * TODO: Ask the user whether to save the resources, but only if they
         * have changed. How to ask Eclipse whether there are resource changes?
         * if (outInvitationUI.confirmProjectSave(peer)) getOpenEditors =>
         * filter per Project => if dirty ask to save
         */
        EditorAPI.saveProject(project, false);

        String prefix = projectID + this.projectIDDelimiter;
        if (project == null) {
            log.debug(projectID + ": is null");
        }

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
        } finally {
            monitor.done();
        }
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
                sessionID.getValue(), processID));
    }

    private void deleteCollectors() {
        remoteFileListResponseCollector.cancel();
    }

    private void sendArchive(File archive, JID remoteContact,
        String transferID, IProgressMonitor monitor)
        throws SarosCancellationException, IOException {

        if (archive == null) {
            log.debug(this + " : The archive is empty.");
            monitor.done();
            return;
        }

        monitor.beginTask("Sending archive file...", 100);

        log.debug(this + " : Sending archive...");

        assert fileTransferManager != null;

        try {
            OutgoingFileTransfer transfer = fileTransferManager
                .createOutgoingFileTransfer(remoteContact.toString());

            transfer.sendFile(archive, transferID);
            monitorFileTransfer(transfer, monitor);
        } catch (XMPPException e) {
            throw new IOException(e.getMessage(), e.getCause());
        } finally {
            monitor.done();
        }

        log.debug(this + " : Archive send.");
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
        List<IProject> projectsToShare, SubMonitor monitor)
        throws LocalCancellationException {

        monitor.beginTask(Messages.SarosSessionManager_creating_file_list,
            projectsToShare.size());

        List<ProjectExchangeInfo> pInfos = new ArrayList<ProjectExchangeInfo>(
            projectsToShare.size());

        for (IProject iProject : projectsToShare) {
            if (monitor.isCanceled())
                throw new LocalCancellationException(null,
                    CancelOption.DO_NOT_NOTIFY_PEER);
            try {
                String projectID = sarosSession.getProjectID(iProject);
                String projectName = iProject.getName();

                FileList projectFileList = FileListFactory.createFileList(
                    iProject, sarosSession.getSharedResources(iProject),
                    checksumCache, useVersionControl,
                    monitor.newChild(100 / projectsToShare.size()));

                projectFileList.setProjectID(projectID);
                boolean partial = !sarosSession.isCompletelyShared(iProject);

                ProjectExchangeInfo pInfo = new ProjectExchangeInfo(projectID,
                    "", projectName, partial, projectFileList);

                pInfos.add(pInfo);

            } catch (CoreException e) {
                throw new LocalCancellationException(e.getMessage(),
                    CancelOption.DO_NOT_NOTIFY_PEER);
            }
        }
        return pInfos;
    }

    @Override
    public String toString() {
        return "OPN [remote side: " + peer + "]";
    }
}
