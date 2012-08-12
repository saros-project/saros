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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.FileListFactory;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.activities.business.ProjectsAddedActivity;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.IChecksumCache;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.util.FileZipper;
import de.fu_berlin.inf.dpp.util.MappedList;
import de.fu_berlin.inf.dpp.util.Utils;

public class OutgoingProjectNegotiation extends ProjectNegotiation {

    private static Logger log = Logger
        .getLogger(OutgoingProjectNegotiation.class);

    /**
     * this maps the currently exchanging projects. projectID => project in
     * workspace
     */
    protected List<IProject> projects;

    protected ISarosSession sarosSession;

    protected AtomicBoolean cancelled = new AtomicBoolean(false);
    protected SubMonitor monitor;
    protected SarosCancellationException cancellationCause;
    /**
     * projectID => List of {@link IPath files} that will be send to peer
     */
    protected MappedList<String, IPath> projectFilesToSend = new MappedList<String, IPath>();

    @Inject
    protected SessionIDObservable sessionID;
    protected final static Random INVITATION_RAND = new Random();

    protected boolean useVersionControl;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected XMPPReceiver xmppReceiver;

    @Inject
    protected IChecksumCache checksumCache;

    protected Map<IProject, List<IResource>> projectResources;
    protected List<ProjectExchangeInfo> projectExchangeInfos;

    protected SarosPacketCollector remoteFileListResponseCollector;

    public OutgoingProjectNegotiation(JID to, ISarosSession sarosSession,
        Map<IProject, List<IResource>> partialResources,
        SarosContext sarosContext,
        List<ProjectExchangeInfo> projectExchangeInfos) {
        super(to, sarosContext);

        this.processID = String.valueOf(INVITATION_RAND.nextLong());
        this.sarosSession = sarosSession;

        this.projectResources = partialResources;
        this.projects = new ArrayList<IProject>(projectResources.keySet());
        this.projectExchangeInfos = projectExchangeInfos;
    }

    public void start(SubMonitor monitor) throws SarosCancellationException {
        this.monitor = monitor;

        createCollectors();
        File zipArchive = null;

        List<File> zipArchives = new ArrayList<File>();

        this.monitor.beginTask(
            "Retrieving list of files needed for synchronization...", 100);

        try {
            sendFileList(monitor.newChild(0));
            monitor.subTask("");
            getRemoteFileList(monitor.newChild(0));
            monitor.subTask("");
            editorManager.setAllLocalOpenedEditorsLocked(false);
            // pack each project into one archive and check if it was
            // cancelled.
            zipArchives = createProjectArchives(monitor.newChild(30),
                this.projectFilesToSend);
            checkCancellation(CancelOption.NOTIFY_PEER);
            if (zipArchives.size() > 0) {

                // pack all archive files into one big archive
                zipArchive = File.createTempFile("SarosSyncArchive", ".zip");
                FileZipper.zipFiles(zipArchives, zipArchive, false,
                    monitor.newChild(20));

                zipArchives.add(zipArchive);
            }
            // send the big archive
            monitor.subTask("");

            sendArchive(monitor.newChild(50), zipArchive, processID);

            projectExchangeProcesses.removeProjectExchangeProcess(this);
        } catch (IOException e) {
            String errorMsg = "Unknown error: " + e;
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (SarosCancellationException cancel) {
            localCancel("", CancelOption.NOTIFY_PEER);
            executeCancellation();
        } finally {
            for (File archive : zipArchives) {
                if (!archive.delete())
                    log.warn("could not archive file: "
                        + archive.getAbsolutePath());
            }
            deleteCollectors();
            monitor.done();
        }
    }

    /**
     * Build the list of files (with checksums, so it might take a while) that
     * should be present on the remote users computer to participate in the
     * session. The filelists are contained in the {@link ProjectExchangeInfo}
     * Objects for each Project, and wrapped in a {@link ProjectsAddedActivity}
     * that is finally sent to the user
     * 
     * @param monitor
     * @throws LocalCancellationException
     */
    private void sendFileList(SubMonitor monitor)
        throws LocalCancellationException {

        try {

            if (this.projectExchangeInfos == null) {

                monitor.beginTask("Creating file lists with checksums",
                    projects.size());

                useVersionControl = sarosSession.useVersionControl();
                projectExchangeInfos = new ArrayList<ProjectExchangeInfo>(
                    this.projects.size());

                for (IProject iProject : this.projects) {
                    if (monitor.isCanceled())
                        break;

                    String projectID = sarosSession.getProjectID(iProject);
                    String projectName = iProject.getName();

                    /*
                     * Create FileList involves computing checksums for all
                     * files so expect this to take a while when large files are
                     * included...
                     */
                    if (this.projectResources.get(iProject) != null) {
                        monitor.subTask("Listung & Hashing "
                            + this.projectResources.get(iProject).size()
                            + " files in project " + iProject.getName());
                    } else {
                        monitor.subTask("Listung & Hashing files in project "
                            + iProject.getName());
                    }
                    FileList projectFileList = FileListFactory.createFileList(
                        iProject, projectResources.get(iProject),
                        checksumCache, useVersionControl, monitor.newChild(1));

                    if (monitor.isCanceled())
                        break;

                    projectFileList.setProjectID(projectID);

                    boolean partial = !sarosSession
                        .isCompletelyShared(iProject);

                    ProjectExchangeInfo pInfo = new ProjectExchangeInfo(
                        projectID, "", projectName, partial, projectFileList);

                    projectExchangeInfos.add(pInfo);
                }
            }

            if (monitor.isCanceled())
                throw new LocalCancellationException(null,
                    CancelOption.NOTIFY_PEER);

            monitor.subTask("");
            log.debug("Inv" + Utils.prefix(peer) + ": Sending file list...");
            monitor.setTaskName("Sending file list...");

            sarosSession.sendActivity(sarosSession.getUser(peer),
                new ProjectsAddedActivity(sarosSession.getLocalUser(),
                    projectExchangeInfos, processID));

        } catch (CoreException e) {
            throw new LocalCancellationException(e.getMessage(),
                CancelOption.NOTIFY_PEER);
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

        log.debug("Inv" + Utils.prefix(peer)
            + ": Waiting for remote file list...");

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

            List<FileList> remoteFileLists = deserializeRemoteFileList(packet,
                monitor);

            log.debug("Inv" + Utils.prefix(peer)
                + ": Remote file list has been received.");

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

    /**
     * Checks whether the invitation process or the monitor has been canceled.
     * If the monitor has been canceled but the invitation process has not yet,
     * it cancels the invitation process.
     * 
     * @throws SarosCancellationException
     *             if the invitation process or the monitor has already been
     *             canceled.
     */
    protected void checkCancellation(CancelOption cancelOption)
        throws SarosCancellationException {
        if (cancelled.get()) {
            log.debug("Inv" + Utils.prefix(peer) + ": Cancellation checkpoint");
            throw new SarosCancellationException();
        }

        if (monitor == null) {
            log.warn("Inv" + Utils.prefix(peer) + ": The monitor is null.");
            return;
        }

        if (monitor.isCanceled()) {
            log.debug("Inv" + Utils.prefix(peer) + ": Cancellation checkpoint");
            localCancel(null, cancelOption);
            throw new SarosCancellationException();
        }
    }

    /**
     * This method does <strong>not</strong> execute the cancellation but only
     * sets the {@link #cancellationCause}. It should be called if the
     * cancellation was initiated by the <strong>local</strong> user. The
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
        log.debug("Inv" + Utils.prefix(peer) + ": localCancel: " + errorMsg);
        if (monitor != null)
            monitor.setCanceled(true);
        cancellationCause = new LocalCancellationException(errorMsg,
            cancelOption);
    }

    /**
     * Cancels the invitation process based on the exception stored in
     * {@link #cancellationCause}. This method is always called by this local
     * object, so even if an another object "cancels" the invitation (
     * {@link #localCancel(String, CancelOption)}, {@link #remoteCancel(String)}
     * ), the exceptions will be thrown up on the stack to the caller of
     * {@link #start(SubMonitor)}, and not to the object which has "cancelled"
     * the process. The cancel methods (
     * {@link #localCancel(String, CancelOption)}, {@link #remoteCancel(String)}
     * ) do not cancel the invitation alone, but they set the
     * {@link #cancellationCause} and cancel the {@link #monitor}. Now it is the
     * responsibility of the objects which use the {@link #monitor} to throw a
     * {@link SarosCancellationException} (or it's subclasses), which will be
     * caught by this object causing a call to this method. If this does not
     * happen, the next {@link #checkCancellation(CancelOption)} cancels the
     * invitation.
     */
    protected void executeCancellation() throws SarosCancellationException {

        log.debug("Inv" + Utils.prefix(peer) + ": executeCancellation");
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
                log.warn("Inv" + Utils.prefix(peer)
                    + ": This case is not expected here.");
            }

            if (errorMsg != null) {
                cancelMessage = "Invitation was cancelled locally"
                    + " because of an error: " + errorMsg;
                log.error("Inv" + Utils.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation failed. (" + errorMsg + ")");
            } else {
                cancelMessage = "Invitation was cancelled by local user.";
                log.debug("Inv" + Utils.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation has been cancelled.");
            }

        } else if (cancellationCause instanceof RemoteCancellationException) {
            RemoteCancellationException e = (RemoteCancellationException) cancellationCause;

            errorMsg = e.getMessage();
            if (errorMsg != null) {
                cancelMessage = "Invitation was cancelled by the remote user "
                    + " because of an error on his/her side: " + errorMsg;
                log.error("Inv" + Utils.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation failed.");
            } else {
                cancelMessage = "Invitation was cancelled by the remote user.";
                log.debug("Inv" + Utils.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation has been cancelled.");
            }
        } else {
            log.error("This type of exception is not expected here: ",
                cancellationCause);
            monitor.setTaskName("Invitation failed.");
        }

        if (sarosSession.getRemoteUsers().isEmpty())
            sessionManager.stopSarosSession();

        projectExchangeProcesses.removeProjectExchangeProcess(this);
        throw cancellationCause;
    }

    /**
     * This method does <strong>not</strong> execute the cancellation but only
     * sets the {@link #cancellationCause}. It should be called if the
     * cancellation was initiated by the <strong>remote</strong> user. The
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
     */
    @Override
    public void remoteCancel(String errorMsg) {
        if (!cancelled.compareAndSet(false, true))
            return;
        log.debug("Inv" + Utils.prefix(peer) + ": remoteCancel: " + errorMsg);
        if (monitor != null)
            monitor.setCanceled(true);
        cancellationCause = new RemoteCancellationException(errorMsg);
    }

    /**
     * 
     * @param projectFilesToSend
     *            projectID => List of {@link IPath files} that will be sent to
     *            peer
     * @return List of project archives
     */
    protected List<File> createProjectArchives(SubMonitor monitor,
        MappedList<String, IPath> projectFilesToSend) throws IOException,
        SarosCancellationException {

        log.debug("Inv" + Utils.prefix(peer) + ": Creating archive...");
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
        log.debug("Inv" + Utils.prefix(peer) + ": Stopping users: "
            + usersToStop);
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

        try {

            List<File> archivesToSend = new LinkedList<File>();

            for (Map.Entry<String, List<IPath>> entry : projectFilesToSend
                .entrySet()) {

                String projectID = entry.getKey();
                List<IPath> toSend = entry.getValue();

                File projectArchive = createProjectArchive(
                    monitor.newChild(1, SubMonitor.SUPPRESS_NONE), toSend,
                    projectID);

                if (projectArchive != null) {
                    archivesToSend.add(projectArchive);
                }
            }

            return archivesToSend;

        } finally {

            monitor.done();

            // START all users
            for (StartHandle startHandle : startHandles) {
                log.debug("Inv" + Utils.prefix(peer) + ": Starting user "
                    + Utils.prefix(startHandle.getUser().getJID()));
                startHandle.start();
            }
        }
    }

    protected void sendArchive(SubMonitor monitor, File archive,
        String projectID) throws SarosCancellationException, IOException {

        monitor.beginTask("Sending archive...", 100);
        monitor.setTaskName("Sending archive...");

        log.debug("Inv" + Utils.prefix(peer) + ": Sending archive...");

        if (archive == null) {
            log.debug("Inv" + Utils.prefix(peer) + ": The archive is empty.");
            monitor.done();
            return;
        }

        try {
            transmitter.sendProjectArchive(peer, projectID, archive,
                monitor.newChild(100, SubMonitor.SUPPRESS_NONE));
        } finally {
            monitor.done();
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
                FileZipper.createProjectZipArchive(toSend, tempArchive,
                    project, monitor);
            }
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
            .createCollector(PacketExtensionUtils.getIncomingFileListFilter(
                new IncomingTransferObjectExtensionProvider(),
                sessionID.getValue(), processID, peer));
    }

    private void deleteCollectors() {
        remoteFileListResponseCollector.cancel();
    }

    private List<FileList> deserializeRemoteFileList(Packet packet,
        SubMonitor monitor) throws SarosCancellationException, IOException {
        IncomingTransferObject result = new IncomingTransferObjectExtensionProvider()
            .getPayload(packet);

        if (monitor.isCanceled()) {
            result.reject();
            throw new LocalCancellationException();
        }

        String fileListAsString = new String(result.accept(monitor), "UTF-8");

        // We disassemble the complete fileListString to an array of
        // fileListStrings...
        String[] fileListStrings = fileListAsString.split("---next---");

        List<FileList> fileLists = new ArrayList<FileList>();

        // and make a new FileList out of each XML-String
        for (int i = 0; i < fileListStrings.length; i++) {
            FileList fileList = FileList.fromXML(fileListStrings[i]);
            if (fileList != null) {
                fileLists.add(fileList);
            }
        }

        return fileLists;
    }
}
