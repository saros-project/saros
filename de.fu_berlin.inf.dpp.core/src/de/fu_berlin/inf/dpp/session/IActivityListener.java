package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.IActivity;

/**
 * Used to listen to what an {@link IActivityProducer} does, i.e. to react on <em>locally</em>
 * created activities right after they were created, for which reasons should be quite rare. If
 * you're interested in incoming activities from <em>other</em> session participants, which is much
 * more likely, {@link IActivityConsumer} is the interface you're looking for.
 */
public interface IActivityListener {

  /**
   * Called when an activity was created.
   *
   * @param activity The IActivity that was created.
   */
  public void created(IActivity activity);
}
