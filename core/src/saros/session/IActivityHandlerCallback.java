package saros.session;

import java.util.List;
import saros.activities.IActivity;
import saros.session.internal.ActivityHandler;

/**
 * Callback interface used by the {@link ActivityHandler} to notify the logic that an activity can
 * now be sent or executed. The implementing class is responsible for proper thread synchronization
 * as the callback methods may be called by multiple threads simultaneously.
 *
 * @author Stefan Rossbach
 */
public interface IActivityHandlerCallback {

  /**
   * Gets called when an activity should be send to several session users.
   *
   * @param recipients a list containing the users that should receive the activity
   * @param activity the activity to send
   */
  public void send(List<User> recipients, IActivity activity);

  /**
   * Gets called when an activity should be executed.
   *
   * @param activity the activity to execute
   */
  public void execute(IActivity activity);
}
