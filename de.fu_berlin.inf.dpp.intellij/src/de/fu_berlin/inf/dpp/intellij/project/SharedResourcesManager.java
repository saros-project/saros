package de.fu_berlin.inf.dpp.intellij.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderCreatedActivity;
import de.fu_berlin.inf.dpp.activities.FolderDeletedActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IFileSystemModificationActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorManipulator;
import de.fu_berlin.inf.dpp.intellij.editor.SelectedEditorState;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.AnnotationManager;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJWorkspaceImpl;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.ISarosSession;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.Startable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The SharedResourcesManager creates and handles file and folder activities.
 */
public class SharedResourcesManager extends AbstractActivityProducer
    implements Startable {

    private static final Logger LOG = Logger
        .getLogger(SharedResourcesManager.class);

    private static final int DELETION_FLAGS = 0;
    private static final boolean FORCE = false;

    private final ISarosSession sarosSession;

    private final FileSystemChangeListener fileSystemListener;

    /**
     * Should return <code>true</code> while executing resource changes to avoid
     * an infinite resource event loop.
     */
    private final FileReplacementInProgressObservable fileReplacementInProgressObservable;

    private final LocalEditorHandler localEditorHandler;

    private final LocalEditorManipulator localEditorManipulator;

    private final IntelliJWorkspaceImpl intelliJWorkspaceImpl;

    private final AnnotationManager annotationManager;

    @Override
    public void start() {
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {

            @Override
            public void run() {
                sarosSession.addActivityProducer(SharedResourcesManager.this);
                sarosSession.addActivityConsumer(consumer, Priority.ACTIVE);
                intelliJWorkspaceImpl.addResourceListener(fileSystemListener);

            }
        }, ModalityState.defaultModalityState());
    }

    @Override
    public void stop() {
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {

            @Override
            public void run() {
                intelliJWorkspaceImpl
                    .removeResourceListener(fileSystemListener);
                sarosSession
                    .removeActivityProducer(SharedResourcesManager.this);
                sarosSession.removeActivityConsumer(consumer);
            }
        }, ModalityState.defaultModalityState());
    }

    public SharedResourcesManager(ISarosSession sarosSession,
        EditorManager editorManager,
        FileReplacementInProgressObservable fileReplacementInProgressObservable,
        LocalEditorHandler localEditorHandler,
        LocalEditorManipulator localEditorManipulator,
        IntelliJWorkspaceImpl intelliJWorkspaceImpl,
        AnnotationManager annotationManager) {

        this.sarosSession = sarosSession;
        this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
        this.localEditorHandler = localEditorHandler;
        this.localEditorManipulator = localEditorManipulator;
        fileSystemListener = new FileSystemChangeListener(this, editorManager);
        this.intelliJWorkspaceImpl = intelliJWorkspaceImpl;
        this.annotationManager = annotationManager;
    }

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void exec(IActivity activity) {
            if (!(activity instanceof IFileSystemModificationActivity)) {
                return;
            }

        /*
         * FIXME this will lockout everything. File changes made in the
         * meantime from another background job are not recognized. See
         * AddMultipleFilesTest STF test which fails randomly.
         */
            fileReplacementInProgressObservable.startReplacement();
            fileSystemListener.setEnabled(false);
            super.exec(activity);

            LOG.trace("execing " + activity + " in " + Thread.currentThread()
                .getName());

            fileReplacementInProgressObservable.replacementDone();
            fileSystemListener.setEnabled(true);
            LOG.trace("done execing " + activity);
        }

        @Override
        public void receive(FileActivity activity) {
            try {
                handleFileActivity(activity);
            } catch (IOException e) {
                LOG.error("Failed to execute activity: " + activity, e);
            }
        }

        @Override
        public void receive(FolderCreatedActivity activity) {
            try {
                handleFolderActivity(activity);
            } catch (IOException e) {
                LOG.error("Failed to execute activity: " + activity, e);
            }
        }

        @Override
        public void receive(FolderDeletedActivity activity) {
            try {
                handleFolderActivity(activity);
            } catch (IOException e) {
                LOG.error("Failed to execute activity: " + activity, e);
            }
        }
    };

    private void handleFileActivity(
        @NotNull
            FileActivity activity) throws IOException {

        if (activity.isRecovery()) {
            handleFileRecovery(activity);
            return;
        }

        switch (activity.getType()) {
        case CREATED:
            handleFileCreation(activity);
            break;
        case REMOVED:
            handleFileDeletion(activity);
            break;
        case MOVED:
            handleFileMove(activity);
            break;
        default:
            throw new UnsupportedOperationException(
                "FileActivity type " + activity.getType()
                    + " not supported. Dropped activity: " + activity);
        }
    }

    private void handleFileRecovery(FileActivity activity) throws IOException {
        SPath path = activity.getPath();

        LOG.debug("performing recovery for file: " + activity.getPath()
            .getFullPath());

        FileActivity.Type type = activity.getType();

        try {
            if (type == FileActivity.Type.CREATED) {
                handleFileCreation(activity);
            } else if (type == FileActivity.Type.REMOVED) {
                localEditorManipulator.closeEditor(path);
                handleFileDeletion(activity);
            } else {
                LOG.warn("performing recovery for type " + type
                    + " is not supported");
            }
        } finally {
            /*
             * always reset Jupiter or we will get into trouble because the
             * vector time is already reseted on the host
             */
            sarosSession.getConcurrentDocumentClient().reset(path);
        }
    }

    /**
     * Applies the given move FileActivity. Subsequently cleans up the
     * EditorPool and AnnotationManager for the moved file if necessary.
     *
     * @param activity the move activity to execute
     * @throws IOException if the creation of the new file or the deletion of
     *                     the old file fails
     */
    private void handleFileMove(
        @NotNull
            FileActivity activity) throws IOException {

        SPath oldPath = activity.getOldPath();
        SPath newPath = activity.getPath();

        IFile oldFile = oldPath.getFile();
        IFile newFile = newPath.getFile();

        if (!oldFile.exists()) {
            LOG.warn("Could not move file " + oldFile + " as it does not exist."
                + " source: " + oldFile + " destination: " + newFile);

            return;
        }

        boolean fileOpen = localEditorHandler.isOpenEditor(oldPath);

        SelectedEditorState selectedEditorState = null;

        if (fileOpen) {
            selectedEditorState = new SelectedEditorState();
            selectedEditorState.captureState();
        }

        localEditorManipulator.closeEditor(oldPath);

        annotationManager.updateAnnotationPath(oldFile, newFile);

        try {
            fileSystemListener.setEnabled(false);

            localEditorHandler.saveDocument(oldPath);

            newFile.create(oldFile.getContents(), FORCE);

            if (fileOpen) {
                localEditorManipulator.openEditor(newPath, false);

                try {
                    selectedEditorState.replaceSelectedFile(oldFile, newFile);
                } catch (IllegalStateException e) {
                    LOG.warn(
                        "Failed to update the captured selected editor state",
                        e);
                }

                selectedEditorState.applyCapturedState();
            }

            oldFile.delete(DELETION_FLAGS);

        } finally {
            fileSystemListener.setEnabled(true);
        }

        //TODO reset the vector time for the old file
    }

    private void handleFileDeletion(
        @NotNull
            FileActivity activity) throws IOException {

        SPath path = activity.getPath();
        IFile file = path.getFile();

        if (!file.exists()) {
            LOG.warn(
                "Could not delete file " + file + " as it does not exist.");

            return;
        }

        if (localEditorHandler.isOpenEditor(path)) {
            localEditorManipulator.closeEditor(path);
        }

        try {
            fileSystemListener.setEnabled(false);

            localEditorHandler.saveDocument(path);

            file.delete(DELETION_FLAGS);

        } finally {
            fileSystemListener.setEnabled(true);
        }

        annotationManager.removeAnnotations(file);

        //TODO reset the vector time for the deleted file
    }

    private void handleFileCreation(
        @NotNull
            FileActivity activity) throws IOException {

        SPath path = activity.getPath();
        IFile file = path.getFile();

        if (file.exists()) {
            LOG.warn(
                "Could not create file " + file + " as it already exists.");

            return;
        }

        InputStream contents = new ByteArrayInputStream(activity.getContent());

        try {
            fileSystemListener.setEnabled(false);

            file.create(contents, FORCE);

        } finally {
            fileSystemListener.setEnabled(true);
        }
    }

    private void handleFolderActivity(IFileSystemModificationActivity activity)
        throws IOException {

        SPath path = activity.getPath();

        IFolder folder = path.getProject()
            .getFolder(path.getProjectRelativePath());
        fileSystemListener.setEnabled(false);
        //HACK: It does not work to disable the fileSystemListener temporarly,
        //because a fileCreated event will be fired asynchronously,
        //so we have to add this file to the filter list
        try {
            if (activity instanceof FolderCreatedActivity) {
                FileUtils.create(folder);
            } else if (activity instanceof FolderDeletedActivity) {

                if (folder.exists()) {
                    FileUtils.delete(folder);
                }

            }
            fileSystemListener
                .addIncomingFileToFilterFor(folder.getLocation().toFile());
        } finally {
            fileSystemListener.setEnabled(true);
        }
    }

    void internalFireActivity(IActivity activity) {
        // HACK for now
        if (fileReplacementInProgressObservable.isReplacementInProgress()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(
                    "File replacement in progress - Ignoring local activity "
                        + activity);
            }

            return;
        }

        fireActivity(activity);
    }

    public ISarosSession getSession() {
        return sarosSession;
    }
}