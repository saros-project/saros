package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.IActivity;

/**
 * Implementations of this class can be
 * {@linkplain ISarosSession#addActivityConsumer(IActivityConsumer) registered
 * to the SarosSession}, so they will be informed when new activities are ready
 * to be {@linkplain #exec(IActivity) executed}.
 * <p>
 * Usually, you want your consumer to <em>be selective</em>, i.e. just
 * interested in certain kinds of activities; you want it to <em>be simple</em>,
 * i.e. you don't want to dispatch activities among several instances, because
 * you only have one recipient anyway; you want it to <em>be isolated</em>, i.e.
 * it does not matter whether other consumers get activities before or after
 * you.
 * <p>
 * If this describes your consumer pretty well, you probably want to use a
 * {@link AbstractActivityConsumer} and register it to the Saros Session.<br>
 * If your consumer is not isolated or otherwise needs custom dispatching (e.g.
 * in a certain order or to certain recipients), you'll need to override the
 * {@link #exec(IActivity)} method, like in this example:
 * 
 * <pre>
 * public class MyConsumingThingy {
 *   ...
 *   private IActivityConsumer otherConsumer = new IActivityConsumer() {
 *      &#064;Override
 *      public void exec(IActivity activity) {
 *        // do this first
 *      }
 *   };
 * 
 *   private IActivityConsumer consumer = new IActivityConsumer() {
 *      &#064;Override
 *      public void exec(IActivity activity) {
 *        otherConsumer.exec(activity);
 *        // do this after
 *      }
 *   };
 * 
 *   void start() {
 *     sarosSession.addActivityConsumer(consumer);
 *   }
 * 
 *   void stop() {
 *     sarosSession.removeActivityConsumer(consumer);
 *   }
 * }
 * </pre>
 */
public interface IActivityConsumer {
    /**
     * Executes the given activity.
     * <p>
     * Implementations may expect that this method is called from the UI thread
     * (EDT).
     */
    public void exec(IActivity activity);

    /**
     * @JTourBusStop 2, Architecture Overview, User Interface -
     *               ActivityConsumers:
     * 
     *               On the other side, Activity Consumers are responsible for
     *               transforming Activities from remote users into actions that
     *               can be executed on the local Saros-Instance.
     */
}
