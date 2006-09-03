package de.fu_berlin.inf.dpp.project;

import java.util.List;

import de.fu_berlin.inf.dpp.activities.IActivity;

public interface IActivityManager {
    public void addProvider(IActivityProvider provider);
    public void removeProvider(IActivityProvider provider);

    public List<IActivity> flush();
    public void exec(IActivity activity);
}
