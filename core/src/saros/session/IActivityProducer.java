package saros.session;

import saros.activities.IActivity;

/**
 * An {@link IActivityProducer} is expected to listen for certain events or actions, e.g. in the
 * IDE, create new {@link IActivity} objects, and inform all registered {@link IActivityListener}s
 * about this. The action represented by such an {@link IActivity} needs to be performed locally
 * first, and then an {@link IActivity} is created and given to the {@link IActivityListener}s.
 *
 * <p>Instead of implementing this interface from scratch, you probably want to extend {@link
 * AbstractActivityProducer} instead.
 */
public interface IActivityProducer {
  /**
   * @JTourBusStop 1, Architecture Overview, ActivityProducers:
   *
   * <p>Saros observes pretty much everything that happens in the local IDE of a session
   * participant. For every important event there is a listener that converts local actions of the
   * user into activities that can be applied in the remote IDEs. Those activity-creating classes
   * are called ActivityProducers.
   */

  /**
   * @JTourBusStop 1, Activity sending, The IActivityProducer interface:
   *
   * <p>Activities are used to exchange information about local actions or events in a session. A
   * class that creates activities must implement this interface. Activities are "sent" by invoking
   * IActivityListener.created() on the registered listeners.
   */

  /**
   * Registers the given listener, so it will be informed via {@link
   * IActivityListener#created(IActivity)}.
   */
  public void addActivityListener(IActivityListener listener);

  /**
   * Removes a listener previously registered with {@link #addActivityListener(IActivityListener)}.
   */
  public void removeActivityListener(IActivityListener listener);
}
