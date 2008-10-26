package de.fu_berlin.inf.dpp.project;

import java.util.List;

import de.fu_berlin.inf.dpp.activities.IActivity;

public interface IActivityManager {
    public void addProvider(IActivityProvider provider);

    public void exec(IActivity activity);

    public List<IActivity> flush();

    public void removeProvider(IActivityProvider provider);
}
