/*
 * $Id: InsertOperation.java 2755 2006-03-06 09:29:34Z zbinl $
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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * The InsertOperation is used to hold a text together with its position index.
 * The text is to be inserted in the document model.
 */
public class InsertOperation implements Operation {

    private static final long serialVersionUID = -1285726114177092936L;

    /**
     * the text to be inserted.
     */
    private String text;

    /**
     * the position index in the document model.
     */
    private int position;

    /**
     * the origin position index where the insert operation was originally
     * intended. This concept could be extended in such a way that two origin
     * positions could be compared to each other based on the same context.
     * Therefore, if the two positions do not relate on the same document
     * context, a least synchronization point (LSP) would have to be determined.
     */
    private int origin;

    /**
     * Class constructor.
     * 
     * @param position
     *            the position in the document
     * @param text
     *            the text to be inserted
     */
    public InsertOperation(int position, String text) {
        setPosition(position);
        setText(text);
        this.origin = getPosition();
    }

    /**
     * Class constructor.
     * 
     * @param position
     *            the position in the document
     * @param text
     *            the text to be inserted
     * @param isUndo
     *            flag to indicate an undo operation
     */
    public InsertOperation(int position, String text, boolean isUndo) {
        this(position, text);
        this.origin = getPosition();
    }

    /**
     * Class constructor.
     * 
     * @param position
     *            the position in the document
     * @param text
     *            the text to be inserted
     * @param origin
     *            the origin position of this insert operation
     */
    public InsertOperation(int position, String text, int origin) {
        this(position, text);
        this.origin = origin;
    }

    /**
     * @param position
     *            the position in the document
     * @param text
     *            the text to be inserted
     * @param origin
     *            the origin position of this insert operation
     * @param isUndo
     *            flag to indicate an undo operation
     */
    public InsertOperation(int position, String text, int origin, boolean isUndo) {
        this(position, text, origin);
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
     * Returns the text to be deleted.
     * 
     * @return the text to be deleted
     */
    public String getText() {
        return this.text;
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
     * Returns the origin position.
     * 
     * @return the origin position
     */
    public int getOrigin() {
        return this.origin;
    }

    /**
     * Sets the origin position.
     * 
     * @param origin
     *            the origin position to set
     */
    public void setOrigin(int origin) {
        if (origin < 0) {
            throw new IllegalArgumentException("origin index must be >= 0");
        }
        this.origin = origin;
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
        return "Insert(" + this.position + ",'"
            + Util.escapeForLogging(this.text) + "'," + this.origin + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj.getClass().equals(getClass())) {
            InsertOperation op = (InsertOperation) obj;
            return (op.position == this.position) && op.text.equals(this.text)
                && (op.origin == this.origin);
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
        hashcode += 13 * this.origin;
        hashcode += 13 * this.text.hashCode();
        return hashcode;
    }

    public List<TextEditActivity> toTextEdit(IPath path, String source) {
        return Collections.singletonList(new TextEditActivity(getPosition(),
            getText(), "", path, source));
    }

}
