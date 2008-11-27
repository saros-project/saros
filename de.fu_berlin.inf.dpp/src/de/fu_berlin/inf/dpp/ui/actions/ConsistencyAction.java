package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.concurrent.management.IConsistencyListener;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;

public class ConsistencyAction extends Action implements IConsistencyListener,
	ISessionListener {

    public ConsistencyAction() {
	setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
		.getImageDescriptor(ISharedImages.IMG_OBJS_WARN_TSK));
	setEnabled(false);
	Saros.getDefault().getSessionManager().addSessionListener(this);
    }

    public void inconsistencyDetected() {
	setEnabled(true);
	setToolTipText("Inconsistency Detected!");
    }

    public void inconsistencyResolved() {
	setEnabled(false);
	setToolTipText("");
    }

    public void sessionStarted(ISharedProject session) {
	Saros.getDefault().getSessionManager().getSharedProject()
		.getConcurrentDocumentManager().addConsistencyListener(this);
    }

    public void invitationReceived(IIncomingInvitationProcess invitation) {
	// ignore
    }

    public void sessionEnded(ISharedProject session) {
	Saros.getDefault().getSessionManager().getSharedProject()
		.getConcurrentDocumentManager().removeConsistencyListener(this);
    }

}
