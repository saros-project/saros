/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.ITextOperation;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.intellij.editor.colorstorage.ColorModel;
import de.fu_berlin.inf.dpp.intellij.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.ResourceConverter;

import org.apache.log4j.Logger;

import java.awt.Color;

/**
 * This class applies the logic for activities that were received from remote.
 *
 */
public class LocalEditorManipulator {

    private static final Logger LOG = Logger
        .getLogger(LocalEditorManipulator.class);

    private final ProjectAPI projectAPI;
    private final EditorAPI editorAPI;

    /**
     * This is just a reference to {@link EditorManager}'s editorPool and not a
     * separate pool.
     */
    private EditorPool editorPool;

    private EditorManager manager;

    public LocalEditorManipulator(ProjectAPI projectAPI, EditorAPI editorAPI) {
        this.projectAPI = projectAPI;
        this.editorAPI = editorAPI;
    }

    /**
     * Initializes all fields that require an EditorManager.
     */
    public void initialize(EditorManager editorManager) {
        editorPool = editorManager.getEditorPool();
        manager = editorManager;
    }

    /**
     * Opens an editor for the given path, if it exists.
     *
     * @param path
     * @return the editor for the given path,
     *      or <code>null</code> if the file does not exist
     */
    public Editor openEditor(SPath path) {

        VirtualFile virtualFile = ResourceConverter.toVirtualFile(path);
        if (virtualFile.exists()) {
            //todo: in case it is already open, need to activate only, not open
            Editor editor = projectAPI.openEditor(virtualFile);
            manager.startEditor(editor);
            editorPool.add(path, editor);
            return editor;
        } else {
            LOG.warn("File not exist " + path);
        }
        return null;
    }

    /**
     * Closes the editor under path.
     *
     * @param path
     */
    public void closeEditor(SPath path) {
        VirtualFile virtualFile = ResourceConverter.toVirtualFile(path);
        if (virtualFile != null && virtualFile.exists()) {
            if (projectAPI.isOpen(virtualFile)) {
                Document doc = editorPool.getDocument(path);
                if (doc != null) {
                    manager.getDocumentListener().stopListening();
                }

                projectAPI.closeEditor(virtualFile);
            }
            editorPool.removeEditor(path);

        } else {
            LOG.warn("File not exist " + path);
        }
    }

    /**
     * Replaces the content of the document at the given path. The text is only
     * replaced, if the editor is writable.
     *
     * @param path path of the editor
     * @param text text to set the document's content to
     * @return Returns <code>true</code> if replacement was successful,
     *     <code>false</code> if the path was <code>null></code>, if the path points
     *     to a non-existing document or the document was not writable.
     */
    public boolean replaceText(SPath path, String text) {
        Document doc = editorPool.getDocument(path);
        if (doc == null) {
            return false;
        }
        if (!doc.isWritable()) {
            LOG.error("File to replace text in is not writeable: " + path);
            return false;
        }

        editorAPI.setText(doc, text);
        return true;
    }

    /**
     * Applies the text operations on the path and marks them in color.
     *
     * @param path
     * @param operations
     * @param color
     */
    public void applyTextOperations(SPath path, Operation operations,
        Color color) {
        Document doc = editorPool.getDocument(path);
        //If the document was not opened in an editor yet, it is not in the editorPool
        //so we have to create it temporarily here.
        if (doc == null) {
            VirtualFile virtualFile = ResourceConverter.toVirtualFile(path);
            doc = projectAPI.createDocument(virtualFile);
        }
         /*
         * Disable documentListener temporarily to avoid being notified of the
         * change
         */
        manager.disableDocumentListener();
        for (ITextOperation op : operations.getTextOperations()) {
            if (op instanceof DeleteOperation) {
                editorAPI.deleteText(doc, op.getPosition(),
                    op.getPosition() + op.getTextLength());
            } else {
                boolean writePermission = doc.isWritable();
                if (!writePermission) {
                    doc.setReadOnly(false);
                }
                editorAPI.insertText(doc, op.getPosition(), op.getText());
                if (!writePermission) {
                    doc.setReadOnly(true);
                }
                Editor editor = editorPool.getEditor(path);
                if (editor != null) {
                    editorAPI.textMarkAdd(editor, op.getPosition(),
                        op.getPosition() + op.getTextLength(), color);
                }
            }
        }

        manager.enableDocumentListener();
    }

    /**
     * Selects the specified range for the editor of the given path.
     *
     * @param path
     * @param position
     * @param length
     * @param colorModel
     */
    public void selectText(SPath path, int position, int length,
        ColorModel colorModel) {
        Editor editor = editorPool.getEditor(path);
        if (editor != null) {
            editorAPI.textMarkRemove(editor, colorModel.getSelect());
            RangeHighlighter highlighter = editorAPI
                .textMarkAdd(editor, position, position + length,
                    colorModel.getSelectColor());
            colorModel.setSelect(highlighter);
            //editorAPI.setSelection(editor, position, position + length,color); //todo: calculate new line char win and unix differences
        }
    }

    /**
     * Sets the viewport of the editor for path to the specified range.
     *
     * @param path
     * @param lineStart
     * @param lineEnd
     */
    public void setViewPort(final SPath path, final int lineStart,
        final int lineEnd) {
        Editor editor = editorPool.getEditor(path);
        if (editor != null) {
            editorAPI.setViewPort(editor, lineStart, lineEnd);
        }
    }

    /**
     * Removes all text marks from the path.
     *
     * @param path
     */
    public void clearSelection(SPath path) {
        Editor editor = editorPool.getEditor(path);
        if (editor != null) {
            editorAPI.textMarkRemove(editor, null);
        }
    }

    /**
     * Adjusts viewport. Focus is set on the center of the range, but priority
     * is given to selected lines.
     *
     * @param editor    Editor of the open Editor
     * @param range     viewport of the followed user. Must not be <code>null</code>.
     * @param selection text selection of the followed user. Must not be <code>null</code>.
     */
    public void adjustViewport(Editor editor, LineRange range,
        TextSelection selection) {
        if (editor == null || selection == null || range == null) {
            return;
        }

        editorAPI.setSelection(editor, selection.getOffset(),
            selection.getOffset() + selection.getLength(), null);
        editorAPI.setViewPort(editor, range.getStartLine(),
            range.getStartLine() + range.getNumberOfLines());

        //todo: implement actual viewport adjustment logic
    }
}
