package de.fu_berlin.inf.dpp.intellij.project;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;

import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.intellij.editor.AbstractStoppableListener;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJProjectImplV2;
import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Virtual file system listener. It receives events for all files in all projects
 * opened by the user.
 * <p/>
 * It filters for files that are shared and calls the corresponding methods for
 * {@link IActivity}-creation on the {@link SharedResourcesManager}.
 */
public class FileSystemChangeListener extends AbstractStoppableListener
    implements VirtualFileListener {

    private static final Logger LOG = Logger
        .getLogger(FileSystemChangeListener.class);
    private final SharedResourcesManager resourceManager;

    public FileSystemChangeListener(SharedResourcesManager resourceManager,
        EditorManager editorManager) {
        super(editorManager);
        this.resourceManager = resourceManager;
    }

    @Override
    public void contentsChanged(
        @NotNull
        VirtualFileEvent virtualFileEvent) {
        //NOP
    }

    @Override
    public void fileCreated(
        @NotNull
        VirtualFileEvent virtualFileEvent) {

        VirtualFile file = virtualFileEvent.getFile();

        IProject module = getModuleForFile(file);

        if (module != null) {
            LOG.error(file + " created in shared module " + module);
            SafeDialogUtils.showError("Saros/I detected the creation of " +
                file + " in the shared module " + module.getName() + ". " +
                "The creation of new shared files during a session is " +
                "currently not supported. This means that the current " +
                "session could behave in unexpected/unwanted ways and should " +
                "therefore not be continued.", "File creation not supported!");
        }
    }

    @Override
    public void fileDeleted(
        @NotNull
        VirtualFileEvent virtualFileEvent) {
        //NOP
    }

    @Override
    public void fileMoved(
        @NotNull
        VirtualFileMoveEvent virtualFileMoveEvent) {
        //NOP
    }

    @Override
    public void propertyChanged(
        @NotNull
        VirtualFilePropertyEvent filePropertyEvent) {
        //NOP
    }

    @Override
    public void fileCopied(
        @NotNull
        VirtualFileCopyEvent virtualFileCopyEvent) {

        VirtualFile file = virtualFileCopyEvent.getFile();
        VirtualFile originalFile = virtualFileCopyEvent.getOriginalFile();

        IProject module = getModuleForFile(file);

        if (module != null) {
            LOG.error(originalFile + " copied to " + file + " in shared " +
                "module " + module);
            SafeDialogUtils.showError("Saros/I detected the creation of " +
                file + " by copying " + originalFile + " in the " +
                "shared module " + module.getName() + ". The creation of " +
                "new shared files during a session is currently not " +
                "supported. This means that the current session could behave " +
                "in unexpected/unwanted ways and should therefore not be " +
                "continued.", "File creation not supported!");
        }
    }

    @Override
    public void beforePropertyChange(
        @NotNull
        VirtualFilePropertyEvent filePropertyEvent) {

        VirtualFile file = filePropertyEvent.getFile();
        String propertyName = filePropertyEvent.getPropertyName();
        Object oldValue = filePropertyEvent.getOldValue();
        Object newValue = filePropertyEvent.getNewValue();

        IProject module = getModuleForFile(file);

        if (module != null) {
            LOG.error(file + " property changed in shared module " + module +
                " ; property: " + propertyName +" ; old value: " + oldValue +
                " ; new value: " + newValue);
            SafeDialogUtils.showError("Saros/I detected a property change of " +
                file + " in the shared module " + module.getName() +". The " +
                "property \"" + propertyName + "\" was changed from " +
                oldValue + " to " + newValue + ". Changing the property of " +
                "shared files during a session is currently not supported. " +
                "This means that the current session could behave in " +
                "unexpected/unwanted ways and should therefore not be " +
                "continued.", "File property change not supported!");
        }
    }

    @Override
    public void beforeContentsChange(
        @NotNull
        VirtualFileEvent virtualFileEvent) {
        //Do nothing
    }

    @Override
    public void beforeFileDeletion(
        @NotNull
        VirtualFileEvent virtualFileEvent) {

        VirtualFile file = virtualFileEvent.getFile();

        IProject module = getModuleForFile(file);

        if (module != null) {
            LOG.error(file + " deleted in shared module " + module);
            SafeDialogUtils.showError("Saros/I detected the deletion of " +
                file + " in the shared module " + module.getName() + ". " +
                "The deletion of shared files during a session is " +
                "currently not supported. This means that the current " +
                "session could behave in unexpected/unwanted ways and should " +
                "therefore not be continued.", "File deletion not supported!");
        }
    }

    @Override
    public void beforeFileMovement(
        @NotNull
        VirtualFileMoveEvent virtualFileMoveEvent) {

        VirtualFile file = virtualFileMoveEvent.getFile();
        VirtualFile oldParent = virtualFileMoveEvent.getOldParent();
        VirtualFile newParent = virtualFileMoveEvent.getNewParent();

        IProject module = getModuleForFile(file);

        if (module != null) {
            LOG.error(file + " moved in shared module " + module +
                " ; old parent: " + oldParent + " ; new parent: " + newParent);
            SafeDialogUtils.showError("Saros/I detected the move of " +
                file + " in the shared module " + module.getName() + ". " +
                "The file was moved from " + oldParent + " to " + newParent +
                ". The move of shared files during a session is currently " +
                "not supported. This means that the current session could " +
                "behave in unexpected/unwanted ways and should therefore not " +
                "be continued.", "File move not supported!");
        }
    }

    /**
     * Returns module for given VirtualFile if held by session.
     */
    private IntelliJProjectImplV2 getModuleForFile(VirtualFile file) {
        for (IProject sessionModule : resourceManager.getSession()
            .getProjects()) {

            IntelliJProjectImplV2 module =
                (IntelliJProjectImplV2) sessionModule;

            if (module.getResource(file) != null) {
                return module;
            }
        }

        return null;
    }
}
