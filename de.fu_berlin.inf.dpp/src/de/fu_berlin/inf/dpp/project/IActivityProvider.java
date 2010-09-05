package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.activities.business.IActivity;

/**
 * An activity provider is responsible for creating and executing one or more
 * activity types.<br>
 * <br>
 * Activity providers execute their activities locally. They are expected to
 * ignore activities they're not responsible for.<br>
 * <br>
 * Providers report when they create an activity to the registered listeners by
 * calling {@link IActivityListener#activityCreated(IActivity)}. The provider is
 * intended to use {@link ISarosSession#addActivityProvider(IActivityProvider)}
 * , which in turn will register the ISarosSession to the provider. This way,
 * the provider can fire activities for a Saros session by calling
 * activityCreated().
 * 
 * @see AbstractActivityProvider
 */
public interface IActivityProvider {

    /**
     * Executes the given activity.
     * 
     * @swt The implementor may expect that this method is called from the SWT
     *      thread.
     */
    public void exec(IActivity activity);

    /**
     * Adds the given listener to the list of listeners. This IActivityProvider
     * is expected to inform the listeners when it created an activity.
     */
    public void addActivityListener(IActivityListener listener);

    /**
     * Removes a listener previously registered with addActivityListener.
     */
    public void removeActivityListener(IActivityListener listener);
}
