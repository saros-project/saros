package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.activities.business.IActivity;

/**
 * Every activity provider is responsible for one or more activity types.
 * 
 * It can...<br>
 * ...cause an activity to be executed locally (exec)<br>
 * ...report an activity to the registered listeners
 * {@link IActivityListener#activityCreated(IActivity)}
 * 
 * @author rdjemili
 */
public interface IActivityProvider {

    /**
     * Instructs the implementor of the interface to execute the given activity.
     * 
     * @swt The implementor may expect that this method is called from the SWT
     *      thread.
     */
    public void exec(IActivity activity);

    /**
     * Adds the given listener to the list of listeners. This IActivityProvider
     * is expected to inform theses listeners when it created an activity.
     */
    public void addActivityListener(IActivityListener listener);

    /**
     * Removes a listener previously registered with addActivityListener.
     */
    public void removeActivityListener(IActivityListener listener);
}
