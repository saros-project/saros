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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.ISharedProject;
import de.fu_berlin.inf.dpp.ITransmitter;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.TextLoadActivity;
import de.fu_berlin.inf.dpp.xmpp.JID;

/**
 * An outgoing invitation process has the following states.
 * 
 * @author rdjemili
 */
public class OutgoingInvitationProcess extends InvitationProcess 
    implements IOutgoingInvitationProcess {

    private final ISharedProject sharedProject;

    private int                  filesToSend;
    private FileList             remoteFileList;
    private IProgressMonitor     progressMonitor;
    private ICallback            callback;

    
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
    public void setCallback(ICallback callback) {
        this.callback = callback;
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IOutgoingInvitationProcess
     */
    public void startSynchronization(IProgressMonitor monitor) {
        assertState(State.GUEST_FILELIST_SENT);
        
        state = State.SYNCHRONIZING;
        
        try {
            FileList local = new FileList(sharedProject.getProject());
            
            FileList diff = remoteFileList.diff(local);
            filesToSend = diff.getAddedPaths().size() + diff.getAlteredPaths().size(); 
            
            if (filesToSend == 0) {
                state = State.SYNCHRONIZING_DONE;
                return;
            }
                
            progressMonitor = monitor;
            progressMonitor.beginTask("Synchronizing shared project", filesToSend);
            
            for (IPath path : diff.getAddedPaths()) {
                transmitter.sendResource(peer, path);
            }
            
            for (IPath path : diff.getAlteredPaths()) {
                transmitter.sendResource(peer, path);
            }
            
            // block until all files have been sent
            while(state != State.SYNCHRONIZING_DONE) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            sharedProject.getSequencer().activityCreated(
                new TextLoadActivity(sharedProject.getDriverPath())); // HACK
            
            monitor.done();
            
        } catch (CoreException e) {
            state = State.FAILED;
            exception = e;
            
            e.printStackTrace();
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
            state = State.FAILED;
            exception = e;
            
            e.printStackTrace();
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void fileListReceived(JID from, FileList fileList) {
        assertState(State.HOST_FILELIST_SENT);
        
        remoteFileList = fileList;
        state = State.GUEST_FILELIST_SENT;
        
        if (callback != null) {
            callback.outgoingInvitationAccepted(this);
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void resourceSent(IPath path) {
        // TODO make sure its our file
        
        filesToSend--;
        progressMonitor.worked(1);
        
        if (filesToSend == 0)
            state = State.SYNCHRONIZING_DONE;
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void joinReceived(JID from) {
        assertState(State.SYNCHRONIZING_DONE);
        
        sharedProject.addUser(new User(from)); // HACK
        state = State.DONE;
        
        transmitter.removeInvitationProcess(this); // HACK
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.InvitationProcess
     */
    public void resourceReceived(JID from, IPath path, InputStream in) {
        failState();
    }
}
