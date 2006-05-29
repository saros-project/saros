/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
package de.fu_berlin.inf.dpp.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.ISharedProject;
import de.fu_berlin.inf.dpp.ITransmitter;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SessionManager;
import de.fu_berlin.inf.dpp.xmpp.JID;

/**
 * An incoming invitation process.
 * 
 * @author rdjemili
 */
public class IncomingInvitationProcess extends InvitationProcess 
    implements IIncomingInvitationProcess {
    
    private FileList         remoteFileList;
    private IProject         localProject;

    private int              filesToSynchronizeLeft;
    private IProgressMonitor progressMonitor;
    
    
    public IncomingInvitationProcess(ITransmitter transmitter, JID from, String description) {
        super(transmitter, from, description);
        
        this.description = description;
        this.state = State.INVITATION_SENT;
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IInvitationProcess
     */
    public void fileListReceived(JID from, FileList fileList) {
        assertState(State.HOST_FILELIST_REQUESTED);
        
        remoteFileList = fileList;
        state = State.HOST_FILELIST_SENT;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IIncomingInvitationProcess
     */
    public FileList requestRemoteFileList(IProgressMonitor monitor) {
        assertState(State.INVITATION_SENT);
        
        monitor.beginTask("Requesting remote file list", IProgressMonitor.UNKNOWN);
        
        transmitter.sendRequestForFileListMessage(peer);
        state = State.HOST_FILELIST_REQUESTED;
        
        while (remoteFileList == null) {
            try {
                Thread.sleep(500);
                monitor.worked(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        monitor.done();
        
        return remoteFileList;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IIncomingInvitationProcess
     */
    public void accept(IProject baseProject, String newProjectName, 
        IProgressMonitor monitor) throws InvitationException {
        
        try {
            assertState(State.HOST_FILELIST_SENT);

            if (newProjectName != null) {
                localProject = createFromExistingProject(newProjectName, baseProject);
            } else {
                localProject = baseProject;
            }
            filesToSynchronizeLeft = handleDiff(baseProject, localProject, remoteFileList);

            progressMonitor = monitor;
            progressMonitor.beginTask("Synchronizing...", filesToSynchronizeLeft);
            state = State.SYNCHRONIZING;

            transmitter.sendFileList(peer, new FileList(baseProject));

            try {
                while(filesToSynchronizeLeft > 0) {
                    Thread.sleep(500);
                    
                    if (monitor.isCanceled()) {
                        cancel();
                        return;
                    }
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            done();
        
        } catch (Exception e) {
            setException(e);
            throw new InvitationException(e);
            
        } finally {
            progressMonitor.done();
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void fileListRequested(JID from) {
        failState();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void joinReceived(JID from) {
        failState();
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void resourceReceived(JID from, IPath path, InputStream in) {
        try {
            IFile file = localProject.getFile(path);
            if (file.exists()) {
                file.setContents(in, IResource.FORCE, new NullProgressMonitor());
            } else {
                file.create(in, true, new NullProgressMonitor());
            }
        } catch (Exception e) {
            setException(e);
        }
        
        progressMonitor.worked(1);
        filesToSynchronizeLeft--;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void resourceSent(IPath path) {
        failState();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IIncomingInvitationProcess
     */
    public FileList getRemoteFileList() {
        return remoteFileList;
    }
    
    private IProject createFromExistingProject(String newProjectName, 
        IProject baseProject) throws CoreException {
        
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = workspaceRoot.getProject(newProjectName);
        baseProject.copy(project.getFullPath(), true, new NullProgressMonitor());
        
        return project;
    }

    private int handleDiff(IProject baseProject, IProject localProject, 
        FileList remoteFileList) throws CoreException {
        
        FileList diff = new FileList(baseProject).diff(remoteFileList);
        
        for (IPath path : diff.getRemovedPaths()) {
            IFile file = localProject.getFile(path);
            
            // TODO check if this triggers the resource listener
            file.delete(true, new NullProgressMonitor());
        }
        
        return diff.getAddedPaths().size() + diff.getAlteredPaths().size();
    }

    private void done() {
        JID host = peer;
        JID driver = peer;
        
        // HACK
        List<JID> users = new ArrayList<JID>();
        users.add(host);
        users.add(Saros.getDefault().getMyJID());
        
        SessionManager sessionManager = Saros.getDefault().getSessionManager();
        ISharedProject sharedProject = sessionManager.createIncomingSharedProject(
            localProject, host, driver, users);
        
        transmitter.sendJoinMessage(sharedProject);
        transmitter.removeInvitationProcess(this); // HACK
        
        state = State.DONE;
    }
}
