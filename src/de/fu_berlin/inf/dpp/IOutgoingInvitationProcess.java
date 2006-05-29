
package de.fu_berlin.inf.dpp;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IOutgoingInvitationProcess extends IInvitationProcess {

    public interface ICallback {
        public void outgoingInvitationAccepted(IOutgoingInvitationProcess process);
    }
    
    public void setCallback(ICallback callback);

    public void startSynchronization(IProgressMonitor monitor);
}