package de.fu_berlin.inf.dpp.invitation;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.inf.dpp.FileList;

/**
 * An incoming invitation process. Is created when we receive an invitation to a
 * shared project on a remote system.
 * 
 * @author rdjemili
 */
public interface IIncomingInvitationProcess extends IInvitationProcess {

    /**
     * Requests the file list of the remotly shared project. This methods blocks
     * until the file list is retrieved.
     * 
     * This method can be called while in INVITATION_SENT state.
     */
    public FileList requestRemoteFileList(IProgressMonitor monitor);

    /**
     * @return the file list of the remotly shared project or <code>null</code>
     *         if it hasn't been requested and retrieved yet. Make a call to
     *         {@link #requestRemoteFileList(IProgressMonitor)} before using
     *         this method.
     */
    public FileList getRemoteFileList();

    /**
     * Accepts the incoming invitation and creates the shared project. This
     * method blocks until the synchronization is done.
     * 
     * This method can be called by the user while in HOST_FILELIST_SENT state.
     * 
     * @param baseProject
     *            the local project that is used as file base for the following
     *            replication. If this is <code>null</code> no project will be
     *            used as file base.
     * @param newProjectName
     *            the project name of the new project that is to be generated.
     *            If this is <code>null</code> the <code>baseProject</code> will
     *            be overwritten.
     * @param monitor
     *            a progressmonitor that monitors the whole process. Can not be
     *            <code>null</code>.
     * 
     * @throws InvitationException
     *             -
     */
    public void accept(IProject baseProject, String newProjectName,
        IProgressMonitor monitor);

    public void setInvitationUI(IInvitationUI inviteUI);

}