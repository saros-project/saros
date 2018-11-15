package de.fu_berlin.inf.dpp.intellij.project;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.openapi.vfs.encoding.EncodingProjectManager;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.FileActivity.Type;
import de.fu_berlin.inf.dpp.activities.FolderCreatedActivity;
import de.fu_berlin.inf.dpp.activities.FolderDeletedActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.editor.AbstractStoppableListener;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.ProjectAPI;
import de.fu_berlin.inf.dpp.intellij.editor.StoppableDocumentListener;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.AnnotationManager;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJFileImpl;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJPathImpl;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJProjectImpl;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJWorkspaceImpl;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Virtual file system listener. It receives events for all files in all
 * projects opened by the user.
 * It filters for files that are shared and creates the corresponding activities
 * dispatches these activities using
 * {@link SharedResourcesManager#fireActivity(IActivity)}.
 * <p>
 * The listener is enabled by default when the session context is created.
 * </p>
 *
 * @see VirtualFileListener
 */
//TODO decouple from SharedResourceManager and add to session context instead
public class FileSystemChangeListener extends AbstractStoppableListener
    implements VirtualFileListener {

    private static final Logger LOG = Logger
        .getLogger(FileSystemChangeListener.class);

    private final SharedResourcesManager resourceManager;
    private final IntelliJWorkspaceImpl intellijWorkspace;
    private final ISarosSession session;

    @Inject
    private ProjectAPI projectAPI;

    @Inject
    private AnnotationManager annotationManager;

    //HACK: This list is used to filter events for files that were created from
    //remote, because we can not disable the listener for them
    private final List<File> incomingFilesToFilterFor = new ArrayList<File>();

    public FileSystemChangeListener(SharedResourcesManager resourceManager,
        EditorManager editorManager, IntelliJWorkspaceImpl intellijWorkspace,
        ISarosSession session) {

        super(editorManager);

        this.resourceManager = resourceManager;
        this.intellijWorkspace = intellijWorkspace;
        this.session = session;

        SarosPluginContext.initComponent(this);

        intellijWorkspace.addResourceListener(this);
    }

    private void generateFolderMove(SPath oldSPath, SPath newSPath,
        boolean before) {
        User user = resourceManager.getSession().getLocalUser();
        IntelliJProjectImpl project = (IntelliJProjectImpl) oldSPath
            .getProject();
        IActivity createActivity = new FolderCreatedActivity(user, newSPath);
        resourceManager.internalFireActivity(createActivity);

        IFolder folder = before ? oldSPath.getFolder() : newSPath.getFolder();

        IResource[] members = new IResource[0];
        try {
            members = folder.members();
        } catch (IOException e) {
            LOG.error("error reading folder: " + folder, e);
        }

        for (IResource resource : members) {
            SPath oldChildSPath = new IntelliJFileImpl(project, new File(
                oldSPath.getFullPath().toOSString() + File.separator + resource
                    .getName())).getSPath();
            SPath newChildSPath = new IntelliJFileImpl(
                (IntelliJProjectImpl) newSPath.getProject(), new File(
                newSPath.getFullPath().toOSString() + File.separator + resource
                    .getName())).getSPath();
            if (resource.getType() == IResource.FOLDER) {
                generateFolderMove(oldChildSPath, newChildSPath, before);
            } else {
                generateFileMove(oldChildSPath, newChildSPath, before);
            }
        }

        IActivity removeActivity = new FolderDeletedActivity(user, oldSPath);
        resourceManager.internalFireActivity(removeActivity);

        project.addFile(newSPath.getFile().getLocation().toFile());
        project.removeResource(oldSPath.getProjectRelativePath());
    }

    private void generateFileMove(SPath oldSPath, SPath newSPath,
        boolean before) {
        User user = resourceManager.getSession().getLocalUser();
        IntelliJProjectImpl project = (IntelliJProjectImpl) newSPath
            .getProject();
        IntelliJProjectImpl oldProject = (IntelliJProjectImpl) oldSPath
            .getProject();

        IFile file;

        project.addFile(newSPath.getFile().getLocation().toFile());

        //TODO what happens if the other participant is working on the now renamed file
        if (before) {
            file = oldProject.getFile(oldSPath.getFullPath());
            editorManager.saveDocument(oldSPath);

            editorManager.replaceAllEditorsForPath(oldSPath, newSPath);
        } else {
            editorManager.replaceAllEditorsForPath(oldSPath, newSPath);

            file = project.getFile(newSPath.getFullPath());
            editorManager.saveDocument(newSPath);
        }

        oldProject.removeResource(oldSPath.getProjectRelativePath());

        byte[] bytes = FileUtils.getLocalFileContent(file);
        String charset = getEncoding(file);
        IActivity activity = new FileActivity(user, Type.MOVED,
            Purpose.ACTIVITY, newSPath, oldSPath, bytes, charset);

        resourceManager.internalFireActivity(activity);
    }

    /**
     * {@inheritDoc}
     * Works for all files in the application scope, including meta-files like
     * Intellij configuration files.
     * <p></p>
     * File changes done though an Intellij editor are processed in the
     * {@link StoppableDocumentListener} instead.
     *
     * @param virtualFileEvent {@inheritDoc}
     * @see StoppableDocumentListener
     */
    @Override
    public void beforeContentsChange(
            @NotNull
                    VirtualFileEvent virtualFileEvent) {

        assert enabled : "the before contents change listener was triggered while it was disabled";

        VirtualFile file = virtualFileEvent.getFile();

        if (LOG.isTraceEnabled()) {
            LOG.trace("Reacting before resource contents changed: " + file);
        }

        SPath path = VirtualFileConverter.convertToSPath(file);

        if (path == null || !session.isShared(path.getResource())) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(
                        "Ignoring non-shared resource's contents change: " + file);
            }

            return;
        }

        if (virtualFileEvent.isFromSave()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Ignoring contents change for " + file
                        + " as they were caused by a document save.");
            }

            //TODO dispatch save activity for saved file
            return;
        }

        if (virtualFileEvent.isFromRefresh()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Ignoring contents change for " + file
                        + " as they were caused by a filesystem snapshot refresh. "
                        + "This is already handled by the document listener.");
            }

            return;
        }

        //TODO figure out if this can happen
        LOG.warn("Detected unhandled content change on the virtual file level "
                + "for " + file + ", requested by: " + virtualFileEvent
                .getRequestor());
    }

    /**
     * {@inheritDoc}
     * <p></p>
     * Generates and dispatches a creation activity for the new resource.
     *
     * @param virtualFileEvent {@inheritDoc}
     */
    @Override
    public void fileCreated(
            @NotNull
                    VirtualFileEvent virtualFileEvent) {

        assert enabled : "the file created listener was triggered while it was disabled";

        VirtualFile createdVirtualFile = virtualFileEvent.getFile();

        if (LOG.isTraceEnabled()) {
            LOG.trace("Reacting to resource creation: " + createdVirtualFile);
        }

        SPath path = VirtualFileConverter.convertToSPath(createdVirtualFile);

        if (path == null || !session.isShared(path.getResource())) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Ignoring non-shared resource creation: "
                        + createdVirtualFile);
            }

            return;
        }

        User user = session.getLocalUser();

        IActivity activity;

        if (createdVirtualFile.isDirectory()) {
            activity = new FolderCreatedActivity(user, path);

        } else {
            String charset = createdVirtualFile.getCharset().name();

            byte[] content = getContent(createdVirtualFile);

            activity = new FileActivity(user, Type.CREATED,
                    FileActivity.Purpose.ACTIVITY, path, null, content, charset);

            editorManager.openEditor(path, false);
        }

        fireActivity(activity);
    }

    /**
     * {@inheritDoc}
     * <p></p>
     * Generates and dispatches a deletion activity for the deleted resource. If
     * the resource was a file, subsequently removes any editors for the file
     * from the editor pool and drops any held annotations for the file.
     *
     * @param virtualFileEvent {@inheritDoc}
     */
    @Override
    public void beforeFileDeletion(
            @NotNull
                    VirtualFileEvent virtualFileEvent) {

        assert enabled : "the before file deletion listener was triggered while it was disabled";

        VirtualFile deletedVirtualFile = virtualFileEvent.getFile();

        if (LOG.isTraceEnabled()) {
            LOG.trace(
                    "Reacting before resource deletion: " + deletedVirtualFile);
        }

        SPath path = VirtualFileConverter.convertToSPath(deletedVirtualFile);

        if (path == null || !session.isShared(path.getResource())) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Ignoring non-shared resource deletion: "
                        + deletedVirtualFile);
            }

            return;
        }

        User user = session.getLocalUser();

        IActivity activity;

        if (deletedVirtualFile.isDirectory()) {
            //TODO create deletion activities for child resources
            //TODO clean up editor pool and annotations for child resources
            activity = new FolderDeletedActivity(user, path);

        } else {
            activity = new FileActivity(user, Type.REMOVED,
                    FileActivity.Purpose.ACTIVITY, path, null, null, null);

            editorManager.removeAllEditorsForPath(path);

            annotationManager.removeAnnotations(path.getFile());
        }

        fireActivity(activity);

        //TODO reset the vector time for the deleted file or contained files if folder
    }

    @Override
    public void fileMoved(
        @NotNull
        VirtualFileMoveEvent virtualFileMoveEvent) {
        if (!enabled) {
            return;
        }

        File newFile = convertVirtualFileEventToFile(virtualFileMoveEvent);
        if (incomingFilesToFilterFor.remove(newFile)) {
            return;
        }

        IPath path = IntelliJPathImpl.fromString(newFile.getPath());
        IntelliJProjectImpl project = getProjectForResource(path);

        if (project == null || !isValidProject(project) ||
            !isCompletelyShared(project)) {
            return;
        }

        path = makeAbsolutePathProjectRelative(path, project);

        SPath newSPath = new SPath(project, path);

        IPath oldParent = IntelliJPathImpl
            .fromString(virtualFileMoveEvent.getOldParent().getPath());
        IPath oldPath = oldParent.append(virtualFileMoveEvent.getFileName());
        IProject oldProject = getProjectForResource(oldPath);

        oldPath = makeAbsolutePathProjectRelative(oldPath, project);
        SPath oldSPath = new SPath(oldProject, oldPath);

        //FIXME: Handle cases where files are moved from outside the shared project
        //into the shared project
        if (oldProject == null) {
            LOG.error(
                " can not move files from unshared project to shared project");
            return;
        }
        if (project.equals(oldProject)) {
            if (newFile.isFile()) {
                generateFileMove(oldSPath, newSPath, false);
            } else {
                generateFolderMove(oldSPath, newSPath, false);
            }
        }
    }

    @Override
    public void propertyChanged(
        @NotNull
        VirtualFilePropertyEvent filePropertyEvent) {
        if (!enabled) {
            return;
        }

        File oldFile = new File(
            filePropertyEvent.getFile().getParent().getPath() + File.separator
                + filePropertyEvent.getOldValue());
        File newFile = convertVirtualFileEventToFile(filePropertyEvent);

        if (incomingFilesToFilterFor.remove(newFile)) {
            return;
        }

        IPath oldPath = IntelliJPathImpl.fromString(oldFile.getPath());
        IntelliJProjectImpl project = getProjectForResource(oldPath);

        if (project == null || !isValidProject(project)) {
            return;
        }

        oldPath = makeAbsolutePathProjectRelative(oldPath, project);
        SPath oldSPath = new SPath(project, oldPath);

        IPath newPath = IntelliJPathImpl.fromString(newFile.getPath());
        newPath = makeAbsolutePathProjectRelative(newPath, project);

        SPath newSPath = new SPath(project, newPath);
        //we handle this as a move activity
        if (newFile.isFile()) {
            generateFileMove(oldSPath, newSPath, false);
        } else {
            generateFolderMove(oldSPath, newSPath, false);
        }
    }

    /**
     * {@inheritDoc}
     * <p></p>
     * Generates and dispatches creation activities for copied files. Copied
     * directories are handled by {@link #fileCreated(VirtualFileEvent)} and
     * contained files are subsequently handled by this listener.
     *
     * @param virtualFileCopyEvent {@inheritDoc}
     */
    @Override
    public void fileCopied(
            @NotNull
                    VirtualFileCopyEvent virtualFileCopyEvent) {

        assert enabled : "the file copied listener was triggered while it was disabled";

        VirtualFile copy = virtualFileCopyEvent.getFile();

        assert !copy
                .isDirectory() : "Unexpected copying event for directory. This should have been handled by the creation listener.";

        if (LOG.isTraceEnabled()) {
            LOG.trace("Reacting to resource copying - original: "
                    + virtualFileCopyEvent.getOriginalFile() + ", copy: " + copy);
        }

        SPath copyPath = VirtualFileConverter.convertToSPath(copy);

        if (copyPath == null || !session.isShared(copyPath.getResource())) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Ignoring non-shared resource copy: " + copy);
            }

            return;
        }

        User user = session.getLocalUser();
        String charset = copy.getCharset().name();

        byte[] content = getContent(copy);

        IActivity activity = new FileActivity(user, Type.CREATED,
                FileActivity.Purpose.ACTIVITY, copyPath, null, content, charset);

        fireActivity(activity);
    }

    @Override
    public void beforePropertyChange(
        @NotNull
        VirtualFilePropertyEvent filePropertyEvent) {
        // Not interested
    }

    @Override
    public void beforeFileMovement(
        @NotNull
        VirtualFileMoveEvent virtualFileMoveEvent) {
        //Do nothing
    }

    private IPath makeAbsolutePathProjectRelative(IPath path,
        IProject project) {
        return path.removeFirstSegments(project.getLocation().segmentCount());
    }

    private File convertVirtualFileEventToFile(
        VirtualFileEvent virtualFileEvent) {
        return new File(virtualFileEvent.getFile().getPath());
    }

    private String getEncoding(IFile file) {
        String charset = null;

        try {
            charset = file.getCharset();
        } catch (IOException e) {
            LOG.warn("could not determine encoding for file: " + file, e);
        }
        if (charset == null)
            return EncodingProjectManager.getInstance().getDefaultCharset()
                .name();

        return charset;
    }

    private boolean isCompletelyShared(IntelliJProjectImpl project) {
        return resourceManager.getSession().isCompletelyShared(project);
    }

    private boolean isValidProject(IntelliJProjectImpl project) {
        return project != null && project.exists();
    }

    /**
     * Searches for a resource with the passed path in the resources of all
     * currently with the session registered projects.
     *
     * @param path path to the resource
     * @return project with which the passed resources is registered or
     * <b>null</b> if no such project exists
     */
    private IntelliJProjectImpl getProjectForResource(IPath path){
        for(IProject sessionProject: resourceManager.getSession().getProjects()){

            IntelliJProjectImpl project = (IntelliJProjectImpl)sessionProject;

            if(project.isMember(path)){
                return project;
            }
        }
        
        return null;
    }

    /**
     * Returns the content of the given file. If available, the cached document
     * content representing the file held by Intellij will be used. Otherwise,
     * the file content on disk (obtained using the <code>VirtualFile</code>)
     * will be used.
     *
     * @param file the file to get the content for
     * @return the content for the given file (cached by Intellij or read from
     * disk if no cache is available) or an empty byte array if the content of
     * the file could not be obtained.
     * @see Document
     * @see VirtualFile
     */
    private byte[] getContent(VirtualFile file) {
        Document document = projectAPI.getDocument(file);

        try {
            if (document != null) {
                return document.getText().getBytes(file.getCharset().name());

            } else {
                LOG.debug("Could not get Document for file " + file
                        + ", using file content on disk instead. This content might"
                        + " not correctly represent the current state of the file"
                        + " in Intellij.");

                return file.contentsToByteArray();
            }

        } catch (IOException e) {
            LOG.warn("Could not get content for file " + file, e);

            return new byte[0];
        }
    }

    /**
     * Dispatches the given activity.
     *
     * @param activity the activity to fire
     * @see SharedResourcesManager#internalFireActivity(IActivity)
     */
    private void fireActivity(
            @NotNull
                    IActivity activity) {

        LOG.debug("Dispatching resource activity " + activity);

        resourceManager.internalFireActivity(activity);
    }

    /**
     * Enables or disables the filesystem listener. This is done by registering
     * or unregistering the listener.
     * <p>
     * This method does nothing if the given state already matches the
     * current state.
     * </p>
     *
     * @param enabled <code>true</code> to enable the listener,
     *                <code>false</code> to disable the listener
     */
    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled && !enabled) {
            LOG.trace("Disabling filesystem listener");

            this.enabled = false;

            intellijWorkspace.removeResourceListener(this);

        } else if (!this.enabled && enabled) {
            LOG.trace("Enabling filesystem listener");

            this.enabled = true;

            intellijWorkspace.addResourceListener(this);
        }
    }
}
