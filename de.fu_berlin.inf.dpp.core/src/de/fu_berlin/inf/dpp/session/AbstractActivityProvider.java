package de.fu_berlin.inf.dpp.session;

import java.util.concurrent.CopyOnWriteArrayList;

import de.fu_berlin.inf.dpp.activities.business.IActivity;

// TODO Split up into Producer and Consumer
public abstract class AbstractActivityProvider implements IActivityProvider {

    protected CopyOnWriteArrayList<IActivityListener> activityListeners = new CopyOnWriteArrayList<IActivityListener>();

    @Override
    public void addActivityListener(IActivityListener listener) {
        assert listener != null;
        activityListeners.addIfAbsent(listener);
    }

    @Override
    public abstract void exec(IActivity activity);

    @Override
    public void removeActivityListener(IActivityListener listener) {
        activityListeners.remove(listener);
    }

    /**
     * @JTourBusStop 2, Activity sending, The abstract class to extend:
     * 
     *               But instead of implementing the IActivityProvider interface
     *               one should extend the AbstractActivityProvider class and
     *               call the fireActivity method on newly created activities to
     *               inform all listeners.
     */
    public void fireActivity(IActivity activity) {
        for (IActivityListener activityListener : activityListeners) {
            activityListener.activityCreated(activity);
        }
    }
}
