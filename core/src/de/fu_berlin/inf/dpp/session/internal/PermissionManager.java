package de.fu_berlin.inf.dpp.session.internal;

import de.fu_berlin.inf.dpp.activities.PermissionActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.util.concurrent.CancellationException;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

/**
 * This manager is responsible for handling {@link Permission} changes. It both produces and
 * consumes activities.
 *
 * @author rdjemili
 */
@Component(module = "core")
public class PermissionManager extends AbstractActivityProducer implements Startable {
  private static final Logger LOG = Logger.getLogger(PermissionManager.class);

  private final IActivityConsumer consumer =
      new AbstractActivityConsumer() {
        @Override
        public void receive(PermissionActivity activity) {
          handlePermissionChange(activity);
        }
      };

  private final SarosSession session;

  private final UISynchronizer synchronizer;

  public PermissionManager(SarosSession session, UISynchronizer synchronizer) {
    this.session = session;
    this.synchronizer = synchronizer;
  }

  @Override
  public void start() {
    session.addActivityProducer(this);
    session.addActivityConsumer(consumer, Priority.ACTIVE);
  }

  @Override
  public void stop() {
    session.removeActivityProducer(this);
    session.removeActivityConsumer(consumer);
  }

  /**
   * This method is responsible for handling incoming permission changes from other clients
   *
   * @param activity
   */
  private void handlePermissionChange(PermissionActivity activity) {
    User user = activity.getAffectedUser();
    Permission permission = activity.getPermission();

    session.setPermission(user, permission);
  }

  /**
   * Initiates a {@link Permission} change for a specific user.
   *
   * @param target The user who's {@link Permission} has to be changed
   * @param permission The new {@link Permission} of the user
   * @throws CancellationException
   * @throws InterruptedException
   * @throws IllegalStateException if called inside the application/session thread
   * @throws IllegalStateException if the local user is not the host of the session
   */
  public void changePermission(final User target, final Permission permission)
      throws CancellationException, InterruptedException {

    if (synchronizer.isUIThread())
      throw new IllegalStateException(
          "cannot change permission, illegal thread access: " + Thread.currentThread().getName());

    final User localUser = session.getLocalUser();

    if (!localUser.isHost())
      throw new IllegalStateException("only the host can initiate permission changes");

    Runnable fireActivityrunnable =
        new Runnable() {

          @Override
          public void run() {
            fireActivity(new PermissionActivity(localUser, target, permission));

            session.setPermission(target, permission);
          }
        };

    if (target.isHost()) {
      synchronizer.syncExec(ThreadUtils.wrapSafe(LOG, fireActivityrunnable));
    } else {
      StartHandle startHandle = session.getStopManager().stop(target, "Permission change");

      synchronizer.syncExec(ThreadUtils.wrapSafe(LOG, fireActivityrunnable));

      if (!startHandle.start())
        LOG.error(
            "failed to resume user: "
                + target
                + ", the user might no longer be able to perform changes in the current session!");
    }
  }
}
