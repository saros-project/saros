package de.fu_berlin.inf.dpp.session.internal;

import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.activities.PermissionActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * This manager is responsible for handling {@link Permission} changes. It both
 * produces and consumes activities.
 * 
 * @author rdjemili
 */
@Component(module = "core")
public class PermissionManager extends AbstractActivityProducer implements
    Startable {
    private static final Logger LOG = Logger.getLogger(PermissionManager.class);

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void receive(PermissionActivity activity) {
            handlePermissionChange(activity);
        }
    };

    private final ISarosSession sarosSession;

    private final UISynchronizer synchronizer;

    public PermissionManager(ISarosSession sarosSession,
        UISynchronizer synchronizer) {
        this.sarosSession = sarosSession;
        this.synchronizer = synchronizer;
    }

    @Override
    public void start() {
        sarosSession.addActivityProducer(this);
        sarosSession.addActivityConsumer(consumer, Priority.ACTIVE);
    }

    @Override
    public void stop() {
        sarosSession.removeActivityProducer(this);
        sarosSession.removeActivityConsumer(consumer);
    }

    /**
     * This method is responsible for handling incoming permission changes from
     * other clients
     * 
     * @param activity
     */
    private void handlePermissionChange(PermissionActivity activity) {
        User user = activity.getAffectedUser();
        Permission permission = activity.getPermission();

        sarosSession.setPermission(user, permission);
    }

    /**
     * Initiates a {@link Permission} change for a specific user.
     * 
     * @host This method may only called by the host.
     * @noSWT This method mustn't be called from the SWT UI thread
     * 
     * @blocking Returning after the {@link Permission} change is complete
     * 
     * @param target
     *            The user who's {@link Permission} has to be changed
     * @param newPermission
     *            The new {@link Permission} of the user
     * 
     * @throws CancellationException
     * @throws InterruptedException
     */
    public void initiatePermissionChange(final User target,
        final Permission newPermission) throws CancellationException,
        InterruptedException {

        final User localUser = sarosSession.getLocalUser();

        if (!localUser.isHost())
            throw new IllegalStateException(
                "only the host can initiate permission changes");

        Runnable fireActivityrunnable = new Runnable() {

            @Override
            public void run() {
                fireActivity(new PermissionActivity(localUser, target,
                    newPermission));

                sarosSession.setPermission(target, newPermission);
            }
        };

        if (target.isHost()) {
            synchronizer.syncExec(ThreadUtils.wrapSafe(LOG,
                fireActivityrunnable));
        } else {
            StartHandle startHandle = sarosSession.getStopManager().stop(
                target, "Permission change");

            synchronizer.syncExec(ThreadUtils.wrapSafe(LOG,
                fireActivityrunnable));

            if (!startHandle.start())
                LOG.error("failed to resume user: "
                    + target
                    + ", the user might no longer be able to perform changes in the current session!");
        }
    }
}
