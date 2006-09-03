
package de.fu_berlin.inf.dpp.invitation;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * By contract calls to this invitiation process that are not expected, will
 * throw a IllegalStateException. Use {@link #getPeer()} to decide wether a 
 * incoming message is destined for this process.
 * 
 * @author rdjemili
 */
public interface IInvitationProcess {
    // TODO add special invitation process ID which can be used to specificly
    // address certain invitations
        
    /**
     * All states that an invitiation process can possibly have.
     */
    public static enum State {
        INVITATION_SENT, 
        HOST_FILELIST_REQUESTED, 
        HOST_FILELIST_SENT, 
        GUEST_FILELIST_SENT, 
        SYNCHRONIZING, 
        SYNCHRONIZING_DONE,
        DONE,
        CANCELED
    }
    
    public static final String USER_CANCEL_MSG = "Invitation was cancelled by user";
    
    /**
     * @return the exception that occured while executing the process or
     * <code>null</code> if no exception was thrown.
     */
    public Exception getException();

    /**
     * @return the current state of the process.
     */
    public State getState();

    /**
     * @return the peer that is participating with us in this process. For an
     * incoming invitation this is the inviter. For an outgoing invitation this
     * is the invitee.
     */
    public JID getPeer();

    /**
     * @return the user-provided informal description that can be provided with
     * an invitiation.
     */
    public String getDescription();
    
    /**
     * Cancels the invitation process. Is ignored if invitation was already
     * cancelled.
     * 
     * @param errorMsg the error that caused the cancellation. This should be some
     * user-friendly text as it might be presented to the user.
     * <code>null</code> if the cancellation was caused by the users request
     * and not by some error.
     * @param replicated <code>true</code> if this cancellation is caused by
     * an remote system. <code>false</code> if it originates on our system. If
     * <code>false</code> we send an cancellation message to our peer.
     */
    public void cancel(String errorMsg, boolean replicated);

    /**
     * @return <code>true</code> if this invitation process has consumed the
     * input stream. <code>false</code> otherwise.
     */
    public void resourceReceived(JID from, IPath path, InputStream input);
    
    public void fileListReceived(JID from, FileList fileList);
    public void fileListRequested(JID from); // TODO rename to invitationAccepted
    public void joinReceived(JID from);
}