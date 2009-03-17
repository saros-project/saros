/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.activities;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.util.Util;

public class TextSelectionActivity extends AbstractActivity {
    private final int offset;

    private final int length;

    private IPath editor;

    public TextSelectionActivity(int offset, int length, IPath path) {
        this.offset = offset;
        this.length = length;
        this.editor = path;
    }

    public int getLength() {
        return this.length;
    }

    public int getOffset() {
        return this.offset;
    }

    public IPath getEditor() {
        return this.editor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextSelectionActivity) {
            TextSelectionActivity activity = (TextSelectionActivity) obj;
            return (this.offset == activity.offset)
                && (this.length == activity.length)
                && (this.source.equals(activity.source))
                && (this.editor == activity.editor);
        }

        return false;
    }

    @Override
    public String toString() {
        return "TextSelectionActivity(offset:" + this.offset + ",length:"
            + this.length + ",src:" + getSource() + ",path:" + this.editor
            + ")";
    }

    public boolean dispatch(IActivityReceiver receiver) {
        return receiver.receive(this);
    }

    public void toXML(StringBuilder sb) {
        assert getEditor() != null;

        sb.append("<textSelection ");
        sb.append("offset=\"").append(getOffset()).append("\" ");
        sb.append("length=\"").append(getLength()).append("\" ");
        sb.append("editor=\"").append(
            Util.escapeCDATA(getEditor().toPortableString())).append("\"");
        sb.append(" />");
    }
}
