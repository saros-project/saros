package de.fu_berlin.inf.dpp;

import java.util.List;

import de.fu_berlin.inf.dpp.activities.IActivity;


public interface IActivitySequencer extends IActivityListener {
    public List<IActivity> flush();
    public void exec(int time, IActivity activity);
    public void addProvider(IActivityProvider provider);
    public void removeProvider(IActivityProvider provider);
    public List<IActivity> getLog();
    public int incTime(int amount);
}
