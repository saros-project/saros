package de.fu_berlin.inf.dpp.concurrent.jupiter;

import java.util.List;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.ITextOperation;

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

    /**
     * Returns a list of all operations represented by this operation that
     * perform text changes in the order they should be executed. This method
     * can return an empty list (for NoOperations for instance).
     */
    List<ITextOperation> getTextOperations();

}
