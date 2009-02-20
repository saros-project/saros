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
package de.fu_berlin.inf.dpp.invitation.internal;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.ErrorMessageDialog;

/**
 * An incoming invitation process.
 * 
 * @author rdjemili
 */
public class IncomingInvitationProcess extends InvitationProcess implements
    IIncomingInvitationProcess {

    private static Logger logger = Logger
        .getLogger(IncomingInvitationProcess.class);

    private FileList remoteFileList;

    private IProject localProject;

    private int filesLeftToSynchronize;

    /** size of current transfered part of archive file. */
    private int transferedFileSize = 0;

    private IProgressMonitor progressMonitor;

    protected String projectName;

    public IncomingInvitationProcess(ITransmitter transmitter, JID from,
        String projectName, String description, int colorID) {

        super(transmitter, from, description, colorID);

        this.projectName = projectName;
        setState(State.INVITATION_SENT);

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IInvitationProcess
     */
    public void fileListReceived(JID from, FileList fileList) {
        assertState(State.HOST_FILELIST_REQUESTED);

        if (fileList == null) {
            cancel("Failed to receive remote file list.", false);
        } else {
            this.remoteFileList = fileList;
            setState(State.HOST_FILELIST_SENT);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IIncomingInvitationProcess
     */
    public FileList requestRemoteFileList(IProgressMonitor monitor) {
        assertState(State.INVITATION_SENT);

        monitor.beginTask("Requesting remote file list",
            IProgressMonitor.UNKNOWN);

        this.transmitter.sendRequestForFileListMessage(this.peer);
        setState(State.HOST_FILELIST_REQUESTED);

        while ((this.remoteFileList == null) && (this.state != State.CANCELED)) {
            if (monitor.isCanceled()) {
                cancel(null, false);
            }

            try {
                Thread.sleep(500);
                monitor.worked(1);
            } catch (InterruptedException e) {
                cancel(null, false);
            }
        }

        monitor.done();

        return this.remoteFileList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IIncomingInvitationProcess
     */
    public void accept(IProject baseProject, String newProjectName,
        IProgressMonitor monitor) {

        if ((newProjectName == null) && (baseProject == null)) {
            throw new IllegalArgumentException(
                "At least newProjectName or baseProject have to be not null.");
        }

        try {
            assertState(State.HOST_FILELIST_SENT);

            if (newProjectName != null) {
                this.localProject = createNewProject(newProjectName,
                    baseProject);
            } else {
                this.localProject = baseProject;
            }

            this.filesLeftToSynchronize = handleDiff(this.localProject,
                this.remoteFileList);

            this.progressMonitor = monitor;
            if (this.transferMode == TransferMode.IBB) {
                this.progressMonitor.beginTask("Synchronizing",
                    100 + this.filesLeftToSynchronize);
                this.progressMonitor.subTask("Receiving Archive...");
            } else {
                this.progressMonitor.beginTask("Synchronizing",
                    this.filesLeftToSynchronize);
            }
            setState(State.SYNCHRONIZING);

            this.transmitter.sendFileList(this.peer, new FileList(
                this.localProject), this);

            if (blockUntilAllFilesSynchronized(monitor)) {
                done();
            } else {
                cancel(null, false);
            }

        } catch (Exception e) {
            ErrorMessageDialog.showErrorMessage(new Exception(
                "Exception during create project."));
            failed(e);

        } finally {
            monitor.done();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void invitationAccepted(JID from) {
        failState();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void joinReceived(JID from) {
        failState();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void resourceReceived(JID from, IPath path, InputStream in) {
        IncomingInvitationProcess.logger.debug("new file received: " + path);
        if (this.localProject == null) {
            return; // we do not have started the new project yet, so received
            // resources are not welcomed
        }

        try {
            IFile file = this.localProject.getFile(path);
            if (file.exists()) {
                ResourceAttributes attributes = new ResourceAttributes();
                attributes.setReadOnly(false);
                file.setResourceAttributes(attributes);
                file
                    .setContents(in, IResource.FORCE, new NullProgressMonitor());

                // TODO Set ReadOnly again?
            } else {
                file.create(in, true, new NullProgressMonitor());
                IncomingInvitationProcess.logger.debug("New File created: "
                    + file.getName());
            }

        } catch (Exception e) {
            failed(e);
        }

        this.progressMonitor.worked(1);
        this.progressMonitor.subTask("Files left: "
            + this.filesLeftToSynchronize);

        this.filesLeftToSynchronize--;
        IncomingInvitationProcess.logger.debug("file counter: "
            + this.filesLeftToSynchronize);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IIncomingInvitationProcess
     */
    public FileList getRemoteFileList() {
        return this.remoteFileList;
    }

    /**
     * Blocks until all files have been synchronized or cancel has been
     * selected.
     * 
     * @return <code>true</code> if all files were synchronized.
     *         <code>false</code> if operation was canceled by user.
     */
    private boolean blockUntilAllFilesSynchronized(IProgressMonitor monitor) {
        // TODO: deadlock abfangen.
        while (this.filesLeftToSynchronize > 0) {
            if (monitor.isCanceled() || (getState() == State.CANCELED)) {
                return false;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return false;
            }
        }

        return true;
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
     * @throws CoreException
     *             if something goes wrong while creating the new project.
     */
    private IProject createNewProject(String newProjectName,
        final IProject baseProject) throws CoreException {

        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        final IProject project = workspaceRoot.getProject(newProjectName);

        final File projectDir = new File(workspaceRoot.getLocation().toString()
            + File.separator + newProjectName);
        if (projectDir.exists()) {
            projectDir.delete();
        }

        /* run project read only settings in progress monitor thread. */
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                    Display.getDefault().getActiveShell());
                try {
                    dialog.run(true, false, new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor) {

                            try {

                                monitor.beginTask("Copy local resources ... ",
                                    IProgressMonitor.UNKNOWN);

                                project.clearHistory(null);
                                project.refreshLocal(IResource.DEPTH_INFINITE,
                                    null);

                                if (baseProject == null) {
                                    project.create(new NullProgressMonitor());
                                    project.open(new NullProgressMonitor());
                                } else {
                                    baseProject.copy(project.getFullPath(),
                                        true, new NullProgressMonitor());
                                }

                            } catch (CoreException e) {
                                IncomingInvitationProcess.logger
                                    .warn(
                                        "Exception during copy local ressources",
                                        e);
                                monitor.done();
                            }

                            monitor.done();

                        }

                    });
                } catch (InvocationTargetException e) {
                    IncomingInvitationProcess.logger.warn("", e);
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    IncomingInvitationProcess.logger.warn("", e);
                    e.printStackTrace();
                }

            }
        });

        // TODO CO: What is this???
        // project.clearHistory(null);
        // project.refreshLocal(IProject.DEPTH_INFINITE, null);

        return project;
    }

    /**
     * Prepares for receiving the missing resources.
     * 
     * @param localProject
     *            the project that is used for the base of the replication.
     * @param remoteFileList
     *            the file list of the remote project.
     * @return the number of files that we need to receive to end the
     *         synchronization.
     * @throws CoreException
     *             is thrown when getting all files of the local project.
     */
    private int handleDiff(IProject localProject, FileList remoteFileList)
        throws CoreException {

        // TODO: Thread
        FileList diff = new FileList(localProject).diff(remoteFileList);

        removeUnneededResources(localProject, diff);
        int addedPaths = addAllFolders(localProject, diff);

        return diff.getAddedPaths().size() - addedPaths
            + diff.getAlteredPaths().size();
    }

    /**
     * Removes all local resources that aren't part of the shared project we're
     * currently joining. This includes files and folders.
     * 
     * @param localProject
     *            the local project were the shared project will be replicated.
     * @param diff
     *            the fileList which contains the diff information.
     * @throws CoreException
     */
    private void removeUnneededResources(IProject localProject, FileList diff)
        throws CoreException {

        // TODO dont throw CoreException
        // TODO check if this triggers the resource listener
        for (IPath path : diff.getRemovedPaths()) {
            if (path.hasTrailingSeparator()) {
                IFolder folder = localProject.getFolder(path);

                if (folder.exists()) {
                    folder.delete(true, new NullProgressMonitor());
                }

            } else {
                IFile file = localProject.getFile(path);

                // check if file exists because it might have already been
                // deleted when deleting its folder
                if (file.exists()) {
                    file.delete(true, new NullProgressMonitor());
                }
            }
        }
    }

    private int addAllFolders(IProject localProject, FileList diff)
        throws CoreException {

        int addedFolders = 0;

        for (IPath path : diff.getAddedPaths()) {
            if (path.hasTrailingSeparator()) {
                IFolder folder = localProject.getFolder(path);
                if (!folder.exists()) {
                    folder.create(true, true, new NullProgressMonitor());
                }

                addedFolders++;
            }
        }

        return addedFolders;
    }

    /**
     * Ends the incoming invitation process.
     */
    private void done() {
        JID host = this.peer;

        ISessionManager sessionManager = Saros.getDefault().getSessionManager();
        ISharedProject sharedProject = sessionManager.joinSession(
            this.localProject, host, colorID);

        // TODO Will block 1000 ms to ensure something...
        this.transmitter.sendJoinMessage(sharedProject);
        this.transmitter.removeInvitationProcess(this);

        sharedProject.setProjectReadonly(true);

        setState(State.DONE);
    }

    public String getProjectName() {
        return this.projectName;
    }

    public TransferMode getTransferMode() {
        return this.transferMode;
    }

    public void fileSent(IPath path) {
        // do nothing
    }

    public void fileTransferFailed(IPath path, Exception e) {
        failed(e);
    }

    public void transferProgress(int transfered) {
        this.progressMonitor.worked(transfered - this.transferedFileSize);
        this.transferedFileSize = transfered;
    }

    public void setTransferMode(TransferMode mode) {
        this.transferMode = mode;
    }

    @Override
    public void cancel(String errorMsg, boolean replicated) {
        super.cancel(errorMsg, replicated);

        Saros.getDefault().getSessionManager().cancelIncomingInvitation();
    }
}
