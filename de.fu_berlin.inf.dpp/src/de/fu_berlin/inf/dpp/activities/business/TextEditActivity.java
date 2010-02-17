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
package de.fu_berlin.inf.dpp.activities.business;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * An immutable text activityDataObject.
 * 
 * @author rdjemili
 */
public class TextEditActivity extends AbstractActivity {

    protected final int offset;
    protected final String text;
    protected final String replacedText;
    protected final SPath editor;

    /**
     * @param offset
     *            the offset inside the document where this activityDataObject
     *            happened.
     * @param text
     *            the text that was inserted.
     * @param replacedText
     *            the text that was replaced by this activityDataObject.
     * @param editor
     *            path of the editor where this activityDataObject happened.
     * @param source
     *            JID of the user that caused this activityDataObject
     */
    public TextEditActivity(User source, int offset, String text,
        String replacedText, SPath editor) {

        super(source);
        if (text == null)
            throw new IllegalArgumentException("Text cannot be null");
        if (replacedText == null)
            throw new IllegalArgumentException("ReplacedText cannot be null");
        if (editor == null)
            throw new IllegalArgumentException("Editor cannot be null");

        this.offset = offset;
        this.text = text;
        this.replacedText = replacedText;
        this.editor = editor;
    }

    public int getOffset() {
        return offset;
    }

    public String getText() {
        return text;
    }

    public String getReplacedText() {
        return replacedText;
    }

    public SPath getEditor() {
        return this.editor;
    }

    @Override
    public String toString() {
        return "TextEditActivity("
            + this.offset
            + ",new:'"
            + Util.escapeForLogging(StringUtils.abbreviate(this.text, 150))
            + "',old:'"
            + Util.escapeForLogging(StringUtils.abbreviate(this.replacedText,
                150)) + "',path:" + this.editor.toString() + ",src:"
            + this.source + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((editor == null) ? 0 : editor.hashCode());
        result = prime * result + offset;
        result = prime * result
            + ((replacedText == null) ? 0 : replacedText.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof TextEditActivity))
            return false;

        TextEditActivity other = (TextEditActivity) obj;

        if (offset != other.offset)
            return false;

        if (!ObjectUtils.equals(this.editor, other.editor))
            return false;

        if (!ObjectUtils.equals(this.replacedText, other.replacedText))
            return false;

        if (!ObjectUtils.equals(this.text, other.text))
            return false;

        return true;
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
                && (this.replacedText.equals(other.replacedText));
        }
        return false;
    }

    /**
     * Convert this TextEditActivity to an Operation
     */
    public Operation toOperation() {

        // delete activityDataObject
        if ((replacedText.length() > 0) && (text.length() == 0)) {
            return new DeleteOperation(offset, replacedText);
        }

        // insert activityDataObject
        if ((replacedText.length() == 0) && (text.length() > 0)) {
            return new InsertOperation(offset, text);
        }

        // replace operation has to be split into delete and insert operation
        if ((replacedText.length() > 0) && (text.length() > 0)) {
            return new SplitOperation(
                new DeleteOperation(offset, replacedText), new InsertOperation(
                    offset, text));
        }

        // Cannot happen
        assert false;
        return null;
    }

    public boolean dispatch(IActivityConsumer consumer) {
        return consumer.consume(this);
    }

    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    public IActivityDataObject getActivityDataObject(ISharedProject project) {
        return new TextEditActivityDataObject(source.getJID(), offset, text,
            replacedText, editor.toSPathDataObject(project));
    }
}
