package de.fu_berlin.inf.dpp.activities.business;

import java.util.List;

import de.fu_berlin.inf.dpp.User;

/**
 * This is an interface for activities that are only sent to a specific set of
 * users.
 */
public interface ITargetedActivity {
    /**
     * @return the users that should receive this activity
     */
    List<User> getRecipients();
}
