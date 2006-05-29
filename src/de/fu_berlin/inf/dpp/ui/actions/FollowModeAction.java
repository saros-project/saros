package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.ISharedProject;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.listeners.ISessionListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class FollowModeAction extends Action implements ISessionListener {
    private boolean isFollowMode = false;

    public FollowModeAction() {
        setToolTipText("Enable follow mode");
        setImageDescriptor(SarosUI.getImageDescriptor("/icons/monitor_add.png"));
    }
    
    @Override
    public void run() {
        isFollowMode = !isFollowMode;
        getSharedProject().getEditorManager().setEnableFollowing(isFollowMode);
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
    }
    
    private ISharedProject getSharedProject() {
        return Saros.getDefault().getSessionManager().getSharedProject();
    }
}
