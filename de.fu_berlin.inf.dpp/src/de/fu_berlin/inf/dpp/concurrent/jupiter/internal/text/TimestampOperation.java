package de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text;

import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;

/**
 * This operation contains a new vector time for the algorithm.
 * 
 * TODO TimestampOperations are never used.
 * 
 * @author orieger
 */
@XStreamAlias("timestampOp")
public class TimestampOperation implements Operation {

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Timestamp(0,'')";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj.getClass().equals(getClass())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hashcode = 38;
        return hashcode;
    }

    public List<TextEditActivity> toTextEdit(SPath path, User source) {
        return Collections.emptyList();
    }

    public List<ITextOperation> getTextOperations() {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    public Operation invert() {
        return new NoOperation(); // TimestampOperations don't cause effects
    }
}
