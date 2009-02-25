package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Action to enter into FollowMode
 * 
 * TODO Allow to track individual observers
 */
public class FollowModeAction extends Action {

    private static final Logger log = Logger.getLogger(FollowModeAction.class
        .getName());

    ISharedProjectListener roleChangeListener = new AbstractSharedProjectListener() {
        @Override
        public void roleChanged(User user, boolean replicated) {
            if (Saros.getDefault().getLocalUser().equals(user)) {
                updateEnablement();
            }
        }
    };

    ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionStarted(ISharedProject session) {
            /*
             * Automatically start follow mode at the beginning of a session if
             * Auto-Follow-Mode is enabled.
             */
            if (Saros.getDefault().getPreferenceStore().getBoolean(
                PreferenceConstants.AUTO_FOLLOW_MODE)) {
                setFollowing(true);
            }
            session.addListener(roleChangeListener);
            updateEnablement();
        }

        @Override
        public void sessionEnded(ISharedProject session) {
            session.removeListener(roleChangeListener);
            updateEnablement();
        }
    };

    public FollowModeAction() {
        super(null, AS_CHECK_BOX);

        setImageDescriptor(SarosUI.getImageDescriptor("/icons/monitor_add.png"));
        setToolTipText("Enable/disable follow mode");

        EditorManager.getDefault().addSharedEditorListener(
            new AbstractSharedEditorListener() {
                @Override
                public void followModeChanged(final boolean enabled) {
                    Util.runSafeSWTAsync(log, new Runnable() {
                        public void run() {
                            setChecked(enabled);
                        }
                    });

                }
            });

        Saros.getDefault().getSessionManager().addSessionListener(
            sessionListener);
        updateEnablement();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                log.info("setFollowing to " + !isFollowing());
                setFollowing(!isFollowing());
            }
        });
    }

    public boolean isFollowing() {
        return EditorManager.getDefault().isFollowing();
    }

    public void setFollowing(boolean enable) {
        EditorManager.getDefault().setEnableFollowing(enable);
    }

    public void updateEnablement() {
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        boolean canFollow = project != null && !project.isDriver();

        if (!canFollow && isFollowing()) {
            setFollowing(false);
        }

        if (isEnabled() != canFollow)
            setEnabled(canFollow);
    }
}
