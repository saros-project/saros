/*
 * $Id: DeleteOperation.java 2434 2005-12-12 07:49:51Z sim $
 *
 * ace - a collaborative editor
 * Copyright (C) 2005 Mark Bigler, Simon Raess, Lukas Zbinden
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;

/**
 * The DeleteOperation is used to hold a text together with its position that is
 * to be deleted in the document model.
 */
public class DeleteOperation implements Operation {

    /**
     * the text to be deleted.
     */
    private String text;

    /**
     * the position in the document where the text is to be deleted.
     */
    private int position;

    /**
     * Class constructor.
     */
    public DeleteOperation() {
        // Empty Default constructor
    }

    /**
     * Class constructor.
     *
     * @param position
     *            the position into the document
     * @param text
     *            the text to be deleted
     */
    public DeleteOperation(int position, String text) {
        setPosition(position);
        setText(text);
    }

    /**
     * Class constructor.
     *
     * @param position
     *            the position into the document
     * @param text
     *            the text to be deleted
     * @param isUndo
     *            flag to indicate whether this operation is an undo
     */
    public DeleteOperation(int position, String text, boolean isUndo) {
        setPosition(position);
        setText(text);
    }

    /**
     * Returns the position.
     *
     * @return the position
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * Sets the position of this operation.
     *
     * @param position
     *            the position to set
     */
    public void setPosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("position index must be >= 0");
        }
        this.position = position;
    }

    /**
     * Returns the text length.
     *
     * @return the length of the text
     */
    public int getTextLength() {
        return this.text.length();
    }

    /**
     * Returns the text to be deleted.
     *
     * @return the text to be deleted
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets the text to be deleted.
     *
     * @param text
     *            the text to be deleted
     */
    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text may not be null");
        }
        this.text = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Delete(" + this.position + ",'" + this.text + "')";
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
            DeleteOperation op = (DeleteOperation) obj;
            return (op.position == this.position) && op.text.equals(this.text);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hashcode = this.position;
        hashcode += 13 * this.text.hashCode();
        return hashcode;
    }
}
