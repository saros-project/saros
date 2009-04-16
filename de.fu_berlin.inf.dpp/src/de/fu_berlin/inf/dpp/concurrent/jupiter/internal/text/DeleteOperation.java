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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.util.Util;

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
     * @param text
     *            the text to be deleted
     */
    public DeleteOperation(int position, String text) {
        setPosition(position);
        setText(text);
    }

    /**
     * @param position
     *            the position in the document
     * @param text
     *            the text to be deleted
     * @param isUndo
     *            flag to indicate whether this operation is an undo
     */
    public DeleteOperation(int position, String text, boolean isUndo) {
        setPosition(position);
        setText(text);
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("position index must be >= 0");
        }
        this.position = position;
    }

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
        return "Delete(" + this.position + ",'"
            + Util.escapeForLogging(this.text) + "')";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DeleteOperation other = (DeleteOperation) obj;
        if (position != other.position)
            return false;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + position;
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    public List<TextEditActivity> toTextEdit(IPath path, String source) {
        return Collections.singletonList(new TextEditActivity(source,
            getPosition(), "", getText(), path));
    }
}
