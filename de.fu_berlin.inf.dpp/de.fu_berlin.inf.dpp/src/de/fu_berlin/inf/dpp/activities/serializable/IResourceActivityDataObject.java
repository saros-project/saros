package de.fu_berlin.inf.dpp.activities.serializable;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;

/**
 * An interface for activityDataObjects that are resource related (e.g.
 * FileActivityDataObject)
 */
public interface IResourceActivityDataObject extends IActivityDataObject {

    /**
     * The path to the file that this activityDataObject is about. For instance
     * for creating a file this path denotes the file which is created.
     */
    public SPathDataObject getPath();

    /**
     * Returns the old/source path in case this activityDataObject represents a
     * moving of files.
     */
    public SPathDataObject getOldPath();

}