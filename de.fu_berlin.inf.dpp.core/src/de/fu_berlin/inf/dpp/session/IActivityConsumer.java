package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.activities.business.IActivity;

public interface IActivityConsumer {
    /**
     * Executes the given activity.
     * <p>
     * The implementor may expect that this method is called from the EDT
     * thread.
     */
    public void exec(IActivity activity);
}
