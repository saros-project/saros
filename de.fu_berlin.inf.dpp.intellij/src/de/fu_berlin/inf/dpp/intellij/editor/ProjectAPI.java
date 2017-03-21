package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
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

    private Project project;
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
        this.project = project;
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
     * @param path path of the file to open
     * @param activate activate editor after opening
     * @return Editor managing the passed file
     */
    public Editor openEditor(final VirtualFile path, final boolean activate){
        final AtomicReference<Editor> result = new AtomicReference<>();

        UIUtil.invokeAndWaitIfNeeded(new ReadAction(new Runnable() {
            @Override
            public void run() {
                result.set(editorFileManager.openTextEditor(
                    new OpenFileDescriptor(project,path), activate));
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
