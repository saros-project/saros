package de.fu_berlin.inf.dpp.project.internal;

import java.text.MessageFormat;

import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.PermissionActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.Messages;

/**
 * This manager is responsible for handling {@link Permission} changes.
 * 
 * @author rdjemili
 */
@Component(module = "core")
public class PermissionManager extends AbstractActivityProvider implements
    Startable {
    private final ISarosSession sarosSession;

    public PermissionManager(ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
    }

    @Override
    public void start() {
        sarosSession.addActivityProvider(this);
    }

    @Override
    public void stop() {
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
