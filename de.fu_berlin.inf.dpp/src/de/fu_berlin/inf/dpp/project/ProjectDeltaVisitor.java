package de.fu_berlin.inf.dpp.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.util.FileUtils;

/* TODO refactor the error handling, either handle Core/IOExceptions in all methods or throw them up to handle them only in one place */

/**
 * Visits the resource changes in a shared project.<br>
 * The visitor is not supposed to be reused for different resourceChanged
 * events.
 */
public class ProjectDeltaVisitor implements IResourceDeltaVisitor {
    private static final Logger log = Logger
        .getLogger(ProjectDeltaVisitor.class);
    protected final EditorManager editorManager;

    protected final User user;

    protected final ISarosSession sarosSession;

    /** The project visited. */
    protected final SharedProject sharedProject;

    public ProjectDeltaVisitor(EditorManager editorManager,
        ISarosSession sarosSession, SharedProject sharedProject) {
        this.sarosSession = sarosSession;
        this.editorManager = editorManager;
        this.sharedProject = sharedProject;
        this.user = sarosSession.getLocalUser();
    }

    /** Stores activities to be sent due to one change event. */
    protected List<IResourceActivity> pendingActivities = new ArrayList<IResourceActivity>();
    /**
     * Don't generate activities for a resource if
     * <code>ignoredPath.isPrefixOf(resource.getFullPath())</code>.
     */
    private IPath ignoredPath = null;

    /**
     * Don't send activities yet.
     */
    private boolean postponeSending = false;

    @Override
    public boolean visit(IResourceDelta delta) {
        IResource resource = delta.getResource();
        if (resource.isDerived()) {
            return false;
        }

        /*
         * TODO Refactor this, we don't need to make a distinction here.
         * Resource is resource. It's just Saros that insists on having separate
         * activities for files and folders.
         */
        if (resource instanceof IFile) {
            handleFileDelta(delta);
            return true;
        } else if (resource instanceof IFolder) {
            // Note: IProject is not visited.
            return handleFolderDelta(delta);
        }

        return true;
    }

    protected boolean handleFolderDelta(IResourceDelta delta) {
        IResource resource = delta.getResource();

        int kind = delta.getKind();
        switch (kind) {
        case IResourceDelta.ADDED:
            add(resource);
            return true;
        case IResourceDelta.REMOVED:
            remove(resource);
            // We don't want to visit the children if this folder was removed.
            // The only interesting case is that a file was moved out of this or
            // a child folder, but we're still going to visit the move target.
            setIgnoreChildren(resource);
            return true;
        default:
            final boolean change = kind != IResourceDelta.NO_CHANGE;
            return change;
        }
    }

    protected void handleFileDelta(IResourceDelta delta) {
        IResource resource = delta.getResource();
        int kind = delta.getKind();

        IProject project = resource.getProject();
        boolean contentChange = isContentChange(delta);
        switch (kind) {
        case IResourceDelta.CHANGED:
            if (contentChange)
                contentChanged(resource);

            return;

        case IResourceDelta.ADDED:

            // Was this file moved or renamed?
            if (isMovedFrom(delta)) {

                // Adds have getMovedFrom set:
                IPath oldFullPath = delta.getMovedFromPath();
                IProject oldProject = ProjectDeltaVisitor
                    .getProject(oldFullPath);

                if (project.equals(oldProject)) {
                    // Moving inside this project
                    try {
                        move(resource, oldFullPath, oldProject, contentChange);
                        return;
                    } catch (IOException e) {
                        log.warn("Resource could not be read for"
                            + " sending to peers:" + resource.getLocation());
                    }
                } else {
                    // Moving a file into the shared project
                    // -> Treat like an add!

                    // Fall-through
                }
            }

            // usual files adding procedure

            add(resource);

            return;

        case IResourceDelta.REMOVED:
            if (isMoved(delta)) {

                // REMOVED deltas have MovedTo set
                IPath newPath = delta.getMovedToPath();
                IProject newProject = ProjectDeltaVisitor.getProject(newPath);
                if (project.equals(newProject)) {
                    // Ignore "REMOVED" while moving into shared project
                    return;
                }
                // else moving file away from shared project, need to tell
                // others to delete! Fall-through...
            }

            remove(resource);
            return;

        default:
            return;
        }
    }

    /** Indicates not to generate activities for children of the resource. */
    protected void setIgnoreChildren(IResource resource) {
        if (!ignoreChildren(resource))
            ignoredPath = resource.getFullPath();
    }

    /**
     * Returns true if we're not supposed to generate activities for children of
     * the resource.
     */
    protected boolean ignoreChildren(IResource resource) {
        IPath fullPath = resource.getFullPath();
        return ignoreChildren(fullPath);
    }

    /**
     * {@link #ignoreChildren(IResource)}
     */
    protected boolean ignoreChildren(IPath fullPath) {
        if (ignoredPath == null || !ignoredPath.isPrefixOf(fullPath)) {
            ignoredPath = null;
            return false;
        }
        return true;
    }

    protected void add(IResource resource) {
        sharedProject.add(resource);
        final SPath spath = new SPath(ResourceAdapterFactory.create(resource));

        if (resource.getType() == IResource.FILE) {
            byte[] content = FileUtils.getLocalFileContent((IFile) resource
                .getAdapter(IFile.class));

            if (content == null)
                log.error("could not read contents of file: "
                    + resource.getFullPath());
            else
                // TODO add encoding
                addActivity(FileActivity.created(user, spath, content, null,
                    Purpose.ACTIVITY));

        } else {
            addActivity(new FolderActivity(user, FolderActivity.Type.CREATED,
                spath));
        }
    }

    protected void move(IResource resource, IPath oldFullPath,
        IProject oldProject, boolean contentChange) throws IOException {
        sharedProject.move(resource, oldFullPath);

        byte[] content = null;

        if (contentChange) {
            assert resource.getType() == IResource.FILE;

            content = FileUtils.getLocalFileContent((IFile) resource
                .getAdapter(IFile.class));

            if (content == null)
                throw new IOException();
        }

        // TODO add encoding
        addActivity(FileActivity.moved(
            user,
            new SPath(ResourceAdapterFactory.create(resource)),
            new SPath(ResourceAdapterFactory.create(oldProject),
                ResourceAdapterFactory.create(oldFullPath
                    .removeFirstSegments(1))), content, null));
    }

    protected void remove(IResource resource) {
        sharedProject.remove(resource);
        if (resource instanceof IFile) {
            addActivity(FileActivity.removed(user, new SPath(
                ResourceAdapterFactory.create(resource)), Purpose.ACTIVITY));
        } else {
            addActivity(new FolderActivity(user, FolderActivity.Type.REMOVED,
                new SPath(ResourceAdapterFactory.create(resource))));
        }
    }

    protected static IProject getProject(IPath newPath) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject newProject = root.getProject(newPath.segment(0));
        return newProject;
    }

    /**
     * Adds an activity to {@link ProjectDeltaVisitor#pendingActivities}.
     */
    protected void addActivity(IResourceActivity activity) {
        pendingActivities.add(activity);
    }

    /**
     * Adds a FileActivity.created if the file is not currently in any open
     * editor. We ignore opened files because otherwise we might send CHANGED
     * events for files that are also handled by the editor manager. We also
     * ignore files that are not part of the current sharing.
     * 
     * @param resource
     */
    private void contentChanged(IResource resource) {

        final SPath spath = new SPath(ResourceAdapterFactory.create(resource));

        if (!sarosSession.isShared(ResourceAdapterFactory.create(resource)))
            return;

        if (editorManager.isOpened(spath)
            || editorManager
                .isManaged((IFile) resource.getAdapter(IFile.class)))
            return;

        log.debug("Resource " + resource.getName() + " changed");

        assert resource.getType() == IResource.FILE;

        byte[] content = FileUtils.getLocalFileContent((IFile) resource
            .getAdapter(IFile.class));

        if (content == null)
            log.error("could not read contents of file: "
                + resource.getFullPath());
        // TODO add encoding
        else
            addActivity(FileActivity.created(user, spath, content, null,
                Purpose.ACTIVITY));
    }

    /**
     * Returns true if the "Moved"-flags are set.
     */
    protected boolean isMoved(IResourceDelta delta) {
        return (isMovedFrom(delta) || isMovedTo(delta));
    }

    protected boolean isMovedFrom(IResourceDelta delta) {
        return ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0);
    }

    protected boolean isMovedTo(IResourceDelta delta) {
        return ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0);
    }

    protected boolean isSync(IResourceDelta delta) {
        return ((delta.getFlags() & IResourceDelta.SYNC) != 0);
    }

    /**
     * Returns true if the CONTENT flag is set, which means that the file's
     * timestamp changed.
     */
    protected boolean isContentChange(IResourceDelta delta) {
        return ((delta.getFlags() & IResourceDelta.CONTENT) != 0);

    }

    protected void setPostponeSending(boolean value) {
        postponeSending = value;
    }

    public boolean postponeSending() {
        return postponeSending;
    }
}
