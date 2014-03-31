package de.fu_berlin.inf.dpp.project.internal;

import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.PermissionActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.session.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * This manager is responsible for handling {@link Permission} changes.
 * 
 * @author rdjemili
 */
@Component(module = "core")
public class PermissionManager extends AbstractActivityProvider implements
    Startable {
    private final ISarosSession sarosSession;

    private static final Logger log = Logger.getLogger(PermissionManager.class);

    public PermissionManager(ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
    }

    private final AbstractActivityReceiver receiver = new AbstractActivityReceiver() {

        @Override
        public void receive(PermissionActivity activity) {
            handlePermissionChange(activity);
        }

    };

    @Override
    public void start() {
        sarosSession.addActivityProvider(this);
    }

    @Override
    public void stop() {
        sarosSession.removeActivityProvider(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exec(IActivity activity) {
        activity.dispatch(receiver);
    }

    /**
     * This method is responsible for handling incoming permission changes from
     * other clients
     * 
     * @param activity
     */
    private void handlePermissionChange(PermissionActivity activity) {

        User user = activity.getAffectedUser();
        if (!user.isInSarosSession()) {
            log.warn("could not change permissions of user " + user
                + " because the user is longer part of the session");
            return;
        }

        Permission permission = activity.getPermission();
        this.sarosSession.setPermission(user, permission);
    }

    /**
     * Initiates a {@link Permission} change for a specific user.
     * 
     * @host This method may only called by the host.
     * @noSWT This method mustn't be called from the SWT UI thread
     * 
     * @blocking Returning after the {@link Permission} change is complete
     * 
     * @param user
     *            The user who's {@link Permission} has to be changed
     * @param newPermission
     *            The new {@link Permission} of the user
     * @param synchronizer
     *            An Abstraction of the SWT-Thread
     * 
     * 
     * @throws CancellationException
     * @throws InterruptedException
     */

    public void initiatePermissionChange(final User user,
        final Permission newPermission, UISynchronizer synchronizer)
        throws CancellationException, InterruptedException {

        final User localUser = sarosSession.getLocalUser();

        Runnable fireActivityrunnable = new Runnable() {

            @Override
            public void run() {
                fireActivity(new PermissionActivity(localUser, user,
                    newPermission));

                sarosSession.setPermission(user, newPermission);

            }
        };

        if (user.isHost()) {
            synchronizer.syncExec(ThreadUtils.wrapSafe(log,
                fireActivityrunnable));

        } else {
            StartHandle startHandle = sarosSession.getStopManager().stop(user,
                "Permission change");

            synchronizer.syncExec(ThreadUtils.wrapSafe(log,
                fireActivityrunnable));

            if (!startHandle.start())
                log.error("Didn't unblock. "
                    + "There still exist unstarted StartHandles.");
        }
    }
}
