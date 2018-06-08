package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * IntelliJ editor file listener
 */
public class StoppableEditorFileListener extends AbstractStoppableListener
    implements FileEditorManagerListener {

    private static final Logger LOG = Logger
        .getLogger(StoppableEditorFileListener.class);

    private MessageBusConnection messageBusConnection;

    StoppableEditorFileListener(EditorManager manager) {
        super(manager);
    }

    /**
     * Calls {@link LocalEditorHandler#openEditor(VirtualFile,boolean)}.
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

        editorManager.getLocalEditorHandler().openEditor(virtualFile,false);
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

        VirtualFile virtualFile = event.getNewFile();

        if (!enabled || virtualFile == null) {
            return;
        }

        editorManager.getLocalEditorHandler().activateEditor(virtualFile);
    }

    /**
     * Subscribes the editor listeners to the given project.
     *
     * @param project the project whose file operations to listen to
     */
    void subscribe(
        @NotNull
            Project project) {

        if (messageBusConnection != null) {
            LOG.warn("Tried to register StoppableEditorListener that was "
                + "already registered");

            return;
        }

        messageBusConnection = project.getMessageBus().connect();

        messageBusConnection.subscribe(FILE_EDITOR_MANAGER, this);
    }

    /**
     * Unsubscribes the editor listeners.
     */
    void unsubscribe(){
        messageBusConnection.disconnect();
    }
}
