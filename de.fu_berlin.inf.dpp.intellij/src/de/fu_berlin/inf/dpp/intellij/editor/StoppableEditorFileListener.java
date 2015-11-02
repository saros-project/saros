package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * IntelliJ editor file listener
 */
public class StoppableEditorFileListener extends AbstractStoppableListener
    implements FileEditorManagerListener {

    public StoppableEditorFileListener(EditorManager manager) {
        super(manager);
    }

    /**
     * Calls {@link LocalEditorHandler#openEditor(VirtualFile)}.
     *
     * @param fileEditorManager
     * @param virtualFile
     */
    @Override
    public void fileOpened(
        @NotNull
        FileEditorManager fileEditorManager,
        @NotNull
        VirtualFile virtualFile) {
        if (!enabled) {
            return;
        }

        editorManager.getLocalEditorHandler().openEditor(virtualFile);
    }

    /**
     * Calls {@link LocalEditorHandler#closeEditor(VirtualFile)}.
     *
     * @param fileEditorManager
     * @param virtualFile
     */
    @Override
    public void fileClosed(
        @NotNull
        FileEditorManager fileEditorManager,
        @NotNull
        VirtualFile virtualFile) {
        if (!enabled) {
            return;
        }

        editorManager.getLocalEditorHandler().closeEditor(virtualFile);
    }

    /**
     * Calls {@link LocalEditorHandler#activateEditor(VirtualFile)}.
     *
     * @param event
     */
    @Override
    public void selectionChanged(
        @NotNull
        FileEditorManagerEvent event) {
        if (!enabled) {
            return;
        }

        editorManager.getLocalEditorHandler()
            .activateEditor(event.getNewFile());
    }
}
