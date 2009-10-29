package de.fu_berlin.inf.dpp.activities.business;

import org.eclipse.core.runtime.IPath;

/**
 * An interface for activityDataObjects that are resource related (e.g.
 * FileActivity)
 */
public interface IResourceObject extends IActivity {

    /**
     * The path to the file that this activityDataObject is about. For instance
     * for creating a file this path denotes the file which is created.
     */
    public IPath getPath();

    /**
     * Returns the old/source path in case this activityDataObject represents a
     * moving of files.
     */
    public IPath getOldPath();

}