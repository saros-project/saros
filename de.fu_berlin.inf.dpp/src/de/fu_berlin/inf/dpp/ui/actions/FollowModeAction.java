package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Action to enter into FollowMode
 * 
 * TODO Rename to GlobalFollowModeAction
 */
@Component(module = "action")
public class FollowModeAction extends Action implements Disposable {

    public static final String ACTION_ID = FollowModeAction.class.getName();

    private static final Logger log = Logger.getLogger(FollowModeAction.class
        .getName());

    protected ISharedProjectListener permissionChangeListener = new AbstractSharedProjectListener() {
        @Override
        public void permissionChanged(User user) {
            updateEnablement();
        }

        @Override
        public void userLeft(User user) {
            updateEnablement();
        }

        @Override
        public void userJoined(User user) {
            updateEnablement();
        }
    };

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {

            newSarosSession.addListener(permissionChangeListener);
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
                Utils.runSafeAsync(log, Utils.delay(1000, new Runnable() {
                    public void run() {
                        Utils.runSafeSWTAsync(log, new Runnable() {
                            public void run() {
                                if (sessionManager.getSarosSession() != null) {
                                    FollowModeAction.this.run();
                                }
                            }
                        });
                    }
                }));

            }
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeListener(permissionChangeListener);
            updateEnablement();
        }
    };

    protected ISharedEditorListener editorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User user, boolean isFollowed) {
            setChecked(isFollowed);
            updateEnablement();

            // should not be disabled but checked
            assert (!isEnabled() && isChecked()) == false;
        }
    };

    @Inject
    protected SarosSessionManager sessionManager;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected Saros saros;

    public FollowModeAction() {
        super(null, AS_CHECK_BOX);

        SarosPluginContext.initComponent(this);

        setImageDescriptor(ImageManager
            .getImageDescriptor("/icons/monitor_add.png"));
        setToolTipText("Enable/Disable Follow Mode");
        setId(ACTION_ID);

        sessionManager.addSarosSessionListener(sessionListener);
        editorManager.addSharedEditorListener(editorListener);

        updateEnablement();
    }

    @Override
    public void run() {
        User toFollow = getNewToFollow();
        log.info("Following: " + toFollow);
        editorManager.setFollowing(toFollow);
    }

    /**
     * Returns the new user to follow.
     * 
     * If there is already a user followed <code>null</code> is returned, i.e.
     * this is a toggeling method, otherwise a random user with
     * {@link User.Permission#WRITE_ACCESS} is returned.
     */
    protected User getNewToFollow() {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        assert sarosSession != null;

        if (editorManager.isFollowing()) {
            return null;
        } else {
            for (User user : sarosSession.getParticipants()) {
                if (user.isRemote() && user.hasWriteAccess()) {
                    return user;
                }
            }
            log.error("No user with write access to follow but action was enabled");
            return null;
        }
    }

    /**
     * Returns <code>true</code> if the follow mode button should be enabled,
     * <code>false</code> otherwise.
     */
    protected boolean canFollow() {
        ISarosSession sarosSession = sessionManager.getSarosSession();

        if (sarosSession == null)
            return false;

        if (editorManager.isFollowing()) {
            // While following the button must be enabled to allow deactivation
            // of follow mode.
            return true;
        }

        int usersWithWriteAccessCount = 0;
        for (User user : sarosSession.getParticipants()) {
            if (user.isRemote() && user.hasWriteAccess())
                usersWithWriteAccessCount++;
        }
        return usersWithWriteAccessCount == 1;
    }

    protected void updateEnablement() {
        setEnabled(canFollow());
    }

    public void dispose() {
        sessionManager.removeSarosSessionListener(sessionListener);
        editorManager.removeSharedEditorListener(editorListener);
    }
}
