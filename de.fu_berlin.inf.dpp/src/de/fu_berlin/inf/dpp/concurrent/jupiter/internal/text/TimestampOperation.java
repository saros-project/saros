package de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;

/**
 * This operation contains a new vector time for the algorithm.
 * 
 * @author orieger
 */
public class TimestampOperation implements Operation {

    public int getPosition() {
        return 0;
    }

    public void setPosition(int position) {
        throw new UnsupportedOperationException();
    }

    public int getTextLength() {
        return 0;
    }

    /**
     * Returns the text to be deleted.
     * 
     * @return the text to be deleted
     */
    public String getText() {
        return "";
    }

    /**
     * Sets the text to be deleted.
     * 
     * @param text
     *            the text to be deleted
     */
    public void setText(String text) {
        throw new UnsupportedOperationException();
    }

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

    public List<TextEditActivity> toTextEdit(IPath path, String source) {
        return Collections.emptyList();
    }
}
