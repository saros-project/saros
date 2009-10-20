package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.activities.IActivityDataObject;

/**
 * Every activityDataObject provider is responsible for one or more
 * activityDataObject types.
 * 
 * It can...
 * 
 * ...cause an activityDataObject to be executed locally (exec)
 * 
 * ...report an activityDataObject to the registered listeners
 * {@link IActivityListener#activityCreated(IActivityDataObject)}
 * 
 * @author rdjemili
 */
public interface IActivityProvider {

    /**
     * Will cause implementor of the interface to execute the given
     * activityDataObject.
     * 
     * @swt The implementor may expect that this method is called from the SWT
     *      thread.
     */
    public void exec(IActivityDataObject activityDataObject);

    /**
     * Add the given listener to the list of listeners which are informed when
     * an activityDataObject occurred locally.
     */
    public void addActivityListener(IActivityListener listener);

    /**
     * Remove a listener previously registered with addActivityListener.
     */
    public void removeActivityListener(IActivityListener listener);
}
