package de.fu_berlin.inf.dpp.concurrent.jupiter;

import java.util.List;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.TextEditActivity;

/**
 * An Operation is a representation of a user activity for the use by an
 * algorithm like Jupiter.
 * 
 * This interface must be implemented by all operations. An operation is
 * application dependent and therefore this interface does not contain any
 * specific methods at all. Operations are immutable.
 */
public interface Operation {

    /**
     * Returns a sequence of {@link TextEditActivity}s which represent this
     * operation if applied in order to the editor denoted by the given path by
     * the user identified by the given source.
     */
    List<TextEditActivity> toTextEdit(IPath path, String source);

}
