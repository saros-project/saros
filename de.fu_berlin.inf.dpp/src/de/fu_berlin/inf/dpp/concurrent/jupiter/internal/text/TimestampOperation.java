package de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;

/**
 * This operation update have new vector time for the algorithm.
 * 
 * @author orieger
 * 
 */

public class TimestampOperation implements Operation {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2756378905499193184L;

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
     * Returns the position.
     * 
     * @return the position
     */
    public int getPosition() {
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
     * Returns the text length.
     * 
     * @return the length of the text
     */
    public int getTextLength() {
	return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	int hashcode = 38;
	return hashcode;
    }

    /**
     * Sets the position of this operation.
     * 
     * @param position
     *            the position to set
     */
    public void setPosition(int position) {
	throw new UnsupportedOperationException();
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
}
