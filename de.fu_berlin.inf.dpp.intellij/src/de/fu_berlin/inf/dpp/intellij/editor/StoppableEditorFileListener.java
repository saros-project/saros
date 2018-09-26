package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.AnnotationManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

/**
 * IntelliJ editor file listener
 */
public class StoppableEditorFileListener extends AbstractStoppableListener
    implements FileEditorManagerListener {

    private static final Logger LOG = LogManager
        .getLogger(StoppableEditorFileListener.class);

    private final BeforeEditorActionListener beforeEditorActionListener;

    private final VirtualFileConverter virtualFileConverter;
    private final AnnotationManager annotationManager;

    private MessageBusConnection messageBusConnection;

    StoppableEditorFileListener(EditorManager manager,
        VirtualFileConverter virtualFileConverter,
        AnnotationManager annotationManager) {

        super(manager);

        this.virtualFileConverter = virtualFileConverter;
        this.annotationManager = annotationManager;

        this.beforeEditorActionListener = new BeforeEditorActionListener();
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

        Editor editor = editorManager.getLocalEditorHandler()
            .openEditor(virtualFile, false);

        SPath sPath = virtualFileConverter.convertToPath(virtualFile);

        if (sPath != null && editor != null) {
            annotationManager.applyStoredAnnotations(sPath.getFile(), editor);
        }
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
        messageBusConnection
            .subscribe(BeforeEditorActionListener.FILE_EDITOR_MANAGER,
                beforeEditorActionListener);
    }

    /**
     * Unsubscribes the editor listeners.
     */
    void unsubscribe(){
        messageBusConnection.disconnect();
    }

    /**
     * Intellij editor listener called <b>before</b> editors are opened or
     * closed
     */
    private class BeforeEditorActionListener
        extends FileEditorManagerListener.Before.Adapter {

        @Override
        public void beforeFileClosed(
            @NotNull
                FileEditorManager source,
            @NotNull
                VirtualFile virtualFile) {

            SPath sPath = virtualFileConverter.convertToPath(virtualFile);

            if (sPath != null) {
                IFile file = sPath.getFile();

                annotationManager.updateAnnotationStore(file);
                annotationManager.removeLocalRepresentation(file);
            }

        }
    }
}
