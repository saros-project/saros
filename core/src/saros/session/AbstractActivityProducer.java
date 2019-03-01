package saros.session;

import java.util.concurrent.CopyOnWriteArrayList;
import saros.activities.IActivity;

/**
 * Standard implementation of {@link IActivityProducer}, which, in addition to the interface
 * methods, provides an easy way to notify all listeners through {@link #fireActivity(IActivity)
 * fireActivity()}.
 *
 * <p>You probably just want to inherit from this class without overriding anything. Before your
 * fired activities can reach other Saros session participants, you need to register your producer
 * to the Saros session -- which provides you with at least <em>one</em> quite attentive listener,
 * i.e. the Saros session -- like this:
 *
 * <pre>
 * public class MyProducer extends AbstractActivityProducer {
 *   ...
 *   void start() {
 *     sarosSession.addActivityProducer(<b>this</b>);
 *   }
 *
 *   void stop() {
 *     sarosSession.removeActivityProducer(<b>this</b>);
 *   }
 *
 *   void something() {
 *     fireActivity(new MyActivity());
 *   }
 * }
 * </pre>
 *
 * However, since {@link #addActivityListener(IActivityListener) addActivityListener()} is public,
 * other interested listeners might also hook into the creation process of your activities -- just
 * as you can hook into their's.
 */
public abstract class AbstractActivityProducer implements IActivityProducer {

  private final CopyOnWriteArrayList<IActivityListener> activityListeners =
      new CopyOnWriteArrayList<IActivityListener>();

  @Override
  public void addActivityListener(IActivityListener listener) {
    assert listener != null;
    activityListeners.addIfAbsent(listener);
  }

  @Override
  public void removeActivityListener(IActivityListener listener) {
    activityListeners.remove(listener);
  }

  /**
   * @JTourBusStop 2, Activity sending, The abstract class to extend:
   *
   * <p>But instead of implementing the IActivityProducer interface you should extend the
   * AbstractActivityProducer class and call the fireActivity() method on newly created activities
   * to inform all listeners.
   */

  /** */
  protected final void fireActivity(IActivity activity) {
    for (IActivityListener activityListener : activityListeners) {
      activityListener.created(activity);
    }
  }
}
