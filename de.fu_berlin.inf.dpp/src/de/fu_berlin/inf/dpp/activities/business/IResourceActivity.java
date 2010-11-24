package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.activities.SPath;

/**
 * An interface for activityDataObjects that are resource related (e.g.
 * FileActivity)
 */
public interface IResourceActivity extends IActivity {

    /**
     * The path to the file that this activityDataObject is about. For instance
     * for creating a file this path denotes the file which is created.
     */
    public SPath getPath();

}