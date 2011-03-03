package de.fu_berlin.inf.dpp.invitation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.fu_berlin.inf.dpp.SarosContext;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.activities.business.ProjectsAddedActivity;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.StreamService;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
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
    protected Map<String, IProject> projects;
    protected boolean doStream;

    protected ISarosSession sarosSession;

    protected AtomicBoolean cancelled = new AtomicBoolean(false);
    protected SubMonitor monitor;
    protected SarosCancellationException cancellationCause;
    /**
     * projectID => List of {@link IPath files} that will be send to peer
     */
    protected MappedList<String, IPath> projectFilesToSend = new MappedList<String, IPath>();
    protected StopManager stopManager;
    protected SessionIDObservable sessionID;
    protected final static Random INVITATION_RAND = new Random();

    protected boolean useVersionControl;

    public OutgoingProjectNegotiation(ITransmitter transmitter, JID to,
        ISarosSession sarosSession, List<IProject> projects,
        ProjectNegotiationObservable projectExchangeProcesses,
        StopManager stopManager, SessionIDObservable sessionID, boolean doStream, SarosContext sarosContext) {
        super(transmitter, to, projectExchangeProcesses, sarosContext);

        this.processID = String.valueOf(INVITATION_RAND.nextLong());
        this.projectExchangeProcesses = projectExchangeProcesses;
        this.doStream = doStream;
        this.sarosSession = sarosSession;
        this.sessionID = sessionID;
        this.stopManager = stopManager;
        this.projects = new HashMap<String, IProject>();
        for (IProject project : projects) {
            this.projects.put(sarosSession.getProjectID(project), project);
        }

    }

    public void start(SubMonitor monitor) throws SarosCancellationException {
        this.monitor = monitor;
        sendFileList(monitor.newChild(1));
        try {
            getRemoteFileList(monitor.newChild(2));
            if (doStream) {
                streamArchive(monitor.newChild(97));
            } else {
                // pack each project into one archive
                List<File> projectArchives = createProjectArchives(
                    monitor.newChild(8), this.projectFilesToSend);
                File zipArchive = null;
                if (projectArchives.size() > 0) {
                    // pack all archive files into one big archive
                    zipArchive = File
                        .createTempFile("SarosSyncArchive", ".zip");
                    FileZipper.zipFiles(projectArchives, zipArchive,
                        monitor.newChild(2));
                }
                // send the big archive
                sendArchive(monitor.newChild(87), zipArchive, processID);

                this.projectExchangeProcesses
                    .removeProjectExchangeProcess(this);
            }
        } catch (IOException e) {
            String errorMsg = "Unknown error: " + e;
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
            executeCancellation();
        } finally {
            monitor.done();
        }
    }

    private void sendFileList(SubMonitor subMonitor)
        throws LocalCancellationException {

        subMonitor.setTaskName("Creating file list...");
        subMonitor.worked(50);
        useVersionControl = sarosSession.useVersionControl();
        List<ProjectExchangeInfo> pInfos = new ArrayList<ProjectExchangeInfo>(
            this.projects.size());
        try {
            for (IProject project : projects.values()) {
                FileList fileList = new FileList(project, useVersionControl);
                fileList.setProjectID(sarosSession.getProjectID(project));
                ProjectExchangeInfo pInfo = new ProjectExchangeInfo(
                    sarosSession.getProjectID(project), "", project.getName(),
                    fileList);
                pInfos.add(pInfo);
            }
        } catch (CoreException e) {
            throw new LocalCancellationException(e.getMessage(),
                CancelOption.NOTIFY_PEER);
        }
        log.debug("Inv" + Utils.prefix(peer) + ": Sending file list...");
        subMonitor.setTaskName("Sending file list...");
        this.sarosSession.sendActivity(sarosSession.getUser(peer),
            new ProjectsAddedActivity(sarosSession.getLocalUser(), pInfos,
                processID, this.doStream));
        subMonitor.done();

    }

    /**
     * Create and send the complete file list, then wait for the peer's partial
     * file list.
     * 
     * @param subMonitor
     * @throws IOException
     * @throws SarosCancellationException
     */
    protected void getRemoteFileList(SubMonitor subMonitor) throws IOException,
        SarosCancellationException {

        checkCancellation(CancelOption.NOTIFY_PEER);

        subMonitor.setWorkRemaining(100);

        checkCancellation(CancelOption.NOTIFY_PEER);

        log.debug("Inv" + Utils.prefix(peer)
            + ": Waiting for remote file list...");

        subMonitor.setTaskName("Waiting for the peer's file list...");

        checkCancellation(CancelOption.NOTIFY_PEER);

        List<FileList> remoteFileLists = transmitter.receiveFileLists(
            processID, peer, subMonitor, true);
        log.debug("Inv" + Utils.prefix(peer)
            + ": Remote file list has been received.");

        checkCancellation(CancelOption.NOTIFY_PEER);
        for (FileList fileList : remoteFileLists) {
            this.projectFilesToSend.put(fileList.getProjectID(),
                fileList.getPaths());
            log.debug(fileList.toString());
        }

        // toSend.addAll(remoteFileLists.getPaths());
    }

    @Override
    public Map<String, String> getProjectNames() {
        Map<String, String> result = new HashMap<String, String>();
        for (IProject project : this.projects.values()) {
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
     * cancellation was initated by the <strong>local</strong> user. The
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
        projectExchangeProcesses.removeProjectExchangeProcess(this);
        throw cancellationCause;
    }

    protected void streamArchive(SubMonitor subMonitor)
        throws SarosCancellationException {
        checkCancellation(CancelOption.NOTIFY_PEER);

        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Creating archive...");

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
            startHandles = stopManager.stop(usersToStop,
                "Synchronizing invitation",
                subMonitor.newChild(25, SubMonitor.SUPPRESS_ALL_LABELS));
        }

        try {
            // TODO: Ask the user whether to save the resources, but only if
            // they have changed. How to ask Eclipse whether there are resource
            // changes?
            // if (outInvitationUI.confirmProjectSave(peer))
            for (IProject project : projects.values()) {
                EditorAPI.saveProject(project, false);
            }
            // else
            // throw new LocalCancellationException();

            // TODO: User is not needed here. it is just handed through to the
            // point where user.getJID() is called
            int numberOfFilesToSend = 0;
            for (List<IPath> l : this.projectFilesToSend.values()) {
                numberOfFilesToSend += l.size();
            }
            streamSession = streamServiceManager.createSession(
                archiveStreamService, sarosSession.getUser(this.peer),
                new Integer(numberOfFilesToSend), sessionListener);

            streamSession.setListener(sessionListener);

            subMonitor.setTaskName("Streaming archive...");

            performFileStream(archiveStreamService, streamSession,
                subMonitor.newChild(75), numberOfFilesToSend);

        } catch (Exception e) {
            log.error("Error while executing archive stream: ", e);
        } finally {
            // START all users
            for (StartHandle startHandle : startHandles) {
                log.debug("Inv" + Utils.prefix(peer) + ": Starting user "
                    + Utils.prefix(startHandle.getUser().getJID()));
                startHandle.start();
            }
            this.projectExchangeProcesses.removeProjectExchangeProcess(this);
        }
    }

    public FileList getRemoteFileList() {
        return null;
    }

    /**
     * This method does <strong>not</strong> execute the cancellation but only
     * sets the {@link #cancellationCause}. It should be called if the
     * cancellation was initated by the <strong>remote</strong> user. The
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

    private void performFileStream(StreamService streamService,
        final StreamSession session, SubMonitor subMonitor,
        int numberOfFilesToSend) throws SarosCancellationException {

        OutputStream output = session.getOutputStream(0);
        ZipOutputStream zout = new ZipOutputStream(output);
        int worked = 0;
        int lastWorked = 0;
        int filesSent = 0;
        double increment = 0.0;

        if (numberOfFilesToSend >= 1) {
            increment = (double) 100 / numberOfFilesToSend;
            subMonitor.beginTask("Streaming files...", 100);
        } else {
            subMonitor.worked(100);
        }

        try {
            for (String projectID : this.projectFilesToSend.keySet()) {
                List<IPath> toSend = this.projectFilesToSend.get(projectID);
                zout = new ZipOutputStream(output);
                for (IPath path : toSend) {
                    IFile file = this.projects.get(projectID).getFile(path);
                    String absPath = file.getLocation().toPortableString();

                    byte[] buffer = new byte[streamService.getChunkSize()[0]];
                    InputStream input = new FileInputStream(absPath);

                    ZipEntry ze = new ZipEntry(path.toPortableString());
                    ze.setExtra(projectID.getBytes());
                    zout.putNextEntry(ze);

                    int numRead = 0;
                    while ((numRead = input.read(buffer)) > 0) {
                        zout.write(buffer, 0, numRead);
                    }
                    input.close();
                    zout.flush();
                    zout.closeEntry();

                    // Progress monitor
                    worked = (int) Math.round(increment * filesSent);

                    if ((worked - lastWorked) > 0) {
                        subMonitor.worked((worked - lastWorked));
                        lastWorked = worked;
                    }

                    filesSent++;

                    checkCancellation(CancelOption.NOTIFY_PEER);
                }
            }
        } catch (IOException e) {
            error = true;
            log.error("Error while sending file: ", e);
            localCancel(
                "An I/O problem occurred while the project's files were being sent: \""
                    + e.getMessage() + "\" The invitation was cancelled.",
                CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (SarosCancellationException e) {
            log.debug("Invitation process was cancelled.");
        } catch (Exception e) {
            log.error("Unknown exception: ", e);
        } finally {
            try {
                if (filesSent >= 1)
                    zout.finish();
            } catch (IOException e) {
                log.warn("IOException occurred when finishing the ZipOutputStream.");
            }
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(zout);
        }
        subMonitor.done();
    }

    /**
     * 
     * @param projectFilesToSend
     *            projectID => List of {@link IPath files} that will be sent to
     *            peer
     * @return List of project archives
     */
    protected List<File> createProjectArchives(SubMonitor subMonitor,
        MappedList<String, IPath> projectFilesToSend) throws IOException,
        SarosCancellationException {

        log.debug("Inv" + Utils.prefix(peer) + ": Creating archive...");
        checkCancellation(CancelOption.NOTIFY_PEER);

        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Creating archive...");

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
            startHandles = stopManager.stop(usersToStop,
                "Synchronizing invitation",
                subMonitor.newChild(25, SubMonitor.SUPPRESS_ALL_LABELS));
        }

        try {
            SubMonitor archiveMonitor = subMonitor.newChild(50,
                SubMonitor.SUPPRESS_ALL_LABELS);
            List<File> archivesToSend = new LinkedList<File>();
            for (String projectID : projectFilesToSend.keySet()) {
                List<IPath> toSend = projectFilesToSend.get(projectID);
                File projectArchive = createProjectArchive(archiveMonitor,
                    toSend, projectID);
                if (projectArchive != null) {
                    archivesToSend.add(projectArchive);
                }
            }
            subMonitor.worked(25);
            return archivesToSend;
        } finally {
            // START all users
            for (StartHandle startHandle : startHandles) {
                log.debug("Inv" + Utils.prefix(peer) + ": Starting user "
                    + Utils.prefix(startHandle.getUser().getJID()));
                startHandle.start();
            }
        }
    }

    protected void sendArchive(SubMonitor subMonitor, File archive,
        String projectID) throws SarosCancellationException, IOException {

        subMonitor.setWorkRemaining(100);

        log.debug("Inv" + Utils.prefix(peer) + ": Sending archive...");
        subMonitor.setTaskName("Sending archive...");
        if (archive == null)
            log.debug("Inv" + Utils.prefix(peer) + ": The archive is empty.");
        transmitter.sendProjectArchive(peer, projectID, archive,
            subMonitor.newChild(100, SubMonitor.SUPPRESS_ALL_LABELS));
    }

    private File createProjectArchive(SubMonitor archiveMonitor,
        List<IPath> toSend, String projectID) throws IOException,
        SarosCancellationException {
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
        if (toSend.size() > 0) {

            tempArchive = File.createTempFile(prefix, ".zip");
            FileZipper.createProjectZipArchive(toSend, tempArchive, project,
                archiveMonitor);
        }

        archiveMonitor.done();
        return tempArchive;
    }
}
