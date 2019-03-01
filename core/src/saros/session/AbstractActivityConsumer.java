package saros.session;

import saros.activities.AbstractActivityReceiver;
import saros.activities.IActivity;
import saros.activities.IActivityReceiver;
import saros.activities.TextSelectionActivity;

/**
 * Use this class if you are just interested in some (e.g. just one) Activity type. Just subclass it
 * by overriding the matching {@link IActivityReceiver receive()} methods.
 *
 * <p>Example for a class that is just interested in instances of {@link TextSelectionActivity}:
 *
 * <pre>
 * public class MyConsumingThingy {
 *     private final IActivityConsumer consumer = new AbstractActivityConsumer() {
 *         &#064;Override
 *         public void receive(TextSelectionActivity tsa) {
 *             // ...
 *         }
 *     };
 *
 *     void start() {
 *         sarosSession.addActivityConsumer(consumer);
 *     }
 *
 *     void stop() {
 *         sarosSession.removeActivityConsumer(consumer);
 *     }
 * }
 * </pre>
 *
 * Override the {@link #exec(IActivity) exec()} method, if you want to have more control about what
 * happens before and after activities reach the {@code receive()} methods (or whether activities
 * reach them at all):
 *
 * <pre>
 * IActivityConsumer loggingConsumer = new AbstractActivityConsumer() {
 *     private boolean active = false;
 *
 *     &#064;Override
 *     public void exec(IActivity activity) {
 *         if (active) {
 *             LOG.debug(&quot;starting&quot;);
 *             super.exec(activity);
 *             LOG.debug(&quot;done&quot;);
 *         }
 *     }
 *
 *     &#064;Override
 *     public void receive(TextSelectionActivity tsa) {
 *         // ...
 *     }
 * };
 * </pre>
 */
public abstract class AbstractActivityConsumer extends AbstractActivityReceiver
    implements IActivityConsumer {

  /**
   * @JTourBusStop 11, Activity sending, The second dispatch:
   *
   * <p>The activity dispatcher of the session has called us -- this was the first dispatch: any
   * instance of this class is just one of many consumers. This default implementation will do a
   * (very simple and straight-forward) second dispatch, by requesting the incoming activity to do
   * the third dispatch on "this".
   */

  /**
   * {@inheritDoc}
   *
   * <p>This method receives all activities, dispatches them, so the matching {@link
   * IActivityReceiver receive()} method is called.
   *
   * <p>You may override this method in case you want to filter or manipulate the activities before
   * they reach the {@code receive()} methods of your implementation -- but don't forget to call
   * {@code super.exec(activity)}.
   */
  @Override
  public void exec(IActivity activity) {
    activity.dispatch(this);
  }
}
