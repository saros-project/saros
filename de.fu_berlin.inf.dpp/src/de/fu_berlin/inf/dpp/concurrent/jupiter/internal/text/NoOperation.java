/*
 * $Id$
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

/**
 * The NoOperation is used to hold a empty text together with the position zero.
 */
public class NoOperation implements Operation {

    public int getPosition() {
        return 0;
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
     * Returns the text length.
     * 
     * @return the length of the text
     */
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
        return "Noop(0,'')";
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
        int hashcode = 37;
        return hashcode;
    }

    public List<TextEditActivity> toTextEdit(IPath path, String source) {
        return Collections.emptyList();
    }
}
