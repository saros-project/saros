package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.IActivity;

public interface IActivityListener {

    /**
     * Called when an activity was created.
     * 
     * @param activityData
     *            The IActivity that was created.
     */
    public void activityCreated(IActivity activityData);
}
