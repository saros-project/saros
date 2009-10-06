/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.invitation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.UserCancellationException;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * An incoming invitation process.
 * 
 * TODO Use {@link WorkspaceModifyOperation}s to wrap the whole invitation
 * process, so that background activityDataObjects such as autoBuilding do not
 * interfere with the InvitationProcess
 * 
 */
public class IncomingInvitationProcess extends InvitationProcess {

    private static Logger log = Logger
        .getLogger(IncomingInvitationProcess.class);
    protected FileList remoteFileList;
    protected IProject localProject;
    protected int filesLeftToSynchronize;
    protected SubMonitor monitor;
    protected String projectName;
    protected SessionManager sessionManager;
    protected IIncomingInvitationUI inInvitationUI;
    protected VersionManager versionManager;
    protected SarosUI sarosUI;
    protected DateTime sessionStart;
    protected ISharedProject sharedProject;
    protected String invitationID;
    /**
     * {@link VersionInfo#compatibility} applies to our client and
     * {@link VersionInfo#version} is the version of the host.
     */
    public VersionInfo versionInfo;
    protected AtomicBoolean cancelled = new AtomicBoolean(false);

    public IncomingInvitationProcess(SessionManager sessionManager,
        ITransmitter transmitter, JID from, String projectName,
        String description, int colorID,
        InvitationProcessObservable invitationProcesses,
        VersionManager versionManager, VersionInfo remoteVersionInfo,
        DateTime sessionStart, SarosUI sarosUI, String invitationID) {

        super(transmitter, from, description, colorID, invitationProcesses);

        this.sessionManager = sessionManager;
        this.projectName = projectName;
        this.versionManager = versionManager;
        this.versionInfo = determineVersion(remoteVersionInfo);
        this.sessionStart = sessionStart;
        this.sarosUI = sarosUI;
        this.invitationID = invitationID;
        setState(State.INVITATION_SENT);
    }

    protected VersionInfo determineVersion(VersionInfo remoteVersionInfo) {
        VersionInfo ultimateVI = remoteVersionInfo;

        // The host could not determine the compatibility, so we do it.
        if (ultimateVI.compatibility == null) {
            ultimateVI.compatibility = versionManager
                .determineCompatibility(ultimateVI.version.toString());
            return ultimateVI;
        }

        // Invert the compatibility information so it applies to our client.
        ultimateVI.compatibility = ultimateVI.compatibility.invert();

        return ultimateVI;
    }

    public void start() {
        invitationReceived();
    }

    protected void invitationReceived() {
        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                inInvitationUI = sarosUI
                    .showIncomingInvitationUI(IncomingInvitationProcess.this);
                sarosUI.openSarosViews();
            }
        });
    }

    public void requestRemoteFileList(SubMonitor monitor) {
        if (checkCancellation()) {
            log.debug("Inv" + Util.prefix(peer) + ": Cancellation checkpoint");
            return;
        }

        assertState(State.INVITATION_SENT);
        // The first monitor we use during the invitation.
        this.monitor = monitor;
        monitor.beginTask("Acquiring remote file list", 100);

        try {
            monitor.subTask("Initializing Jingle...");
            transmitter.awaitJingleManager(peer);
            monitor.worked(5);

            if (checkCancellation()) {
                log.debug("Inv" + Util.prefix(peer)
                    + ": Cancellation checkpoint");
                return;
            }

            monitor.subTask("Sending request...");
            setState(State.HOST_FILELIST_REQUESTED);

            SarosPacketCollector fileListCollector = transmitter
                .getInvitationCollector(invitationID,
                    FileTransferType.FILELIST_TRANSFER);

            transmitter.sendFileListRequest(peer, invitationID);
            log.debug("Inv" + Util.prefix(peer)
                + ": Request for FileList sent.");

            remoteFileList = transmitter.receiveFileList(fileListCollector,
                monitor.newChild(85, SubMonitor.SUPPRESS_ALL_LABELS), true);

            monitor.worked(10);

            if (checkCancellation()) {
                log.debug("Inv" + Util.prefix(peer)
                    + ": Cancellation checkpoint");
                return;
            }

            setState(State.HOST_FILELIST_SENT);
            log.debug("Inv" + Util.prefix(peer) + ": Received FileList.");

        } catch (LocalCancellationException e) {
            cancel(null, CancelLocation.LOCAL, CancelOption.NOTIFY_PEER);
        } catch (UserCancellationException e) {
            // This is essentially a remote cancellation exception
            // We will be canceled via a cancellation extension
        } catch (IOException e) {
            cancel(null, CancelLocation.LOCAL, CancelOption.NOTIFY_PEER);
        } finally {
            // If we do not clear the subTaskName, the next wizard page shows is
            // for a second. Why?
            monitor.subTask("");
            monitor.done();
        }
    }

    public void accept(IProject baseProject, String newProjectName,
        boolean skipSync, SubMonitor monitor) {
        // The second monitor we use during the invitation.
        this.monitor = monitor;

        if ((newProjectName == null) && (baseProject == null)) {
            throw new IllegalArgumentException(
                "At least newProjectName or baseProject must not be null.");
        }

        /*
         * Disable "auto building" while we receive files, because it confuses
         * the invitation process and might cause inconsistent states with
         * regards to file lists
         */
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc = ws.getDescription();
        boolean wasAutobuilding = desc.isAutoBuilding();

        try {
            if (wasAutobuilding) {
                desc.setAutoBuilding(false);
                ws.setDescription(desc);
            }
            acceptUnsafe(baseProject, newProjectName, skipSync);
        } catch (CancellationException uce) {
            cancel(null, CancelLocation.LOCAL, CancelOption.NOTIFY_PEER);
        } catch (IOException e) {
            cancel(null, CancelLocation.LOCAL, CancelOption.NOTIFY_PEER);
        } catch (CoreException e) {
            cancel(null, CancelLocation.LOCAL, CancelOption.NOTIFY_PEER);
        } catch (Exception e) {
            cancel(e.getMessage(), CancelLocation.LOCAL,
                CancelOption.NOTIFY_PEER);
        } finally {
            // Re-enable auto-building...
            if (wasAutobuilding) {
                desc.setAutoBuilding(true);
                try {
                    ws.setDescription(desc);
                } catch (CoreException e) {
                    cancel("An error occurred while synchronising the project",
                        CancelLocation.LOCAL, CancelOption.NOTIFY_PEER);
                }
            }
            monitor.done();
        }
    }

    protected void acceptUnsafe(final IProject baseProject,
        final String newProjectName, boolean skipSync) throws Exception {
        assertState(State.HOST_FILELIST_SENT);
        // If a base project is given, save it
        if (baseProject != null) {
            if (!EditorAPI.saveProject(baseProject)) {
                // User cancelled saving the source project
                throw new Exception("User cancelled saving the source project.");
            }
        }

        if (newProjectName != null) {
            this.localProject = Util.runSWTSync(new Callable<IProject>() {
                public IProject call() throws CoreException,
                    InterruptedException {
                    try {
                        return createNewProject(newProjectName, baseProject);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            });
        } else {
            this.localProject = baseProject;
        }

        // TODO joining the session will already send events, which will be
        // rejected by our peers, because they don't know us yet (JoinMessage is
        // send only later)
        sharedProject = sessionManager.joinSession(this.localProject, peer,
            colorID, sessionStart);

        monitor.beginTask("Synchronizing", 100);
        setState(State.SYNCHRONIZING);

        FileList filesToSynchronize;
        if (skipSync) {
            filesToSynchronize = new FileList();
        } else {
            monitor.subTask("Preparing project for synchronisation...");
            filesToSynchronize = handleDiff(this.localProject,
                this.remoteFileList, monitor.newChild(5,
                    SubMonitor.SUPPRESS_ALL_LABELS));
        }
        filesLeftToSynchronize = filesToSynchronize.getAddedPaths().size()
            + filesToSynchronize.getAlteredPaths().size();

        if (filesLeftToSynchronize < 1) {
            log.debug("Inv" + Util.prefix(peer)
                + ": There are no files to synchronize.");
            // We send an empty file list to the host as a notification that we
            // do not need any files. The host does not answer, so we have to
            // skip the archive receiving part.
            transmitter.sendFileList(peer, invitationID, filesToSynchronize,
                monitor.newChild(10, SubMonitor.SUPPRESS_ALL_LABELS));
        } else {
            monitor.subTask("Sending file list...");

            SarosPacketCollector archiveCollector = transmitter
                .getInvitationCollector(invitationID,
                    FileTransferType.ARCHIVE_TRANSFER);

            transmitter.sendFileList(peer, invitationID, filesToSynchronize,
                monitor.newChild(10, SubMonitor.SUPPRESS_ALL_LABELS));

            if (checkCancellation()) {
                log.debug("Inv" + Util.prefix(peer)
                    + ": Cancellation checkpoint");
                return;
            }

            monitor.subTask("Receiving archive...");

            InputStream archiveStream = transmitter.receiveArchive(
                archiveCollector, monitor.newChild(75,
                    SubMonitor.SUPPRESS_ALL_LABELS), true);

            if (checkCancellation()) {
                log.debug("Inv" + Util.prefix(peer)
                    + ": Cancellation checkpoint");
                return;
            }

            monitor.subTask("Extracting archive...");

            writeArchive(archiveStream, localProject, monitor.newChild(10,
                SubMonitor.SUPPRESS_ALL_LABELS));

            log.debug("Inv" + Util.prefix(peer)
                + ": Archive received and written to disk...");
        }
        done();
    }

    /**
     * Have a look at the description of {@link WorkspaceModifyOperation}!
     * 
     * @see WorkspaceModifyOperation
     */
    protected void writeArchive(final InputStream archiveStream,
        final IProject project, final SubMonitor subMonitor)
        throws CoreException {
        ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                FileUtil.writeArchive(archiveStream, project, subMonitor);
            }
        }, subMonitor);
    }

    /**
     * Creates a new project.
     * 
     * @param newProjectName
     *            the project name of the new project.
     * @param baseProject
     *            if not <code>null</code> all files of the baseProject will be
     *            copied into the new project after having created it.
     * @return the new project.
     * @throws Exception
     * 
     * @swt Needs to be run from the SWT UI Thread
     */
    protected static IProject createNewProject(String newProjectName,
        final IProject baseProject) throws Exception {

        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        final IProject project = workspaceRoot.getProject(newProjectName);

        // TODO Why do some string magic here?
        final File projectDir = new File(workspaceRoot.getLocation().toString()
            + File.separator + newProjectName);

        if (projectDir.exists()) {
            throw new CoreException(new Status(IStatus.ERROR, Saros.SAROS,
                "Project " + newProjectName + " already exists!"));
        }

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(EditorAPI
            .getAWorkbenchWindow().getShell());

        dialog.run(true, true, new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                throws InvocationTargetException {
                try {
                    SubMonitor subMonitor = SubMonitor.convert(monitor,
                        "Copy local resources... ", 300);

                    subMonitor.subTask("Clearing History...");
                    project.clearHistory(subMonitor.newChild(100));

                    subMonitor.subTask("Refreshing Project");
                    project.refreshLocal(IResource.DEPTH_INFINITE, subMonitor
                        .newChild(100));

                    if (baseProject == null) {
                        subMonitor.subTask("Creating Project...");
                        project.create(subMonitor.newChild(50));

                        subMonitor.subTask("Opening Project...");
                        project.open(subMonitor.newChild(50));
                    } else {
                        subMonitor.subTask("Copying Project...");
                        baseProject.copy(project.getFullPath(), true,
                            subMonitor.newChild(100));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e.getCause());
                }
            }
        });
        return project;
    }

    /**
     * Prepares for receiving the missing resources.
     * 
     * @param localProject
     *            the project that is used for the base of the replication.
     * @param remoteFileList
     *            the file list of the remote project.
     * @return a FileList to request from the host. This list does not contain
     *         any directories or files to remove, but just added and altered
     *         files.
     * @throws CoreException
     *             is thrown when getting all files of the local project.
     */
    protected FileList handleDiff(IProject localProject,
        FileList remoteFileList, SubMonitor monitor) throws CoreException {

        monitor.beginTask("Preparing local project for incoming files", 100);

        monitor.subTask("Calculating Diff");
        FileList diff = new FileList(localProject).diff(remoteFileList);
        monitor.worked(20);

        monitor.subTask("Removing unneeded resources");
        diff = diff.removeUnneededResources(localProject, monitor.newChild(40,
            SubMonitor.SUPPRESS_ALL_LABELS));

        monitor.subTask("Adding Folders");
        diff = diff.addAllFolders(localProject, monitor.newChild(40,
            SubMonitor.SUPPRESS_ALL_LABELS));

        monitor.done();
        return diff;
    }

    /**
     * Ends the incoming invitation process.
     */
    protected void done() {
        sharedProject.userInvitationCompleted(sharedProject.getLocalUser());
        log.debug("Inv" + Util.prefix(peer)
            + ": isInvitationComplete has been set to true.");

        /*
         * TODO: Wait until all of the activities in the queue (which arrived
         * during the invitation) are processed and notify the host only after
         * that.
         */

        transmitter.sendInvitationCompleteConfirmation(peer, invitationID);
        log.debug("Inv" + Util.prefix(peer)
            + ": Invitation complete confirmation sent.");

        invitationProcesses.removeInvitationProcess(this);

        sharedProject.start();

        monitor.done();
        setState(State.DONE);
        log.debug("Inv" + Util.prefix(peer) + ": Invitation complete.");
    }

    public void cancel(String errorMsg, CancelLocation cancelLocation,
        CancelOption notification) {

        if (!cancelled.compareAndSet(false, true))
            return;

        if (monitor != null)
            monitor.setCanceled(true);

        switch (cancelLocation) {
        case LOCAL:
            if (errorMsg != null) {
                log.error("Inv" + Util.prefix(peer)
                    + ": Invitation was cancelled locally"
                    + " because of an error: " + errorMsg);
            } else {
                log.debug("Inv" + Util.prefix(peer)
                    + ": Invitation was cancelled by local user.");
            }
            break;
        case REMOTE:
            if (errorMsg != null) {
                log.error("Inv" + Util.prefix(peer)
                    + ": Invitation was cancelled by the remote user"
                    + " because of an error on his side: " + errorMsg);
            } else {
                log.debug("Inv" + Util.prefix(peer)
                    + ": Invitation was cancelled by the remote user.");
            }
        }

        sessionManager.stopSharedProject();
        /*
         * If the sharedProject is null, stopSharedProject() does not clear the
         * sessionID.
         */
        sessionManager.clearSessionID();

        switch (notification) {
        case NOTIFY_PEER:
            transmitter.sendCancelInvitationMessage(peer, errorMsg);
            break;
        case DO_NOT_NOTIFY_PEER:
        }
        if (inInvitationUI != null)
            inInvitationUI.cancel(peer, errorMsg, cancelLocation);
        else
            log.debug("Inv" + Util.prefix(peer) + ": inInvitationUI is null.");
        invitationProcesses.removeInvitationProcess(this);
    }

    /**
     * Checks whether the invitation process or the monitor has been cancelled.
     * If the monitor has been cancelled but the invitation process has not yet,
     * it cancels the invitation process.
     * 
     * @return <code>true</code> if either the invitation process or the monitor
     *         has been cancelled, <code>false</code> otherwise.
     */
    public boolean checkCancellation() {
        if (cancelled.get())
            return true;

        if (monitor == null)
            return false;

        if (monitor.isCanceled()) {
            cancel(null, CancelLocation.LOCAL, CancelOption.NOTIFY_PEER);
            return true;
        }

        return false;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public FileList getRemoteFileList() {
        return this.remoteFileList;
    }
}
