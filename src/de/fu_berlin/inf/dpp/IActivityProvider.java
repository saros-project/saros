package de.fu_berlin.inf.dpp;

import de.fu_berlin.inf.dpp.activities.IActivity;

public interface IActivityProvider {
    public void exec(IActivity activity);
    
    public void addActivityListener(IActivityListener listener);
    public void removeActivityListener(IActivityListener listener);
}
