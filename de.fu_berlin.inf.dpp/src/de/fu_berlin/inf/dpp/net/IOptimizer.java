package de.fu_berlin.inf.dpp.net;

import java.util.List;

import de.fu_berlin.inf.dpp.activities.IActivity;

public interface IOptimizer {
    public List<IActivity> optimze(List<IActivity> activities);
}
