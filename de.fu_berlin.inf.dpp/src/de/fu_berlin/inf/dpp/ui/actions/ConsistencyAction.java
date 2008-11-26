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

    @Override
    public void inconsistencyDetected() {
	setEnabled(true);
	setToolTipText("Inconsistency Detected!");
    }

    @Override
    public void inconsistencyResolved() {
	setEnabled(false);
	setToolTipText("");
    }

    @Override
    public void sessionStarted(ISharedProject session) {
	Saros.getDefault().getSessionManager().getSharedProject()
		.getConcurrentDocumentManager().addConsistencyListener(this);
    }

    @Override
    public void invitationReceived(IIncomingInvitationProcess invitation) {
	// ignore
    }

    @Override
    public void sessionEnded(ISharedProject session) {
	Saros.getDefault().getSessionManager().getSharedProject()
		.getConcurrentDocumentManager().removeConsistencyListener(this);
    }

}
