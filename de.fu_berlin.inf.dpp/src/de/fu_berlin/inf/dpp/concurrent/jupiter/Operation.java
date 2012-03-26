package de.fu_berlin.inf.dpp.concurrent.jupiter;

import java.util.List;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.ITextOperation;

/**
 * An Operation is a representation of a user activityDataObject for the use by
 * an algorithm like Jupiter.
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
    List<TextEditActivity> toTextEdit(SPath path, User source);

    /**
     * Returns a list of all operations represented by this operation that
     * perform text changes in the order they should be executed. This method
     * can return an empty list (for NoOperations for instance).
     */
    List<ITextOperation> getTextOperations();

    /**
     * @return the Operation that reverts the effect of this Operation if it is
     *         executed afterwards; e.g. if this is a DeleteOperation(5,"abc")
     *         the inverted Operation is InsertOperation(5,"abc")
     */
    public Operation invert();

}
