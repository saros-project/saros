package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class FollowModeAction extends Action implements ISessionListener {
	private boolean isFollowMode = false;

	public FollowModeAction() {
		setImageDescriptor(SarosUI.getImageDescriptor("/icons/monitor_add.png"));
		setToolTipText("Enable/disable follow mode");

		Saros.getDefault().getSessionManager().addSessionListener(this);
		updateEnablement();
	}

	@Override
	public void run() {
		isFollowMode = !isFollowMode;
		EditorManager.getDefault().setEnableFollowing(isFollowMode);
	}

	public void sessionStarted(ISharedProject session) {
		updateEnablement();
	}

	public void sessionEnded(ISharedProject session) {
		updateEnablement();
	}

	public void invitationReceived(IIncomingInvitationProcess process) {
		// ignore
	}

	private void updateEnablement() {
		setEnabled(getSharedProject() != null);
		setChecked(isFollowMode);
	}

	private ISharedProject getSharedProject() {
		return Saros.getDefault().getSessionManager().getSharedProject();
	}
}
