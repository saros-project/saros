package de.fu_berlin.inf.dpp.invitation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * An outgoing invitation process, which is used to invite new users to the
 * shared project.
 * 
 * @author rdjemili
 */
public interface IOutgoingInvitationProcess extends IInvitationProcess {

	/**
	 * This class contains untestable UI code which is needed by the invitation
	 * process.
	 */
	public interface IInvitationUI {
		public void runWithProgressBar(IRunnableWithProgress runnable);
	}

	/**
	 * Sets the invitiationUI which will be used for UI related actions.
	 */
	public void setInvitationUI(IInvitationUI invitationUI);

	/**
	 * Synchronizing is the state right before completing the invitation
	 * process, where the files of the shared project are
	 * replicated/synchronized with the local project of the invitee.
	 * 
	 * @param monitor
	 */
	public void startSynchronization(IProgressMonitor monitor);
}