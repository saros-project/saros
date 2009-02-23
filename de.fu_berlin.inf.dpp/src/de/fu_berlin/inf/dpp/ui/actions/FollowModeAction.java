package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

public class FollowModeAction extends Action implements ISessionListener {

    private static final Logger log = Logger.getLogger(FollowModeAction.class
        .getName());

    public FollowModeAction() {
        super();
        setImageDescriptor(SarosUI.getImageDescriptor("/icons/monitor_add.png"));
        setToolTipText("Enable/disable follow mode");

        Saros.getDefault().getSessionManager().addSessionListener(this);
        updateEnablement();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                setFollowMode(!getFollowMode());
            }
        });
    }

    public boolean getFollowMode() {
        return EditorManager.getDefault().isFollowing();
    }

    public void setFollowMode(boolean isFollowMode) {
        EditorManager.getDefault().setEnableFollowing(isFollowMode);
        updateEnablement();
    }

    public void sessionStarted(ISharedProject session) {
        // Automatically start follow mode at the beginning of a session if
        // Auto-Follow-Mode is enabled.
        if (Saros.getDefault().getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_FOLLOW_MODE)) {
            setFollowMode(true);
        }
        Saros.getDefault().getSessionManager().getSharedProject().addListener(
            new ISharedProjectListener() {
                public void roleChanged(User user, boolean replicated) {
                    updateEnablement();
                }

                public void userJoined(JID user) {
                    // ignore
                }

                public void userLeft(JID user) {
                    // ignore
                }
            });

        updateEnablement();
    }

    public void sessionEnded(ISharedProject session) {
        updateEnablement();
    }

    public void invitationReceived(IIncomingInvitationProcess process) {
        // ignore
    }

    public void updateEnablement() {
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();
        setEnabled(project != null && !project.isDriver());
        setChecked(getFollowMode());
    }
}
