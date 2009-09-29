package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.activities.IActivityDataObject;

public interface IActivityListener {

    /**
     * @swt Must be called from the SWT Thread!
     */
    public void activityCreated(IActivityDataObject activityDataObject);
}
