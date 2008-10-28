package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class FollowModeAction extends Action implements ISessionListener {

	private boolean isFollowMode = false;

	public FollowModeAction() {
		super();
		setImageDescriptor(SarosUI.getImageDescriptor("/icons/monitor_add.png"));
		setToolTipText("Enable/disable follow mode");

		Saros.getDefault().getSessionManager().addSessionListener(this);
		updateEnablement();
	}

	@Override
	public void run() {
		setFollowMode(!getFollowMode());
	}

	public boolean getFollowMode() {
		return isFollowMode;
	}

	public void setFollowMode(boolean isFollowMode) {
		this.isFollowMode = isFollowMode;
		EditorManager.getDefault().setEnableFollowing(isFollowMode);
	}

	public void sessionStarted(ISharedProject session) {
		// Automatically start follow mode at the beginning of a session if
		// Auto-Follow-Mode is enabled.
		if (Saros.getDefault().getPreferenceStore()
			.getBoolean(PreferenceConstants.AUTO_FOLLOW_MODE)) {
			setFollowMode(true);
		}
		updateEnablement();
	}

	public void sessionEnded(ISharedProject session) {
		updateEnablement();
	}

	public void invitationReceived(IIncomingInvitationProcess process) {
		// ignore
	}

	private void updateEnablement() {
		setEnabled(Saros.getDefault().getSessionManager().getSharedProject() != null);
		setChecked(getFollowMode());
	}
}
