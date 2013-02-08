package de.fu_berlin.inf.dpp.project.internal;

import java.text.MessageFormat;

import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.PermissionActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.Messages;
import de.fu_berlin.inf.dpp.ui.views.SarosView;

/**
 * This manager is responsible for handling {@link Permission} changes.
 * 
 * @author rdjemili
 */
@Component(module = "core")
public class PermissionManager extends AbstractActivityProvider implements
    Startable {
    private final ISarosSession sarosSession;

    private ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

        @Override
        public void permissionChanged(User user) {

            /*
             * Not nice to have GUI stuff here, but it can't be handled in
             * SessionView because it is not guaranteed there actually is a
             * session view open.
             */
            if (user.isLocal()) {
                SarosView
                    .showNotification(
                        Messages.PermissionManager_permission_changed,
                        MessageFormat.format(
                            Messages.PermissionManager_you_have_now_access,
                            user.getHumanReadableName(),
                            user.hasWriteAccess() ? Messages.PermissionManager_write
                                : Messages.PermissionManager_read_only));
            } else {
                SarosView
                    .showNotification(
                        Messages.PermissionManager_permission_changed,
                        MessageFormat.format(
                            Messages.PermissionManager_he_has_now_access,
                            user.getHumanReadableName(),
                            user.hasWriteAccess() ? Messages.PermissionManager_write
                                : Messages.PermissionManager_read_only));

            }
        }

        @Override
        public void userJoined(User user) {

            SarosView.showNotification(Messages.PermissionManager_buddy_joined,
                MessageFormat.format(
                    Messages.PermissionManager_buddy_joined_text,
                    user.getHumanReadableName()));
        }

        @Override
        public void userLeft(User user) {
            SarosView.showNotification(Messages.PermissionManager_buddy_left,
                MessageFormat.format(
                    Messages.PermissionManager_buddy_left_text,
                    user.getHumanReadableName()));
        }
    };

    public PermissionManager(ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
    }

    @Override
    public void start() {
        sarosSession.addListener(sharedProjectListener);
        sarosSession.addActivityProvider(this);
    }

    @Override
    public void stop() {
        sarosSession.removeListener(sharedProjectListener);
        sarosSession.removeActivityProvider(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    @Override
    public void exec(IActivity activity) {
        if (activity instanceof PermissionActivity) {
            PermissionActivity permissionActivity = (PermissionActivity) activity;
            User user = permissionActivity.getAffectedUser();
            if (!user.isInSarosSession()) {
                throw new IllegalArgumentException(MessageFormat.format(
                    Messages.PermissionManager_buddy_no_participant, user));
            }
            Permission permission = permissionActivity.getPermission();
            this.sarosSession.setPermission(user, permission);
        }
    }
}
