package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.activities.business.IActivity;

public interface IActivityListener {

    /**
     * @swt Must be called from the SWT Thread!
     */
    public void activityCreated(IActivity activityDataObject);
}
