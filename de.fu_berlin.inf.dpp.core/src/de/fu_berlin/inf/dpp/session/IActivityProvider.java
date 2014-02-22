package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.business.IActivity;

/**
 * @JTourBusStop 1, Activity sending, The IActivityProvider interface:
 *
 *      Activities are used to exchange information in a session. A class that
 *      creates or handles activities must implement this interface. Activities
 *      are "sent" by invoking IActivityListener#activityCreated on the registered
 *      listeners.
 */

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
 * In most cases you want to extend {@link AbstractActivityProvider} instead of
 * implementing this interface.
 * 
 * @see AbstractActivityProvider
 */
public interface IActivityProvider {

    /**
     * @JTourBusStop 1, Architecture Overview, User Interface -
     *               ActivityProviders:
     * 
     *               The "User Interface"-Component is responsible for managing
     *               all the visual elements in Saros, listening to changes in
     *               Eclipse and reacting to actions performed by the local
     *               user.
     * 
     *               Saros internally work with Activities. Activities are
     *               created by Activity Providers, which take local actions and
     *               turns them into Activities. Activity Providers are also
     *               responsible for transforming Activities from remote users
     *               into actions that can be executed on the local
     *               Saros-Instance.
     * 
     */

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
