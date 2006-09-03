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
package de.fu_berlin.inf.dpp.invitation.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProject;

/**
 * An outgoing invitation process.
 * 
 * @author rdjemili
 */
public class OutgoingInvitationProcess extends InvitationProcess 
    implements IOutgoingInvitationProcess, IFileTransferCallback {
    
    /**
     * A simple runnable that calls 
     * {@link IOutgoingInvitationProcess#startSynchronization(IProgressMonitor)}
     */
    private class SynchronizationRunnable implements IRunnableWithProgress {
        private final OutgoingInvitationProcess process;

        public SynchronizationRunnable(OutgoingInvitationProcess process) {
            this.process = process;
        }
        
        public void run(IProgressMonitor monitor) {
            process.startSynchronization(monitor);
        }
    }

    private ISharedProject   sharedProject;

    private int              worked;

    private FileList         remoteFileList;
    private IProgressMonitor progressMonitor;
    private IInvitationUI    invitationUI;
    
    private List<IPath>      toSend;

    
    public OutgoingInvitationProcess(ITransmitter transmitter, JID to, 
        ISharedProject sharedProject, String description) {
        
        super(transmitter, to, description);
        
        this.sharedProject = sharedProject;

        transmitter.sendInviteMessage(sharedProject, to, description);
        state = State.INVITATION_SENT;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IOutgoingInvitationProcess
     */
    public void setInvitationUI(IInvitationUI invitationUI) {
        this.invitationUI = invitationUI;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IOutgoingInvitationProcess
     */
    public void startSynchronization(IProgressMonitor monitor) {
        assertState(State.GUEST_FILELIST_SENT);
        
        state = State.SYNCHRONIZING;
        progressMonitor = monitor;
        
        try {
            FileList local = new FileList(sharedProject.getProject());
            
            FileList diff = remoteFileList.diff(local);
            
            List<IPath> added = diff.getAddedPaths();
            List<IPath> altered = diff.getAlteredPaths();
            toSend = new ArrayList<IPath>(added.size() + altered.size());
            toSend.addAll(added);
            toSend.addAll(altered);
            
            progressMonitor.beginTask("Synchronizing shared project", toSend.size());
            sendNext();
            
            if (!blockUntilFilesSent(monitor) || !blockUntilJoinReceived(monitor))
                cancel(null, false);
            
        } catch (CoreException e) {
            failed(e);
            
        } finally {
            progressMonitor.done();
        }
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void fileListRequested(JID from) {
        assertState(State.INVITATION_SENT);

        // HACK add resource specifier to jid 
        if (peer.equals(from))
            peer = from;
        
        try {
            transmitter.sendFileList(peer, sharedProject.getFileList());
            state = State.HOST_FILELIST_SENT;
            
        } catch (Exception e) {
            failed(e);
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void fileListReceived(JID from, FileList fileList) {
        assertState(State.HOST_FILELIST_SENT);
        
        remoteFileList = fileList;
        state = State.GUEST_FILELIST_SENT;
        
        if (invitationUI != null) {
            invitationUI.runWithProgressBar(new SynchronizationRunnable(this));
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void joinReceived(JID from) {
        assertState(State.SYNCHRONIZING_DONE);
        
        sharedProject.addUser(new User(from));
        state = State.DONE;
        
        sendDriverEditors();
        
        transmitter.removeInvitationProcess(this); // HACK
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void resourceReceived(JID from, IPath path, InputStream in) {
        failState();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.net.IFileTransferCallback
     */
    public void fileTransferFailed(IPath path, Exception e) {
        failed(e);
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.net.IFileTransferCallback
     */
    public void fileSent(IPath path) {
        worked++;
        sendNext();
        
        // we dont call monitor.worked() directly from here, because then 
        // we would need to call it from inside a Display runnable
    }
    
    private void sendNext() {
        if (toSend.size() == 0) {
            state = State.SYNCHRONIZING_DONE;
            return;
        }
        
        IPath path = toSend.remove(0);
        transmitter.sendFile(peer, path, this);
    }

    /**
     * Blocks until all files have been sent or the operation was canceled by
     * the user.
     * 
     * @param monitor the progress monitor for the file synchronization.
     * @return <code>true</code> if all files have been synchronized.
     * <code>false</code> if the user chose to cancel.
     */
    private boolean blockUntilFilesSent(IProgressMonitor monitor) {
        while(state != State.SYNCHRONIZING_DONE) {
            if (monitor.isCanceled() || getState() == State.CANCELED)
                return false;
            
            if (worked > 0) {
                monitor.worked(worked);
                monitor.subTask("Files left: "+toSend.size());
                
                worked = 0;
            }
                
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
        }
        
        return true;
    }
    
    /**
     * Blocks until the join message has been received or the user cancelled.
     * 
     * @param monitor the progress monitor for the file synchronization.
     * @return <code>true</code> if the join message has been received.
     * <code>false</code> if the user chose to cancel.
     */
    private boolean blockUntilJoinReceived(IProgressMonitor monitor) {
        monitor.subTask("Waiting for confirmation");
        
        while(state != State.DONE) {
            if (monitor.isCanceled() || getState() == State.CANCELED)
                return false;
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
        }
        
        return true;
    }

    /**
     * Send activities which set the active editors.
     */
    private void sendDriverEditors() {
        EditorManager editorManager = EditorManager.getDefault();
        Set<IPath> driverEditors = editorManager.getDriverEditors();
        IPath activeDriverEditor = editorManager.getActiveDriverEditor();
        driverEditors.remove(activeDriverEditor);
        
        // HACK
        for (IPath path : driverEditors) {
            sharedProject.getSequencer().activityCreated(
                new EditorActivity(EditorActivity.Type.Activated, path));     
        }
        
        sharedProject.getSequencer().activityCreated(
            new EditorActivity(EditorActivity.Type.Activated, activeDriverEditor));
    }
}
