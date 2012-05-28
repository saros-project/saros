package de.fu_berlin.inf.dpp.project;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.ITargetedActivity;
import de.fu_berlin.inf.dpp.activities.business.TargetedActivityWrapper;

public abstract class AbstractActivityProvider implements IActivityProvider {

    protected List<IActivityListener> activityListeners = new CopyOnWriteArrayList<IActivityListener>();

    public void addActivityListener(IActivityListener listener) {
        assert listener != null;
        if (!activityListeners.contains(listener)) {
            this.activityListeners.add(listener);
        }
    }

    public abstract void exec(IActivity activity);

    public void removeActivityListener(IActivityListener listener) {
        this.activityListeners.remove(listener);
    }

    public void fireActivity(IActivity activity) {
        for (IActivityListener activityListener : activityListeners) {
            activityListener.activityCreated(activity);
        }
    }

    public void fireActivity(User user, IActivity activity) {
        ITargetedActivity target = new TargetedActivityWrapper(user, activity);
        fireActivity(target);
    }

    public void fireActivity(List<User> users, IActivity activity) {
        ITargetedActivity target = new TargetedActivityWrapper(users, activity);
        fireActivity(target);
    }
}
