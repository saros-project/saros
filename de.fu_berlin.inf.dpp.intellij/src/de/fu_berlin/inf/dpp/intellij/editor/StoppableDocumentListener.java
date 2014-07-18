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
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import de.fu_berlin.inf.dpp.activities.SPath;
import org.apache.log4j.Logger;

/**
 * A document listener which informs the given EditorManager of changes before
 * they occur in a document (using documentAboutToBeChanged). The DocumentListener
 * is only added to the document that is currently being edited.
 * <p/>
 */
public class StoppableDocumentListener extends AbstractStoppableListener
    implements DocumentListener {

    private Document document;

    private static final Logger LOG = Logger
        .getLogger(StoppableDocumentListener.class);

    public StoppableDocumentListener(EditorManager editorManager) {
        super(editorManager);
    }

    /**
     * Calls
     * {@link EditorManager#generateTextEdit(int, String, String, SPath)}
     *
     * @param event
     */
    @Override
    public void beforeDocumentChange(DocumentEvent event) {
        if (!enabled) {
            return;
        }
        FileDocumentManager.getInstance().getFile(event.getDocument());
        SPath path = editorManager.getEditorPool().getFile(event.getDocument());
        if (path == null) {
            LOG.warn("Could not find path for editor " + event.getDocument());
            return;
        }

        String newText = event.getNewFragment().toString();
        String replacedText = event.getOldFragment().toString();

        editorManager
            .generateTextEdit(event.getOffset(), newText, replacedText, path);
    }

    /**
     * Does nothing.
     *
     * @param event
     */
    @Override
    public void documentChanged(DocumentEvent event) {
        // do nothing. We handled everything in documentAboutToBeChanged
    }

    public Document getDocument() {
        return document;
    }

    /**
     * Removes this listener from the document.
     */
    public void stopListening() {
        if (document != null) {
            document.removeDocumentListener(this);
            document = null;
        }
    }

    /**
     * Removes itself from previously listened documents and registers itself with the new document.
     *
     * @param newDocument
     */
    public void startListening(Document newDocument) {
        if (document == null) {
            document = newDocument;
            document.addDocumentListener(this);
            //TODO: replace by equals?
        } else if (document != newDocument) {
            document.removeDocumentListener(this);
            document = newDocument;
            document.addDocumentListener(this);
        } else {
            //do nothing, as we already listen to that document
        }
    }

}