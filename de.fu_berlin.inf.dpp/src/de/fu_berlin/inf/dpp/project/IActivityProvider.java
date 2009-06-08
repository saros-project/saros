package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.activities.IActivity;

/**
 * Every activity provider is responsible for one or more activity types.
 * 
 * It can...
 * 
 * ...cause an activity to be executed locally (exec)
 * 
 * ...report an activity to the registered listeners
 * {@link IActivityListener#activityCreated(IActivity)}
 * 
 * @author rdjemili
 */
public interface IActivityProvider {

    /**
     * Will cause implementor of the interface to execute the given activity.
     * 
     * @swt The implementor may expect that this method is called from the SWT
     *      thread.
     */
    public void exec(IActivity activity);

    /**
     * Add the given listener to the list of listeners which are informed when
     * an activity occurred locally.
     */
    public void addActivityListener(IActivityListener listener);

    /**
     * Remove a listener previously registered with addActivityListener.
     */
    public void removeActivityListener(IActivityListener listener);
}
