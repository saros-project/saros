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

/**
 * A simple immutable text activity.
 * 
 * @author rdjemili
 */
public class TextEditActivity implements IActivity {
    public final int offset;

    private String source = null;

    private String originalSource = null;

    private IPath editor;
    /**
     * This string only uses \n as line delimiter. Keep this in mind when adding
     * it to an IDocument with probably other line delimiters.
     */
    public String text;

    public final int length;

    /**
     * @param offset
     *            the offset inside the document where this activity happened.
     * @param text
     *            the text that was inserted.
     * @param length
     *            the length of text that was replaced by this activity.
     */
    public TextEditActivity(int offset, String text, int length) {
        this.offset = offset;
        this.text = text;
        this.length = length;
    }

    /**
     * @param offset
     *            the offset inside the document where this activity happened.
     * @param text
     *            the text that was inserted.
     * @param length
     *            the length of text that was replaced by this activity.
     * @param editor
     *            path of the editor where this activity happened.
     */
    public TextEditActivity(int offset, String text, int length, IPath editor) {
        this(offset, text, length);
        this.editor = editor;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextEditActivity) {
            TextEditActivity other = (TextEditActivity) obj;
            return (this.offset == other.offset)
                && this.text.equals(other.text)
                && (this.length == other.length)
                && (this.source == other.source);
        }

        return false;
    }

    @Override
    public String toString() {
        return "TextEditActivity(offset:" + this.offset + ",text:" + this.text
            + ",length:" + this.length + ",path:" + this.editor.toString()
            + ",src:" + this.source + ",oSrc:" + this.originalSource + ")";
    }

    /**
     * Compare text edit information without source settings.
     * 
     * @param obj
     *            TextEditActivity Object
     * @return true if edit information equals. false otherwise.
     */
    public boolean sameLike(Object obj) {
        if (obj instanceof TextEditActivity) {
            TextEditActivity other = (TextEditActivity) obj;
            return (this.offset == other.offset) && (this.editor != null)
                && (other.editor != null) && this.editor.equals(other.editor)
                && this.text.equals(other.text)
                && (this.length == other.length);
        }
        return false;
    }

    public IPath getEditor() {
        return this.editor;
    }

    public void setEditor(IPath editor) {
        this.editor = editor;
    }
}
