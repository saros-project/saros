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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;

import java.util.concurrent.atomic.AtomicReference;

/**
 * IntellIJ API for project-level operations on editors and documents.
 */
public class ProjectAPI {

    private Application application;
    private FileDocumentManager fileDocumentManager;

    private FileEditorManager editorFileManager;

    private class WriteAction implements Runnable {
        Runnable action;

        WriteAction(Runnable action) {
            this.action = action;
        }

        @Override
        public void run() {
            application.runWriteAction(action);
        }
    }

    private class ReadAction implements Runnable {
        Runnable action;

        ReadAction(Runnable action) {
            this.action = action;
        }

        @Override
        public void run() {
            application.runReadAction(action);
        }
    }

    /**
     * Creates an ProjectAPI with the current Project and initializes Fields.
     */
    public ProjectAPI(Project project) {
        this.editorFileManager = FileEditorManager.getInstance(project);

        this.application = ApplicationManager.getApplication();
        this.fileDocumentManager = FileDocumentManager.getInstance();
    }

    /**
     * Returns whether the file is opened.
     *
     * @param file
     * @return
     */
    public boolean isOpen(VirtualFile file) {
        return editorFileManager.isFileOpen(file);
    }

    /**
     * Returns whether the document is opened.
     *
     * @param doc
     * @return
     */
    public boolean isOpen(Document doc) {
        VirtualFile file = fileDocumentManager.getFile(doc);
        return isOpen(file);
    }

    /**
     * Opens an editor for the given path in the UI thread.
     *
     * @param path
     * @return
     */
    public Editor openEditor(final VirtualFile path) {

        final AtomicReference<Editor> result = new AtomicReference<Editor>();

        UIUtil.invokeAndWaitIfNeeded(new ReadAction(new Runnable() {
            @Override
            public void run() {
                editorFileManager.openFile(path, true);
                result.set(editorFileManager.getSelectedTextEditor());
            }
        }));

        return result.get();

    }

    /**
     * Creates the given document.
     *
     * @param path
     * @return
     */
    public Document createDocument(VirtualFile path) {
        return fileDocumentManager.getDocument(path);
    }

    /**
     * Closes the editor for the given file in the UI thread.
     *
     * @param file
     */
    public void closeEditor(final VirtualFile file) {

        Runnable action = new Runnable() {
            @Override
            public void run() {
                editorFileManager.closeFile(file);
            }
        };

        UIUtil.invokeAndWaitIfNeeded(action);
    }

    public void closeEditor(Document doc) {
        VirtualFile file = fileDocumentManager.getFile(doc);
        closeEditor(file);
    }

    public Editor getActiveEditor() {
        return editorFileManager.getSelectedTextEditor();
    }

    /**
     * Saves the given document in the UI thread.
     *
     * @param doc
     */
    public void saveDocument(final Document doc) {
        application.invokeAndWait(new WriteAction(new Runnable() {
            @Override
            public void run() {
                fileDocumentManager.saveDocument(doc);
            }
        }), ModalityState.NON_MODAL);

    }

    /**
     * Reloads the current document in the UI thread.
     *
     * @param doc
     */
    public void reloadFromDisk(final Document doc) {
        application.invokeAndWait(new ReadAction(new Runnable() {
            @Override
            public void run() {
                fileDocumentManager.reloadFromDisk(doc);
            }
        }), ModalityState.NON_MODAL);
    }

    /**
     * Saves all documents in the UI thread.
     */
    public void saveAllDocuments() {

        application.invokeAndWait(new WriteAction(new Runnable() {
            @Override
            public void run() {
                fileDocumentManager.saveAllDocuments();
            }
        }), ModalityState.NON_MODAL);

    }

    public void addFileEditorManagerListener(
        StoppableEditorFileListener listener) {
        editorFileManager.addFileEditorManagerListener(listener);
    }
}
