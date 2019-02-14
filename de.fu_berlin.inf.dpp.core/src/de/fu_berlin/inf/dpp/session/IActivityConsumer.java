package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.IActivity;

/**
 * Implementations of this class can be {@linkplain
 * ISarosSession#addActivityConsumer(IActivityConsumer, Priority) registered to the SarosSession},
 * so they will be informed when new activities are ready to be {@linkplain #exec(IActivity)
 * executed}.
 *
 * <p>There are two types of consumers. First, there are consumers that receive activities and hold
 * some state (such as remembering the currently active editor of each session participant), so
 * these states can be queried at any time without the need to wait for the next activity.<br>
 * The second type of consumers relies on the incoming activities (and maybe those states maintained
 * by the other consumers) and perform actions with visible effects, such as changing the local file
 * copies or displaying awareness information in the user interface.
 *
 * <p>The first type of consumers is called "passive" as they don't have an observable effect on
 * their environment. The second type are the "active" consumers as they provide the actual feature
 * that make up Saros. Since the actions at least sometimes of the latter rely on the first being
 * up-to-date, but never the other way around, passive consumers are always notified first about new
 * activities.
 *
 * <p>Usually, you don't need to implement this interface directly, but can simply extend the {@link
 * AbstractActivityConsumer} class which takes care of dispatching the activities based on their
 * type, so your logic will only be called for the {@link IActivity} implementations you're actually
 * interested in.
 */
public interface IActivityConsumer {
  /**
   * A consumer can either be {@link #PASSIVE} or {@link #ACTIVE}. The value determines in which
   * round this consumer will be notified about a new activity.
   *
   * <p>TODO An alternative implementation would make use of annotations to indicate whether a
   * consumer is meant to be active or passive. (Archnemesis could then check whether a passive
   * consumer has outgoing dependencies to another consumer, which is illegal.)
   */
  enum Priority {
    /**
     * Passive consumers will be notified first. Such consumers don't change anything besides their
     * own state upon activity consumption. They must <em>not</em> rely on other consumers having
     * already consumed a particular activity.
     */
    PASSIVE,
    /**
     * After all passive consumers have been fed, the active consumers are notified in the second
     * round. They may rely on passive consumers to have updated their respective states.
     */
    ACTIVE
  }

  /**
   * @JTourBusStop 2, Architecture Overview, ActivityConsumers:
   *
   * <p>On the other side, ActivityConsumers are responsible for transforming Activities from remote
   * users into actions that can be executed on the local Saros instance.
   */

  /**
   * Executes the given activity.
   *
   * <p>Implementations may expect that this method is called from the UI thread (EDT).
   */
  public void exec(IActivity activity);
}
