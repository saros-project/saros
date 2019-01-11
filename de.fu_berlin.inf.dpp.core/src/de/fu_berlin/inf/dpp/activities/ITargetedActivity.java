package de.fu_berlin.inf.dpp.activities;

import de.fu_berlin.inf.dpp.session.User;

/**
 * This is an interface for activities that are only sent to a specific set of users.
 *
 * <p>TODO The ITargetedActivity-Interface should disappear over time as the Server is supposed to
 * calculate the Targets of an Activity (which is at this time mostly done by the Client-Parts of
 * the participants)
 */
public interface ITargetedActivity extends IActivity {
  /** @return the users that should receive this activity */
  User getTarget();
}
