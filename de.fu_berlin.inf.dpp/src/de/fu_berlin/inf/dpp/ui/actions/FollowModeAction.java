package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Action to enter into FollowMode
 * 
 * TODO Rename to GlobalFollowModeAction
 */
public class FollowModeAction extends Action implements Disposable {

    public static final String ACTION_ID = FollowModeAction.class.getName();

    private static final Logger log = Logger.getLogger(FollowModeAction.class
        .getName());

    protected ISharedProjectListener roleChangeListener = new AbstractSharedProjectListener() {
        @Override
        public void roleChanged(User user, boolean replicated) {
            updateEnablement();
        }
    };

    protected ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionStarted(ISharedProject sharedProject) {

            sharedProject.addListener(roleChangeListener);
            updateEnablement();

            /*
             * Automatically start follow mode at the beginning of a session if
             * Auto-Follow-Mode is enabled.
             */
            if (isEnabled()
                && saros.getPreferenceStore().getBoolean(
                    PreferenceConstants.AUTO_FOLLOW_MODE)) {

                /*
                 * TODO Running this action too early might cause warnings if
                 * the viewport information have not yet arrived!
                 * 
                 * In the worst case, this might be called before the
                 * EditorManager has been initialized, which would probably
                 * cause undefined behavior.
                 * 
                 * Suggested Solution: 1.) We should make sure that
                 * ISessionListeners are sorted in a sane order.
                 * 
                 * 2.) We should think about making initial state information
                 * part of the Invitation process.
                 * 
                 * As a HACK, we run this action 1s after the listener was
                 * called.
                 */
                Util.runSafeAsync(log, Util.delay(1000, new Runnable() {
                    public void run() {
                        Util.runSafeSWTAsync(log, new Runnable() {
                            public void run() {
                                FollowModeAction.this.run();
                            }
                        });
                    }
                }));

            }
        }

        @Override
        public void sessionEnded(ISharedProject sharedProject) {
            sharedProject.removeListener(roleChangeListener);
            updateEnablement();
        }
    };

    protected ISharedEditorListener editorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User user) {

            setChecked(user != null);
            updateEnablement();

            // should not be disabled but checked
            assert (!isEnabled() && isChecked()) == false;

        }
    };

    protected SessionManager sessionManager;

    protected EditorManager editorManager;

    protected Saros saros;

    public FollowModeAction(Saros saros, SessionManager sessionManager,
        EditorManager editorManager) {
        super(null, AS_CHECK_BOX);

        this.saros = saros;
        this.sessionManager = sessionManager;
        this.editorManager = editorManager;

        setImageDescriptor(SarosUI.getImageDescriptor("/icons/monitor_add.png"));
        setToolTipText("Enable/disable follow mode");
        setId(ACTION_ID);

        sessionManager.addSessionListener(sessionListener);
        editorManager.addSharedEditorListener(editorListener);

        updateEnablement();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {

                User toFollow = getNewToFollow();

                log.info("Following: " + toFollow);
                editorManager.setFollowing(toFollow);
            }
        });
    }

    /**
     * Returns the new user to follow.
     * 
     * If there is already a user followed <code>null</code> is returned, i.e.
     * this is a toggeling method, otherwise a random driver is returned.
     */
    protected User getNewToFollow() {
        ISharedProject project = sessionManager.getSharedProject();
        assert project != null;

        if (editorManager.isFollowing()) {
            return null;
        } else {
            for (User user : project.getParticipants()) {
                if (user.isRemote() && user.isDriver()) {
                    return user;
                }
            }
            log.error("no driver to follow but action was enabled");
            return null;
        }
    }

    /**
     * Returns <code>true</code> if the follow mode button should be enabled,
     * <code>false</code> otherwise.
     */
    protected boolean canFollow() {
        ISharedProject project = sessionManager.getSharedProject();

        if (project == null)
            return false;

        if (editorManager.isFollowing()) {
            // While following the button must be enabled to allow deactivation
            // of follow mode.
            return true;
        }

        int driverCount = 0;
        for (User user : project.getParticipants()) {
            if (user.isRemote() && user.isDriver())
                driverCount++;
        }
        return driverCount == 1;
    }

    protected void updateEnablement() {
        setEnabled(canFollow());
    }

    public void dispose() {
        sessionManager.removeSessionListener(sessionListener);
        editorManager.removeSharedEditorListener(editorListener);
    }
}
