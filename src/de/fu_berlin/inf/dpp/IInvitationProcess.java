
package de.fu_berlin.inf.dpp;

import java.io.InputStream;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.xmpp.JID;

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
        FAILED,
        CANCELED
    }
    
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
     * @return the peer which is participating in this process.
     */
    public JID getPeer();

    /**
     * @return the user-provided informal description that can be provided with
     * an invitiation.
     */
    public String getDescription();

    public void fileListReceived(JID from, FileList fileList);

    public void fileListRequested(JID from); // TODO rename to invitationAccepted

    public void joinReceived(JID from);

    public void resourceSent(IPath path);

    /**
     * @return <code>true</code> if this invitation process has consumed the
     * input stream. <code>false</code> otherwise.
     */
    public void resourceReceived(JID from, IPath path, InputStream input);

}