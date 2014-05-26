package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.IActivity;

public interface IActivityProducer {
    /**
     * Registers the given listener, so it will be informed via
     * {@link IActivityListener#activityCreated(IActivity)} when an Activity is
     * created.
     */
    public void addActivityListener(IActivityListener listener);

    /**
     * Removes a listener previously registered with addActivityListener.
     */
    public void removeActivityListener(IActivityListener listener);
}
