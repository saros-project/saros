package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.activities.business.IActivity;

public interface IActivityListener {

    /**
     * Called when an activity was created.
     * 
     * @swt Must be called from the SWT Thread!
     * @param activityData
     *            The IActivity that was created.
     */
    public void activityCreated(IActivity activityData);
}
