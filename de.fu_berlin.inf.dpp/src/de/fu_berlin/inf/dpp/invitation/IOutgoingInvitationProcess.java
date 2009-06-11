package de.fu_berlin.inf.dpp.invitation;

/**
 * An outgoing invitation process, which is used to invite new users to the
 * shared project.
 * 
 * @author rdjemili
 */
public interface IOutgoingInvitationProcess extends IInvitationProcess {
    /**
     * Synchronizing is the state before completing the invitation process,
     * where the files of the shared project are replicated/synchronized with
     * the local project of the invited user.
     */
    public void startSynchronization();

}
