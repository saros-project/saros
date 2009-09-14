package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

/**
 * An interface for activities that are resource related (e.g. FileActivity)
 */
public interface IResourceActivity extends IActivity {

    /**
     * The path to the file that this activity is about. For instance for
     * creating a file this path denotes the file which is created.
     */
    public IPath getPath();

    /**
     * Returns the old/source path in case this activity represents a moving of
     * files.
     */
    public IPath getOldPath();

}