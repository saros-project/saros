package de.fu_berlin.inf.dpp.project;

import java.util.LinkedList;
import java.util.List;

import de.fu_berlin.inf.dpp.activities.IActivityDataObject;

public abstract class AbstractActivityProvider implements IActivityProvider {

    protected List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

    public void addActivityListener(IActivityListener listener) {
        if (!activityListeners.contains(listener)) {
            this.activityListeners.add(listener);
        }
    }

    public abstract void exec(IActivityDataObject activityDataObject);

    public void removeActivityListener(IActivityListener listener) {
        this.activityListeners.remove(listener);
    }

    public void fireActivity(IActivityDataObject activityDataObject) {
        for (IActivityListener activityListener : activityListeners) {
            activityListener.activityCreated(activityDataObject);
        }
    }

}
